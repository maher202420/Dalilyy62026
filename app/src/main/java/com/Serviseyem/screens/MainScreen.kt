package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.models.Category
import com.Serviseyem.models.ServiceProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel) {
    val isArabic by viewModel.isArabic.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val favoriteProviderIds by viewModel.favoriteProviderIds.collectAsState()
    val activeChatProvider by viewModel.activeChatProvider.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }
    var showBookingProvider by remember { mutableStateOf<ServiceProvider?>(null) }
    var chatInputText by remember { mutableStateOf("") }
    var currentBottomNavItem by remember { mutableStateOf("home") }

    // Navigation and structure layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isArabic) "دليل خدمات اليمن الذكي" else "Smart Yemen Services Guide",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "اكتشف طواقم الصيانة المعتمدة" else "Discover verified maintenance crews",
                            color = viewModel.appPrimaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    // Localization Toggle
                    TextButton(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.textButtonColors(contentColor = viewModel.appPrimaryColor)
                    ) {
                        Text(
                            text = if (isArabic) "English (EN)" else "العربية (AR)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    
                    // App Info Icon
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About App Info",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentBottomNavItem == "home",
                        onClick = { currentBottomNavItem = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(if (isArabic) "الرئيسية" else "Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = viewModel.appPrimaryColor,
                            indicatorColor = viewModel.appPrimaryColor,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        selected = currentBottomNavItem == "favorites",
                        onClick = { currentBottomNavItem = "favorites" },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text(if (isArabic) "المفضلة" else "Favorites", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = viewModel.appPrimaryColor,
                            indicatorColor = viewModel.appPrimaryColor,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentBottomNavItem) {
                "home" -> {
                    HomeScreenContent(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedCity = selectedCity,
                        favoriteProviderIds = favoriteProviderIds,
                        isArabic = isArabic,
                        context = context,
                        onShowBooking = { showBookingProvider = it }
                    )
                }
                "favorites" -> {
                    FavoritesScreenContent(
                        viewModel = viewModel,
                        favoriteProviderIds = favoriteProviderIds,
                        isArabic = isArabic,
                        context = context,
                        onShowBooking = { showBookingProvider = it }
                    )
                }
            }

            // Interactive Instant Chat Overlay Panel if triggered
            if (activeChatProvider != null) {
                val provider = activeChatProvider!!
                ChatSimulatorPanel(
                    provider = provider,
                    isArabic = isArabic,
                    messages = chatMessages,
                    inputText = chatInputText,
                    onInputChanged = { chatInputText = it },
                    onSend = {
                        viewModel.sendUserMessage(chatInputText, if (isArabic) "مستخدم يمني" else "Yemeni User")
                        chatInputText = ""
                    },
                    onClose = { viewModel.closeChat() },
                    primaryColor = viewModel.appPrimaryColor
                )
            }

            // Booking Modal
            if (showBookingProvider != null) {
                val provider = showBookingProvider!!
                BookingDialog(
                    provider = provider,
                    isArabic = isArabic,
                    primaryColor = viewModel.appPrimaryColor,
                    onDismiss = { showBookingProvider = null },
                    onConfirm = { date, time ->
                        showBookingProvider = null
                        Toast.makeText(
                            context,
                            if (isArabic) "تم تحديد موعد بنجاح وتأكيده مع ${provider.name}!" 
                            else "Appointment successfully customized & secured with ${provider.name}!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }

            // App Information Modal Dialog
            if (showInfoDialog) {
                AboutAppDialog(
                    isArabic = isArabic,
                    onDismiss = { showInfoDialog = false },
                    primaryColor = viewModel.appPrimaryColor
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    viewModel: AppViewModel,
    searchQuery: String,
    selectedCategory: String?,
    selectedCity: String?,
    favoriteProviderIds: Set<String>,
    isArabic: Boolean,
    context: android.content.Context,
    onShowBooking: (ServiceProvider) -> Unit
) {
    // Filter logical processing
    val filteredProviders = viewModel.providers.filter { provider ->
        val matchesSearch = queryMatchesProvider(provider, searchQuery)
        val matchesCategory = selectedCategory == null || provider.categoryId == selectedCategory
        val matchesCity = selectedCity == null || provider.city == selectedCity
        matchesSearch && matchesCategory && matchesCity
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Welcoming Card Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            WelcomeBanner(isArabic = isArabic, primaryColor = viewModel.appPrimaryColor)
        }

        // Search Section
        item {
            SearchBarCompact(
                query = searchQuery,
                onQueryChanged = { viewModel.setSearchQuery(it) },
                isArabic = isArabic
            )
        }

        // Category Filter Selection Component
        item {
            CategorySelectionRow(
                categories = viewModel.categories,
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.selectCategory(it) },
                isArabic = isArabic,
                primaryColor = viewModel.appPrimaryColor
            )
        }

        // Yemeni City Selection Quick Filter Component
        item {
            CitySelectionRow(
                cities = listOf("صنعاء", "عدن", "تعز", "المكلا"),
                selectedCity = selectedCity,
                onSelectCity = { viewModel.selectCity(it) },
                isArabic = isArabic,
                activeColor = viewModel.appPrimaryColor
            )
        }

        // Section Title: Providers List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "مقدمو الخدمات النشطون (${filteredProviders.size})" else "Active Service Providers (${filteredProviders.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (selectedCategory != null || selectedCity != null || searchQuery.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.selectCategory(null)
                            viewModel.selectCity(null)
                            viewModel.setSearchQuery("")
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = viewModel.appPrimaryColor)
                    ) {
                        Text(if (isArabic) "إعادة تعيين الفلاتر" else "Clear Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Providers List / Grid Render
        if (filteredProviders.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (isArabic) viewModel.noResultsMessageAr else viewModel.noResultsMessageEn,
                    isArabic = isArabic
                )
            }
        } else {
            items(filteredProviders, key = { it.id }) { provider ->
                ServiceProviderCard(
                    provider = provider,
                    viewModel = viewModel,
                    isFavorite = favoriteProviderIds.contains(provider.id),
                    onToggleFavorite = { viewModel.toggleFavorite(provider.id) },
                    onInitiateChat = { viewModel.initiateInstantChatWithProvider(provider, if (isArabic) "صاحب الطلب" else "Yemeni Client") },
                    onShowBooking = { onShowBooking(provider) },
                    isArabic = isArabic
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FavoritesScreenContent(
    viewModel: AppViewModel,
    favoriteProviderIds: Set<String>,
    isArabic: Boolean,
    context: android.content.Context,
    onShowBooking: (ServiceProvider) -> Unit
) {
    val favoriteProviders = viewModel.providers.filter { favoriteProviderIds.contains(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isArabic) "⭐ قائمة المفضلة المحفوظة" else "⭐ Your Saved Favorites",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = if (isArabic) "الوصول السريع للنخبة من الفنيين الذين تفضلهم" else "Quick access to your preferred elite technicians",
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (favoriteProviders.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (isArabic) "لم تقم بإضافة أي كادر لقائمتك المفضلة حتى الآن." else "You haven't added any provider to your favorites yet.",
                    isArabic = isArabic
                )
            }
        } else {
            items(favoriteProviders, key = { it.id }) { provider ->
                ServiceProviderCard(
                    provider = provider,
                    viewModel = viewModel,
                    isFavorite = true,
                    onToggleFavorite = { viewModel.toggleFavorite(provider.id) },
                    onInitiateChat = { viewModel.initiateInstantChatWithProvider(provider, if (isArabic) "مستخدم" else "User") },
                    onShowBooking = { onShowBooking(provider) },
                    isArabic = isArabic
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WelcomeBanner(isArabic: Boolean, primaryColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1C16)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(primaryColor, Color(0xFFFF8C00))
            )
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🇾🇪", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "أهلاً بك في دليل يزن" else "Welcome to Yazan Guide",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArabic) {
                    "ابحث بضغطة زر عن المهندسين والفنيين المؤهلين لسباكة، كهرباء، صيانة شمسية، وتبريد بجميع مدن اليمن الحبيبة بأسعار تنافسية ومراجعات حقيقية."
                } else {
                    "Locate qualified electricians, plumbers, solar installation crews and mechanical technicians across major Yemeni cities in seconds."
                },
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SearchBarCompact(
    query: String,
    onQueryChanged: (String) -> Unit,
    isArabic: Boolean
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = if (isArabic) "ابحث باسم المهندس أو التخصص الدقيق..." else "Search technician name or skill area...",
                fontSize = 12.sp,
                color = Color.Gray
            )
        },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.LightGray)
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFD700),
            unfocusedBorderColor = Color.DarkGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF161619),
            unfocusedContainerColor = Color(0xFF161619)
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_search_field")
    )
}

// Category selection row with beautiful horizontal scroll
@Composable
fun CategorySelectionRow(
    categories: List<Category>,
    selectedCategory: String?,
    onSelectCategory: (String) -> Unit,
    isArabic: Boolean,
    primaryColor: Color
) {
    Column {
        Text(
            text = if (isArabic) "فئات الخدمات المهنية" else "Professional Service Disciplines",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = category.id == selectedCategory
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) primaryColor else Color(0xFF161619)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) Color.Transparent else Color.DarkGray
                        )
                    ),
                    modifier = Modifier
                        .clickable { onSelectCategory(category.id) }
                        .animateContentSize()
                        .testTag("category_pill_${category.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = category.icon, fontSize = 16.sp)
                        Text(
                            text = if (isArabic) category.nameAr else category.nameEn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

// City filter component row 
@Composable
fun CitySelectionRow(
    cities: List<String>,
    selectedCity: String?,
    onSelectCity: (String) -> Unit,
    isArabic: Boolean,
    activeColor: Color
) {
    Column {
        Text(
            text = if (isArabic) "المنطقة / مدينة التواجد" else "Location Area Focus",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(cities) { city ->
                val isSelected = city == selectedCity
                Surface(
                    color = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) activeColor else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onSelectCity(city) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = city,
                            tint = if (isSelected) activeColor else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = city,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) activeColor else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

// Reusable custom Service Provider card component
@Composable
fun ServiceProviderCard(
    provider: ServiceProvider,
    viewModel: AppViewModel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onInitiateChat: () -> Unit,
    onShowBooking: () -> Unit,
    isArabic: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (provider.isVip) Color(0xFF1F1C16) else Color(0xFF161619)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (provider.isVip) viewModel.appPrimaryColor else Color.DarkGray
            )
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("provider_card_${provider.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Info + Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = provider.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        if (provider.isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Provider Check",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (provider.isPinned) {
                            Text("📌", fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "👨‍🔧 ${provider.specialty}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Info Badges: Rating + Location + Fee
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = viewModel.appPrimaryColor, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${provider.rating} (${provider.ratingsCount} ${if (isArabic) "مراجعة" else "reviews"})",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text("•", color = Color.Gray)

                // City badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "City", tint = Color.LightGray, modifier = Modifier.size(13.dp))
                    Text(
                        text = provider.city,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Standard visiting dynamic price badge in YER (Yemeni Rial)
                Text(
                    text = "${provider.baseFee} ${if (isArabic) "ريال" else "YER"}",
                    color = viewModel.appPrimaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Expanded panel showing detail description, phone number, and support hours
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(Color(0xFF0F0F12), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isArabic) "معلومات فنية والخبرات:" else "Expert Bio & Certifications:",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = provider.biography,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "🕒 أوقات التواجد الاستشاري:" else "🕒 Support Hours:",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = provider.availableHours,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "📞 رقم الجوال المباشر:" else "📞 Direct Phone:",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+967 ${provider.phone}",
                            color = viewModel.appPrimaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CTA action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Instant chat button
                Button(
                    onClick = onInitiateChat,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = viewModel.appPrimaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = "Chat Logo", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "محادثة فورية" else "Instant Chat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Booking modal prompt button
                OutlinedButton(
                    onClick = onShowBooking,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, viewModel.appPrimaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = viewModel.appPrimaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Schedule", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "احجز موعد" else "Book Work",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Dialog to simulate scheduling appointment with providers
@Composable
fun BookingDialog(
    provider: ServiceProvider,
    isArabic: Boolean,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String) -> Unit
) {
    var dateChoice by remember { mutableStateOf("غداً") }
    var timePeriod by remember { mutableStateOf("صباحاً (9:00)") }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "جدولة طلب صيانة مع ${provider.name}" else "Schedule visit with ${provider.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isArabic) "اختر اليوم المفضّل للمعاينة:" else "Choose appointment day:",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                // Day pills selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val days = if (isArabic) listOf("اليوم", "غداً", "خلال يومين") else listOf("Today", "Tomorrow", "In 2 Days")
                    days.forEach { day ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (dateChoice == day) primaryColor.copy(alpha = 0.2f) else Color(0xFF1E1E22),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (dateChoice == day) primaryColor else Color.Transparent),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dateChoice = day }
                        ) {
                            Text(
                                text = day,
                                color = if (dateChoice == day) primaryColor else Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = if (isArabic) "الفترة المفضلة لحضور الفني:" else "Preferred service period:",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = if (isArabic) listOf("صباحاً (9:00)", "عصراً (4:00)", "مساءً (7:00)") else listOf("Morning (9AM)", "Afternoon (4PM)", "Evening (7PM)")
                    periods.forEach { period ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (timePeriod == period) primaryColor.copy(alpha = 0.2f) else Color(0xFF1E1E22),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (timePeriod == period) primaryColor else Color.Transparent),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { timePeriod = period }
                        ) {
                            Text(
                                text = period,
                                color = if (timePeriod == period) primaryColor else Color.LightGray,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = if (isArabic) "ملاحظات إضافية للفني (اختياري):" else "Additional Service Instructions (Optional):",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    placeholder = { Text(if (isArabic) "مثال: تسريب مغسلة المطبخ الرئيسي..." else "Example: Main kitchen sink leak...", fontSize = 11.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(dateChoice, timePeriod) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black)
            ) {
                Text(if (isArabic) "حجز وتأكيد" else "Confirm Secure Booking", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                Text(if (isArabic) "إلغاء الأمر" else "Cancel")
            }
        },
        containerColor = Color(0xFF161619)
    )
}

// Full interactive chat simulator floating overlay panel
@Composable
fun ChatSimulatorPanel(
    provider: ServiceProvider,
    isArabic: Boolean,
    messages: List<com.Serviseyem.models.ChatMessage>,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        color = Color.Black.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .background(Color(0xFF161619), RoundedCornerShape(20.dp))
                .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            // Chat header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👨‍🔧", fontSize = 16.sp)
                        }
                    }
                    Column {
                        Text(provider.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text(
                            text = if (provider.isVerified) "✓ فني معتمد" else "فني نشط",
                            color = primaryColor,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close dynamic simulator panel", tint = Color.White)
                }
            }

            Divider(color = Color.DarkGray, thickness = 1.dp)

            // Chat content area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isFromUser = msg.isFromUser
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (isFromUser) primaryColor else Color(0xFF232329),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isFromUser) 12.dp else 2.dp,
                                bottomEnd = if (isFromUser) 2.dp else 12.dp
                            ),
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isFromUser) Color.Black else Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Text(
                            text = msg.senderName,
                            fontSize = 8.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Divider(color = Color.DarkGray, thickness = 0.5.dp)

            // Chat Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    placeholder = { Text(if (isArabic) "اكتب رسالة طلب الصيانة الفورية..." else "Enter your service request message...", fontSize = 11.sp, color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(primaryColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String, isArabic: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔍", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AboutAppDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    primaryColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "حول دليل خدمات اليمن" else "About Yemen Services Guide",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
        },
        text = {
            Text(
                text = if (isArabic) {
                    "أحدث دليل مهني يمني للربط المباشر والفوري بين المواطنين والمنشآت مع كوكبة متميزة من مهندسي الصيانة والمصلحين المعتمدين بمدن الجمهورية اليمنية.\n\nيسهل عليك الدليل البحث، المقارنة، التواصل الهاتفي، المحادثة الفورية، وجدولة المواعيد بكبسة زر واحدة من هاتفك."
                } else {
                    "The premier unified service guide linking Yemeni citizens & shops directly with approved expert technicians, electricians, plumbers & cooling specialists in real-time.\n\nIncludes instant live booking options, certified status validations, and geographical tracking."
                },
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black)
            ) {
                Text(if (isArabic) "فهمت" else "Got It", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF161619)
    )
}

// Logic helper
fun queryMatchesProvider(provider: ServiceProvider, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim().lowercase()
    return provider.name.lowercase().contains(q) ||
           provider.specialty.lowercase().contains(q) ||
           provider.biography.lowercase().contains(q) ||
           provider.city.lowercase().contains(q)
}
