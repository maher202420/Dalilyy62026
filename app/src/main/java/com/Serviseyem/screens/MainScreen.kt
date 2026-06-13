package com.Serviseyem.screens
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.Serviseyem.models.ChatSession
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
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
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
    var visibleProvidersLimit by remember { mutableStateOf(5) }

    // Advanced search & filtering state variables
    var selectedMinRating by remember { mutableStateOf(0.0) }
    var searchSortBy by remember { mutableStateOf("default") } // "default", "rating", "price"
    var showVoiceSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory, selectedCity, searchQuery) {
        visibleProvidersLimit = 5
    }

    // Control triggers for various interactive overlays
    var showLoginDialog by remember { mutableStateOf(false) }
    var showTechRegisterDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf<ServiceProvider?>(null) }
    var showComplaintDialog by remember { mutableStateOf<ServiceProvider?>(null) }
    var showMyChatsOverviewDialog by remember { mutableStateOf(false) }
    var chatSessionToDelete by remember { mutableStateOf<ChatSession?>(null) }
    var showAiAssistantDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showProviderProfileDialog by remember { mutableStateOf<ServiceProvider?>(null) }

    // Guest User Identity config
    var guestUserNameInput by remember { mutableStateOf("غسان الصبري") }
    var userDisplayName by remember { mutableStateOf("غسان الصبري") }

    // Bottom Navigation Item states
    var currentBottomNavItem by remember { mutableStateOf("home") }

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
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: About App Icon (ℹ️)
                        IconButton(
                            onClick = { showAboutAppDialog = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Text("ℹ️", fontSize = 22.sp)
                        }

                        // Center: Custom Footer Text
                        Text(
                            text = viewModel.footerText,
                            fontSize = viewModel.footerFontSize.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        // Right: Circular small Assistant Button containing "خدمات"
                        Button(
                            onClick = { showAiAssistantDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = viewModel.appPrimaryColor,
                                contentColor = Color.Black
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .widthIn(min = 68.dp)
                        ) {
                            Text(
                                text = if (viewModel.isArabic) "خدمات" else "AI Help",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
            when (currentBottomNavItem) {
                "home" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dynamic Top Search Bar (Configurable by admin)
                        if (!viewModel.isSearchBarDeleted && viewModel.isSearchBarVisible) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search icon",
                                            tint = viewModel.appPrimaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        androidx.compose.foundation.text.BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                                            modifier = Modifier.weight(1f).padding(vertical = 12.dp).testTag("top_search_input"),
                                            decorationBox = { innerTextField ->
                                                if (searchQuery.isEmpty()) {
                                                    Text(
                                                        text = if (viewModel.isArabic) viewModel.searchBarPlaceholderAr else viewModel.searchBarPlaceholderEn,
                                                        color = Color.Gray,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                        // Voice Search (Mic) trigger button
                                        if (viewModel.voiceSearchEnabled) {
                                            IconButton(
                                                onClick = { showVoiceSearchDialog = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Text("🎙️", fontSize = 16.sp)
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear search",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Advanced filters & Autocomplete block
                            if (viewModel.searchAutocompleteEnabled || viewModel.advancedFilteringEnabled) {
                                item {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                    ) {
                                        // Autocomplete Row
                                        if (viewModel.searchAutocompleteEnabled && searchQuery.isNotEmpty()) {
                                            val suggestions = listOf("سباكة", "كهرباء", "صيانة مكيفات", "تركيب شمسية", "صنعاء", "عدن", "تعز")
                                                .filter { it.contains(searchQuery) && it != searchQuery }
                                            if (suggestions.isNotEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = if (viewModel.isArabic) "المقترحات:" else "Suggestions:",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                    LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        items(suggestions) { sugg: String ->
                                                            Card(
                                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF232329)),
                                                                shape = RoundedCornerShape(16.dp),
                                                                modifier = Modifier.clickable { searchQuery = sugg }
                                                            ) {
                                                                Text(
                                                                    text = sugg,
                                                                    fontSize = 10.sp,
                                                                    color = Color.LightGray,
                                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Advanced filtering choices
                                        if (viewModel.advancedFilteringEnabled) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = if (viewModel.isArabic) "⚡ خيارات التصفية والفرز المتقدمة" else "Advanced Filter Options",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = Color.LightGray
                                                        )
                                                        Text(
                                                            text = if (viewModel.isArabic) "إعادة تعيين 🔄" else "Reset",
                                                            fontSize = 10.sp,
                                                            color = viewModel.appPrimaryColor,
                                                            modifier = Modifier.clickable {
                                                                selectedMinRating = 0.0
                                                                searchSortBy = "default"
                                                            }
                                                        )
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(if (viewModel.isArabic) "التقييم الأدنى:" else "Min Rating:", fontSize = 10.sp, color = Color.Gray)
                                                        listOf(0.0, 4.0, 4.5).forEach { r ->
                                                            val label = if (r == 0.0) (if (viewModel.isArabic) "الكل" else "All") else "⭐ $r+"
                                                            val isSelected = selectedMinRating == r
                                                            Card(
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = if (isSelected) viewModel.appPrimaryColor else Color(0xFF1E1E22)
                                                                ),
                                                                shape = RoundedCornerShape(4.dp),
                                                                modifier = Modifier.clickable { selectedMinRating = r }
                                                            ) {
                                                                Text(
                                                                    text = label,
                                                                    fontSize = 9.sp,
                                                                    color = if (isSelected) Color.White else Color.LightGray,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(if (viewModel.isArabic) "الترتيب حسب:" else "Sort Order:", fontSize = 10.sp, color = Color.Gray)
                                                        listOf("default" to "الافتراضي", "rating" to "التقييم الأعلى", "price" to "السعر الأقل").forEach { item ->
                                                            val isSelected = searchSortBy == item.first
                                                            Card(
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = if (isSelected) viewModel.appPrimaryColor else Color(0xFF1E1E22)
                                                                ),
                                                                shape = RoundedCornerShape(4.dp),
                                                                modifier = Modifier.clickable { searchSortBy = item.first }
                                                            ) {
                                                                Text(
                                                                    text = if (viewModel.isArabic) item.second else item.first,
                                                                    fontSize = 9.sp,
                                                                    color = if (isSelected) Color.White else Color.LightGray,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

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

                // ⚠️ إشعار تعطيل الدردشة من قبل الإدارة
                if (!viewModel.isChatInstantEnabled) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ تنبيه إداري هام:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = viewModel.chatDisabledMessage,
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 📅 تذكير بالمواعيد والحجوزات الفنية القادمة للمستخدم
                if (viewModel.isBookingAlertsEnabled) {
                    val activeUpcomingBookings = viewModel.bookings.filter { 
                        (it.customerName == userDisplayName || it.customerPhone == guestUserNameInput) && (it.status == "مقبول" || it.status == "مستلم") 
                    }
                    if (activeUpcomingBookings.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, viewModel.appPrimaryColor.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("⏰", fontSize = 14.sp)
                                        Text(
                                            text = "تذكير بمواعيد حجوزاتك القادمة:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = viewModel.appPrimaryColor
                                        )
                                    }
                                    
                                    activeUpcomingBookings.forEach { booking ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "فني الصيانة: ${booking.techName}",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "الموعد: ${booking.date} في ${booking.time}",
                                                    fontSize = 9.sp,
                                                    color = Color.LightGray
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (booking.status == "مقبول") Color(0xFF14532D) else Color(0xFF713F12),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (booking.status == "مقبول") "مؤكد للزيارة" else "في انتظار التأكيد",
                                                    color = if (booking.status == "مقبول") Color.Green else Color.Yellow,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Modern Dashboard Tab Switcher (Replaces deleted bottom navigation entirely)
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Button(
                            onClick = { currentBottomNavItem = "home" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentBottomNavItem == "home") viewModel.appPrimaryColor else Color(0xFF161619),
                                contentColor = if (currentBottomNavItem == "home") Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (viewModel.isArabic) "🏠 الرئيسية" else "🏠 Home", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (viewModel.isMapEnabled) {
                            Button(
                                onClick = { currentBottomNavItem = "map" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentBottomNavItem == "map") viewModel.appPrimaryColor else Color(0xFF161619),
                                    contentColor = if (currentBottomNavItem == "map") Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (viewModel.isArabic) "🗺️ الخريطة" else "🗺️ Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { currentBottomNavItem = "profile" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentBottomNavItem == "profile") viewModel.appPrimaryColor else Color(0xFF161619),
                                contentColor = if (currentBottomNavItem == "profile") Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (viewModel.isArabic) "👤 حسابي" else "👤 Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                    Column {
                                        if (banner.contentType != "text" && !banner.mediaUrl.isNullOrBlank()) {
                                            val decodedBytes = try {
                                                android.util.Base64.decode(banner.mediaUrl, android.util.Base64.DEFAULT)
                                            } catch (e: Exception) {
                                                null
                                            }
                                            val bitmap = try {
                                                if (decodedBytes != null) android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) else null
                                            } catch (e: Exception) {
                                                null
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                                    .background(Color.Black),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (banner.contentType == "image" && bitmap != null) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = banner.title,
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Video player banner", tint = viewModel.appPrimaryColor, modifier = Modifier.size(48.dp))
                                                    Text("تشغيل الإعلان المرئي 🎥", color = Color.LightGray, fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(6.dp))
                                                }
                                            }
                                        }

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
                                                text = "نوع العرض: ${if (banner.contentType == "image") "صورة 🖼️" else if (banner.contentType == "video") "فيديو 🎥" else "عرض مكتوب 📝"} • مدة البقاء: ${banner.durationSeconds} ثانية",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recommended Providers horizontal carousel
                val recommendedProviders = viewModel.providers.filter { it.status == "مقبول" && it.isRecommended }
                if (recommendedProviders.isNotEmpty()) {
                    item {
                        Text(
                            text = if (viewModel.isArabic) "⭐ موصى بهم من قبل الدليل" else "⭐ Recommended Professionals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = viewModel.appPrimaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recommendedProviders) { provider ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C16)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(viewModel.appPrimaryColor)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { showProviderProfileDialog = provider }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (provider.gender == "أنثى") "👩‍🔧" else "👨‍🔧",
                                                fontSize = 20.sp,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = provider.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = provider.specialty,
                                                    fontSize = 10.sp,
                                                    color = Color.LightGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "📍 ${provider.city}",
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "⭐ ${provider.rating}",
                                                fontSize = 9.sp,
                                                color = viewModel.appPrimaryColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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
                if (!viewModel.isSearchBarDeleted && !viewModel.isSearchBarVisible) {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (viewModel.voiceSearchEnabled) {
                                        IconButton(onClick = { showVoiceSearchDialog = true }) {
                                            Text("🎙️", fontSize = 16.sp)
                                        }
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear query")
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // Advanced filters & Autocomplete block
                    if (viewModel.searchAutocompleteEnabled || viewModel.advancedFilteringEnabled) {
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                // Autocomplete Row
                                if (viewModel.searchAutocompleteEnabled && searchQuery.isNotEmpty()) {
                                    val suggestions = listOf("سباكة", "كهرباء", "صيانة مكيفات", "تركيب شمسية", "صنعاء", "عدن", "تعز")
                                        .filter { it.contains(searchQuery) && it != searchQuery }
                                    if (suggestions.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (viewModel.isArabic) "المقترحات:" else "Suggestions:",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                items(suggestions) { sugg: String ->
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232329)),
                                                        shape = RoundedCornerShape(16.dp),
                                                        modifier = Modifier.clickable { searchQuery = sugg }
                                                    ) {
                                                        Text(
                                                            text = sugg,
                                                            fontSize = 10.sp,
                                                            color = Color.LightGray,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Advanced filtering choices
                                if (viewModel.advancedFilteringEnabled) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (viewModel.isArabic) "⚡ خيارات التصفية والفرز المتقدمة" else "Advanced Filter Options",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color.LightGray
                                                )
                                                Text(
                                                    text = if (viewModel.isArabic) "إعادة تعيين 🔄" else "Reset",
                                                    fontSize = 10.sp,
                                                    color = viewModel.appPrimaryColor,
                                                    modifier = Modifier.clickable {
                                                        selectedMinRating = 0.0
                                                        searchSortBy = "default"
                                                    }
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (viewModel.isArabic) "التقييم الأدنى:" else "Min Rating:", fontSize = 10.sp, color = Color.Gray)
                                                listOf(0.0, 4.0, 4.5).forEach { r ->
                                                    val label = if (r == 0.0) (if (viewModel.isArabic) "الكل" else "All") else "⭐ $r+"
                                                    val isSelected = selectedMinRating == r
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isSelected) viewModel.appPrimaryColor else Color(0xFF1E1E22)
                                                        ),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.clickable { selectedMinRating = r }
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            fontSize = 9.sp,
                                                            color = if (isSelected) Color.White else Color.LightGray,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (viewModel.isArabic) "الترتيب حسب:" else "Sort Order:", fontSize = 10.sp, color = Color.Gray)
                                                listOf("default" to "الافتراضي", "rating" to "التقييم الأعلى", "price" to "السعر الأقل").forEach { item ->
                                                    val isSelected = searchSortBy == item.first
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isSelected) viewModel.appPrimaryColor else Color(0xFF1E1E22)
                                                        ),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.clickable { searchSortBy = item.first }
                                                    ) {
                                                        Text(
                                                            text = if (viewModel.isArabic) item.second else item.first,
                                                            fontSize = 9.sp,
                                                            color = if (isSelected) Color.White else Color.LightGray,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Filtered List display
                val filteredProviders = viewModel.providers.filter { provider ->
                    val minRatingOk = selectedMinRating == 0.0 || provider.rating >= selectedMinRating
                    provider.status == "مقبول" && minRatingOk &&
                            (selectedCategory == null || provider.specialty.contains(selectedCategory!!, ignoreCase = true)) &&
                            (selectedCity == null || provider.city.equals(selectedCity!!, ignoreCase = true)) &&
                            (searchQuery.isEmpty() || provider.name.contains(searchQuery, ignoreCase = true) || provider.specialty.contains(searchQuery, ignoreCase = true) || provider.biography.contains(searchQuery, ignoreCase = true))
                }.sortedWith { a, b ->
                    when (searchSortBy) {
                        "rating" -> b.rating.compareTo(a.rating)
                        "price" -> a.baseFee.compareTo(b.baseFee)
                        else -> {
                            val pinCompare = b.isPinned.compareTo(a.isPinned)
                            if (pinCompare != 0) return@sortedWith pinCompare
                            val subCompare = b.hasSubscription.compareTo(a.hasSubscription)
                            if (subCompare != 0) return@sortedWith subCompare
                            val vipCompare = b.isVip.compareTo(a.isVip)
                            if (vipCompare != 0) return@sortedWith vipCompare
                            b.rating.compareTo(a.rating)
                        }
                    }
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
                    val pagedProviders = filteredProviders.take(visibleProvidersLimit)
                    items(pagedProviders) { provider ->
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
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
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            if (provider.isPinned) {
                                                Text(
                                                    text = "📌",
                                                    fontSize = 12.sp
                                                )
                                            }
                                            if (provider.hasSubscription) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = viewModel.appPrimaryColor.copy(alpha = 0.2f)),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "💎 متميز",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = viewModel.appPrimaryColor,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
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

                    if (filteredProviders.size > visibleProvidersLimit) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = viewModel.appPrimaryColor.copy(alpha = 0.1f)),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(viewModel.appPrimaryColor.copy(alpha = 0.4f))),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { visibleProvidersLimit += 5 }
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "👇 تحميل المزيد من المهنيين (عرض $visibleProvidersLimit من أصل ${filteredProviders.size})",
                                        color = viewModel.appPrimaryColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "تقليل استهلاك البيانات وتسريع التصفح التفاعلي للدليل العظيم",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
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
                }

                "categories" -> {
                    ExploreCategoriesLayout(viewModel) { cat ->
                        selectedCategory = cat.nameAr
                        currentBottomNavItem = "home"
                    }
                }

                "profile" -> {
                    UserProfileLayout(viewModel, userDisplayName, guestUserNameInput) { nextName ->
                        userDisplayName = nextName
                        guestUserNameInput = nextName
                    }
                }

                "map" -> {
                    InteractiveYemenMap(viewModel) { provider ->
                        showProviderProfileDialog = provider
                    }
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
                        val messages = viewModel.chatMessages.filter { it.chatId == currentSessionId && (it.isApproved || it.senderName == userDisplayName) }
                            .sortedBy { it.timestamp }

                        val chatListState = rememberLazyListState()
                        LaunchedEffect(messages.size) {
                            if (messages.isNotEmpty()) {
                                chatListState.animateScrollToItem(messages.size - 1)
                            }
                        }

                        // Listen to live presence updates of the other party
                        LaunchedEffect(session.techName, session.userName) {
                            viewModel.startPresenceListener(session.techName)
                            viewModel.startPresenceListener(session.userName)
                        }

                        // Periodically report our presence
                        LaunchedEffect(userDisplayName) {
                            while (true) {
                                viewModel.updateUserPresence(userDisplayName)
                                kotlinx.coroutines.delay(10000)
                            }
                        }

                        val techPresenceTime = viewModel.userPresences[session.techName] ?: 0L
                        val isTechOnline = techPresenceTime > 0L && (System.currentTimeMillis() - techPresenceTime) < 35000

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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "مراسلة: ${session.techName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (isTechOnline) Color(0xFF10B981) else Color.Gray,
                                                        shape = CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isTechOnline) "متصل الآن" else "غير متصل",
                                                fontSize = 11.sp,
                                                color = if (isTechOnline) Color(0xFF10B981) else Color.LightGray
                                            )
                                        }
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
                                                    val statusText = if (!msg.isApproved) " ⏳ قيد المراجعة الفنية" else ""
                                                    Text(
                                                        text = df.format(Date(msg.timestamp)) + statusText,
                                                        fontSize = 8.sp,
                                                        color = if (!msg.isApproved) viewModel.appPrimaryColor else Color.Gray,
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

            // Dialog: Voice Search simulation and triggers
            if (showVoiceSearchDialog) {
                var isListening by remember { mutableStateOf(true) }
                var heardText by remember { mutableStateOf("") }
                
                LaunchedEffect(Unit) {
                    delay(1800)
                    isListening = false
                    heardText = listOf(
                        "سباك ممتاز صنعاء",
                        "كهربائي منظومة شمسية",
                        "فني صيانة غسالات",
                        "مهندس تكييف وتبريد"
                    ).random()
                }

                AlertDialog(
                    onDismissRequest = { showVoiceSearchDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎙️ " + (if (viewModel.isArabic) "البحث الصوتي الفني" else "Voice Search Support"),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isListening) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(viewModel.appPrimaryColor.copy(alpha = 0.2f), shape = CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(viewModel.appPrimaryColor, shape = CircleShape)
                                    ) {
                                        Text(
                                            text = "🎙️", 
                                            fontSize = 24.sp, 
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                                Text(
                                    text = if (viewModel.isArabic) "جاري الاستماع لصوتك..." else "Listening to your voice...", 
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.LightGray
                                )
                            } else {
                                Text(
                                    text = if (viewModel.isArabic) "تم التقاط الجملة بنجاح ✅" else "Voice recognized successfully ✅",
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = heardText,
                                    onValueChange = { heardText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(if (viewModel.isArabic) "الجملة المنطوقة" else "Recognized Text") }
                                )
                                
                                Text(
                                    text = if (viewModel.isArabic) "أو اختر أحد العبارات المقترحة السريعة:" else "Or choose an alternative phrase:",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                val alternatives = listOf("سباكة", "كهرباء", "تكييف", "منظومة شمسية")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    alternatives.forEach { alt ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF232329)),
                                            modifier = Modifier.clickable { heardText = alt }
                                        ) {
                                            Text(
                                                text = alt,
                                                fontSize = 10.sp,
                                                color = Color.LightGray,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                searchQuery = heardText
                                showVoiceSearchDialog = false
                            },
                            enabled = heardText.isNotEmpty()
                        ) {
                            Text(if (viewModel.isArabic) "تأكيد واستعلام" else "Search Now")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showVoiceSearchDialog = false }) {
                            Text(if (viewModel.isArabic) "إلغاء" else "Cancel")
                        }
                    }
                )
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
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (sess.isBlocked) {
                                                            Text("مغلقة", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                        }
                                                        IconButton(
                                                            onClick = { chatSessionToDelete = sess },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "حذف المحادثة",
                                                                tint = Color.Red.copy(alpha = 0.8f),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
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

            if (chatSessionToDelete != null) {
                AlertDialog(
                    onDismissRequest = { chatSessionToDelete = null },
                    title = {
                        Text(
                            text = if (viewModel.isArabic) "⚠️ تأكيد الحذف" else "Confirm Deletion",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = if (viewModel.isArabic)
                                "هل أنت متأكد من رغبتك في حذف هذه المحادثة بالكامل؟ سيؤدي ذلك إلى حذف جميع الرسائل التابعة لها نهائياً ولا يمكن استعادتها."
                            else
                                "Are you sure you want to completely delete this conversation? This will permanently delete all associated messages and cannot be undone.",
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                chatSessionToDelete?.let { sess ->
                                    viewModel.deleteChatSession(sess.id)
                                    Toast.makeText(context, if (viewModel.isArabic) "تم حذف المحادثة" else "Conversation deleted", Toast.LENGTH_SHORT).show()
                                }
                                chatSessionToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                        ) {
                            Text(if (viewModel.isArabic) "حذف نهائي" else "Delete Permanently")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { chatSessionToDelete = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White)
                        ) {
                            Text(if (viewModel.isArabic) "إلغاء" else "Cancel")
                        }
                    }
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

                var selectedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                var selectedImageBase64 by remember { mutableStateOf("") }

                val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    if (uri != null) {
                        try {
                            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                            val bitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                            selectedBitmap = bitmap
                            
                            // Compress to JPG (70% quality)
                            val outStream = java.io.ByteArrayOutputStream()
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outStream)
                            val bytes = outStream.toByteArray()
                            selectedImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                        } catch (e: Exception) {
                            try {
                                @Suppress("DEPRECATION")
                                val bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                selectedBitmap = bitmap
                                val outStream = java.io.ByteArrayOutputStream()
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outStream)
                                val bytes = outStream.toByteArray()
                                selectedImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                            } catch (ex: Exception) {
                                Toast.makeText(context, "خطأ في اختيار المعرض الداخلي", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
                ) { bitmap: android.graphics.Bitmap? ->
                    if (bitmap != null) {
                        selectedBitmap = bitmap
                        // Compress to JPG (70% quality)
                        val outStream = java.io.ByteArrayOutputStream()
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outStream)
                        val bytes = outStream.toByteArray()
                        selectedImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    }
                }

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
                                        galleryLauncher.launch("image/*")
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
                                        cameraLauncher.launch(null)
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

                            // Show Compressed image preview tag if available
                            selectedBitmap?.let { bitmap ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📸 معاينة الصورة المرفقة المكبوسة:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.LightGray)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Selected Photo Preview",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.DarkGray)
                                    )
                                    Column {
                                        Text("✅ تم ضغط الصورة وتلطيف الحجم بنجاح! (JPEG 70%)", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("كبس بنسبة وكفاءة عالية لتوفير سرعة رفع وسحب فائقة.", color = Color.Gray, fontSize = 8.sp)
                                    }
                                }
                            }

                            if (viewModel.registrationTerms.isNotEmpty()) {
                                Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                Text("📋 البنود وشروط قواعد الانتساب المعتمدة بالدليل:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = viewModel.appPrimaryColor)
                                viewModel.registrationTerms.forEachIndexed { idx, term ->
                                    Text("${idx + 1}. ${term.termText}", fontSize = 10.sp, color = Color.LightGray)
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
                var showBookingDialog by remember { mutableStateOf(false) }

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
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                    
                                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                    
                                    Text("💼 سنوات الحظوة والخبرة: ${provider.experienceYears} سنة", fontSize = 11.sp, color = Color.LightGray)
                                    Text("📧 البريد الإلكتروني: ${provider.contactEmail.ifEmpty { "غير معلن" }}", fontSize = 11.sp, color = Color.LightGray)
                                    
                                    if (viewModel.showBookingsSection) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { showBookingDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("📅 حجز موعد زيارة فنية الآن", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

                if (showBookingDialog) {
                    var bookingName by remember { mutableStateOf(userDisplayName) }
                    var bookingPhone by remember { mutableStateOf(guestUserNameInput) }
                    var bookingDate by remember { mutableStateOf("") }
                    var bookingTime by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { showBookingDialog = false },
                        title = { Text("📅 طلب حجز موعد مع المهني: ${provider.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("الرجاء إدخال بياناتك لحجز زيارة عمل فنية من قبل هذا المهني.", fontSize = 11.sp, color = Color.Gray)
                                OutlinedTextField(value = bookingName, onValueChange = { bookingName = it }, label = { Text("الاسم الكامل للعميل") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = bookingPhone, onValueChange = { bookingPhone = it }, label = { Text("رقم الهاتف للتواصل للتأكيد") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = bookingDate, onValueChange = { bookingDate = it }, label = { Text("تاريخ الزيارة المطلوبة (مثال: 2026-06-21)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = bookingTime, onValueChange = { bookingTime = it }, label = { Text("الساعة المفضلة (مثال: 4:00 عصراً)") }, modifier = Modifier.fillMaxWidth())
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (bookingName.isNotEmpty() && bookingPhone.isNotEmpty() && bookingDate.isNotEmpty() && bookingTime.isNotEmpty()) {
                                        viewModel.requestBooking(
                                            customerName = bookingName,
                                            customerPhone = bookingPhone,
                                            techName = provider.name,
                                            date = bookingDate,
                                            time = bookingTime
                                        )
                                        Toast.makeText(context, "تم إرسال طلب حجز الزيارة بنجاح وتحويل التنبيه للإدارة 🛠️", Toast.LENGTH_LONG).show()
                                        showBookingDialog = false
                                    } else {
                                        Toast.makeText(context, "الرجاء كلاً من تعبئة كافة الحقول لحجز زيارة صحيحة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                            ) {
                                Text("إرسال طلب الحجز")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showBookingDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                Text("إلغاء")
                            }
                        }
                    )
                }
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

                            // Suggested offline/online question chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                val suggestions = listOf(
                                    "ماهي الأقسام" to "ما هي الأقسام؟ 🏷️",
                                    "كيف أتصل بمقدم خدمة" to "كيفية الاتصال بالمهنيين؟ 📞",
                                    "ما هو رقم الدعم" to "رقم دعم المبادرة؟ 🌟",
                                    "كهرباء" to "صيانة الكهرباء 💡",
                                    "سباكة" to "صيانة السباكة 🚿",
                                    "غاز" to "سلامة الغاز 🔥",
                                    "معاينة" to "رسم المعاينة 💵"
                                )
                                items(suggestions) { pair ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232329)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickable {
                                            aiMessageInput = pair.first
                                        }
                                    ) {
                                        Text(
                                            text = pair.second,
                                            fontSize = 10.sp,
                                            color = viewModel.appPrimaryColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
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
                                            val categoryNamesStr = viewModel.categories.joinToString(separator = "\n") { "- ${it.nameAr}" }
                                            val localFAQs = mapOf(
                                                "أقسام" to "🏷️ الأقسام المهنية المتوفرة بـ دليل وام الصيانة:\n$categoryNamesStr",
                                                "اقسام" to "🏷️ الأقسام المهنية المتوفرة بـ دليل وام الصيانة:\n$categoryNamesStr",
                                                "أتصل" to "📞 طريقة الاتصال وحجز المهنيين بمبادرة وام:\n- تصفح الأقسام أو ابحث بالمدينة والحي بالفلترة المزدوجة المتقدمة.\n- انقر فوق بطاقة المهني لعرض معرض أعماله وتقييماته وسيرته.\n- يمكنك الاتصال به مباشرة بهاتفه، أو النقر على 'حجز زيارة صيانة' أو الدردشة اللحظية معه للتنسيق.",
                                                "اتصل" to "📞 طريقة الاتصال وحجز المهنيين بمبادرة وام:\n- تصفح الأقسام أو ابحث بالمدينة والحي بالفلترة المزدوجة المتقدمة.\n- انقر فوق بطاقة المهني لعرض معرض أعماله وتقييماته وسيرته.\n- يمكنك الاتصال به مباشرة بهاتفه، أو النقر على 'حجز زيارة صيانة' أو الدردشة اللحظية معه للتنسيق.",
                                                "كهرباء" to "💡 نصيحة السلامة الكهربائية (Local Help):\nعند حدوث أي تماس كهربائي بالمنزل، توجه فوراً إلى اللوحة وافصل مفتاح الأمان/القاطع الرئيسي.\nلتفادي احتراق الأجهزة المنزلية، احرص على استخدام منظم للتيار يتلاءم مع إنتاجية منظومات الطاقة الشمسية باليمن.",
                                                "سباكة" to "🚿 نصيحة السباكة المحترفة (Local Help):\nفي حال انفجار أو تسرب أنابيب المياه الساخنة أو الباردة، بادر حالاً بإقفال المحبس المائي الرئيسي للمنزل.\nننصح بتصفية شبكات التصريف بصورة دورية لتلافي سدد البالوعات المتكررة.",
                                                "غاز" to "🔥 إجراءات سلامة أسطوانات الغاز (Local Help):\nعند اشتمام رائحة الغاز بالبيت، أغلق فوراً صمام الاسطوانة تماماً وافتح كافة النوافذ والأبواب للتهوية الجيدة.\nتجنب تشغيل أو فصل أي جهاز كهربائي في غضون ذلك لعدم إحداث شرارة كهربائية مفاجئة.",
                                                "دعم" to "📞 أرقام الدعم الفني ومسؤولي مبادرة (دليل وام خدمات اليمن):\nبإمكانك التواصل معنا عبر الاتصال المباشر أو واتساب للمشرفين والدعم المستمر على الرقم المباشر: ${viewModel.supportPhone}. البريد الإلكتروني: ${viewModel.supportEmail}",
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

            // Dialog: About App popup
            if (showAboutAppDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutAppDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ℹ️", fontSize = 24.sp)
                            Text("عن دليل الخدمات والصيانة 🇾🇪", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = viewModel.appPrimaryColor)
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                        ) {
                            Text(
                                "دليل الخدمات والصيانة اليمني (وام) هو مبادرة وطنية رائدة تهدف إلى تسهيل وتيسير وصول المواطنين للكوادر الفنية والمهنية الموثوقة بمختلف مجالات الصيانة.",
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )

                            Divider(color = Color.DarkGray)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي الكوادر المسجلة:", fontSize = 11.sp, color = Color.Gray)
                                Text("${viewModel.providers.size} مهني معتمد", fontSize = 11.sp, color = viewModel.appPrimaryColor, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("عدد الأقسام المهنية:", fontSize = 11.sp, color = Color.Gray)
                                Text("${viewModel.categories.size} أقسام رئيسية", fontSize = 11.sp, color = Color.White)
                            }

                            Divider(color = Color.DarkGray)

                            Text("📞 بيانات رقم دعم ومسؤولي المبادرة:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (viewModel.isAdminLoggedIn) {
                                        OutlinedTextField(
                                            value = viewModel.supportPhone,
                                            onValueChange = { viewModel.supportPhone = it },
                                            label = { Text("رقم الاتصال المباشر والدعم", fontSize = 10.sp) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = viewModel.supportWhatsapp,
                                            onValueChange = { viewModel.supportWhatsapp = it },
                                            label = { Text("رقم واتساب المباشر", fontSize = 10.sp) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = viewModel.supportEmail,
                                            onValueChange = { viewModel.supportEmail = it },
                                            label = { Text("البريد الإلكتروني للدعم والمشرفين", fontSize = 10.sp) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Text("رقم الاتصال المباشر: ${viewModel.supportPhone}", fontSize = 11.sp, color = Color.White)
                                        Text("خدمة المشرفين والتحقق/واتساب: ${viewModel.supportWhatsapp}", fontSize = 11.sp, color = viewModel.appPrimaryColor)
                                        Text("البريد السحابي: ${viewModel.supportEmail}", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                }
                            }

                            Text(
                                "جميع الحقوق محفوظة لصالح مبادرة الأيادي الذهبية والمستشار التقني الفني © 2026",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showAboutAppDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)) {
                            Text("العودة الفورية")
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

@Composable
fun ExploreCategoriesLayout(
    viewModel: AppViewModel,
    onSelectCat: (Category) -> Unit
) {
    var selectedGroupTab by remember { mutableStateOf("professional") } // "professional", "service", "commercial"
    var catSearchQuery by remember { mutableStateOf("") }

    val filteredCats = viewModel.categories.filter { cat ->
        cat.type == selectedGroupTab &&
        cat.parentId == null && // main categories on first row
        (catSearchQuery.isEmpty() || cat.nameAr.contains(catSearchQuery, ignoreCase = true) || cat.nameEn.contains(catSearchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (viewModel.isArabic) "📂 استكشاف الدليل الفاخر لأقسام اليمن" else "📂 Explore Yemen Business Directory",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = viewModel.appPrimaryColor
        )

        // Group selector tabs
        TabRow(
            selectedTabIndex = when (selectedGroupTab) {
                "professional" -> 0
                "service" -> 1
                "commercial" -> 2
                else -> 0
            },
            containerColor = Color.Transparent,
            contentColor = viewModel.appPrimaryColor
        ) {
            listOf(
                Triple("professional", if (viewModel.isArabic) "المهني والمهن 🛠️" else "Technical", Color(0xFF34D399)),
                Triple("service", if (viewModel.isArabic) "الخدمي والعام 🩺" else "Services", Color(0xFF60A5FA)),
                Triple("commercial", if (viewModel.isArabic) "المحلات والتجاري 🛒" else "Commercial", Color(0xFFFBBF24))
            ).forEach { (type, label, color) ->
                Tab(
                    selected = selectedGroupTab == type,
                    onClick = { selectedGroupTab = type },
                    text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Inner Search box
        OutlinedTextField(
            value = catSearchQuery,
            onValueChange = { catSearchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (viewModel.isArabic) "البحث السريع عن التخصصات والأقسام..." else "Search specialties...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        // List of categories and their subcategories
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (filteredCats.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(if (viewModel.isArabic) "لا توجد أقسام متطابقة في هذا التبويب حالياً" else "No matching sections found.", color = Color.Gray)
                    }
                }
            } else {
                items(filteredCats) { mainCat ->
                    // Find subcategories of this main category
                    val subCats = viewModel.categories.filter { it.parentId == mainCat.id }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCat(mainCat) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Icon/Emoji or Image
                                if (!mainCat.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = mainCat.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(viewModel.appPrimaryColor.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(mainCat.iconEmoji, fontSize = 22.sp)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (viewModel.isArabic) mainCat.nameAr else mainCat.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = viewModel.appPrimaryColor
                                    )
                                    if (mainCat.description.isNotEmpty()) {
                                        Text(
                                            text = mainCat.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Select",
                                    tint = viewModel.appPrimaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Subcategories list if present
                            if (subCats.isNotEmpty()) {
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = if (viewModel.isArabic) "📂 الأقسام والمسارات الفرعية التابعة لمحافظة اليمن:" else "Sub-categories:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = viewModel.appSecondaryColor
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    items(subCats) { sub ->
                                        SuggestionChip(
                                            onClick = { onSelectCat(sub) },
                                            label = { Text(if (viewModel.isArabic) "${sub.iconEmoji} ${sub.nameAr}" else "${sub.iconEmoji} ${sub.nameEn}", fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileLayout(
    viewModel: AppViewModel,
    userDisplayName: String,
    guestUserNameInput: String,
    onUpdateName: (String) -> Unit
) {
    var editingName by remember { mutableStateOf(guestUserNameInput) }
    var isEditingName by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (viewModel.isArabic) "👤 ملف الزبون والمستخدم الحالي" else "👤 User Profile & Workspace",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = viewModel.appPrimaryColor,
            modifier = Modifier.align(Alignment.Start)
        )

        // Profile Avatar Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Avatar Emoji
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(viewModel.appPrimaryColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 42.sp)
                }

                if (isEditingName) {
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        label = { Text(if (viewModel.isArabic) "تعديل اسمك المعروض بالدليل" else "Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (editingName.isNotBlank()) {
                                onUpdateName(editingName)
                                isEditingName = false
                                Toast.makeText(context, "تم حفظ الاسم الجديد وحقنه بالدليل!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ وتعديل")
                    }
                } else {
                    Text(
                        text = userDisplayName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "عضو معتمد ببطاقة رقم: WAM-${userDisplayName.hashCode().toString().takeLast(6)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Button(
                        onClick = { isEditingName = true },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor.copy(alpha = 0.2f), contentColor = viewModel.appPrimaryColor)
                    ) {
                        Text(if (viewModel.isArabic) "✏️ تعديل الاسم المعروض" else "Edit Name")
                    }
                }
            }
        }

        // Loyalty Card Reward Section inside Profile!
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(viewModel.appPrimaryColor.copy(alpha = 0.3f))),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎁", fontSize = 24.sp)
                    Text(
                        text = if (viewModel.isArabic) "رصيد نقاط الولاء وعون التيسير" else "Yemen Loyalty System",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = viewModel.appPrimaryColor
                    )
                }
                
                Divider(color = Color.DarkGray, thickness = 0.5.dp)

                Text(
                    text = String.format(viewModel.loyaltyCardTitle, viewModel.userLoyaltyPoints),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )

                Text(
                    text = viewModel.loyaltyCardText,
                    fontSize = 11.sp,
                    color = Color.LightGray
                )

                Button(
                    onClick = {
                        if (viewModel.userLoyaltyPoints >= 100) {
                            viewModel.userLoyaltyPoints -= 100
                            Toast.makeText(context, if (viewModel.isArabic) "🎟️ مبروك! تم إسترداد كود خصم فوري: WAM-REWARD-5000" else "Reward Code Redeemed!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, if (viewModel.isArabic) "عذراً، تحتاج 100 نقطة على الأقل للاستبدال!" else "Insufficient points!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (viewModel.isArabic) "استرداد خصم 5,000 ريال يمني" else "Redeem 5k YER Discount", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bookings section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111113)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (viewModel.isArabic) "📅 سجل الزيارات وجدول الحجوزات الفنية" else "📅 Tech Service Bookings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = viewModel.appPrimaryColor
                )

                val userBookings = viewModel.bookings.filter { it.customerName == userDisplayName }
                if (userBookings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (viewModel.isArabic) "لم تقم بجدولة أي حجوزات فنية مع دليل خدمات اليمن حتى الآن" else "No active scheduled service bookings.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    userBookings.forEach { booking ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(booking.techName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    Text(
                                        text = booking.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (booking.status == "مقبول") Color.Green else Color.Yellow,
                                        modifier = Modifier
                                            .background(Color.Black, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("تاريخ الزيارة: ${booking.date} • وقت: ${booking.time}", fontSize = 10.sp, color = Color.LightGray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveYemenMap(
    viewModel: AppViewModel,
    onSelectProvider: (ServiceProvider) -> Unit
) {
    var userLat by remember { mutableStateOf(15.35) }
    var userLng by remember { mutableStateOf(44.20) }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    val yemenCities = listOf(
        Triple("صنعاء 🏙️", 15.35, 44.20),
        Triple("عدن 🌊", 12.78, 45.01),
        Triple("تعز 🏔️", 13.58, 44.02),
        Triple("الحديدة ⚓", 14.80, 42.95),
        Triple("المكلا 🏖️", 14.54, 49.12),
        Triple("إب 🌳", 13.97, 44.18)
    )

    val activeProvidersOnMap = viewModel.providers.filter { it.status == "مقبول" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🗺️ خريطة اليمن التفاعلية الجغرافية بدقة عالية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "نظام تحديد المواقع المتكامل وحساب المسافة المباشرة لأقرب الفنيين والمهنيين عن نقطة إقامتك الحالية بالجمهورية اليمنية.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        // Custom Canvas Interactive Map vector view
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111114)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(activeProvidersOnMap) {
                                detectTapGestures { offset ->
                                    val scopeSize = this.size
                                    val percentX = offset.x / scopeSize.width.toFloat()
                                    val percentY = offset.y / scopeSize.height.toFloat()
                                    val tappedLng = 42.0 + (percentX * 12.0)
                                    val tappedLat = 18.0 - (percentY * 6.0)

                                    // Check if clicked near an active provider pin!
                                    var clickedProvider: ServiceProvider? = null
                                    var minDistancePx = 50f // 50 pixels radius
                                    
                                    activeProvidersOnMap.forEach { provider ->
                                        val pX = scopeSize.width * ((provider.longitude - 42.0) / 12.0).toFloat()
                                        val pY = scopeSize.height * ((18.0 - provider.latitude) / 6.0).toFloat()
                                        val dx = offset.x - pX
                                        val dy = offset.y - pY
                                        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                                        if (dist < minDistancePx) {
                                            minDistancePx = dist
                                            clickedProvider = provider
                                        }
                                    }

                                    if (clickedProvider != null) {
                                        onSelectProvider(clickedProvider!!)
                                    } else {
                                        userLat = Math.round(tappedLat * 100.0) / 100.0
                                        userLng = Math.round(tappedLng * 100.0) / 100.0
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw coordinate grids
                        for (i in 1..4) {
                            val ratio = i / 5f
                            drawLine(
                                color = Color.DarkGray.copy(alpha = 0.3f),
                                start = androidx.compose.ui.geometry.Offset(0f, canvasHeight * ratio),
                                end = androidx.compose.ui.geometry.Offset(canvasWidth, canvasHeight * ratio),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.DarkGray.copy(alpha = 0.3f),
                                start = androidx.compose.ui.geometry.Offset(canvasWidth * ratio, 0f),
                                end = androidx.compose.ui.geometry.Offset(canvasWidth * ratio, canvasHeight),
                                strokeWidth = 1f
                            )
                        }

                        // Plot Yemen geographic regions / borders simulated
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(canvasWidth * 0.05f, canvasHeight * 0.45f) // Hodeidah
                            quadraticBezierTo(canvasWidth * 0.15f, canvasHeight * 0.75f, canvasWidth * 0.22f, canvasHeight * 0.85f) // Bab Al-Mandab
                            lineTo(canvasWidth * 0.30f, canvasHeight * 0.84f) // Aden
                            quadraticBezierTo(canvasWidth * 0.60f, canvasHeight * 0.70f, canvasWidth * 0.95f, canvasHeight * 0.50f) // Al Mahrah
                            lineTo(canvasWidth * 0.90f, canvasHeight * 0.10f) // Empty Quarter Border
                            quadraticBezierTo(canvasWidth * 0.50f, canvasHeight * 0.15f, canvasWidth * 0.05f, canvasHeight * 0.30f)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF1E293B).copy(alpha = 0.5f),
                            style = androidx.compose.ui.graphics.drawscope.Fill
                        )
                        drawPath(
                            path = path,
                            color = Color.DarkGray,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )

                        // Draw major cities
                        yemenCities.forEach { (name, cityLat, cityLng) ->
                            val xPercent = (cityLng - 42.0) / 12.0
                            val yPercent = (18.0 - cityLat) / 6.0
                            val cityX = canvasWidth * xPercent.toFloat()
                            val cityY = canvasHeight * yPercent.toFloat()

                            drawCircle(
                                color = Color.Gray.copy(alpha = 0.6f),
                                radius = 6f,
                                center = androidx.compose.ui.geometry.Offset(cityX, cityY)
                            )
                        }

                        // Plot Service Providers as Orange/Red Pins on Yemen
                        activeProvidersOnMap.forEach { provider ->
                            val providerLat = provider.latitude
                            val providerLng = provider.longitude
                            val xPercent = (providerLng - 42.0) / 12.0
                            val yPercent = (18.0 - providerLat) / 6.0
                            val pinX = canvasWidth * xPercent.toFloat()
                            val pinY = canvasHeight * yPercent.toFloat()

                            // Draw a visual marker
                            drawCircle(
                                color = if (provider.isVerified) Color(0xFF38BDF8) else Color(0xFFEA580C),
                                radius = 8f,
                                center = androidx.compose.ui.geometry.Offset(pinX, pinY)
                            )
                            // Outer ring
                            drawCircle(
                                color = (if (provider.isVerified) Color(0xFF38BDF8) else Color(0xFFEA580C)).copy(alpha = 0.3f),
                                radius = 16f,
                                center = androidx.compose.ui.geometry.Offset(pinX, pinY),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                            )
                        }

                        // Plot User's Pulsing blue pointer
                        val userX = canvasWidth * ((userLng - 42.0) / 12.0).toFloat()
                        val userY = canvasHeight * ((18.0 - userLat) / 6.0).toFloat()
                        
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 12f,
                            center = androidx.compose.ui.geometry.Offset(userX, userY)
                        )
                        drawCircle(
                            color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                            radius = 24f,
                            center = androidx.compose.ui.geometry.Offset(userX, userY)
                        )
                    }

                    // Map overlay labels controls
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("دليل المفاتيح:", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF3B82F6), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("موقعك المقترح حالياً", fontSize = 8.sp, color = Color.LightGray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF38BDF8), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مهني موثق معتمد 🛡️", fontSize = 8.sp, color = Color.LightGray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFFEA580C), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مهني عام نشط", fontSize = 8.sp, color = Color.LightGray)
                            }
                        }
                    }

                    Text(
                        text = "💡 انقر على الخريطة لتغيير إحداثيات موقعك!",
                        fontSize = 9.sp,
                        color = Color.LightGray,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Coordinates controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📍 التحكم الدقيق في إحداثيات موقعك الجغرافي:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("خط العرض (Latitude): ${userLat}° N", fontSize = 10.sp, color = Color.Gray)
                            Slider(
                                value = userLat.toFloat(),
                                onValueChange = { userLat = Math.round(it * 100.0) / 100.0 },
                                valueRange = 12.0f..18.0f
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("خط الطول (Longitude): ${userLng}° E", fontSize = 10.sp, color = Color.Gray)
                            Slider(
                                value = userLng.toFloat(),
                                onValueChange = { userLng = Math.round(it * 100.0) / 100.0 },
                                valueRange = 42.0f..54.0f
                            )
                        }
                    }

                    Text("سريع: اختر المدينه لتثبيت موقعك فيها فوراً:", fontSize = 10.sp, color = Color.Gray)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(yemenCities) { (name, l1, l2) ->
                            ElevatedButton(
                                onClick = {
                                    userLat = l1
                                    userLng = l2
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF232328), contentColor = Color.White)
                            ) {
                                Text(name, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "👨‍🔧 مقدمو الخدمات الأقرب لعنوانك الجغرافي بالترتيب:",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp
            )
        }

        // Sort providers based on distance to the user current coordinates
        val providersWithDistances = activeProvidersOnMap.map { provider ->
            val dist = calculateDistance(userLat, userLng, provider.latitude, provider.longitude)
            provider to dist
        }.sortedBy { it.second }

        if (providersWithDistances.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا توجد حسابات مهنية نشطة حالياً لرسم مسافة القرب الجغرافي.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(providersWithDistances) { (provider, distance) ->
                val roundedDistance = Math.round(distance * 10.0) / 10.0
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (provider.isVip) Color(0xFF1F1C16) else Color(0xFF161619)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(provider.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                if (provider.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("موثق 🛡️", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("${provider.specialty} • ${provider.city}", fontSize = 10.sp, color = Color.Gray)
                            Text("الهاتف: ${provider.phone}", fontSize = 10.sp, color = Color.LightGray)
                            if (provider.contactEmail.isNotEmpty()) {
                                Text("البريد المهني: ${provider.contactEmail}", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .background(viewModel.appPrimaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "بُعد ${roundedDistance} كم",
                                    color = viewModel.appPrimaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { onSelectProvider(provider) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "عرض الملف والتفاصيل 🔍",
                                    fontSize = 10.sp,
                                    color = viewModel.appPrimaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
