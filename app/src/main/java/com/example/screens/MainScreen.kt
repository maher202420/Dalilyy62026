package com.example.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.models.BannerAd
import com.example.models.Category
import com.example.models.ServiceProvider
import com.example.services.FirebaseService
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLaunchAI: () -> Unit
) {
    val context = LocalContext.current
    val categoriesState = FirebaseService.categories.collectAsState()
    val providersState = FirebaseService.serviceProviders.collectAsState()
    val bannersState = FirebaseService.banners.collectAsState()
    val settingsState = FirebaseService.settings.collectAsState()
    val citiesState = FirebaseService.cities.collectAsState()

    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var maxInspectionPrice by remember { mutableStateOf(5000f) }
    var showOnlyVip by remember { mutableStateOf(false) }

    // Banner logic
    val activeBanners = bannersState.value.filter { it.isActive }
    var currentBannerIndex by remember { mutableStateOf(0) }

    LaunchedEffect(activeBanners) {
        if (activeBanners.isNotEmpty()) {
            while (true) {
                val banner = activeBanners.getOrNull(currentBannerIndex)
                val duration = (banner?.durationSeconds ?: 5) * 1000L
                delay(duration)
                currentBannerIndex = (currentBannerIndex + 1) % activeBanners.size
            }
        }
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Header Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = settingsState.value.welcomeMsg,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث عن: كهربائي، سباك، نجار، ماهر...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_input")
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            Row {
                                IconButton(onClick = {
                                    searchQuery = "ماهر"
                                    Toast.makeText(context, "🎤 تم محاكاة البحث الصوتي بنجاح: [ماهر]", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = MaterialTheme.colorScheme.primary)
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Banners Slider Area
            if (activeBanners.isNotEmpty()) {
                val banner = activeBanners.getOrNull(currentBannerIndex)
                if (banner != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (banner.type == "IMAGE" && banner.bannerUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = banner.bannerUrl,
                                    contentDescription = "Banner Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                // Dark overlay for text readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = banner.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (banner.linkUrl.isNotEmpty()) {
                                    Text(
                                        text = "اضغط للتفاصيل",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(banner.linkUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "${banner.linkUrl}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Categories horizontal bar
            Text(
                text = "الأقسام الرئيسية والفرعية والمحافظات",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    val isAllSelected = selectedCategory == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { selectedCategory = null },
                        label = { Text("الكل") },
                        leadingIcon = { Icon(Icons.Default.Category, "All", modifier = Modifier.size(16.dp)) }
                    )
                }
                items(categoriesState.value.filter { !it.isSubCategory }) { cat ->
                    val isSelected = selectedCategory?.id == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.nameAr) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat.icon),
                                contentDescription = cat.nameAr,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Location geographic filtering pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    val isAllSelected = selectedCity == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { selectedCity = null },
                        label = { Text("كل المدن 🇾🇪") }
                    )
                }
                items(citiesState.value) { city ->
                    val isSelected = selectedCity == city
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCity = city },
                        label = { Text(city) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, city, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            // Sub Categories sub-chips
            if (selectedCategory != null) {
                val subs = categoriesState.value.filter { it.isSubCategory && it.parentId == selectedCategory!!.id }
                if (subs.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "التخصص الفرعي في ${selectedCategory!!.nameAr}:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(subs) { subCat ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (searchQuery.contains(subCat.nameAr)) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                else Color.Transparent
                                            )
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                searchQuery = subCat.nameAr
                                                Toast.makeText(context, "تم تحديد: ${subCat.nameAr}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = subCat.nameAr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tech List Layout
            val filteredTechs = providersState.value.filter { p ->
                val matchesSearch = p.nameAr.contains(searchQuery, true) ||
                        p.nameEn.contains(searchQuery, true) ||
                        p.workAddress.contains(searchQuery, true) ||
                        (categoriesState.value.find { it.id == p.categoryId }?.nameAr?.contains(searchQuery, true) ?: false) ||
                        (categoriesState.value.find { it.id == p.subCategoryId }?.nameAr?.contains(searchQuery, true) ?: false)

                val matchesCat = selectedCategory == null || p.categoryId == selectedCategory!!.id
                val matchesCity = selectedCity == null || p.residenceAr == selectedCity
                val matchesVip = !showOnlyVip || p.isVip || p.isPinned

                matchesSearch && matchesCat && matchesCity && matchesVip
            }.sortedWith(compareByDescending<ServiceProvider> { it.isPinned }
                .thenByDescending { it.isRecommended }
                .thenByDescending { it.rating }
            )

            if (filteredTechs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "Empty",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "عذراً، لم يتم العثور على مقدم خدمة يطابق شروط البحث الفنية.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredTechs) { tech ->
                        TechnicianCard(
                            tech = tech,
                            categoryName = categoriesState.value.find { it.id == tech.categoryId }?.nameAr ?: "خدمة عامة",
                            subCategoryName = categoriesState.value.find { it.id == tech.subCategoryId }?.nameAr ?: "",
                            onNavigateToChat = onNavigateToChat
                        )
                    }
                }
            }
        }

        // Custom filter dialog
        if (showFilterDialog) {
            Dialog(onDismissRequest = { showFilterDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "فلاتر التخصيص والبحث على الخريطة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // VIP Filter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showOnlyVip,
                                onCheckedChange = { showOnlyVip = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("أعضاء النخبة والموصى بهم فقط (⭐ VIP)")
                        }

                        // Distance radius simulation slider
                        Text(
                            text = "محيط البحث الجغرافي (تصفية الخريطة): ${maxInspectionPrice.toInt()} متر",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Slider(
                            value = maxInspectionPrice,
                            onValueChange = { maxInspectionPrice = it },
                            valueRange = 500f..10000f,
                            steps = 19
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showOnlyVip = false
                                maxInspectionPrice = 5000f
                                selectedCity = null
                                selectedCategory = null
                                showFilterDialog = false
                            }) {
                                Text("إعادة تعيين")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { showFilterDialog = false }) {
                                Text("تطبيق الفلاتر")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicianCard(
    tech: ServiceProvider,
    categoryName: String,
    subCategoryName: String,
    onNavigateToChat: (String, String) -> Unit
) {
    val context = LocalContext.current
    
    // Explicit high-contrast background and text mapping for absolute screen contrast (Problem 2)
    val cardBackground = if (tech.isPinned) {
        // Subtle luxurious gold ambient shadow/border for pinned providers
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                width = if (tech.isPinned) 1.5.dp else 1.dp,
                brush = if (tech.isPinned) {
                    Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFC5A059)))
                } else {
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), Color.Transparent))
                },
                shape = RoundedCornerShape(20.dp)
            )
            .shadow(elevation = if (tech.isPinned) 4.dp else 1.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .background(cardBackground)
        ) {
            // Elegant Editorial Gold Top border accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFFD4AF37))
            )

            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // Badges section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category indicators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getCategoryIcon(tech.categoryId),
                        contentDescription = "Category",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$categoryName ${if (subCategoryName.isNotEmpty()) "• $subCategoryName" else ""}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Elite Badges in Arabic
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (tech.isPinned || tech.isVip) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFD700))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "VIP نخبة",
                                fontSize = 10.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (tech.isVerified) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF007AFF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "موثق ✓",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                Box(modifier = Modifier.size(62.dp)) {
                    if (tech.avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = tech.avatarUrl,
                            contentDescription = tech.nameAr,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Metadata
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tech.nameAr,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground, // Solid white in dark mode, solid black in light mode. Robust contrast.
                            fontSize = 17.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // Ratings text made extremely high contrast
                        Text(
                            text = "${tech.rating} (${tech.reviewCount} تقييم)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = "Distance",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        // Distance text bold representation
                        Text(
                            text = "يبعد ${tech.distance} كم تقريباً",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Geographic WorkAddress Text with high visibility
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Address",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tech.workAddress,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Interaction Row: Call, WhatsApp, Chat (Realtime Chat, Problem 2 details)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Call Phone button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tech.phone}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "لا يمكن تشغيل تطبيق الاتصال", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(19.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اتصال برقم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // WhatsApp button
                Button(
                    onClick = {
                        try {
                            val cleanNumber = tech.phone.replace("+", "").replace(" ", "")
                            // Format for Yemeni numbers or international
                            val formatted = if (cleanNumber.startsWith("7")) "967$cleanNumber" else cleanNumber
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formatted&text=${Uri.encode("مرحباً أخي الكريم ${tech.nameAr}، أرغب في طلب خدمتك المعروضة عبر تطبيق دليل خدمات WAM...")}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "الرجاء تثبيت واتساب أولاً", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(19.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Chat button (Realtime Sync Messenger)
                OutlinedButton(
                    onClick = {
                        onNavigateToChat(tech.id, tech.nameAr)
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("محادثة فورية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}

// Map database tags to Material Icons for premium appearance
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "electrical", "cat_elec" -> Icons.Default.ElectricalServices
        "plumbing", "cat_plumb" -> Icons.Default.Plumbing
        "carpentry", "cat_carp" -> Icons.Default.Construction
        "ac_unit", "cat_ac" -> Icons.Default.AcUnit
        else -> Icons.Default.Build
    }
}
