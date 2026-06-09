package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.models.Category
import com.Serviseyem.models.ChatMessage
import com.Serviseyem.models.City
import com.Serviseyem.models.ServiceProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current

    // Local filter state selection
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Control triggers for various interactive overlays
    var showLoginDialog by remember { mutableStateOf(false) }
    var showTechRegisterDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf<ServiceProvider?>(null) }
    var showComplaintDialog by remember { mutableStateOf<ServiceProvider?>(null) }
    var showMyChatsOverviewDialog by remember { mutableStateOf(false) }

    // Guest User Identity config
    var guestUserNameInput by remember { mutableStateOf("غسان الصبري") }
    var userDisplayName by remember { mutableStateOf("غسان الصبري") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = viewModel.appLogoEmoji,
                            fontSize = 24.sp,
                            modifier = Modifier.testTag("app_logo_emoji")
                        )
                        Column {
                            Text(
                                text = if (viewModel.isArabic) viewModel.appNameAr else viewModel.appNameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "دليل ذكي موثوق",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    // Top bar icons display dynamically based on the Admin's configured order
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        viewModel.topBarIconsOrderList.forEach { iconMarker ->
                            when (iconMarker) {
                                "🏠" -> {
                                    IconButton(
                                        onClick = {
                                            selectedCategory = null
                                            selectedCity = null
                                            searchQuery = ""
                                            Toast.makeText(context, if (viewModel.isArabic) "تمت العودة للرئيسية والفلترة الافتراضية" else "Back to Default Filter", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("top_icon_home")
                                    ) {
                                        Text("🏠", fontSize = 20.sp)
                                    }
                                }
                                "🔐" -> {
                                    IconButton(
                                        onClick = { showLoginDialog = true },
                                        modifier = Modifier.testTag("top_icon_admin_gate")
                                    ) {
                                        Text("🔐", fontSize = 20.sp)
                                    }
                                }
                                "👤" -> {
                                    IconButton(
                                        onClick = { showTechRegisterDialog = true },
                                        modifier = Modifier.testTag("top_icon_register_tech")
                                    ) {
                                        Text("👤", fontSize = 20.sp)
                                    }
                                }
                                "🌐" -> {
                                    IconButton(
                                        onClick = { viewModel.isArabic = !viewModel.isArabic },
                                        modifier = Modifier.testTag("top_icon_language")
                                    ) {
                                        Text("🌐", fontSize = 20.sp)
                                    }
                                }
                                "🔄" -> {
                                    IconButton(
                                        onClick = {
                                            viewModel.triggerDynamicCleanCycle()
                                            Toast.makeText(context, if (viewModel.isArabic) "تم تحديث البيانات وتنظيف الكاش" else "Cache Cleaned Successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("top_icon_refresh")
                                    ) {
                                        Text("🔄", fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (viewModel.isFooterVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(bottom = 28.dp, top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.footerText,
                        fontSize = viewModel.footerFontSize.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        floatingActionButton = {
            // Check if floating action is allowed by settings
            if (!viewModel.isChatIconMutedHidden && !viewModel.isChatIconPermDeleted) {
                FloatingActionButton(
                    onClick = {
                        if (!viewModel.isChatInstantEnabled) {
                            Toast.makeText(context, viewModel.chatDisabledMessage, Toast.LENGTH_LONG).show()
                        } else {
                            showMyChatsOverviewDialog = true
                        }
                    },
                    containerColor = viewModel.chatSettingsIconColor,
                    modifier = Modifier
                        .size(viewModel.chatSettingsIconSize.dp)
                        .testTag("floating_direct_chat_hub")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chats Manager Overview",
                        tint = Color.White,
                        modifier = Modifier.size((viewModel.chatSettingsIconSize * 0.5f).dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F0F11))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header brand greeting and loyalty points
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                nameAr = viewModel.appGreetingMessageAr,
                                nameEn = viewModel.appGreetingMessageEn,
                                isArabic = viewModel.isArabic,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Loyalty Section Control
                            if (viewModel.showLoyaltySection) {
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    text = String.format(viewModel.loyaltyCardTitle, viewModel.userLoyaltyPoints),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = viewModel.appPrimaryColor
                                )
                                Text(
                                    text = viewModel.loyaltyCardText,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp, bottom = viewModel.loyaltyCardHeightPadding.dp)
                                )
                            }
                        }
                    }
                }

                // Dynamic banner carousel
                item {
                    val activeBanners = viewModel.banners.filter { it.isVisible }
                    if (activeBanners.isNotEmpty()) {
                        Text(
                            text = if (viewModel.isArabic) "🔥 إعلانات وعروض الديل المميزة" else "🔥 Highlighted Active Offers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = viewModel.appPrimaryColor
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(activeBanners) { banner ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F24)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(viewModel.appPrimaryColor.copy(alpha = 0.4f))),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.width(300.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = banner.title,
                                            fontSize = 13.sp,
                                            maxLines = 2,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "مدة العرض: ${banner.durationSeconds}ثانية",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Categories Quick filter list
                item {
                    Text(
                        text = if (viewModel.isArabic) "📂 أقسام دليل الخدمات" else "📂 Choose Service Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text(if (viewModel.isArabic) "جميع الأقسام" else "All Categories") }
                            )
                        }
                        items(viewModel.categories) { cat ->
                            val label = if (viewModel.isArabic) cat.nameAr else cat.nameEn
                            FilterChip(
                                selected = selectedCategory == cat.nameAr,
                                onClick = { selectedCategory = cat.nameAr },
                                leadingIcon = { Text(cat.iconEmoji) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Cities geographical dynamic filter
                item {
                    Text(
                        text = if (viewModel.isArabic) "📍 نطاق تغطية المدن اليمنية" else "📍 Operating Cities in Yemen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCity == null,
                                onClick = { selectedCity = null },
                                label = { Text(if (viewModel.isArabic) "كل اليمن" else "All Cities") }
                            )
                        }
                        items(viewModel.cities) { city ->
                            val label = if (viewModel.isArabic) city.nameAr else city.nameEn
                            FilterChip(
                                selected = selectedCity == city.nameAr,
                                onClick = { selectedCity = city.nameAr },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Search Box and Statistics details
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                        label = { Text(if (viewModel.isArabic) "ابحث عن اسم، تخصص، صفة فنية..." else "Search by provider name, specialty...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear query")
                                }
                            }
                        }
                    )
                }

                // Filtered List display
                val filteredProviders = viewModel.providers.filter { provider ->
                    provider.status == "مقبول" &&
                            (selectedCategory == null || provider.specialty.contains(selectedCategory!!, ignoreCase = true)) &&
                            (selectedCity == null || provider.city.equals(selectedCity!!, ignoreCase = true)) &&
                            (searchQuery.isEmpty() || provider.name.contains(searchQuery, ignoreCase = true) || provider.specialty.contains(searchQuery, ignoreCase = true) || provider.biography.contains(searchQuery, ignoreCase = true))
                }

                if (filteredProviders.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Build, contentDescription = "No Results", tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (viewModel.isArabic) "لا توجد نتائج مطابقة لبحثك الفني حالياً" else "No matching providers found in this region.",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProviders) { provider ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (provider.isVip) Color(0xFF1F1C16) else Color(0xFF161619)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (provider.isVip) viewModel.appPrimaryColor else Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("provider_card_${provider.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = provider.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White
                                            )
                                            if (provider.isVerified) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified Provider",
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .padding(start = 4.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "👨‍🔧 ${provider.specialty} • 📍 ${provider.city}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    // Rating
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C30)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = viewModel.appPrimaryColor, modifier = Modifier.size(14.dp))
                                            Text(
                                                text = " ${provider.rating} (${provider.ratingsCount})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                if (provider.biography.isNotEmpty()) {
                                    Text(
                                        text = provider.biography,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "المعاينة المعيارية للتواجد: ~${provider.baseFee} ريال يمني",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = viewModel.appPrimaryColor
                                    )
                                }

                                Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                                // Quick Actions
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Chat Button -> MUST instantly open conversation directly
                                    Button(
                                        onClick = {
                                            if (!viewModel.isChatInstantEnabled) {
                                                Toast.makeText(context, viewModel.chatDisabledMessage, Toast.LENGTH_LONG).show()
                                            } else if (provider.isChatMuted) {
                                                Toast.makeText(context, if (viewModel.isArabic) "عذراً، محطة محادثة هذا الكادر معطلة حالياً" else "Sorry, chat is disabled with this tech.", Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.initiateInstantChatWithProvider(provider, userDisplayName)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.chatSettingsIconColor),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chat_this_tech_${provider.id}")
                                    ) {
                                        Icon(Icons.Outlined.Chat, contentDescription = "Instant Chat Initiator", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (viewModel.isArabic) "محادثة فورية" else "Chat Now", fontSize = 11.sp)
                                    }

                                    // Booking Button
                                    if (viewModel.showBookingsSection) {
                                        OutlinedButton(
                                            onClick = { showBookingDialog = provider },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Outlined.CalendarMonth, contentDescription = "Book", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (viewModel.isArabic) "احجز موعد" else "Book Vis", fontSize = 11.sp)
                                        }
                                    }

                                    // Complain Button
                                    OutlinedButton(
                                        onClick = { showComplaintDialog = provider },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                    ) {
                                        Icon(Icons.Outlined.Report, contentDescription = "Report", modifier = Modifier.size(14.dp), tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }

                // Global Contacts Block
                item {
                    Text(
                        text = if (viewModel.isArabic) "📞 خط الدعم الفني للدليل" else "Support Desk Services",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📱 الجوال الدائم: ${viewModel.supportPhone}", fontSize = 12.sp, color = Color.White)
                            Text("💬 واتساب الإدارة: ${viewModel.supportWhatsapp}", fontSize = 12.sp, color = Color.White)
                            Text("📧 البريد المهني: ${viewModel.supportEmail}", fontSize = 12.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Floating Main active Chat dialog overlay (Appears immediately when session triggers!)
            if (viewModel.activeChatSessionId != null) {
                val currentSessionId = viewModel.activeChatSessionId!!
                val session = viewModel.chatSessions.find { it.id == currentSessionId }
                if (session != null) {
                    if (session.isBlocked) {
                        AlertDialog(
                            onDismissRequest = { viewModel.activeChatSessionId = null },
                            title = { Text(if (viewModel.isArabic) "المحادثة مغلقة" else "Chat block") },
                            text = { Text(if (viewModel.isArabic) "تم إيقاف أو تجميد هذه الجلسة بقرار إشرافي للسلامة المهنية" else "This conversation has been locked down by admin.") },
                            confirmButton = {
                                Button(onClick = { viewModel.activeChatSessionId = null }) {
                                    Text(if (viewModel.isArabic) "حسناً" else "OK")
                                }
                            }
                        )
                    } else {
                        // Display direct messenger portal
                        var directMessageText by remember { mutableStateOf("") }
                        val messages = viewModel.chatMessages.filter { it.chatId == currentSessionId }
                            .sortedBy { it.timestamp }

                        AlertDialog(
                            onDismissRequest = { viewModel.activeChatSessionId = null },
                            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .fillMaxHeight(0.85f),
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "مراسلة: ${session.techName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "الطرف الثاني: $userDisplayName",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(onClick = { viewModel.activeChatSessionId = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Portal")
                                    }
                                }
                            },
                            text = {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Chat content history scrollbox
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(Color(0xFF0A0A0C))
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (messages.isEmpty()) {
                                            item {
                                                Text(
                                                    text = "لا توجد مراسلات سابقة. اكتب رسالتك للبدء فوراً.",
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(24.dp)
                                                )
                                            }
                                        } else {
                                            items(messages) { msg ->
                                                val isMe = msg.senderRole == "user"
                                                val align = if (isMe) Alignment.End else Alignment.Start
                                                val bubbleBg = if (isMe) viewModel.chatSettingsIconColor else Color(0xFF2C2C30)
                                                val textColor = Color.White

                                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = bubbleBg),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            Text(
                                                                text = msg.senderName,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 9.sp,
                                                                color = if (isMe) viewModel.appPrimaryColor else Color.LightGray
                                                            )
                                                            Text(
                                                                text = msg.messageText,
                                                                fontSize = 13.sp,
                                                                color = textColor
                                                            )
                                                        }
                                                    }
                                                    val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                                    Text(
                                                        text = df.format(Date(msg.timestamp)),
                                                        fontSize = 8.sp,
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Reply Input field
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = directMessageText,
                                            onValueChange = { directMessageText = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("chat_text_input_field"),
                                            placeholder = { Text("اكتب رسالة فنية...", fontSize = 12.sp) }
                                        )
                                        Button(
                                            onClick = {
                                                if (directMessageText.trim().isNotEmpty()) {
                                                    viewModel.sendInstantChatMessage(
                                                        chatId = currentSessionId,
                                                        senderName = userDisplayName,
                                                        senderRole = "user",
                                                        text = directMessageText.trim()
                                                    )
                                                    directMessageText = ""
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.chatSettingsIconColor),
                                            enabled = directMessageText.trim().isNotEmpty(),
                                            modifier = Modifier.testTag("chat_send_message_button")
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Send Message")
                                        }
                                    }
                                }
                            },
                            confirmButton = {}
                        )
                    }
                }
            }

            // Dialog: Customer Active Chat Sessions Overview (Accessed via FAB)
            if (showMyChatsOverviewDialog) {
                val mySessions = viewModel.chatSessions.filter { it.userName == userDisplayName }

                AlertDialog(
                    onDismissRequest = { showMyChatsOverviewDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (viewModel.isArabic) "💬 محادثاتي السابقة" else "My Live Chats", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showMyChatsOverviewDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Chats Manager")
                            }
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Quick guest nickname edit
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = guestUserNameInput,
                                    onValueChange = { guestUserNameInput = it },
                                    label = { Text("اسم الهوية الخاص بك للدردشة") },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        userDisplayName = guestUserNameInput.ifEmpty { "العميل غسان" }
                                        Toast.makeText(context, "تم حفظ الاسم كـ: $userDisplayName", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text("حفظ")
                                }
                            }

                            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                            if (mySessions.isEmpty()) {
                                Text(
                                    text = "لا توجد لك أي محادثات نشطة مع مقدمي الخدمات حالياً. اضغط على 'محادثة فورية' في بطاقة المهنيين لبدء الكلام معه.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    items(mySessions) { sess ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.activeChatSessionId = sess.id
                                                    showMyChatsOverviewDialog = false
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "الفني: ${sess.techName}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                    if (sess.isBlocked) {
                                                        Text("مغلقة", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Text(
                                                    text = "آخر رسالة: ${sess.lastMessage}",
                                                    fontSize = 11.sp,
                                                    color = Color.LightGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            // Dialog: Login Door
            if (showLoginDialog) {
                var inputUser by remember { mutableStateOf("") }
                var inputPass by remember { mutableStateOf("") }
                var isPassVisible by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showLoginDialog = false },
                    title = { Text(if (viewModel.isArabic) "🔐 تسجيل دخول الطاقم الإداري" else "Staff Gate") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inputUser,
                                onValueChange = { inputUser = it },
                                label = { Text("اسم المستخدم (Username)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_user_field")
                            )
                            OutlinedTextField(
                                value = inputPass,
                                onValueChange = { inputPass = it },
                                label = { Text("رمز المرور السري (Password)") },
                                visualTransformation = if (isPassVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPassVisible = !isPassVisible }) {
                                        Icon(
                                            imageVector = if (isPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Show secret"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_pass_field")
                            )

                            // Quick login shortcut help
                            Text(
                                text = "حساب الإدارة: WAM2026 / الرقم: maher736462",
                                fontSize = 10.sp,
                                color = viewModel.appPrimaryColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (inputUser == viewModel.adminUsernameSecret && inputPass == viewModel.adminPasswordSecret) {
                                    viewModel.isAdminLoggedIn = true
                                    viewModel.activeAdminUsername = "WAM2026 Admin"
                                    showLoginDialog = false
                                    Toast.makeText(context, "أهلاً بك مشرف الدليل الأعلى! تم فك حظر الألواح بنجاح.", Toast.LENGTH_SHORT).show()
                                    onNavigateToAdmin()
                                } else if (inputPass == viewModel.ownerPasswordSecret) {
                                    viewModel.isAdminLoggedIn = true
                                    viewModel.activeAdminUsername = "صاحب المالكي"
                                    showLoginDialog = false
                                    onNavigateToAdmin()
                                } else {
                                    Toast.makeText(context, "الاسم أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                            modifier = Modifier.testTag("admin_auth_button")
                        ) {
                            Text("دخول")
                        }
                    }
                )
            }

            // Dialog: ServiceProvider Self Registration requests
            if (showTechRegisterDialog) {
                var candidateName by remember { mutableStateOf("") }
                var candidatePhone by remember { mutableStateOf("") }
                var candidateSpecialty by remember { mutableStateOf(viewModel.categories.firstOrNull()?.nameAr ?: "سباكة") }
                var candidateCity by remember { mutableStateOf(viewModel.cities.firstOrNull()?.nameAr ?: "صنعاء") }
                var expandedSpec by remember { mutableStateOf(false) }
                var expandedCity by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showTechRegisterDialog = false },
                    title = { Text(if (viewModel.isArabic) "👤 طلب انضمام فني جديد" else "Join Network") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "انضم لأكبر شبكة صيانة ودليل خدمات باليمن للربط المباشر بآلاف الزوار شهرياً.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            OutlinedTextField(
                                value = candidateName,
                                onValueChange = { candidateName = it },
                                label = { Text("الاسم الكامل فنيًا") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_name_field")
                            )

                            OutlinedTextField(
                                value = candidatePhone,
                                onValueChange = { candidatePhone = it },
                                label = { Text("رقم الهاتف (9 أرقام)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_phone_field")
                            )

                            // Specialty select
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = candidateSpecialty,
                                    onValueChange = {},
                                    label = { Text("مجال التخصص الرئيسي") },
                                    readOnly = true,
                                    trailingIcon = { IconButton(onClick = { expandedSpec = true }) { Icon(Icons.Default.ArrowDropDown, "") } },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(expanded = expandedSpec, onDismissRequest = { expandedSpec = false }) {
                                    viewModel.categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.nameAr) },
                                            onClick = {
                                                candidateSpecialty = cat.nameAr
                                                expandedSpec = false
                                            }
                                        )
                                    }
                                }
                            }

                            // City select
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = candidateCity,
                                    onValueChange = {},
                                    label = { Text("المدينة الرئيسية") },
                                    readOnly = true,
                                    trailingIcon = { IconButton(onClick = { expandedCity = true }) { Icon(Icons.Default.ArrowDropDown, "") } },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(expanded = expandedCity, onDismissRequest = { expandedCity = false }) {
                                    viewModel.cities.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c.nameAr) },
                                            onClick = {
                                                candidateCity = c.nameAr
                                                expandedCity = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (candidateName.isNotEmpty() && candidatePhone.isNotEmpty()) {
                                    viewModel.requestTechnicianRegistration(
                                        name = candidateName,
                                        phone = candidatePhone,
                                        specialty = candidateSpecialty,
                                        city = candidateCity,
                                        photoMethodSelection = "كاميرا الهاتف"
                                    )
                                    showTechRegisterDialog = false
                                    Toast.makeText(context, "تم إرسال طلبك. الطلب معلق للمراجعة والتدقيق الإداري الفوري.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "الرجاء كمالة جميع الحقول للمصداقية", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                            modifier = Modifier.testTag("register_submit_button")
                        ) {
                            Text("إرسال طلب الانضمام")
                        }
                    }
                )
            }

            // Dialog: Customer Booking
            showBookingDialog?.let { provider ->
                var nameBook by remember { mutableStateOf("") }
                var phoneBook by remember { mutableStateOf("") }
                var dateBook by remember { mutableStateOf("2026-06-15") }
                var timeBook by remember { mutableStateOf("11:30 صباحًا") }

                AlertDialog(
                    onDismissRequest = { showBookingDialog = null },
                    title = { Text("احجز موعد تواجد صيانة") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("حجز فوري للزيارة مع: ${provider.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            OutlinedTextField(
                                value = nameBook,
                                onValueChange = { nameBook = it },
                                label = { Text("الاسم") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phoneBook,
                                onValueChange = { phoneBook = it },
                                label = { Text("الهاتف") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(
                                    value = dateBook,
                                    onValueChange = { dateBook = it },
                                    label = { Text("التاريخ") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = timeBook,
                                    onValueChange = { timeBook = it },
                                    label = { Text("الساعة") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nameBook.isNotEmpty() && phoneBook.isNotEmpty()) {
                                    viewModel.requestBooking(nameBook, phoneBook, provider.name, dateBook, timeBook)
                                    showBookingDialog = null
                                    Toast.makeText(context, "تم تسجيل الحجز بنجاح ومزامنته فوراً!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "الرجاء إدخال الاسم والهاتف", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("أكد الزيارة")
                        }
                    }
                )
            }

            // Dialog: Complaints register
            showComplaintDialog?.let { provider ->
                var nameComp by remember { mutableStateOf("") }
                var txtComp by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showComplaintDialog = null },
                    title = { Text("تقديم بلاغ إشرافي فوري ضد مزود الخدمة") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("نأخذ الجودة بجدية مطلقة وسنقوم بتعليق حساب المرفوع ضدّ للتحقيق.", fontSize = 11.sp, color = Color.Gray)
                            Text("المهني المستهدف: ${provider.name}", fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = nameComp,
                                onValueChange = { nameComp = it },
                                label = { Text("اسم مقدم البلاغ") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = txtComp,
                                onValueChange = { txtComp = it },
                                label = { Text("تفاصيل الشكوى بوضوح تام") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nameComp.isNotEmpty() && txtComp.isNotEmpty()) {
                                    viewModel.addComplaint(provider.name, nameComp, txtComp)
                                    showComplaintDialog = null
                                    Toast.makeText(context, "تم حفظ الشكوى بنجاح وسيتابعها الإشراف فوراً.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "الرجاء تعبئة الاسم والشكوى", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("إرسال الشكوى")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Text(
    nameAr: String,
    nameEn: String,
    isArabic: Boolean,
    style: androidx.compose.ui.text.TextStyle,
    textAlign: TextAlign?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (isArabic) nameAr else nameEn,
        style = style,
        textAlign = textAlign,
        color = color,
        modifier = modifier
    )
}
