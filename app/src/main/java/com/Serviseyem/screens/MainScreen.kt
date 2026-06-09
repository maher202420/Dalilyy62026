package com.Serviseyem.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.graphicsLayer
import com.Serviseyem.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dialogue triggers
    var showRatingDialog by remember { mutableStateOf(false) }
    var showNewBookingCreator by remember { mutableStateOf(false) }
    var showChatConversationPanel by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showAiAssistantDialog by remember { mutableStateOf(false) }

    var backPressTime by remember { mutableStateOf(0L) }

    BackHandler {
        if (showRatingDialog) {
            showRatingDialog = false
        } else if (showNewBookingCreator) {
            showNewBookingCreator = false
        } else if (showChatConversationPanel) {
            showChatConversationPanel = false
        } else if (showAboutAppDialog) {
            showAboutAppDialog = false
        } else if (showAiAssistantDialog) {
            showAiAssistantDialog = false
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressTime < 2000) {
                (context as? ComponentActivity)?.finish()
            } else {
                backPressTime = currentTime
                Toast.makeText(context, "اضغط مرة أخرى للخروج من التطبيق", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Filter and search elements
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedCityFilter by remember { mutableStateOf<String?>(null) }
    var userSearchRadiusLimit by remember { mutableDoubleStateOf(viewModel.mapRadiusKm) }

    // Other Dialogue states
    var selectedProviderForRating by remember { mutableStateOf<ServiceProvider?>(null) }
    var ratingStarsSelected by remember { mutableIntStateOf(5) }
    var ratingCommentText by remember { mutableStateOf("") }

    // Voice search triggers
    var isVoiceSpeakingSimulated by remember { mutableStateOf(false) }
    var voiceTimerJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Booking appointment triggers
    var bookingCustomerName by remember { mutableStateOf("") }
    var bookingCustomerPhone by remember { mutableStateOf("") }
    var selectedTechForBooking by remember { mutableStateOf<ServiceProvider?>(null) }
    var bookingDateStr by remember { mutableStateOf("2026-06-12") }
    var bookingTimeStr by remember { mutableStateOf("04:30 م") }

    // Chat bubble triggers
    var selectSessionForChat by remember { mutableStateOf<ChatSession?>(null) }
    var newChatMessageInput by remember { mutableStateOf("") }

    // Backdoor secret tapping indicator
    var secretHeaderFlagTapCount by remember { mutableIntStateOf(0) }
    var secretFooterVersionTapCount by remember { mutableIntStateOf(0) }

    // AI Intelligent Assistant state variables (Gemini compatible offline fallback)
    var aiAssistantMessages by remember {
        mutableStateOf(listOf(
            ChatMessage(
                chatId = "ai",
                senderName = "مساعد دليلي الذكي 🤖",
                senderRole = "admin",
                messageText = "مرحباً بك! أنا مستشارك الذكي اليمني في منصة دليلي الفاخرة للخدمات. اسألني عن الفنيين، كلفة المعاينة والتشخيص، أو كيفية التسجيل والانضمام بقائمة المهن وسأجيبك فوراً بدقة واحترافية وبشكل كامل دون إنترنت!"
            )
        ))
    }
    var newAiAssistantInputText by remember { mutableStateOf("") }

    // Match filtering on active providers
    val activeAndApprovedProviders = remember(viewModel.providers, searchQuery, selectedCategoryFilter, selectedCityFilter, userSearchRadiusLimit) {
        viewModel.providers.filter { p ->
            p.status == "مقبول" &&
            (selectedCategoryFilter == null || p.specialty == selectedCategoryFilter) &&
            (selectedCityFilter == null || p.city == selectedCityFilter) &&
            (searchQuery.isEmpty() || p.name.contains(searchQuery, ignoreCase = true) || p.biography.contains(searchQuery, ignoreCase = true) || p.specialty.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🇾🇪",
                                fontSize = 26.sp,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable {
                                        secretHeaderFlagTapCount++
                                        if (secretHeaderFlagTapCount >= 5) {
                                            secretHeaderFlagTapCount = 0
                                            Toast.makeText(context, "🔓 تم كشف بوابة المالك السرية عبر الراية!", Toast.LENGTH_SHORT).show()
                                            onNavigateToAdmin()
                                        }
                                    }
                            )
                            Text(
                                text = "دليل خدمات اليمن",
                                color = viewModel.appPrimaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        IconButton(onClick = { onNavigateToAdmin() }) {
                            Icon(
                                imageVector = Icons.Default.SettingsInputComposite,
                                contentDescription = "بوابة المالك",
                                tint = viewModel.appPrimaryColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0C0C0C))
            )
        },
        containerColor = Color(0xFF0C0D0E)
    ) { paddingVals ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
            ) {

                // 1. Marquee Banner Yemen Welcome Flag
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
                        border = BorderStroke(1.dp, Color(0xFF252525))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🇾🇪", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            AutoMarqueeText(
                                text = "أهلاً ومرحباً بكم مع تطبيق دليل كل خدمات اليمن - الرفيق الموثوق للأعمال المهنية وصيانة المنازل بدقة معيارية لحظية متميزة",
                                color = viewModel.appPrimaryColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 1.5 Admin Chat/Status Alert Announcement
                if (!viewModel.isChatInstantEnabled) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "تنبيه", tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = viewModel.chatDisabledMessage,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 2. Active Sponsored Banner
                if (viewModel.banners.any { it.isVisible }) {
                    val currentAd = viewModel.banners.first { it.isVisible }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                            border = BorderStroke(1.dp, viewModel.appPrimaryColor)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔥 إعلان ممول معتمد",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E3E)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "عرض رسمي",
                                            color = Color.LightGray,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentAd.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "توجيه تلقائي إلى تخصص: ${currentAd.targetSectionId}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedCategoryFilter = currentAd.targetSectionId
                                        Toast.makeText(context, "تم التوجيه وتصفية الدليل إلى: ${currentAd.targetSectionId}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.align(Alignment.End),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("عرض الفنيين المشمولين", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. Search and Radius Filters Panel
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF222222), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it 
                                    if (searchQuery == "/admin" || searchQuery == "7777") {
                                        onNavigateToAdmin()
                                    }
                                },
                                placeholder = { Text("بحث باسم الفني أو المهارة...", fontSize = 12.sp, color = Color.Gray) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = viewModel.appPrimaryColor) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0A0A0A),
                                    unfocusedContainerColor = Color(0xFF0A0A0A)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            // Mic Speech Recognition Button
                            if (viewModel.voiceSearchEnabled) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (isVoiceSpeakingSimulated) Color.Red else viewModel.appPrimaryColor,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (!isVoiceSpeakingSimulated) {
                                                isVoiceSpeakingSimulated = true
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "🎙️ جاري الاستماع الصوتي... تحدث الآن!",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                                voiceTimerJob = coroutineScope.launch {
                                                    delay(3000)
                                                    isVoiceSpeakingSimulated = false
                                                    // Make a simulated search result
                                                    val results = listOf("سباكة", "تبريد وتكييف", "كهرباء")
                                                    val sp = results.random()
                                                    selectedCategoryFilter = sp
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "🔊 تم تمييز الصوت بنجاح: '$sp'",
                                                            Toast.LENGTH_LONG
                                                        )
                                                        .show()
                                                }
                                            } else {
                                                isVoiceSpeakingSimulated = false
                                                voiceTimerJob?.cancel()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isVoiceSpeakingSimulated) Icons.Default.MicNone else Icons.Default.Mic,
                                        contentDescription = "بحث صوتي",
                                        tint = if (isVoiceSpeakingSimulated) Color.White else Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Search Radius limit selector
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📍 نطاق الفرز الجغرافي: ${userSearchRadiusLimit.toInt()} كم",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = userSearchRadiusLimit.toFloat(),
                                onValueChange = { userSearchRadiusLimit = it.toDouble() },
                                valueRange = 5f..50f,
                                steps = 9,
                                modifier = Modifier.width(180.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = viewModel.appPrimaryColor,
                                    activeTrackColor = viewModel.appPrimaryColor
                                )
                            )
                        }
                    }
                }

                // 4. Horizontal elite recommendation cards
                item {
                    val recommendedTechs = viewModel.providers.filter { it.isVip || it.isVerified }
                    if (recommendedTechs.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "★ الكوادر الموصى بها ونخبة الـ VIP:",
                                color = viewModel.appPrimaryColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(recommendedTechs) { tech ->
                                    Card(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .border(
                                                1.dp,
                                                if (tech.isVip) viewModel.appPrimaryColor else Color.Gray,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(Color(0xFF0F0F0F), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = if (tech.isVip) "👑" else "★", fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = tech.name,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = tech.specialty,
                                                color = Color.LightGray,
                                                fontSize = 9.sp,
                                                maxLines = 1
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = viewModel.appPrimaryColor, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(text = tech.rating.toString(), color = Color.White, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Interactive Categories Click Row Shortcuts
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "■ فئات الخدمات السريعة تفاعلية:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            viewModel.categories.take(5).forEach { cat ->
                                val isSelected = selectedCategoryFilter == cat.nameAr
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            selectedCategoryFilter = if (isSelected) null else cat.nameAr
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                if (isSelected) viewModel.appPrimaryColor else Color(0xFF151515),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) viewModel.appPrimaryColor else Color(0xFF252525),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = cat.iconEmoji, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = cat.nameAr,
                                        color = if (isSelected) viewModel.appPrimaryColor else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Geographic city filter buttons list
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clear filter button
                        FilterChip(
                            selected = selectedCityFilter == null,
                            onClick = { selectedCityFilter = null },
                            label = { Text("الكل 🇾🇪", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = viewModel.appPrimaryColor,
                                selectedLabelColor = Color.Black
                            )
                        )
                        viewModel.cities.forEach { city ->
                            val isSelected = selectedCityFilter == city.nameAr
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCityFilter = if (isSelected) null else city.nameAr },
                                label = { Text(city.nameAr, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = viewModel.appPrimaryColor,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // 6.5 Bookings section if enabled
                if (viewModel.showBookingsSection) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10141D)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📅 حجز موعد ومتابعة الطلبات الجارية",
                                        color = viewModel.appPrimaryColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = { showNewBookingCreator = !showNewBookingCreator }) {
                                        Text(
                                            text = if (showNewBookingCreator) "إغلاق الحجز" else "طلب موعد جديد +",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (showNewBookingCreator) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TextField(
                                        value = bookingCustomerName,
                                        onValueChange = { bookingCustomerName = it },
                                        placeholder = { Text("اسمك الكامل", fontSize = 11.sp, color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextField(
                                        value = bookingCustomerPhone,
                                        onValueChange = { bookingCustomerPhone = it },
                                        placeholder = { Text("رقم الواتساب / الجوال", fontSize = 11.sp, color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Technician Picker dropdown sim
                                    Text(
                                        text = "اختر كادراً فنياً للموعد:",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        viewModel.providers.forEach { tech ->
                                            val isChosen = selectedTechForBooking?.id == tech.id
                                            FilterChip(
                                                selected = isChosen,
                                                onClick = { selectedTechForBooking = tech },
                                                label = { Text(tech.name, fontSize = 9.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = viewModel.appPrimaryColor, selectedLabelColor = Color.Black)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            if (bookingCustomerName.isEmpty() || bookingCustomerPhone.isEmpty() || selectedTechForBooking == null) {
                                                Toast.makeText(context, "الرجاء إدخال البيانات وتحديد الفني المناسب للحجز أولاً.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val newB = Booking(
                                                    customerName = bookingCustomerName,
                                                    customerPhone = bookingCustomerPhone,
                                                    techName = selectedTechForBooking!!.name,
                                                    date = bookingDateStr,
                                                    time = bookingTimeStr
                                                )
                                                viewModel.bookings = viewModel.bookings + newB
                                                bookingCustomerName = ""
                                                bookingCustomerPhone = ""
                                                showNewBookingCreator = false
                                                Toast.makeText(context, "تم إرسال طلب الحجز بنجاح للاستجابة السريعة! حالة الطلب: معلق", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تأكيد حجز موعد المباشر مع الفني", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Show active bookings count
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "الحجوزات النشطة بحسابك (${viewModel.bookings.size}):",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                                viewModel.bookings.forEach { book ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2330))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(book.customerName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("كادر: ${book.techName} • ${book.date}", color = Color.LightGray, fontSize = 10.sp)
                                            }
                                            val badgeColor = when (book.status) {
                                                "مقبول" -> Color(0xFF047857)
                                                "مكتمل" -> Color(0xFF1D4ED8)
                                                "مرفوض" -> Color(0xFFB91C1C)
                                                else -> Color(0xFFD97706)
                                            }
                                            Card(colors = CardDefaults.cardColors(containerColor = badgeColor)) {
                                                Text(book.status, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Dynamic Technician List
                item {
                    Text(
                        text = "قائمة الأخصائيين المسجلين بالدليل والنشطين حالياً (${activeAndApprovedProviders.size}):",
                        color = viewModel.appPrimaryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (activeAndApprovedProviders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "لا توجد نتائج مطابقة لبحثك في محافظة/مدينة الفرز المحددة أو النطاق الجغرافي المحدد.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(activeAndApprovedProviders) { tech ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (tech.isVip) viewModel.appPrimaryColor else Color(0xFF262626),
                                    RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar representation emoji
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(Color(0xFF0F0F0F), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = if (tech.isMale) "👨‍🔧" else "👩‍🔧", fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = tech.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (tech.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified badge",
                                                    tint = Color(0xFF2563EB),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            if (tech.isVip) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F)),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "VIP",
                                                        color = Color(0xFFFFD700),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${tech.specialty} • ${tech.city}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = viewModel.appPrimaryColor, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${tech.rating} (${tech.ratingsCount} تقييمات) • ✓ متاح للعمل اللحظي",
                                                color = Color(0xFF10B981),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = tech.biography,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                // Diagnostic inspection fee
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF262626), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("💰 قيمة المعاينة والتشخيص الأولي بالمنزل:", color = Color.LightGray, fontSize = 10.sp)
                                        Text("${tech.baseFee} ريال يمني", color = viewModel.appPrimaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                // Action Rows
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Call Direct
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tech.phone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "الرقم المسجل: ${tech.phone}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("اتصل 📞", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // WhatsApp Direct
                                    Button(
                                        onClick = {
                                            try {
                                                val waUri = Uri.parse("https://wa.me/967${tech.phone}")
                                                val intent = Intent(Intent.ACTION_VIEW, waUri)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "رقم الجوال: ${tech.phone}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("واتساب 💬", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Map Navigation
                                    Button(
                                        onClick = {
                                            if (tech.latitude != null && tech.longitude != null) {
                                                try {
                                                    val mapUri = Uri.parse("google.navigation:q=${tech.latitude},${tech.longitude}")
                                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${tech.latitude},${tech.longitude}")
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                                }
                                            } else {
                                                Toast.makeText(context, "لم يقرر الكادر بعد إحداثيات GPS الدقيقة لخرائط الرادار.", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("الاتجاهات 📍", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Rating custom modal trigger
                                    Button(
                                        onClick = {
                                            selectedProviderForRating = tech
                                            ratingStarsSelected = 5
                                            ratingCommentText = ""
                                            showRatingDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78350F)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("تقييم ⭐", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. User loyalty points promotion card (Controlled by Admin in settings)
                if (viewModel.showLoyaltySection) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, viewModel.appPrimaryColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(viewModel.loyaltyCardHeightPadding.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = try {
                                        viewModel.loyaltyCardTitle.format(viewModel.userLoyaltyPoints)
                                    } catch (e: Exception) {
                                        "🎁 رصيد نقاط الولاء: ${viewModel.userLoyaltyPoints} نقطة"
                                    },
                                    color = viewModel.appPrimaryColor,
                                    fontSize = viewModel.loyaltyCardProgressSize.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = viewModel.appFontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewModel.loyaltyCardText,
                                    color = Color.White,
                                    fontSize = (viewModel.loyaltyCardProgressSize - 2f).sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = viewModel.appFontFamily
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (viewModel.userLoyaltyPoints >= 100) {
                                            viewModel.userLoyaltyPoints -= 100
                                            Toast.makeText(
                                                context,
                                                "🎉 تهانينا! كود الخصم المعياري مفعل حالياً لوالتك بقيمة 5000 ريال يمني على طلباتك القادمة: DALILI-YEM-2026",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "عذراً، رصيدك هو ${viewModel.userLoyaltyPoints} نقطة، تحتاج 100 نقطة كحد أدنى للاستبدال.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("استبدل الـ 100 نقطة الآن", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = viewModel.appFontFamily)
                                }
                            }
                        }
                    }
                }

                // 9. Simple bottom footer info as in image layout specs
                if (viewModel.isFooterVisible) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "عن المنصة وبنود الدليل ℹ️",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontFamily = viewModel.appFontFamily,
                                    modifier = Modifier
                                        .clickable {
                                            showAboutAppDialog = true
                                        }
                                )

                                // Tapped version 7 times backdoor gate
                                Text(
                                    text = viewModel.footerText,
                                    color = viewModel.appPrimaryColor,
                                    fontSize = viewModel.footerFontSize.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = viewModel.appFontFamily,
                                    modifier = Modifier.clickable {
                                        secretFooterVersionTapCount++
                                        if (secretFooterVersionTapCount >= 7) {
                                            secretFooterVersionTapCount = 0
                                            Toast.makeText(context, "🎯 فتح بوابة لوحة التحكم الإدارية عبر الإصدار!", Toast.LENGTH_SHORT).show()
                                            onNavigateToAdmin()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 10. Instant Chat Floating Message Room Bubble
            if (viewModel.chatSettingsVisible && !viewModel.chatSettingsDeleted && viewModel.isChatInstantEnabled) {
                val chatAlignment = if (viewModel.chatSettingsAlignmentIsRight) Alignment.BottomEnd else Alignment.BottomStart
                val chatPadStart = if (viewModel.chatSettingsAlignmentIsRight) 0.dp else 16.dp
                val chatPadEnd = if (viewModel.chatSettingsAlignmentIsRight) 16.dp else 0.dp
                Box(
                    modifier = Modifier
                        .align(chatAlignment)
                        .padding(start = chatPadStart, end = chatPadEnd, bottom = (14f + viewModel.chatSettingsOffsetY).dp)
                        .offset(x = viewModel.chatSettingsOffsetX.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(viewModel.chatSettingsIconSize.dp)
                            .background(viewModel.chatSettingsIconColor, CircleShape)
                            .clickable {
                                // Default pick session
                                selectSessionForChat = viewModel.chatSessions.firstOrNull()
                                showChatConversationPanel = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.chatSettingsIconEmoji,
                            fontSize = (viewModel.chatSettingsIconSize / 2.2f).sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            // 11. AI Smart Assistant Floating Bubble (Gemini compatible offline fallback)
            if (viewModel.aiAssistantVisible && !viewModel.aiAssistantDeleted) {
                val aiAlignment = if (viewModel.aiAssistantAlignmentIsRight) Alignment.BottomEnd else Alignment.BottomStart
                val aiPadStart = if (viewModel.aiAssistantAlignmentIsRight) 0.dp else 16.dp
                val aiPadEnd = if (viewModel.aiAssistantAlignmentIsRight) 16.dp else 0.dp
                Box(
                    modifier = Modifier
                        .align(aiAlignment)
                        .padding(start = aiPadStart, end = aiPadEnd, bottom = (14f + viewModel.aiAssistantOffsetY).dp)
                        .offset(x = viewModel.aiAssistantOffsetX.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(viewModel.aiAssistantIconSize.dp)
                            .background(viewModel.aiAssistantIconColor, CircleShape)
                            .clickable {
                                showAiAssistantDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.aiAssistantIconEmoji,
                            fontSize = (viewModel.aiAssistantIconSize / 2.2f).sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG POPUP: RATE PROVIDER DESIGN ---
    if (showRatingDialog && selectedProviderForRating != null) {
        val tech = selectedProviderForRating!!
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐", fontSize = 24.sp, modifier = Modifier.padding(end = 4.dp))
                    Text("تقييم المهني الفني", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ما هو تقييم جودة معاينات وسلوك الفني: ${tech.name}؟",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            Text(
                                text = if (i <= ratingStarsSelected) "★" else "☆",
                                color = if (i <= ratingStarsSelected) viewModel.appPrimaryColor else Color.Gray,
                                fontSize = 38.sp,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { ratingStarsSelected = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = ratingCommentText,
                        onValueChange = { ratingCommentText = it },
                        placeholder = { Text("اكتب تعليقك بخصوص نزاهة الفني والتزامه بالمواعيد...", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF050505),
                            unfocusedContainerColor = Color(0xFF050505)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.userLoyaltyPoints += 15
                        Toast.makeText(
                            context,
                            "تم إرسال تقييمك بـ $ratingStarsSelected نجمة بنجاح! مضاف لرصيدك +15 نقطة ولاء 🎁",
                            Toast.LENGTH_LONG
                        ).show()
                        showRatingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor)
                ) {
                    Text("إرسال التقييم المعتمد", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // --- DIALOG POPUP: FULL CHAT MESSAGE FLOW PANEL ---
    if (showChatConversationPanel && selectSessionForChat != null) {
        ChatConversationDialog(
            visible = showChatConversationPanel,
            session = selectSessionForChat!!,
            viewModel = viewModel,
            onDismiss = { showChatConversationPanel = false }
        )
    }

    // --- DIALOG POPUP: ABOUT APPLICATION WITH DOWNLOAD SHARING IMAGE PRESETS ---
    if (showAboutAppDialog) {
        AlertDialog(
            onDismissRequest = { showAboutAppDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️ عن ${viewModel.appNameAr}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = viewModel.appFontFamily)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Image or Emoji display
                    if (viewModel.appInfoUploadedImagePath != null) {
                        coil.compose.AsyncImage(
                            model = viewModel.appInfoUploadedImagePath,
                            contentDescription = "App Logo Image",
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color(0xFF1B2330), RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color(0xFF1B2330), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(viewModel.appInfoImageEmoji, fontSize = 38.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = viewModel.aboutAppTitle,
                        color = viewModel.appPrimaryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontFamily = viewModel.appFontFamily
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.aboutAppDescription,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = viewModel.appFontFamily
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Stats section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2533))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("عدد المشتركين 👥", color = Color.Gray, fontSize = 9.sp, fontFamily = viewModel.appFontFamily)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(viewModel.aboutAppUsersStat, color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2533))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الفنيين المعتمدين 🏅", color = Color.Gray, fontSize = 9.sp, fontFamily = viewModel.appFontFamily)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(viewModel.aboutAppProvidersStat, color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Support Contacts
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2533))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📞 رقم الدعم: ${viewModel.supportPhone}", color = Color.White, fontSize = 10.sp, fontFamily = viewModel.appFontFamily)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("✉️ البريد الإلكتروني: ${viewModel.supportEmail}", color = Color.White, fontSize = 10.sp, fontFamily = viewModel.appFontFamily)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("💬 واتساب الدعم: ${viewModel.supportWhatsapp}", color = Color.White, fontSize = 10.sp, fontFamily = viewModel.appFontFamily)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("إصدار التطبيق: ${viewModel.aboutAppVersion}", color = Color.Gray, fontSize = 9.sp, fontFamily = viewModel.appFontFamily)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2533))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("رابط التنزيل المباشر المتاح للمشاركة:", color = Color.Gray, fontSize = 9.sp, fontFamily = viewModel.appFontFamily)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = viewModel.appDownloadLink,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, "بوابة خدمات اليمن للكوادر المهنية الفورية! حمل التطبيق الآن: ${viewModel.appDownloadLink}")
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة رابط التحميل"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("مشاركة الرابط الآن 🔗", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = viewModel.appFontFamily)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutAppDialog = false }) {
                    Text("إغلاق الشاشة", color = Color.White, fontFamily = viewModel.appFontFamily)
                }
            },
            containerColor = Color(0xFF111E2E)
        )
    }

    // --- DIALOG POPUP: AI SMART ASSISTANT CONVERSATION ROOM ---
    if (showAiAssistantDialog) {
        AiAssistantConversationDialog(
            visible = showAiAssistantDialog,
            viewModel = viewModel,
            onDismiss = { showAiAssistantDialog = false }
        )
    }
}

// ==========================================
// CUSTOM COMPOSABLES FOR FOCUS PROTECTION & CHAT DESIGN
// ==========================================

@Composable
fun ChatConversationDialog(
    visible: Boolean,
    session: ChatSession,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    if (!visible) return
    val coroutineScope = rememberCoroutineScope()
    var newChatMessageInput by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(viewModel.chatSettingsIconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(session.techName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = viewModel.appFontFamily)
                        Text("دردشة العميل: ${session.userName}", color = Color.Gray, fontSize = 10.sp, fontFamily = viewModel.appFontFamily)
                    }
                }
                if (session.isBlocked) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.Red)) {
                        Text("محظور", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontFamily = viewModel.appFontFamily)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        reverseLayout = false
                    ) {
                        val activeMsgs = viewModel.chatMessages
                        items(activeMsgs) { msg ->
                            val isMe = msg.senderRole == "user"
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) Color(0xFF005C4B) else Color(0xFF202C33)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = msg.senderName,
                                            color = viewModel.appPrimaryColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = viewModel.appFontFamily
                                        )
                                        Text(
                                            text = msg.messageText,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = viewModel.appFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (session.isBlocked) {
                    Text(
                        text = "عذراً، لقد تم تجميد صلاحيات إرسال الرسائل الخاصة بك من قبل الأدمن لضمان الخصوصية.",
                        color = Color.Red,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 8.dp),
                        fontFamily = viewModel.appFontFamily
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newChatMessageInput,
                            onValueChange = { newChatMessageInput = it },
                            placeholder = { Text("اكتب رسالتك كعميل هنا...", color = Color.LightGray, fontSize = 11.sp, fontFamily = viewModel.appFontFamily) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = viewModel.appFontFamily),
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1C2533),
                                unfocusedContainerColor = Color(0xFF1C2533),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (newChatMessageInput.isNotEmpty()) {
                                    val m = ChatMessage(
                                        chatId = "1",
                                        senderName = session.userName,
                                        senderRole = "user",
                                        messageText = newChatMessageInput
                                    )
                                    viewModel.chatMessages = viewModel.chatMessages + m
                                    session.lastMessage = newChatMessageInput
                                    newChatMessageInput = ""
                                    
                                    coroutineScope.launch {
                                        delay(1500)
                                        val responseMsg = ChatMessage(
                                            chatId = "1",
                                            senderName = session.techName,
                                            senderRole = "tech",
                                            messageText = "مرحباً بك يا غالي تواصلك محط اهتمامي، سأتواجد لقضاء العمل فوراً!"
                                        )
                                        viewModel.chatMessages = viewModel.chatMessages + responseMsg
                                        session.lastMessage = responseMsg.messageText
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "إرسال",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = viewModel.appFontFamily
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .graphicsLayer(scaleX = -1f) // Flipped for Arabic RTL
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color.White, fontFamily = viewModel.appFontFamily)
            }
        },
        containerColor = Color(0xFF111E2E)
    )
}

@Composable
fun AiAssistantConversationDialog(
    visible: Boolean,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    if (!visible) return
    val coroutineScope = rememberCoroutineScope()
    var newAiAssistantInputText by remember { mutableStateOf("") }
    var aiAssistantMessages by remember {
        mutableStateOf(listOf(
            ChatMessage(
                chatId = "ai",
                senderName = "مساعد دليلي الذكي 🤖",
                senderRole = "admin",
                messageText = "مرحباً بك! أنا مستشارك الذكي اليمني في منصة دليلي الفاخرة للخدمات. اسألني عن الفنيين، كلفة المعاينة والتشخيص، أو كيفية التسجيل والانضمام بقائمة المهن وسأجيبك فوراً بدقة واحترافية وبشكل كامل دون إنترنت!"
            )
        ))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(viewModel.aiAssistantIconEmoji, fontSize = 24.sp, modifier = Modifier.padding(end = 6.dp))
                Column {
                    Text(
                        text = "مساعد دليلي الذكي (Gemini)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = viewModel.appFontFamily
                    )
                    Text(
                        text = "متاح للعمل دون اتصال بالإنترنت ⚡",
                        color = viewModel.appPrimaryColor,
                        fontSize = 9.sp,
                        fontFamily = viewModel.appFontFamily
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(aiAssistantMessages) { msg ->
                            val isMe = msg.senderRole == "user"
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) Color(0xFF005C4B) else Color(0xFF2E2E3E)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isMe) viewModel.appPrimaryColor else Color(0xFF334155)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = if (isMe) "أنت (مستعلم)" else msg.senderName,
                                            color = viewModel.appPrimaryColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = viewModel.appFontFamily
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.messageText,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = viewModel.appFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newAiAssistantInputText,
                        onValueChange = { newAiAssistantInputText = it },
                        placeholder = {
                            Text(
                                text = "اسأل عن فني، مدينة، أسعار فحص...",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontFamily = viewModel.appFontFamily
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = viewModel.appFontFamily
                        ),
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1A2235),
                            unfocusedContainerColor = Color(0xFF1A2235),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Button(
                        onClick = {
                            if (newAiAssistantInputText.isNotEmpty()) {
                                val userQuery = newAiAssistantInputText
                                val userMsg = ChatMessage(
                                    chatId = "ai",
                                    senderName = "العميل",
                                    senderRole = "user",
                                    messageText = userQuery
                                )
                                aiAssistantMessages = aiAssistantMessages + userMsg
                                newAiAssistantInputText = ""

                                coroutineScope.launch {
                                    delay(1000)
                                    val queryNormalized = userQuery.lowercase()
                                    val replyText = when {
                                        queryNormalized.contains("سباك") || queryNormalized.contains("سباكة") -> {
                                            "المستشار الذكي: ممتاز! لدينا نخبة من السباكين كأعمال السباكة المنزلية الفاخرة مثل الأخصائي 'أبو ماجد البريحي' في إب، وقيمة معاينته 4000 ريال يمني ومتاح للاتصال اللحظي."
                                        }
                                        queryNormalized.contains("صنعاء") -> {
                                            "المستشار الذكي: نعم، في العاصمة صنعاء لدينا فنيون متميزون في عدة مجالات، فمثلاً 'المهندس وليد الصنعاني' هو عميد فنيي التبريد والتكييف، وهناك حدادون مهرة لمعالجة كافة الطلبات."
                                        }
                                        queryNormalized.contains("تكييف") || queryNormalized.contains("تبريد") || queryNormalized.contains("مكيف") -> {
                                            "المستشار الذكي: للتدفئة والتكييف والبرودة، لدينا 'المهندس وليد الصنعاني' في صنعاء، عميد التبريد المركزي وخبير الفريون والغسيل لمعاينة دقيقة تبلغ 5000 ريال يمني."
                                        }
                                        queryNormalized.contains("كهربا") || queryNormalized.contains("طاقة") -> {
                                            "المستشار الذكي: للكهرباء والطاقة الشمسية، الفني الموصى به بشدة هو 'أحمد جلال الحديدي' بمدينة الحديدة، ويمتاز بالتمديدات الآمنة وصيانة المولدات بكلفة معاينة 3500 ريال يمني فقط."
                                        }
                                        queryNormalized.contains("أسعار") || queryNormalized.contains("سعر") || queryNormalized.contains("كلفة") || queryNormalized.contains("فلوس") -> {
                                            "المستشار الذكي: يسير دليلنا على هيكل أسعار معياري عادل بالريال اليمني. تتراوح رسوم المعاينة في المنزل بين 3500 و 6000 ريال حسب التخصص، ويتم التفاهم على أجر الإصلاح الإجمالي بوضوح."
                                        }
                                        queryNormalized.contains("تسجيل") || queryNormalized.contains("انضمام") || queryNormalized.contains("كيف") -> {
                                            "المستشار الذكي: للانضمام كمهني معتمد، ادخل لوحة المالك واقرأ شروطه ثم أرسل صورتك والضمانات الفنية عبر نافذة التسجيل المتكاملة ليوافق عليها المشرف."
                                        }
                                        queryNormalized.contains("نقاط") || queryNormalized.contains("ولاء") -> {
                                            "المستشار الذكي: يمكنك كسب نقاط الولاء بتقييم الفنيين بعد إنجاز العمل (+15 نقطة)، وعند بلوغ 100 نقطة تستبدلها بخصم بقيمة 5000 ريال يمني!"
                                        }
                                        else -> {
                                            "المستشار الذكي: تساؤلك محط اهتمامي البالغ في دليلي الفني لخدمات اليمن. يتوفر لدينا سباكون، كهربائيون، نجارون، أخصائيو تبريد وحدادون بكافة المحافظات مجاناً وتواصل مباشر باتصال أو واتساب بنقرة واحدة!"
                                        }
                                    }
                                    val aiMsg = ChatMessage(
                                        chatId = "ai",
                                        senderName = "مساعد دليلي الذكي 🤖",
                                        senderRole = "admin",
                                        messageText = replyText
                                    )
                                    aiAssistantMessages = aiAssistantMessages + aiMsg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "إرسال",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = viewModel.appFontFamily
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send AI",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer(scaleX = -1f) // Flipped for Arabic RTL
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق المحادثة", color = Color.White, fontFamily = viewModel.appFontFamily)
            }
        },
        containerColor = Color(0xFF111E2E)
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AutoMarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    speedMs: Int = 45
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        modifier = modifier.basicMarquee(
            iterations = Int.MAX_VALUE,
            delayMillis = 1000
        )
    )
}
