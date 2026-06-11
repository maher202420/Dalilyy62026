package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.geometry.Offset
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.models.ChatSession
import com.Serviseyem.models.ServiceProvider
import com.Serviseyem.models.Category
import com.Serviseyem.models.ColorPalette
import com.Serviseyem.models.Booking
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf("settings") } // "settings", "stats", "chats", "providers", "bookings", "notifications", "logs"

    // Dialog state for active admin reply
    var selectedAdminChatSession by remember { mutableStateOf<ChatSession?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "لوحة التحكم المشتركة المعيارية 👑",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "الموقع الحالي للمشرف: ${viewModel.activeAdminUsername ?: "إداري عام"}",
                            fontSize = 11.sp,
                            color = viewModel.appPrimaryColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to main")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal Admin tabs
            ScrollableTabRow(
                selectedTabIndex = when (activeSubTab) {
                    "settings" -> 0
                    "stats" -> 1
                    "categories" -> 2
                    "chats" -> 3
                    "providers" -> 4
                    "bookings" -> 5
                    "notifications" -> 6
                    "logs" -> 7
                    else -> 0
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = viewModel.appPrimaryColor
            ) {
                Tab(
                    selected = activeSubTab == "settings",
                    onClick = { activeSubTab = "settings" },
                    text = { Text("⚙️ إعدادات وتخصيص", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_settings")
                )
                Tab(
                    selected = activeSubTab == "stats",
                    onClick = { activeSubTab = "stats" },
                    text = { Text("📊 الإحصائيات والمخططات", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_stats")
                )
                Tab(
                    selected = activeSubTab == "categories",
                    onClick = { activeSubTab = "categories" },
                    text = { Text("🏷️ الأقسام والتصنيفات", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_categories")
                )
                Tab(
                    selected = activeSubTab == "chats",
                    onClick = { activeSubTab = "chats" },
                    text = { Text("💬 الدردشات والتحكم", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_chats")
                )
                Tab(
                    selected = activeSubTab == "providers",
                    onClick = { activeSubTab = "providers" },
                    text = { Text("👨‍🔧 مقدمي الخدمات", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_providers")
                )
                Tab(
                    selected = activeSubTab == "bookings",
                    onClick = { activeSubTab = "bookings" },
                    text = { Text("📅 الحجوزات والفروع", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_bookings")
                )
                Tab(
                    selected = activeSubTab == "notifications",
                    onClick = { activeSubTab = "notifications" },
                    text = { Text("🔔 الإشعارات والتحكم", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_notifications")
                )
                Tab(
                    selected = activeSubTab == "logs",
                    onClick = { activeSubTab = "logs" },
                    text = { Text("📜 سجل العمليات والمصادقة", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_tab_logs")
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (activeSubTab) {
                    "settings" -> AdminSettingsSubSection(viewModel)
                    "stats" -> AdminStatsSubSection(viewModel)
                    "categories" -> AdminCategoriesSubSection(viewModel)
                    "chats" -> AdminChatsSubSection(viewModel, onOpenChatSession = { selectedAdminChatSession = it })
                    "providers" -> AdminProvidersSubSection(viewModel)
                    "bookings" -> AdminBookingsSubSection(viewModel)
                    "notifications" -> AdminNotificationsSubSection(viewModel)
                    "logs" -> AdminLogsSubSection(viewModel)
                }
            }
        }

        // Expanded Super-Admin reply Dialog modal
        selectedAdminChatSession?.let { session ->
            var adminReplyText by remember { mutableStateOf("") }
            val messages = viewModel.chatMessages.filter { it.chatId == session.id }
                .sortedBy { it.timestamp }

            val adminChatListState = rememberLazyListState()
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    adminChatListState.animateScrollToItem(messages.size - 1)
                }
            }

            AlertDialog(
                onDismissRequest = { selectedAdminChatSession = null },
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
                            Text("بوابة المراقبة الفائقة: دردشة رقم ${session.id.take(6)}", fontSize = 14.sp)
                            Text("الأطراف: ${session.userName} <=> ${session.techName}", fontSize = 11.sp, color = viewModel.appPrimaryColor)
                        }
                        IconButton(onClick = { selectedAdminChatSession = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close View")
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Action buttons: Mute / Block Chat
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.toggleBlockChatSession(session)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (session.isBlocked) Color.Gray else Color.Red
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (session.isBlocked) "إلغاء قفل المحادثة" else "إيقاف وحظر الجلسة", fontSize = 11.sp)
                            }

                            val correspondingTech = viewModel.providers.find { it.id == session.techId }
                            if (correspondingTech != null) {
                                Button(
                                    onClick = {
                                        viewModel.toggleProviderChatMute(correspondingTech)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (correspondingTech.isChatMuted) viewModel.appPrimaryColor else Color(0xFF1E1E22)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (correspondingTech.isChatMuted) "إلغاء كتم الفني للتواصل" else "كتم مراسلات هذا الفني", fontSize = 11.sp)
                                }
                            }
                        }

                        // Messages scroll
                        LazyColumn(
                            state = adminChatListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { msg ->
                                val alignment = when (msg.senderRole) {
                                    "user" -> Alignment.Start
                                    "tech" -> Alignment.End
                                    else -> Alignment.CenterHorizontally
                                }
                                val cardBg = when (msg.senderRole) {
                                    "user" -> Color(0xFF2E2E32)
                                    "tech" -> Color(0xFF0C2B1C)
                                    else -> Color(0xFF3B1E1E) // super admin
                                }

                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardBg),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "${msg.senderName} [${msg.senderRole.uppercase()}]",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = viewModel.appPrimaryColor
                                            )
                                            Text(msg.messageText, fontSize = 12.sp, color = Color.White)
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Super Admin direct Reply Injector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = adminReplyText,
                                onValueChange = { adminReplyText = it },
                                placeholder = { Text("اكتب رد كـ مشرف أعلى...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_inject_chat_field")
                            )
                            Button(
                                onClick = {
                                    if (adminReplyText.trim().isNotEmpty()) {
                                        viewModel.sendInstantChatMessage(
                                            chatId = session.id,
                                            senderName = "إشراف الدليل الأعلى",
                                            senderRole = "admin",
                                            text = adminReplyText.trim()
                                        )
                                        adminReplyText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                                enabled = adminReplyText.trim().isNotEmpty()
                            ) {
                                Text("الرد كمشرف")
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun AdminSettingsSubSection(viewModel: AppViewModel) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // App Customization details
        item {
            Text("🎨 الهوية المرئية وتخصيص الألوان والسمات", fontWeight = FontWeight.Bold, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.appNameAr,
                        onValueChange = { viewModel.appNameAr = it },
                        label = { Text("اسم التطبيق بالعربية (AppName Ar)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.appNameEn,
                        onValueChange = { viewModel.appNameEn = it },
                        label = { Text("اسم التطبيق بالإنجليزية (AppName En)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.appLogoEmoji,
                        onValueChange = { viewModel.appLogoEmoji = it },
                        label = { Text("أيقونة شعار الترويسة (Emoji App Logo)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.appPrimaryColorStr,
                        onValueChange = { viewModel.appPrimaryColorStr = it },
                        label = { Text("كود الـ Hex للون الرئيسي (مثال: #1B5E20)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.appSecondaryColorStr,
                        onValueChange = { viewModel.appSecondaryColorStr = it },
                        label = { Text("كود الـ Hex للون الثانوي (مثال: #FFC700)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.appBackgroundColorStr,
                        onValueChange = { viewModel.appBackgroundColorStr = it },
                        label = { Text("كود الـ Hex لخلفية الشاشات (مثال: #FFFFFF)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.appTextColorStr,
                        onValueChange = { viewModel.appTextColorStr = it },
                        label = { Text("كود الـ Hex للون النصوص (مثال: #000000)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("حدد الخط العام للتطبيق والمناسب لنسب العرض:", fontSize = 11.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Default", "Monospace", "SansSerif", "Serif").forEach { font ->
                            val isSelected = viewModel.appSelectedFontName == font
                            ElevatedButton(
                                onClick = { viewModel.appSelectedFontName = font },
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = if (isSelected) viewModel.appPrimaryColor else Color(0xFF2C2C30),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                )
                            ) {
                                Text(font, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Top Bar icon sorting order controls (CRITICAL FEATURE REQUEST)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🔄 ترتيب وتعديل تموضع الأيقونات بالشريط العلوي",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "اضغط على الأسهم للتحكم في الأولوية المعمارية وتغيير تموضع الأزرار فورياً وبلحظتها لجميع الهواتف المتصلة بالإنترنت باليمن والشرق الأوسط:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    viewModel.topBarIconsOrderList.forEachIndexed { index, icon ->
                        val actionName = when (icon) {
                            "🏠" -> "الشاشة الرئيسية ومحو الفلتر (Home Dashboard)"
                            "🔐" -> "بوابة المشرفين والولوج الآمن (Staff Gates)"
                            "👤" -> "انضمام كـ فني وبناء الفروع (Provider Signup)"
                            "🌐" -> "مبدل اللغات الفوري (Arabic <-> English)"
                            "🔄" -> "تطهير الكاش ومزامن البيانات (Direct Stream Sync)"
                            else -> "عنصر مجهول"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF222226), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = icon,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = actionName,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row {
                                // Up Button
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val newList = viewModel.topBarIconsOrderList.toMutableList()
                                            val temp = newList[index]
                                            newList[index] = newList[index - 1]
                                            newList[index - 1] = temp
                                            viewModel.topBarIconsOrderList = newList
                                            Toast.makeText(context, "تم رفع أولوية الأيقونة بنجاح ومزامنتها", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = index > 0
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = if (index > 0) viewModel.appPrimaryColor else Color.Gray, modifier = Modifier.size(16.dp))
                                }

                                // Down Button
                                IconButton(
                                    onClick = {
                                        if (index < viewModel.topBarIconsOrderList.size - 1) {
                                            val newList = viewModel.topBarIconsOrderList.toMutableList()
                                            val temp = newList[index]
                                            newList[index] = newList[index + 1]
                                            newList[index + 1] = temp
                                            viewModel.topBarIconsOrderList = newList
                                            Toast.makeText(context, "تم خفض أولوية الأيقونة بنجاح ومزامنتها", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = index < viewModel.topBarIconsOrderList.size - 1
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = if (index < viewModel.topBarIconsOrderList.size - 1) viewModel.appPrimaryColor else Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Global System Warning Message Config
        item {
            Text("🪧 بنرات الإشعارات وتذييل الشاشات المعياري", fontWeight = FontWeight.Bold, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("مستند تذييل التطبيق مرئي للكل", fontSize = 12.sp, color = Color.White)
                        Switch(checked = viewModel.isFooterVisible, onCheckedChange = { viewModel.isFooterVisible = it })
                    }
                    OutlinedTextField(
                        value = viewModel.footerText,
                        onValueChange = { viewModel.footerText = it },
                        label = { Text("نص التذييل التعريفي الدقيق") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.footerFontSize.toString(),
                        onValueChange = {
                            val fVal = it.toFloatOrNull() ?: viewModel.footerFontSize
                            viewModel.footerFontSize = fVal
                        },
                        label = { Text("حجم خط ورسم التذييل (SP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Loyalty and greeting messages text customization
        item {
            Text("✍️ رسائل ترحيب وقسم الولاء وتخفيض الخدمة", fontWeight = FontWeight.Bold, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.appGreetingMessageAr,
                        onValueChange = { viewModel.appGreetingMessageAr = it },
                        label = { Text("رسالة ترحيب التيسير العربية لشريط البيت") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.appGreetingMessageEn,
                        onValueChange = { viewModel.appGreetingMessageEn = it },
                        label = { Text("الغبطة الترحيبية المترجمة للإنجليزية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Divider(color = Color.DarkGray)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تمكين قسم نقاط الولاء للأعضاء باليمن", fontSize = 12.sp, color = Color.White)
                        Switch(checked = viewModel.showLoyaltySection, onCheckedChange = { viewModel.showLoyaltySection = it })
                    }
                    OutlinedTextField(
                        value = viewModel.loyaltyCardTitle,
                        onValueChange = { viewModel.loyaltyCardTitle = it },
                        label = { Text("رأس بطاقة الولاء (استخدم %d لتمثيل الرصيد المالي)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.loyaltyCardText,
                        onValueChange = { viewModel.loyaltyCardText = it },
                        label = { Text("تفاصيل بطاقة الاسترداد المالي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 🔍 التحكم في شريط البحث العلوي للتطبيق
        item {
            Text("🔍 التحكم وتعديل أو إخفاء/حذف شريط البحث العلوي", fontWeight = FontWeight.Bold, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("شريط البحث مرئي وظاهر (Visible)", fontSize = 12.sp, color = Color.White)
                        Switch(checked = viewModel.isSearchBarVisible, onCheckedChange = { viewModel.isSearchBarVisible = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حذف شريط البحث بالكامل من الشاشة (Deleted)", fontSize = 12.sp, color = Color.Red)
                        Switch(checked = viewModel.isSearchBarDeleted, onCheckedChange = { viewModel.isSearchBarDeleted = it })
                    }

                    OutlinedTextField(
                        value = viewModel.searchBarPlaceholderAr,
                        onValueChange = { viewModel.searchBarPlaceholderAr = it },
                        label = { Text("نص تلميح البحث بالعربية (Placeholder Ar)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewModel.searchBarPlaceholderEn,
                        onValueChange = { viewModel.searchBarPlaceholderEn = it },
                        label = { Text("نص تلميح البحث بالإنجليزية (Placeholder En)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 🎨 إدارة لوحات الألوان والسمات المخصصة
        item {
            Text("🎨 تخصيص وسجل سمات الألوان المخصصة بالكامل", fontWeight = FontWeight.Bold, color = Color.White)
        }

        items(viewModel.colorPalettes) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111113)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = p.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored circles representing palette
                        listOf(p.primaryColor, p.secondaryColor, p.backgroundColor, p.textColor).forEach { hex ->
                            val colorValue = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Color.Gray
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(colorValue)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "رئيسي: ${p.primaryColor} • خلفية: ${p.backgroundColor}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.activateColorPalette(p) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = try { Color(android.graphics.Color.parseColor(p.primaryColor)) } catch (e: Exception) { viewModel.appPrimaryColor }, 
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تطبيق وتنشيط", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { viewModel.deleteColorPalette(p.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Palette", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            var newPaletteName by remember { mutableStateOf("") }
            var newPalettePrimary by remember { mutableStateOf("#4CAF50") }
            var newPaletteSecondary by remember { mutableStateOf("#FFC107") }
            var newPaletteBackground by remember { mutableStateOf("#0E1B10") }
            var newPaletteTextColor by remember { mutableStateOf("#FFFFFF") }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("➕ إضافة لوحة ألوان / سمة مخصصة جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = viewModel.appPrimaryColor)
                    
                    OutlinedTextField(
                        value = newPaletteName,
                        onValueChange = { newPaletteName = it },
                        label = { Text("اسم لوحة الألوان (مثال: الأخضر الفيروزي الممتاز)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPalettePrimary,
                            onValueChange = { newPalettePrimary = it },
                            label = { Text("لون رئيسي Hex") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newPaletteSecondary,
                            onValueChange = { newPaletteSecondary = it },
                            label = { Text("لون ثانوي Hex") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPaletteBackground,
                            onValueChange = { newPaletteBackground = it },
                            label = { Text("لون خلفية Hex") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newPaletteTextColor,
                            onValueChange = { newPaletteTextColor = it },
                            label = { Text("لون نصوص Hex") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            if (newPaletteName.isNotEmpty() && newPalettePrimary.startsWith("#") && newPaletteBackground.startsWith("#")) {
                                try {
                                    android.graphics.Color.parseColor(newPalettePrimary)
                                    android.graphics.Color.parseColor(newPaletteSecondary)
                                    android.graphics.Color.parseColor(newPaletteBackground)
                                    android.graphics.Color.parseColor(newPaletteTextColor)

                                    viewModel.addColorPalette(
                                        name = newPaletteName,
                                        primary = newPalettePrimary,
                                        secondary = newPaletteSecondary,
                                        background = newPaletteBackground,
                                        text = newPaletteTextColor
                                    )
                                    newPaletteName = ""
                                    Toast.makeText(context, "تمت إضافة لوحة الألوان بنجاح!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "خطأ: كود الـ hex غير صالح للتمثيل اللوني", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "الرجاء تعبئة الاسم والالتزام بالصيغة الرمزية #Hex!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("اعتماد ونشر لوحة الألوان الجديدة")
                    }
                }
            }
        }

        // 🧹 مسح وإعادة بناء قواعد البيانات بالكامل
        item {
            var showConfirmWipeDialog by remember { mutableStateOf(false) }
            var isWiping by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(12.dp))
            Text("⚠️ تطهير قواعد البيانات والبدء من جديد (Reset App Data)", fontWeight = FontWeight.Bold, color = Color.Red)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E1E)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.Red.copy(alpha = 0.5f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تقوم هذه الميزة بحذف كافة البيانات، الحسابات، طلبات التسجيل، المحادثات، وبنرات الإعلانات بالكامل من قواعد البيانات وإعادة بنائها وفق الإعدادات والبيانات الافتراضية النقية والمعتمدة للدليل لحظيّاً 🇾🇪",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    
                    Button(
                        onClick = { showConfirmWipeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isWiping
                    ) {
                        if (isWiping) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري طمس البيانات وإعادة البناء...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مسح كامل البيانات وإعادة بناء الدليل العظيم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (showConfirmWipeDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmWipeDialog = false },
                    title = { Text("⚠️ تحذير أمني شديد!", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Text("هل أنت متأكد من رغبتك في مسح كافة البيانات، البنرات الأقسام والمحادثات النشطة نهائياً من الـ Firestore وإعادة توليدها بشكل جديد بالكامل؟ لا يمكن التراجع عن هذا الإجراء الإداري مطلقاً.", fontSize = 12.sp, color = Color.White)
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmWipeDialog = false
                                isWiping = true
                                viewModel.wipeAndRebuildFullDatabase { success ->
                                    isWiping = false
                                    if (success) {
                                        Toast.makeText(context, "تم مسح كافة البيانات وجرف القواعد وإعادة بناء الدليل بالقيم الافتراضية بنجاح! ⚡", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "اكتمل البناء مع وجود تقلبات طفيفة بالمزامنة الكلية", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("نعم، امسح كل البيانات وابنِ من جديد")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showConfirmWipeDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("تراجع")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminChatsSubSection(viewModel: AppViewModel, onOpenChatSession: (ChatSession) -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Chat general toggles and parameters configuration
        item {
            Text("💬 التمكين الجغرافي للمحادثات وضبط الهوية", fontWeight = FontWeight.Bold, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("الخدمة الفورية نشطة للكل بالدليل", fontSize = 12.sp, color = Color.White)
                            Text("تعطيل الخدمة يظهر إعلان التوجيه للتواصل المباشر", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(checked = viewModel.isChatInstantEnabled, onCheckedChange = { viewModel.isChatInstantEnabled = it })
                    }

                    OutlinedTextField(
                        value = viewModel.chatDisabledMessage,
                        onValueChange = { viewModel.chatDisabledMessage = it },
                        label = { Text("رسالة التعطيل المخصصة (حالة الحجب الكلي)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = Color.DarkGray)

                    Text("🔮 ضبط وبصمة الأيقونة والإنذار (الدردشة العائمة)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("كتم وإخفاء أيقونة الدردشة مؤقتاً", fontSize = 11.sp, color = Color.LightGray)
                        Switch(checked = viewModel.isChatIconMutedHidden, onCheckedChange = { viewModel.isChatIconMutedHidden = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حذف الأيقونة نهائياً عن الشاشات", fontSize = 11.sp, color = Color.LightGray)
                        Switch(checked = viewModel.isChatIconPermDeleted, onCheckedChange = { viewModel.isChatIconPermDeleted = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("حجم الأيقونة العائمة (DP): ${viewModel.chatSettingsIconSize.toInt()}", fontSize = 11.sp, color = Color.LightGray)
                            Text("صغير (نصف الحجم الإنشائي 30dp) <=> كبير (60dp)", fontSize = 9.sp, color = Color.Gray)
                        }
                        Slider(
                            value = viewModel.chatSettingsIconSize,
                            onValueChange = { viewModel.chatSettingsIconSize = it },
                            valueRange = 30f..80f,
                            modifier = Modifier.width(140.dp)
                        )
                    }

                    OutlinedTextField(
                        value = viewModel.chatSettingsIconColorStr,
                        onValueChange = { viewModel.chatSettingsIconColorStr = it },
                        label = { Text("كود Hex للون الأيقونة العائمة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Chats lists for Super-Admin Reply & monitoring
        item {
            Text("🗄️ مرافئ وجلسات الدردشة المفتوحة حالياً (${viewModel.chatSessions.size})", fontWeight = FontWeight.Bold, color = Color.White)
            Text("اضغط على أي جلسة دردشة لفتح مستند المحادثة المعياري، المصادقة، والرد كمشرف لحل النزاعات فورياً:", fontSize = 11.sp, color = Color.Gray)
        }

        if (viewModel.chatSessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا توجد أي جلسات حوارية في قاعدة البيانات حالياً لمراقبتها.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(viewModel.chatSessions.sortedByDescending { it.lastUpdated }) { sess ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (sess.isBlocked) Color(0xFF261D1D) else Color(0xFF161619)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChatSession(sess) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "المستخدم: ${sess.userName} 🤝 الفني: ${sess.techName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            if (sess.isBlocked) {
                                Card(colors = CardDefaults.cardColors(containerColor = Color.Red)) {
                                    Text("محظورة", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                        Text(
                            text = "آخر رسالة: ${sess.lastMessage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealtimePieChartDashboard(providers: List<ServiceProvider>) {
    val distribution = providers.groupBy { it.specialty }
        .mapValues { it.value.size }
        .filter { it.value > 0 }

    val total = distribution.values.sum().toFloat()

    val colorsList = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444), // Red
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF14B8A6)  // Teal
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E2E33))),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 رسم بياني دائري (Live Pie Chart) - توزيع المهنيين في اليمن",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تحديث فوري وتفاعلي متزامن مع قبول طلبات الشبكة الفرعية.",
                fontSize = 10.sp,
                color = Color.LightGray
            )

            if (total == 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا تتوفر إحصائيات كافية لعدم وجود مزودي خدمات نشطين حالياً.", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Draw Pie Chart on Canvas
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .size(100.dp)
                            .testTag("analytics_pie_chart")
                    ) {
                        var startAngle = 0f
                        distribution.values.forEachIndexed { index, count ->
                            val sweepAngle = (count / total) * 360f
                            val color = colorsList[index % colorsList.size]
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Legend values
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        distribution.entries.forEachIndexed { index, entry ->
                            val color = colorsList[index % colorsList.size]
                            val percentage = (entry.value / total) * 100
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = "${entry.key}: ${entry.value} (${String.format(java.util.Locale.US, "%.1f", percentage)}%)",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProvidersSubSection(viewModel: AppViewModel) {
    val context = LocalContext.current

    // Local controller tools for adding technicians
    var showAddTechForm by remember { mutableStateOf(false) }
    var editProviderDialog by remember { mutableStateOf<ServiceProvider?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Real-time Pie Chart Dashboard Card
        item {
            RealtimePieChartDashboard(viewModel.providers)
        }

        // Pending approval queue
        item {
            Text("📥 طلبات الانضمام المعلقة للتسجيل والترخيص (${viewModel.registrationRequests.size})", fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (viewModel.registrationRequests.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("لا توجد طلبات انضمام فني جديدة معلقة متبقية للمراجعة الإشرافية.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(viewModel.registrationRequests) { request ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2126)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "اسم الفني: ${request.name}", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "الهاتف: ${request.phone} • المجال: ${request.specialty} • المدينة: ${request.city}", fontSize = 11.sp, color = Color.LightGray)
                        Text(text = "الجنس: ${request.gender} • المصدر: ${request.photoSource} • نوع الصورة: ${request.photoType}", fontSize = 11.sp, color = Color(0xFF818CF8))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.approveRequest(request.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("موافقة وقبول", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.rejectRequest(request.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رفض وحذف", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Active providers list with custom individual chat silence switches
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("👨‍🔧 مقدمي الخدمات المسجلين والدعم لليمن (${viewModel.providers.size})", fontWeight = FontWeight.Bold, color = Color.White)
                Button(
                    onClick = { showAddTechForm = !showAddTechForm },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                ) {
                    Text(if (showAddTechForm) "إغلاق النموذج" else "➕ إضافة فني يدوياً")
                }
            }
        }

        // Entry Form if expanded
        if (showAddTechForm) {
            item {
                var newName by remember { mutableStateOf("") }
                var newPhone by remember { mutableStateOf("") }
                var newSpecialty by remember { mutableStateOf("سباكة") }
                var newCity by remember { mutableStateOf("صنعاء") }
                var isVip by remember { mutableStateOf(false) }
                var biography by remember { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("تسجيل وحقن فني جديد في الشبكة يدوياً", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = viewModel.appPrimaryColor)
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("الاسم الكامل") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("رقم الهاتف") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = biography, onValueChange = { biography = it }, label = { Text("نبذة صغيرة وخبرات للتوضيح للزبون") }, modifier = Modifier.fillMaxWidth())

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("وضع مميز (VIP Golden Card)", fontSize = 11.sp, color = Color.White)
                            Switch(checked = isVip, onCheckedChange = { isVip = it })
                        }

                        Button(
                            onClick = {
                                if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                                    val resultText = viewModel.addManualTechnician(newName, newPhone, newSpecialty, newCity, isVip, biography, "كاميرا المعاينة التلقائية")
                                    Toast.makeText(context, resultText, Toast.LENGTH_LONG).show()
                                    newName = ""
                                    newPhone = ""
                                    biography = ""
                                    showAddTechForm = false
                                } else {
                                    Toast.makeText(context, "الرجاء تعبئة الاسم والهاتف", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("اعتماد ونشر في دليل الخدمات")
                        }
                    }
                }
            }
        }

        // Active listing items
        items(viewModel.providers) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(p.name, fontWeight = FontWeight.Bold, color = Color.White)
                                if (p.isVip) {
                                    Text(" (VIP ⭐)", fontSize = 9.sp, color = viewModel.appPrimaryColor, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("تخصص: ${p.specialty} • فرع: ${p.city} • جوال: ${p.phone}", fontSize = 11.sp, color = Color.Gray)
                        }

                        IconButton(onClick = { viewModel.deleteActiveProvider(p.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Provider", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }

                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    // Individual Chat Silencer Toggle (Mute Chat specific service providers)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("حظر وكتم محادثات هذا الفني", fontSize = 11.sp, color = Color.LightGray)
                            Text("تعطيل تلقي الفني لأي رسائل واردة من الزبائن", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = p.isChatMuted,
                            onCheckedChange = { viewModel.toggleProviderChatMute(p) }
                        )
                    }

                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    // Gallery Availability Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("تنشيط معرض أعمال المهني (Gallery)", fontSize = 11.sp, color = Color.LightGray)
                            Text("السماح للمهني برفع صوره واستعراضها بالكامل", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = p.galleryEnabled,
                            onCheckedChange = { viewModel.updateProviderGallerySettings(p, it, p.maxGalleryImages) }
                        )
                    }

                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    // Portfolio max gallery images allowed setup
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("السقف الأقصى لعدد الصور المرفوعة", fontSize = 11.sp, color = Color.LightGray)
                            Text("الحد الأقصى المسموح برعايته: ${p.maxGalleryImages} صور", fontSize = 9.sp, color = Color.Gray)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (p.maxGalleryImages > 1) viewModel.updateProviderGallerySettings(p, p.galleryEnabled, p.maxGalleryImages - 1) },
                                modifier = Modifier.size(28.dp),
                                enabled = p.maxGalleryImages > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = if (p.maxGalleryImages > 1) Color.Red else Color.DarkGray)
                            }

                            Text(
                                text = "${p.maxGalleryImages}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            IconButton(
                                onClick = { if (p.maxGalleryImages < 20) viewModel.updateProviderGallerySettings(p, p.galleryEnabled, p.maxGalleryImages + 1) },
                                modifier = Modifier.size(28.dp),
                                enabled = p.maxGalleryImages < 20
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = if (p.maxGalleryImages < 20) Color.Green else Color.DarkGray)
                            }
                        }
                    }

                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Button(
                        onClick = { editProviderDialog = p },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appSecondaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("🖊️ تعديل بيانات الحساب والخبرات والتفاصيل والموقع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Geographical sectors and cities creator
        item {
            Text("⛰️ تخصيص فروع الجغرافيا اليمنية والخرائط", fontWeight = FontWeight.Bold, color = Color.White)
        }

        item {
            var inputArCity by remember { mutableStateOf("") }
            var inputEnCity by remember { mutableStateOf("") }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إضافة مدينة أو نطاق تشغيل جغرافي جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = inputArCity, onValueChange = { inputArCity = it }, label = { Text("الاسم بالعربية") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = inputEnCity, onValueChange = { inputEnCity = it }, label = { Text("الاسم بالإنجليزية") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (inputArCity.isNotEmpty() && inputEnCity.isNotEmpty()) {
                                viewModel.addCity(inputArCity, inputEnCity)
                                inputArCity = ""
                                inputEnCity = ""
                                Toast.makeText(context, "تم حفظ النشر للموقع الجغرافي!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("اعتماد ونشر النطاق")
                    }
                }
            }
        }
    }

    // Dialog: Edit Provider Full Profile Details (Experience, Years, Contact email, verified status, coordinates, etc.)
    editProviderDialog?.let { provider ->
        var editName by remember { mutableStateOf(provider.name) }
        var editPhone by remember { mutableStateOf(provider.phone) }
        var editSpecialty by remember { mutableStateOf(provider.specialty) }
        var editCity by remember { mutableStateOf(provider.city) }
        var editBio by remember { mutableStateOf(provider.biography) }
        var editBaseFee by remember { mutableStateOf(provider.baseFee.toString()) }
        var editVerified by remember { mutableStateOf(provider.isVerified) }
        var editVip by remember { mutableStateOf(provider.isVip) }
        var editExpYears by remember { mutableStateOf(provider.experienceYears.toString()) }
        var editEmail by remember { mutableStateOf(provider.contactEmail) }
        var editLat by remember { mutableStateOf(provider.latitude.toString()) }
        var editLng by remember { mutableStateOf(provider.longitude.toString()) }

        AlertDialog(
            onDismissRequest = { editProviderDialog = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            title = { Text("🛠️ تعديل حساب المهني: ${provider.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                ) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("الاسم الكامل للمهني") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("رقم الهاتف المهني المعول") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editSpecialty, onValueChange = { editSpecialty = it }, label = { Text("التخصص الفني الفرعي") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editCity, onValueChange = { editCity = it }, label = { Text("المدينة / التغطية") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("السيرة والخبرة المختصره") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editBaseFee, onValueChange = { editBaseFee = it }, label = { Text("أجرة الكشف والاستكشاف (ر.ي)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editExpYears, onValueChange = { editExpYears = it }, label = { Text("عدد سنوات الخبرة الفعلية") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("البريد الإلكتروني للأعمال (Contact Email)") }, modifier = Modifier.fillMaxWidth())
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(value = editLat, onValueChange = { editLat = it }, label = { Text("خط العرض Lat") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editLng, onValueChange = { editLng = it }, label = { Text("خط الطول Lng") }, modifier = Modifier.weight(1f))
                    }

                    // Verified Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("شارة موثق معتمد 🛡️", fontSize = 12.sp, color = Color.White)
                        }
                        Switch(checked = editVerified, onCheckedChange = { editVerified = it })
                    }

                    // VIP Golden state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("عضوية مميزة (VIP ⭐)", fontSize = 12.sp, color = Color.White)
                        Switch(checked = editVip, onCheckedChange = { editVip = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = editBaseFee.toIntOrNull() ?: provider.baseFee
                        val exp = editExpYears.toIntOrNull() ?: provider.experienceYears
                        val latVal = editLat.toDoubleOrNull() ?: provider.latitude
                        val lngVal = editLng.toDoubleOrNull() ?: provider.longitude

                        viewModel.updateProviderFullDetails(
                            id = provider.id,
                            name = editName,
                            phone = editPhone,
                            specialty = editSpecialty,
                            city = editCity,
                            biography = editBio,
                            baseFee = fee,
                            isVerified = editVerified,
                            isVip = editVip,
                            experienceYears = exp,
                            contactEmail = editEmail,
                            latitude = latVal,
                            longitude = lngVal,
                            galleryUrls = provider.galleryUrls
                        )
                        Toast.makeText(context, "تم حفظ وتحديث كود حساب المهني بنجاح", Toast.LENGTH_SHORT).show()
                        editProviderDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                ) {
                    Text("حفظ التحديثات الكاملة")
                }
            },
            dismissButton = {
                Button(
                    onClick = { editProviderDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("إلغاء الإجراء")
                }
            }
        )
    }
}

@Composable
fun AdminLogsSubSection(viewModel: AppViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📜 سجل عمليات التدقيق اللحظي الفوري لخدمات اليمن", fontWeight = FontWeight.Bold, color = Color.White)
                Button(onClick = { viewModel.triggerDynamicCleanCycle() }) {
                    Text("تصفير آمن وقسري ومسح")
                }
            }
        }

        items(viewModel.adminActivityLogs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111113)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = log,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = Color(0xFF4ADE80), // Terminal green
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun AdminCategoriesSubSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf<Category?>(null) }

    // Form fields (used for both Add and Edit)
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var iconEmoji by remember { mutableStateOf("🔧") }
    var catType by remember { mutableStateOf("professional") } // "professional", "service", "commercial"
    var parentId by remember { mutableStateOf<String?>(null) }
    var imageUrl by remember { mutableStateOf("") }

    // Quick fill inputs when selecting category for editing
    LaunchedEffect(isEditing) {
        if (isEditing != null) {
            nameAr = isEditing!!.nameAr
            nameEn = isEditing!!.nameEn
            description = isEditing!!.description
            iconEmoji = isEditing!!.iconEmoji
            catType = isEditing!!.type
            parentId = isEditing!!.parentId
            imageUrl = isEditing!!.imageUrl ?: ""
        } else {
            nameAr = ""
            nameEn = ""
            description = ""
            iconEmoji = "🔧"
            catType = "professional"
            parentId = null
            imageUrl = ""
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Create / Edit Category Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEditing != null) "✏️ تعديل بيانات القسم المحدد" else "📂 إضافة قسم مهني أو خدمي أو تجاري جديد",
                        fontWeight = FontWeight.Bold,
                        color = viewModel.appPrimaryColor,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text("الاسم بالعربية (مثال: محاماة واستشارات)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("الاسم بالإنجليزية (مثال: Legal & Advocacy)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("وصف مختصر للقسم يظهر للزبائن") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = iconEmoji,
                            onValueChange = { iconEmoji = it },
                            label = { Text("رمز تعبيري (Emoji)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("رابط صورة/أيقونة ويب (اختياري)") },
                            modifier = Modifier.weight(2f)
                        )
                    }

                    // Category Type select
                    Text("طبيعة ونوع القسم الدليلي:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Triple("professional", "🛠️ مهني / صيانة", Color(0xFF34D399)),
                            Triple("service", "🩺 خدمي وعام", Color(0xFF60A5FA)),
                            Triple("commercial", "🛒 تجاري ومبيعات", Color(0xFFFBBF24))
                        ).forEach { (type, label, color) ->
                            val isSelected = catType == type
                            Button(
                                onClick = { catType = type },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) color else Color(0xFF222226),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Parent category selector (Sub-category support)
                    Text("القسم الأب (اختر في حال كونه قسماً فرعياً أو ثانوياً):", fontSize = 11.sp, color = Color.Gray)
                    // List main categories ONLY (parentId == null)
                    val parentOptions = viewModel.categories.filter { it.parentId == null && it.id != isEditing?.id }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Chips or select button
                        AssistChip(
                            onClick = { parentId = null },
                            label = { Text(if (parentId == null) "✓ رئيسي أساسي (Root)" else "رئيسي أساسي (Root)") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (parentId == null) viewModel.appPrimaryColor else Color(0xFF222226),
                                labelColor = if (parentId == null) Color.Black else Color.White
                            )
                        )

                        // Scrollable parent selections
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(parentOptions) { parentCat ->
                                val isSelected = parentId == parentCat.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { parentId = parentCat.id },
                                    label = { Text(parentCat.nameAr) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (nameAr.isNotEmpty() && nameEn.isNotEmpty()) {
                                    if (isEditing == null) {
                                        viewModel.addCategory(
                                            nameAr = nameAr,
                                            nameEn = nameEn,
                                            desc = description,
                                            iconEmoji = iconEmoji,
                                            type = catType,
                                            parentId = parentId,
                                            imageUrl = imageUrl.ifBlank { null }
                                        )
                                        Toast.makeText(context, "تمت إضافة وتعميم القسم بنجاح وفوراً!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateCategory(
                                            id = isEditing!!.id,
                                            nameAr = nameAr,
                                            nameEn = nameEn,
                                            desc = description,
                                            iconEmoji = iconEmoji,
                                            type = catType,
                                            parentId = parentId,
                                            imageUrl = imageUrl.ifBlank { null }
                                        )
                                        isEditing = null
                                        Toast.makeText(context, "تم حفظ تعديلات القسم المعين بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                    // Reset fields
                                    nameAr = ""
                                    nameEn = ""
                                    description = ""
                                    iconEmoji = "🔧"
                                    catType = "professional"
                                    parentId = null
                                    imageUrl = ""
                                } else {
                                    Toast.makeText(context, "الرجاء كلاً من الاسم العربي والإنجليزي!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = viewModel.appPrimaryColor, 
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(2f)
                        ) {
                            Text(if (isEditing != null) "تعميم التعديلات الحالية" else "نشـر وإعتماد القسم بالدليل العربي")
                        }

                        if (isEditing != null) {
                            Button(
                                onClick = { isEditing = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء التعديل")
                            }
                        }
                    }
                }
            }
        }

        // Section label
        item {
            Text("📂 الأقسام السائدة والمدعومة حالياً بالشبكة (${viewModel.categories.size})", fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Active List layout
        items(viewModel.categories) { cat ->
            val parentName = if (cat.parentId != null) {
                viewModel.categories.find { it.id == cat.parentId }?.nameAr ?: "قسم مجهول"
            } else null

            val typeText = when (cat.type) {
                "professional" -> "مهني صيانة 🛠️"
                "service" -> "خدمي طبي وعام 🩺"
                "commercial" -> "تجاري مبيعات 🛒"
                else -> "أخرى"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111113)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(cat.iconEmoji, fontSize = 20.sp)
                            Text(cat.nameAr, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("(${cat.nameEn})", fontSize = 11.sp, color = Color.Gray)
                        }
                        if (cat.description.isNotEmpty()) {
                            Text(cat.description, fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.padding(top = 4.dp))
                        }
                        Text("طبيعة التقسيم: $typeText", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        
                        if (parentName != null) {
                            Text("تفريع مسار القسم الأب: $parentName", fontSize = 10.sp, color = viewModel.appSecondaryColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                        } else {
                            Text("المستوى: قسم رئيسي أساسي (Root)", fontSize = 10.sp, color = Color(0xFF4ADE80), modifier = Modifier.padding(top = 2.dp))
                        }

                        if (!cat.imageUrl.isNullOrBlank()) {
                            Text("صورة القسم: ${cat.imageUrl}", fontSize = 9.sp, color = Color.Cyan)
                        }
                    }

                    Row {
                        IconButton(onClick = { isEditing = cat }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Category", tint = Color.Green, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBookingsSubSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    var isEditingBooking by remember { mutableStateOf<Booking?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Bookings configurations
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚙️ خيارات الحجز والتوجه والتوزيع", fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("تشغيل نظام الحجوزات العام", fontSize = 11.sp, color = Color.White)
                            Text("تنشيط أو تعطيل جدول الزيارات وحجوزات الفنيين", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = viewModel.showBookingsSection,
                            onCheckedChange = { viewModel.showBookingsSection = it }
                        )
                    }

                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Text("حدد الجهة التي تصل إليها الحجوزات الصادرة:", fontSize = 11.sp, color = Color.White)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("both" to "الإدارة والمهني", "tech" to "المهني فقط", "admin" to "الإدارة فقط").forEach { (term, label) ->
                            val isSelected = viewModel.bookingRoutingDestination == term
                            Button(
                                onClick = { viewModel.bookingRoutingDestination = term },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) viewModel.appPrimaryColor else Color(0xFF232328),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(label, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("📋 كشف طلبات الحجز الفني الحالية (${viewModel.bookings.size})", fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (viewModel.bookings.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("لا توجد حجوزات فنية معلنة أو قائمة حالياً في الشبكة.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(viewModel.bookings) { b ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("الزبون: ${b.customerName}", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("رقم الاتصال: ${b.customerPhone}", fontSize = 11.sp, color = Color.LightGray)
                            }
                            // Status badge
                            val badgeColor = when (b.status) {
                                "مقبول" -> Color.Green
                                "قيد الانتظار" -> Color.Yellow
                                "مكتمل" -> Color(0xFF38BDF8)
                                else -> Color.Red
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(b.status, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text("المهني المستهدف: ${b.techName}", fontSize = 11.sp, color = viewModel.appSecondaryColor, fontWeight = FontWeight.Medium)
                        Text("موعد الزيارة المقترح: ${b.date} في تمام الساعة: ${b.time}", fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.padding(top = 2.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateBookingStatus(b.id, "مقبول") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 1.dp)
                            ) {
                                Text("موافقة وقبول", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.updateBookingStatus(b.id, "مكتمل") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color.Black),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 1.dp)
                            ) {
                                Text("اعتماد كمكتمل", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { isEditingBooking = b },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 1.dp)
                            ) {
                                Text("تعديل تفصيلي", fontSize = 10.sp)
                            }
                            IconButton(
                                onClick = { viewModel.deleteBooking(b.id) },
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                                    .size(34.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit Booking Details
    isEditingBooking?.let { b ->
        var editDate by remember { mutableStateOf(b.date) }
        var editTime by remember { mutableStateOf(b.time) }
        var editStatus by remember { mutableStateOf(b.status) }

        AlertDialog(
            onDismissRequest = { isEditingBooking = null },
            title = { Text("✍️ تعديل حجز الزبون: ${b.customerName}", fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        label = { Text("التاريخ المقترح (مثال: 2026-06-21)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTime,
                        onValueChange = { editTime = it },
                        label = { Text("العنوان والوقت (مثال: 4:00 عصراً)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("حالة طلب الحجز الحالي:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("قيد الانتظار", "مقبول", "مرفوض", "مكتمل").forEach { st ->
                            val isSel = editStatus == st
                            ElevatedButton(
                                onClick = { editStatus = st },
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = if (isSel) viewModel.appPrimaryColor else Color(0xFF2C2C30),
                                    contentColor = if (isSel) Color.Black else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(st, fontSize = 9.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateBookingFull(b.id, editDate, editTime, editStatus)
                        Toast.makeText(context, "تم حفظ بيانات الحجز وتنبيه العميل بنجاح", Toast.LENGTH_SHORT).show()
                        isEditingBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                ) {
                    Text("التحديث الفوري")
                }
            },
            dismissButton = {
                Button(
                    onClick = { isEditingBooking = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AdminNotificationsSubSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }
    var notifRecipient by remember { mutableStateOf("") }
    var notifType by remember { mutableStateOf("general") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Section send notifications
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📢 بث وإرسال إشعار لحظي جديد", fontWeight = FontWeight.Bold, color = viewModel.appPrimaryColor, fontSize = 12.sp)
                    
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("عنوان التنبيه بالإشعار (مثال: تحديث أمني هام 🛡️)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notifBody,
                        onValueChange = { notifBody = it },
                        label = { Text("تفاصيل ومحتوى رسالة الإشعار التوجيهي") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = notifRecipient,
                        onValueChange = { notifRecipient = it },
                        label = { Text("اسم المستلم المعين (اتركه فارغاً للبث للجميع)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تصنيف الإشعار:", fontSize = 10.sp, color = Color.Gray)
                        listOf("general" to "عام للكل", "booking_status" to "حجوزات", "registration_status" to "تسجيل").forEach { (typ, name) ->
                            val isSel = notifType == typ
                            ElevatedButton(
                                onClick = { notifType = typ },
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = if (isSel) viewModel.appSecondaryColor else Color(0xFF2C2C30),
                                    contentColor = if (isSel) Color.Black else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(name, fontSize = 9.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (notifTitle.isNotEmpty() && notifBody.isNotEmpty()) {
                                viewModel.sendAppNotification(
                                    title = notifTitle,
                                    body = notifBody,
                                    recipientName = notifRecipient.trim().ifEmpty { null },
                                    type = notifType
                                )
                                Toast.makeText(context, "تم إرسال ونشر الإشعار في كافة الهواتف بنجاح", Toast.LENGTH_SHORT).show()
                                notifTitle = ""
                                notifBody = ""
                                notifRecipient = ""
                            } else {
                                Toast.makeText(context, "الرجاء كلاً من تعبئة العنوان والرسالة", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text("🚀 بث الإشعار الفوري الآن")
                    }
                }
            }
        }

        item {
            Text("🔔 كشف الإشعارات المرسلة حالياً للزبائن والمهنيين (${viewModel.notifications.size})", fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (viewModel.notifications.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("لا توجد إشعارات سابقة مرسلة في الأرشيف.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(viewModel.notifications) { n ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141417)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(n.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Text("المستهدف: ${n.recipientName ?: "جميع كادر وعملاء التطبيق (عام)"}", fontSize = 10.sp, color = viewModel.appSecondaryColor, fontWeight = FontWeight.Medium)
                            }
                            IconButton(
                                onClick = { viewModel.deleteNotification(n.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(n.body, fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                        
                        val dateString = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a", n.timestamp).toString()
                        Text("تاريخ البث: $dateString • النوع: ${n.type}", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatsSubSection(viewModel: AppViewModel) {
    val activeProviders = viewModel.providers.filter { it.status == "مقبول" }
    val totalBookings = viewModel.bookings
    
    // Calculate Specialty popularity from bookings
    val providerToSpecialty = viewModel.providers.associate { it.name to it.specialty }
    val specialtyBookings = totalBookings.groupBy { booking ->
        providerToSpecialty[booking.techName] ?: "خدمات عامة"
    }.mapValues { it.value.size }

    // Booking status counts
    val pendingBookings = totalBookings.count { it.status == "معلق" }
    val acceptedBookings = totalBookings.count { it.status == "مقبول" }
    val completedBookings = totalBookings.count { it.status == "مكتمل" }
    val rejectedBookings = totalBookings.count { it.status == "مرفوض" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Statistical summary cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Providers Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👨‍🔧 الكادر النشط", fontSize = 10.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activeProviders.size} مهني",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text("إجمالي: ${viewModel.providers.size}", fontSize = 8.sp, color = Color.Gray)
                    }
                }

                // Total Bookings Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📅 إجمالي الطلبات", fontSize = 10.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${totalBookings.size} طلب حجز",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = viewModel.appPrimaryColor
                        )
                        Text("المكتملة: $completedBookings", fontSize = 8.sp, color = Color.Gray)
                    }
                }

                // Pending Approvals Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3F1B1B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📥 طلبات معلقة", fontSize = 10.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${viewModel.registrationRequests.size} طلب",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171)
                        )
                        Text("بانتظار الموافقة", fontSize = 8.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Booking status distribution (Vertical Bar Chart)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151518)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 توزيع وحالات الحجوزات النشطة (Booking Status)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مقارنة حية لنسب قبول وانجاز الحجوزات من قبل مقدمي الخدمات والعملاء.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val maxCount = maxOf(pendingBookings, acceptedBookings, completedBookings, rejectedBookings, 1).toFloat()
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            val barWidth = w / 7f
                            val spacing = w / 14f
                            
                            val statuses = listOf(
                                Triple("معلق", pendingBookings, Color(0xFFF59E0B)),     // Golden / Amber
                                Triple("مقبول", acceptedBookings, Color(0xFF3B82F6)),    // Blue
                                Triple("مكتمل", completedBookings, Color(0xFF10B981)),   // Green
                                Triple("مرفوض", rejectedBookings, Color(0xFFEF4444))     // Red
                            )

                            statuses.forEachIndexed { i, (label, count, color) ->
                                val barHeight = (count / maxCount) * (h - 40f)
                                val xPos = spacing + i * (barWidth + spacing)
                                val yPos = h - 20f - barHeight
                                
                                // Draw Shadow/Background track
                                drawRoundRect(
                                    color = Color.DarkGray.copy(alpha = 0.15f),
                                    topLeft = Offset(xPos, 0f),
                                    size = androidx.compose.ui.geometry.Size(barWidth, h - 20f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                )

                                // Draw colored bar with linear gradient
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(color, color.copy(alpha = 0.6f))
                                    ),
                                    topLeft = Offset(xPos, yPos),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                )
                            }
                        }

                        // Labels & count overlays
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val statuses = listOf(
                                Triple("معلق", pendingBookings, Color(0xFFF59E0B)),
                                Triple("مقبول", acceptedBookings, Color(0xFF3B82F6)),
                                Triple("مكتمل", completedBookings, Color(0xFF10B981)),
                                Triple("مرفوض", rejectedBookings, Color(0xFFEF4444))
                            )
                            statuses.forEach { (label, count, color) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(60.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(130.dp)) // push down labels
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Specialty Popularity / Most Demanded Services (Horizontal Progress bars)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151518)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔥 أكثر التخصصات والخدمات طلباً (Most Demanded Fields)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ترتيب المهن المطلوبة جغرافياً بحجم حجوزات الفنيين النشطة.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val activeCategories = viewModel.categories.filter { it.isPublished }
                    val specialtyRankMap = activeCategories.associate { cat ->
                        cat.nameAr to (specialtyBookings[cat.nameAr] ?: 0)
                    }.toList().sortedByDescending { it.second }

                    val maximumValue = maxOf(specialtyRankMap.maxOfOrNull { it.second } ?: 1, 1).toFloat()

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        specialtyRankMap.take(5).forEachIndexed { index, (specialtyName, bookingCount) ->
                            val progressRatio = bookingCount.toFloat() / maximumValue
                            
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${index + 1}. $specialtyName",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$bookingCount حجز",
                                        fontSize = 10.sp,
                                        color = viewModel.appPrimaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progressRatio)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(viewModel.appPrimaryColor, viewModel.appSecondaryColor)
                                                ),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // New Professional Growth Trend line chart (Cumulative Curve)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151518)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📈 نمو المهنيين ومزودي الخدمات الجدد (Professional Onboarding)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تقرير بياني حركي يوضح مسار انضمام الكادر الفني خلال الـ 6 أشهر الأخيرة.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Draw growth curve points. Total active providers over pseudo periods
                    val mockPeriods = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو")
                    // Distribute total provider counts cumulatively for the line chart
                    val totalP = activeProviders.size
                    val dataPoints = if (totalP > 5) {
                        listOf(
                            totalP * 0.2f,
                            totalP * 0.35f,
                            totalP * 0.5f,
                            totalP * 0.7f,
                            totalP * 0.85f,
                            totalP.toFloat()
                        )
                    } else {
                        listOf(3f, 5f, 8f, 12f, 15f, 21f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            val path = Path()
                            val fillPath = Path()
                            
                            val maxValP = maxOf(dataPoints.maxOrNull() ?: 10f, 10f)
                            val colStep = w / 5f
                            
                            dataPoints.forEachIndexed { idx, value ->
                                val x = idx * colStep
                                val y = h - 20f - ((value / maxValP) * (h - 40f))
                                
                                if (idx == 0) {
                                    path.moveTo(x, y)
                                    fillPath.moveTo(x, h - 20f)
                                    fillPath.lineTo(x, y)
                                } else {
                                    val prevX = (idx - 1) * colStep
                                    val prevY = h - 20f - ((dataPoints[idx - 1] / maxValP) * (h - 40f))
                                    // Smooth bezier curve control points
                                    val cx1 = prevX + colStep / 2f
                                    val cy1 = prevY
                                    val cx2 = prevX + colStep / 2f
                                    val cy2 = y
                                    path.cubicTo(cx1, cy1, cx2, cy2, x, y)
                                    fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                                }
                                
                                if (idx == dataPoints.size - 1) {
                                    fillPath.lineTo(x, h - 20f)
                                    fillPath.close()
                                }
                            }

                            // Draw gradient area under curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.35f), Color.Transparent)
                                )
                            )

                            // Draw glowing line
                            drawPath(
                                path = path,
                                color = Color(0xFF38BDF8),
                                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                            )

                            // Plot data circles
                            dataPoints.forEachIndexed { idx, value ->
                                val x = idx * colStep
                                val y = h - 20f - ((value / maxValP) * (h - 40f))
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = Color(0xFF38BDF8),
                                    radius = 8f,
                                    center = Offset(x, y),
                                    style = Stroke(width = 2f)
                                )
                            }
                        }

                        // Label overlay texts
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            mockPeriods.forEachIndexed { i, month ->
                                Text(
                                    text = month,
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 0.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

