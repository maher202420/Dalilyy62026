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
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.models.ChatSession
import com.Serviseyem.models.ServiceProvider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf("settings") } // "settings", "chats", "providers", "logs"

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
                    "chats" -> 1
                    "providers" -> 2
                    "logs" -> 3
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
                    "chats" -> AdminChatsSubSection(viewModel, onOpenChatSession = { selectedAdminChatSession = it })
                    "providers" -> AdminProvidersSubSection(viewModel)
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
fun AdminProvidersSubSection(viewModel: AppViewModel) {
    val context = LocalContext.current

    // Local controller tools for adding technicians
    var showAddTechForm by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
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
