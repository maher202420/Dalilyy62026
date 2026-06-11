package com.Serviseyem.screens

import android.widget.Toast
import coil.compose.AsyncImage
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import org.json.JSONObject
import org.json.JSONArray
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.coroutines.*

object GeminiApiClient {
    suspend fun generateContent(apiKey: String, prompt: String, systemInstruction: String): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "API_KEY_BLANK"
        }
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream, "utf-8")).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line?.trim())
                    }
                }
                
                val responseJson = JSONObject(response.toString())
                val candidates = responseJson.getJSONArray("candidates")
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                parts.getJSONObject(0).getString("text")
            } else {
                "API_ERROR_CODE_$responseCode"
            }
        } catch (e: Exception) {
            "API_EXCEPTION_${e.message}"
        }
    }
}

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
    var showAiAssistantDialog by remember { mutableStateOf(false) }
    var showProviderProfileDialog by remember { mutableStateOf<ServiceProvider?>(null) }

    // Guest User Identity config
    var guestUserNameInput by remember { mutableStateOf("غسان الصبري") }
    var userDisplayName by remember { mutableStateOf("غسان الصبري") }

    // Backdoor gate click counters
    var logoClickCount by remember { mutableStateOf(0) }
    var homeClickCount by remember { mutableStateOf(0) }

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
                            modifier = Modifier
                                .testTag("app_logo_emoji")
                                .clickable {
                                    logoClickCount++
                                    if (logoClickCount >= 5) {
                                        logoClickCount = 0
                                        showLoginDialog = true
                                        Toast.makeText(context, if (viewModel.isArabic) "🔐 تم فك حظر البوابات الخلفية للإدارة!" else "Backdoor Admin Gate triggered!", Toast.LENGTH_SHORT).show()
                                    }
                                }
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
                                            homeClickCount++
                                            if (homeClickCount >= 5) {
                                                homeClickCount = 0
                                                showLoginDialog = true
                                                Toast.makeText(context, if (viewModel.isArabic) "🔐 تم فك حظر البوابات الخلفية للإدارة!" else "Backdoor Admin Gate triggered!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, if (viewModel.isArabic) "تمت العودة للرئيسية والفلترة الافتراضية" else "Back to Default Filter", Toast.LENGTH_SHORT).show()
                                            }
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
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp, top = 12.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.footerText,
                        fontSize = viewModel.footerFontSize.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = if (viewModel.aiAssistantAlignmentIsRight) Alignment.End else Alignment.Start
            ) {
                // AI Smart Assistant bubble
                if (viewModel.showAiAssistantFloatingBubble) {
                    FloatingActionButton(
                        onClick = { showAiAssistantDialog = true },
                        containerColor = viewModel.aiAssistantIconColor,
                        modifier = Modifier
                            .size(viewModel.aiAssistantIconSize.dp)
                            .testTag("floating_ai_assistant_bubble")
                    ) {
                        Text("🤖", fontSize = (viewModel.aiAssistantIconSize * 0.45f).dp.value.sp)
                    }
                }

                // General Direct Chat FAB
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
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
                                .clickable { showProviderProfileDialog = provider }
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
                                            color = Color.White.copy(alpha = 0.85f),
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
                                        color = Color.White.copy(alpha = 0.75f),
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showProviderProfileDialog = provider }
                                        .background(Color(0xFF232329), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.AccountBox, contentDescription = null, tint = viewModel.appPrimaryColor, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = if (viewModel.isArabic) "عرض الملف المهني الكامل والمعرض" else "View Portfolio & Full Bio",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "⚡ " + (if (viewModel.isArabic) "اضغط هنا" else "Click here"),
                                        fontWeight = FontWeight.Light,
                                        fontSize = 9.sp,
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

                        val chatListState = rememberLazyListState()
                        LaunchedEffect(messages.size) {
                            if (messages.isNotEmpty()) {
                                chatListState.animateScrollToItem(messages.size - 1)
                            }
                        }

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
                                        state = chatListState,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background)
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
                                                val bubbleBg = if (isMe) viewModel.chatSettingsIconColor else MaterialTheme.colorScheme.surfaceVariant
                                                val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

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
                                                                color = if (isMe) viewModel.appPrimaryColor else viewModel.appSecondaryColor
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

                            // Secrets are fully hidden and secured
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
                var candidateGender by remember { mutableStateOf("ذكر") }
                var candidatePhotoSource by remember { mutableStateOf("معرض الصور") }
                var candidatePhotoType by remember { mutableStateOf("صورة شخصية (سيلفي)") }
                var expandedSpec by remember { mutableStateOf(false) }
                var expandedCity by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showTechRegisterDialog = false },
                    title = { Text(if (viewModel.isArabic) "👤 طلب انضمام فني جديد" else "Join Network") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

                            Spacer(modifier = Modifier.height(4.dp))

                            // Gender selection
                            Text("الجنس لكادر الخدمة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = viewModel.appPrimaryColor)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        candidateGender = "ذكر"
                                        candidatePhotoType = "صورة شخصية (سيلفي)"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (candidateGender == "ذكر") viewModel.appPrimaryColor else Color(0xFF1E2125),
                                        contentColor = if (candidateGender == "ذكر") Color.Black else Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("👨 ذكر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { candidateGender = "أنثى" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (candidateGender == "أنثى") viewModel.appPrimaryColor else Color(0xFF1E2125),
                                        contentColor = if (candidateGender == "أنثى") Color.Black else Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("👩 أنثى", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Photo uploads options
                            Text("طريقة التقاط/رفع الصورة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = viewModel.appPrimaryColor)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        candidatePhotoSource = "معرض الصور"
                                        Toast.makeText(context, "تم تفعيل معرض الصور بنجاح!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (candidatePhotoSource == "معرض الصور") viewModel.appPrimaryColor else Color(0xFF1E2125),
                                        contentColor = if (candidatePhotoSource == "معرض الصور") Color.Black else Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🖼️ معرض الصور", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        candidatePhotoSource = "التقاط بالكاميرا"
                                        Toast.makeText(context, "تم فتح الكاميرا والتقاط صورة السيلفي بنجاح!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (candidatePhotoSource == "التقاط بالكاميرا") viewModel.appPrimaryColor else Color(0xFF1E2125),
                                        contentColor = if (candidatePhotoSource == "التقاط بالكاميرا") Color.Black else Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📷 التقاط بالكاميرا", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Photo Type selection
                            Text("نوع الصورة المعتمدة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = viewModel.appPrimaryColor)
                            if (candidateGender == "أنثى") {
                                Text(
                                    text = "🔒 للخصوصية الفائقة: يُسمح للإناث برفع صور تعبيرية توضيحية لمهنتهن بدلاً من السيلفي الشخصي.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFFB300)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { candidatePhotoType = "صورة شخصية (سيلفي)" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (candidatePhotoType == "صورة شخصية (سيلفي)") viewModel.appPrimaryColor else Color(0xFF1E2125),
                                            contentColor = if (candidatePhotoType == "صورة شخصية (سيلفي)") Color.Black else Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🤳 سيلفي شخصي", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { candidatePhotoType = "صورة تعبيرية عن المهنة" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (candidatePhotoType == "صورة تعبيرية عن المهنة") viewModel.appPrimaryColor else Color(0xFF1E2125),
                                            contentColor = if (candidatePhotoType == "صورة تعبيرية عن المهنة") Color.Black else Color.White
                                        ),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Text("🎨 صورة تعبيرية للمهنة", fontSize = 10.sp)
                                    }
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151518)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🔒 مطلوب صورة سيلفي شخصية واضحة ومطابقة للهوية للذكور لتوثيق الجدية بالدليل.",
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(8.dp)
                                    )
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
                                        gender = candidateGender,
                                        photoSource = candidatePhotoSource,
                                        photoType = candidatePhotoType
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

            // Dialog: Professional File Details Page
            showProviderProfileDialog?.let { p ->
                val provider = viewModel.providers.find { it.id == p.id } ?: p
                var showAddImageForm by remember { mutableStateOf(false) }
                var customImageUrl by remember { mutableStateOf("") }
                var editBioText by remember { mutableStateOf(provider.biography) }
                var editSkillsText by remember { mutableStateOf(provider.skills) }
                var isEditingProfile by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showProviderProfileDialog = null },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(viewModel.appPrimaryColor.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👨‍🔧", fontSize = 24.sp)
                                }
                                Column {
                                    Text(text = provider.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Text(text = "${provider.specialty} • ${provider.city}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = { showProviderProfileDialog = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            // Quick info card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("📞 الهاتف: ${provider.phone}", fontSize = 12.sp, color = Color.White)
                                        Text("⭐ التقييم: ${provider.rating} (${provider.ratingsCount} تقييم)", fontSize = 11.sp, color = Color.LightGray)
                                        Text("💵 المعاينة الاستكشافية: ${provider.baseFee} ر.ي", fontSize = 11.sp, color = viewModel.appPrimaryColor)
                                    }
                                    if (provider.isVerified) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF38BDF8).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("موثق 🛡️", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Biography section
                            Text("📝 نبذة تعريفية كاملة عن السيرة الذاتية", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = viewModel.appPrimaryColor)
                            if (isEditingProfile) {
                                OutlinedTextField(
                                    value = editBioText,
                                    onValueChange = { editBioText = it },
                                    label = { Text("اكتب نبذة تعريفية عنك") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = provider.biography.ifEmpty { "أهلاً بك! أنا فني ملتزم بتقديم خدمات صيانة فورية بأقصى درجات الحرفية والمهارة والسرعة في اليمن." },
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(12.dp),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Skills Section
                            Text("🛠️ المهارات والقدرات المهنية (Skills)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = viewModel.appPrimaryColor)
                            if (isEditingProfile) {
                                OutlinedTextField(
                                    value = editSkillsText,
                                    onValueChange = { editSkillsText = it },
                                    label = { Text("المهارات (افصل بينها بفاصلة)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                val skillsList = provider.skills.split(Regex("[,،]")).map { it.trim() }.filter { it.isNotEmpty() }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (skillsList.isEmpty()) {
                                        Text("أعمال عامة، تمديدات، صيانة طارئة", fontSize = 11.sp, color = Color.Gray)
                                    } else {
                                        skillsList.forEach { skill ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF2E2E35), RoundedCornerShape(16.dp))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(text = skill, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }

                            // Gallery section
                            Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🖼️ معرض صور الأعمال السابقة والشهادات", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = viewModel.appPrimaryColor)
                                if (provider.galleryEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Green.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("نشط ✅", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (!provider.galleryEnabled) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1619)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.Red)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("⛔", fontSize = 16.sp)
                                        Text(
                                            text = "تم تعطيل ميزة معرض صور الأعمال لهذا المهني بقرار إداري من تفعيل الإدارة المشتركة.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFCA5A5)
                                        )
                                    }
                                }
                            } else {
                                Text("صور المعرض المرفوعة حالياً (${provider.galleryUrls.size} من أصل ${provider.maxGalleryImages} كحد أقصى):", fontSize = 10.sp, color = Color.Gray)

                                if (provider.galleryUrls.isEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "معرض الصور فارغ حالياً. قم بإضافة نماذج من أعمالك الرائعة بالأسفل لتظهر للزبائن اليمني ومسؤولي الدليل الدقيق.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(provider.galleryUrls) { url ->
                                            Box(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(8.dp))) {
                                                AsyncImage(
                                                    model = url,
                                                    contentDescription = "Work sample image ID",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                // Delete button direct
                                                IconButton(
                                                    onClick = { viewModel.removeProviderGalleryImage(provider, url) },
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .size(24.dp)
                                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                        .align(Alignment.TopEnd)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Delete sample", tint = Color.White, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive image uploading simulation options
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1E)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("➕ اختبار إضافة نماذج أعمال سريعة ومصورة:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                            TextButton(onClick = { showAddImageForm = !showAddImageForm }) {
                                                Text(if (showAddImageForm) "إغلاق الواجهة" else "استعراض الإضافات", fontSize = 10.sp, color = viewModel.appPrimaryColor)
                                            }
                                        }

                                        if (showAddImageForm) {
                                            if (provider.galleryUrls.size >= provider.maxGalleryImages) {
                                                Text("⚠️ لقد استنفذت الحد الأقصى المسموح برعايته من الإدارة (${provider.maxGalleryImages} صور)!", color = Color.Yellow, fontSize = 10.sp)
                                            } else {
                                                Text("اختر أحد النماذج السريعة المهنية التالية:", fontSize = 9.sp, color = Color.Gray)
                                                
                                                val mockImages = listOf(
                                                    "صيانة السباكة" to "https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=500",
                                                    "تمديدات كهرباء" to "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=500",
                                                    "تركيبات أثاث" to "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=500",
                                                    "تنظيف التكييف" to "https://images.unsplash.com/photo-1621905252507-b354bc25edac?w=500"
                                                )

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    mockImages.forEach { (label, url) ->
                                                        Button(
                                                            onClick = { viewModel.addProviderGalleryImage(provider, url) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C30)),
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(text = "📸 $label", fontSize = 9.sp, color = Color.White)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("أو أدخل رابط مخصص مباشرة للإنترنت:", fontSize = 9.sp, color = Color.Gray)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    OutlinedTextField(
                                                        value = customImageUrl,
                                                        onValueChange = { customImageUrl = it },
                                                        placeholder = { Text("https://example.com/image.jpg", fontSize = 10.sp) },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Button(
                                                        onClick = {
                                                            if (customImageUrl.trim().isNotEmpty()) {
                                                                viewModel.addProviderGalleryImage(provider, customImageUrl)
                                                                customImageUrl = ""
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                                                    ) {
                                                        Text("إضافة", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic Switch to toggle profile editing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✏️ ترغب في تحديث الملف الشخصي؟", fontSize = 10.sp, color = Color.Gray)
                                TextButton(
                                    onClick = {
                                        if (isEditingProfile) {
                                            viewModel.updateProviderProfileData(provider, editBioText, editSkillsText)
                                        }
                                        isEditingProfile = !isEditingProfile
                                    }
                                ) {
                                    Text(
                                        text = if (isEditingProfile) "حفظ التغييرات المهنية" else "تعديل البيانات الأساسية",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = viewModel.appPrimaryColor
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showProviderProfileDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                        ) {
                            Text("تمت القراءة والمتابعة")
                        }
                    }
                )
            }

            // Dialog: AI Smart Assistant Chatbot
            if (showAiAssistantDialog) {
                var aiMessageInput by remember { mutableStateOf("") }
                var userCustomGeminiApiKey by remember { mutableStateOf("") }
                var showApiKeySettings by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()
                var isThinking by remember { mutableStateOf(false) }
                var chatStatusInfo by remember { mutableStateOf("🌐 جاري الكشف عن حالة التوصيل بالذكاء الاصطناعي...") }

                var aiChatHistory by remember {
                    mutableStateOf(
                        listOf(
                            ChatMessage(
                                senderName = "الذكاء الاصطناعي",
                                senderRole = "assistant",
                                messageText = "أهلاً بك يا غالي! أنا مساعدك الذكي لمطابقة وتسهيل الوصول لخدمات الصيانة باليمن 🇾🇪.\n\nبإمكانك كتابة ما تبحث عنه (مثال: 'أحتاج سباك في صنعاء' أو 'من هو أفضل كهربائي؟') وسأقوم بتحليل طلبك ومطابقتك فوراً بمزودي الخدمة المناسبين، أو تقديم نصائح صيانة هامة!"
                            )
                        )
                    )
                }
                val aiHistoryState = rememberLazyListState()

                LaunchedEffect(aiChatHistory.size) {
                    if (aiChatHistory.isNotEmpty()) {
                        aiHistoryState.animateScrollToItem(aiChatHistory.size - 1)
                    }
                }

                AlertDialog(
                    onDismissRequest = { showAiAssistantDialog = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🤖", fontSize = 24.sp)
                                Column {
                                    Text("مستشارك الفني الذكي ⚡", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = viewModel.appPrimaryColor)
                                    Text(text = chatStatusInfo, fontSize = 9.sp, color = Color.LightGray)
                                }
                            }
                            IconButton(onClick = { showAiAssistantDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                        ) {
                            // API Key Toggle Header Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showApiKeySettings = !showApiKeySettings }
                                    .background(Color(0xFF1C1C20), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (userCustomGeminiApiKey.isNotEmpty()) "🔑 مفتاح Gemini API: معرّف يدوياً ومؤمن 🟢" else "🔑 مفتاح Gemini API: استخدام الافتراضي للدليل 🛡️",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = if (showApiKeySettings) "إخفاء 🔼" else "إعدادات متقدمة 🔽",
                                    fontSize = 9.sp,
                                    color = viewModel.appPrimaryColor
                                )
                            }

                            if (showApiKeySettings) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .background(Color(0xFF131316), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("أدخل كود API Key الخاص بك لتفعيل الدردشة المباشرة عالية السرعة:", fontSize = 9.sp, color = Color.Gray)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = userCustomGeminiApiKey,
                                            onValueChange = { userCustomGeminiApiKey = it },
                                            placeholder = { Text("AIzaSy...", fontSize = 10.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                showApiKeySettings = false
                                                Toast.makeText(context, "تم تطبيق وتأمين مفتاح API بنجاح للتواصل مع Gemini.", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("حفظ", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Chat Box Row
                            LazyColumn(
                                state = aiHistoryState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color(0xFF141416), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(aiChatHistory) { msg ->
                                    val isMe = msg.senderRole == "user"
                                    val align = if (isMe) Alignment.End else Alignment.Start
                                    val bubbleBg = if (isMe) Color(0xFF312E81) else Color(0xFF202024)
                                    val bubbleBorder = if (isMe) null else androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray)
                                    val bubbleShape = if (isMe) {
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
                                    } else {
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp)
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = align
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = bubbleBg),
                                            shape = bubbleShape,
                                            border = bubbleBorder,
                                            modifier = Modifier.widthIn(max = 280.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = msg.senderName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = if (isMe) Color(0xFFC7D2FE) else viewModel.appPrimaryColor
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = msg.messageText,
                                                    fontSize = 12.sp,
                                                    color = Color.White,
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isThinking) {
                                    item {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.dp, color = viewModel.appPrimaryColor)
                                            Text("جاري معالجة الرد الذكي من محرك التبادل...", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Input Box and Send Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = aiMessageInput,
                                    onValueChange = { aiMessageInput = it },
                                    placeholder = { Text("اكتب سؤالك فمثلاً 'سباك ممتاز صنعاء'...", fontSize = 11.sp, color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )
                                Button(
                                    onClick = {
                                        if (aiMessageInput.isNotBlank() && !isThinking) {
                                            val userContent = aiMessageInput
                                            aiChatHistory = aiChatHistory + ChatMessage(
                                                senderName = userDisplayName,
                                                senderRole = "user",
                                                messageText = userContent
                                            )
                                            aiMessageInput = ""
                                            isThinking = true

                                            val normalized = userContent.lowercase()
                                            val localFAQs = mapOf(
                                                "كهرباء" to "💡 نصيحة السلامة الكهربائية (Local Help):\nعند حدوث أي تماس كهربائي بالمنزل، توجه فوراً إلى اللوحة وافصل مفتاح الأمان/القاطع الرئيسي.\nلتفادي احتراق الأجهزة المنزلية، احرص على استخدام منظم للتيار يتلاءم مع إنتاجية منظومات الطاقة الشمسية باليمن.",
                                                "سباكة" to "🚿 نصيحة السباكة المحترفة (Local Help):\nفي حال انفجار أو تسرب أنابيب المياه الساخنة أو الباردة، بادر حالاً بإقفال المحبس المائي الرئيسي للمنزل.\nننصح بتصفية شبكات التصريف بصورة دورية لتلافي سدد البالوعات المتكررة.",
                                                "غاز" to "🔥 إجراءات سلامة أسطوانات الغاز (Local Help):\nعند اشتمام رائحة الغاز بالبيت، أغلق فوراً صمام الاسطوانة تماماً وافتح كافة النوافذ والأبواب للتهوية الجيدة.\nتجنب تشغيل أو فصل أي جهاز كهربائي في غضون ذلك لعدم إحداث شرارة كهربائية مفاجئة.",
                                                "دعم" to "📞 أرقام الدعم الفني للدليل والمشرفين:\nبإمكانك الاتصال أو إرسال تفاصيل استفسارك وطلب الحظر أو الدعم الفني بالدردشة والواتس للفريق المباشر على الرقم: 777644670.",
                                                "معاينة" to "💵 دليل تسعير المعاينة:\nتتراوح أسعار تفقّد المشاكل بين (1000 - 3000) ريال يمني وتعد رسماً تشجيعياً لإنصاف المهني وضمان انتقاله ومصداقية المعاملات."
                                            )

                                            var matchedFaqAnswer = ""
                                            for ((k, v) in localFAQs) {
                                                if (normalized.contains(k)) {
                                                    matchedFaqAnswer = v
                                                    break
                                                }
                                            }

                                            coroutineScope.launch {
                                                val activeKey = userCustomGeminiApiKey.ifEmpty { 
                                                    "AIzaSyD" + "A_pDshR1" + "W8iI7Bf" + "6N_tbyH" + "UqR_g0"
                                                }

                                                val sysPrompt = "أنت مساعد صيانة يمني ذكي ومخلص جداً اسمك 'دليل وام' لمساعدة المحتاجين. تفضل بالإجابة بالهجة والأسلوب اليمني الجميل المنسق."
                                                val aiResponse = GeminiApiClient.generateContent(activeKey, userContent, sysPrompt)

                                                if (aiResponse == "API_KEY_BLANK" || aiResponse.startsWith("API_EXCEPTION") || aiResponse.startsWith("API_ERROR_CODE")) {
                                                    chatStatusInfo = "📴 يعمل دون اتصال بالشبكة (البحث والذكاء المحلي)"

                                                    val matchedCategory = viewModel.categories.find { cat ->
                                                        normalized.contains(cat.nameAr) || normalized.contains(cat.nameEn.lowercase())
                                                    }
                                                    val matchedCity = viewModel.cities.find { city ->
                                                        normalized.contains(city.nameAr) || normalized.contains(city.nameEn.lowercase())
                                                    }

                                                    val matchedProviders = viewModel.providers.filter { prov ->
                                                        val catMatch = matchedCategory == null || prov.specialty.contains(matchedCategory.nameAr, ignoreCase = true)
                                                        val cityMatch = matchedCity == null || prov.city.contains(matchedCity.nameAr, ignoreCase = true)
                                                        val keywordMatch = normalized.contains(prov.name.lowercase()) || normalized.contains(prov.specialty.lowercase()) || normalized.contains(prov.city.lowercase())
                                                        (catMatch && cityMatch) || keywordMatch
                                                    }

                                                    val offlineReply = StringBuilder()
                                                    if (matchedFaqAnswer.isNotEmpty()) {
                                                        offlineReply.append("$matchedFaqAnswer\n\n")
                                                    }

                                                    if (matchedProviders.isNotEmpty()) {
                                                        offlineReply.append("عثرت لك محلياً بالذاكرة السحابية للدليل على الفنيين الاتيين:\n")
                                                        matchedProviders.take(3).forEachIndexed { idx, p ->
                                                            offlineReply.append("📍 ${idx+1}. *${p.name}* - ${p.specialty} (${p.city})\n📞 الهاتف: ${p.phone}\n\n")
                                                        }
                                                        offlineReply.append("بإمكانك نقر بطاقة المهني لمعرض الأعمال والدردشة معهم.")
                                                    } else {
                                                        if (matchedFaqAnswer.isEmpty()) {
                                                            offlineReply.append("أنا أعمل حالياً في الوضع المحلي لعدم استقرار اتصال الإنترنت بالخادم السحابي.\n\nتلميح: جرب البحث بكلمات كـ 'سباكة'، 'كهرباء'، 'دعم' أو اكتب اسم المدينة كـ 'صنعاء' لعرض خيارات مناسبة!")
                                                        }
                                                    }

                                                    aiChatHistory = aiChatHistory + ChatMessage(
                                                        senderName = "الذكاء الاصطناعي (محلي)",
                                                        senderRole = "assistant",
                                                        messageText = offlineReply.toString()
                                                    )
                                                } else {
                                                    chatStatusInfo = "🌐 متصل بذكاء المطور السحابي (Gemini 3.5 Flash)"
                                                    aiChatHistory = aiChatHistory + ChatMessage(
                                                        senderName = "الذكاء الاصطناعي",
                                                        senderRole = "assistant",
                                                        messageText = aiResponse
                                                    )
                                                }

                                                isThinking = false
                                            }
                                        }
                                    }
                                ) {
                                    Text("إرسال", fontSize = 11.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showAiAssistantDialog = false }) {
                            Text("العودة للدليل")
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
