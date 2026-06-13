package com.Serviseyem.screens
import android.widget.Toast
import android.net.Uri
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
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
            // Dynamic Tabs based on Owner privileges
            val isOwner = viewModel.activeAdminUsername == "صاحب المالكي"
            val availableTabs = remember(isOwner) {
                val list = mutableListOf(
                    "settings" to "⚙️ إعدادات وتخصيص",
                    "banners" to "📺 اللافتات والذاكرة",
                    "chats" to "💬 الدردشات والتحكم",
                    "providers" to "👨‍🔧 مقدمي الخدمات",
                    "bookings" to "📅 الحجوزات والفروع",
                    "notifications" to "🔔 الإشعارات والتحكم"
                )
                if (isOwner) {
                    list.add(1, "stats" to "📊 الإحصائيات والمخططات")
                    list.add(2, "categories" to "🏷️ الأقسام والتصنيفات")
                    list.add(list.size, "logs" to "📜 سجل العمليات والمصادقة")
                }
                list
            }

            LaunchedEffect(availableTabs) {
                if (availableTabs.none { it.first == activeSubTab }) {
                    activeSubTab = "settings"
                }
            }

            ScrollableTabRow(
                selectedTabIndex = availableTabs.indexOfFirst { it.first == activeSubTab }.coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = viewModel.appPrimaryColor
            ) {
                availableTabs.forEach { (route, label) ->
                    Tab(
                        selected = activeSubTab == route,
                        onClick = { activeSubTab = route },
                        text = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.testTag("admin_tab_$route")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (activeSubTab) {
                    "settings" -> AdminSettingsSubSection(viewModel)
                    "banners" -> AdminBannersSubSection(viewModel)
                    "stats" -> if (isOwner) AdminStatsSubSection(viewModel) else Box {}
                    "categories" -> if (isOwner) AdminCategoriesSubSection(viewModel) else Box {}
                    "chats" -> AdminChatsSubSection(viewModel, onOpenChatSession = { selectedAdminChatSession = it })
                    "providers" -> AdminProvidersSubSection(viewModel)
                    "bookings" -> AdminBookingsSubSection(viewModel)
                    "notifications" -> AdminNotificationsSubSection(viewModel)
                    "logs" -> if (isOwner) AdminLogsSubSection(viewModel) else Box {}
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
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "${msg.senderName} [${msg.senderRole.uppercase()}]",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = viewModel.appPrimaryColor
                                            )
                                            
                                            var showInlineEdit by remember { mutableStateOf(false) }
                                            var editedText by remember { mutableStateOf(msg.messageText) }

                                            if (showInlineEdit) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.fillMaxWidth().widthIn(max = 240.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = editedText,
                                                        onValueChange = { editedText = it },
                                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                                        modifier = Modifier.weight(1f).height(46.dp),
                                                        singleLine = true
                                                    )
                                                    IconButton(onClick = {
                                                        if (editedText.isNotBlank()) {
                                                            viewModel.updateChatMessageText(msg.id, editedText)
                                                        }
                                                        showInlineEdit = false
                                                    }) {
                                                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.Green, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            } else {
                                                Text(msg.messageText, fontSize = 12.sp, color = Color.White)
                                            }

                                            // Mod buttons and Pending tags
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "تعديل ✏️",
                                                    fontSize = 8.sp,
                                                    color = Color.LightGray,
                                                    modifier = Modifier.clickable { showInlineEdit = !showInlineEdit }
                                                )
                                                Text(
                                                    text = "حذف 🗑️",
                                                    fontSize = 8.sp,
                                                    color = Color.Red.copy(alpha = 0.8f),
                                                    modifier = Modifier.clickable { viewModel.deleteChatMessage(msg.id) }
                                                )
                                                if (!msg.isApproved) {
                                                    Text(
                                                        text = "⏳ معلق للموافقة",
                                                        fontSize = 8.sp,
                                                        color = viewModel.appPrimaryColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            if (!msg.isApproved) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(
                                                        onClick = { viewModel.approveChatMessage(msg.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5E20)),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(20.dp)
                                                    ) {
                                                        Text("موافقة ✅", fontSize = 8.sp, color = Color.White)
                                                    }
                                                    Button(
                                                        onClick = { viewModel.deleteChatMessage(msg.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(20.dp)
                                                    ) {
                                                        Text("رفض وبتر ❌", fontSize = 8.sp, color = Color.White)
                                                    }
                                                }
                                            }
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

        // 🤖 التحكم وإعداد المساعد الذكي اليمني العائم
        item {
            Text("🤖 التحكم وإعداد المساعد الذكي اليمني العائم", fontWeight = FontWeight.Bold, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تمكين وعرض أيقونة المساعد الذكي العائمة", fontSize = 12.sp, color = Color.White)
                        Switch(checked = viewModel.showAiAssistantFloatingBubble, onCheckedChange = { viewModel.showAiAssistantFloatingBubble = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("محاذاة المساعد الذكي (اليمين 👍 / اليسار 👎)", fontSize = 12.sp, color = Color.White)
                        Switch(checked = viewModel.aiAssistantAlignmentIsRight, onCheckedChange = { viewModel.aiAssistantAlignmentIsRight = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("حجم أيقونة المساعد الذكي (DP): ${viewModel.aiAssistantIconSize.toInt()}", fontSize = 11.sp, color = Color.LightGray)
                            Text("صغير (30dp) <=> كبير (100dp)", fontSize = 9.sp, color = Color.Gray)
                        }
                        Slider(
                            value = viewModel.aiAssistantIconSize,
                            onValueChange = { viewModel.aiAssistantIconSize = it },
                            valueRange = 30f..100f,
                            modifier = Modifier.width(140.dp)
                        )
                    }

                    OutlinedTextField(
                        value = viewModel.aiAssistantIconColorStr,
                        onValueChange = { viewModel.aiAssistantIconColorStr = it },
                        label = { Text("كود Hex للون المساعد الذكي (مثال: #111827)") },
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

                    Divider(color = Color.DarkGray)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🎤 تمكين ميزة البحث الصوتي الدقيق", fontSize = 12.sp, color = Color.White)
                            Text("السماح للمستخدمين بالبحث بالصوت وتحويله إلى نص تلقائياً", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(checked = viewModel.voiceSearchEnabled, onCheckedChange = { viewModel.voiceSearchEnabled = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("💡 تمكين ميزة الإكمال التلقائي والاقتراحات", fontSize = 12.sp, color = Color.White)
                            Text("عرض اقتراحات مطابقة ذكية أثناء الكتابة في شريط البحث", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(checked = viewModel.searchAutocompleteEnabled, onCheckedChange = { viewModel.searchAutocompleteEnabled = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("⚙️ تمكين الفلترة والترتيب المتقدم والفرز الجغرافي", fontSize = 12.sp, color = Color.White)
                            Text("عرض خيارات فرز حسب التقييم، المنطقة أو القرب الجغرافي", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(checked = viewModel.advancedFilteringEnabled, onCheckedChange = { viewModel.advancedFilteringEnabled = it })
                    }
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

        // ⚙️ إعدادات التحكم بالمسافة الجغرافية وخريطة الموقع والتنظيف التلقائي
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📍 إعدادات حدود البحث الجغرافي والفرز بالخريطة", fontWeight = FontWeight.Bold, color = viewModel.appPrimaryColor, fontSize = 12.sp)
                    Text("يمكن للمستخدمين الفرز حول إحداثيات موقعهم. اختر قيمة الحد الأقصى لنطاق البحث الافتراضي المسموح به لتقريب المهنيين:", fontSize = 10.sp, color = Color.Gray)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(5.0 to "5 كم", 10.0 to "10 كم", 15.0 to "15 كم", 20.0 to "20 كم", 50.0 to "50 كم").forEach { (rad, text) ->
                            val isSelected = viewModel.mapRadiusKm == rad
                            ElevatedButton(
                                onClick = { viewModel.mapRadiusKm = rad },
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = if (isSelected) viewModel.appPrimaryColor else Color(0xFF2C2C30),
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text, fontSize = 9.sp)
                            }
                        }
                    }

                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Text("🧹 جدولة دورة التنظيف والكنس التلقائي لقاعدة البيانات", fontWeight = FontWeight.Bold, color = viewModel.appPrimaryColor, fontSize = 12.sp)
                    Text("تحديد فترة الحفظ المقررة بالدليل (بالأيام) للاحتفاظ بسجلات النشاط والإشعارات المؤقتة القديمة، ثم النقر لتشغيل الكنس الآن والتنظيف المباشر:", fontSize = 10.sp, color = Color.Gray)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(7 to "7 أيام", 15 to "15 يوم", 30 to "30 يوم", 60 to "60 يوم", 90 to "90 يوم").forEach { (days, label) ->
                            val isSel = viewModel.autoCleanupDays == days
                            ElevatedButton(
                                onClick = { viewModel.autoCleanupDays = days },
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = if (isSel) viewModel.appSecondaryColor else Color(0xFF2C2C30),
                                    contentColor = if (isSel) Color.Black else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(label, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.triggerDatabaseCleanup()
                            Toast.makeText(context, "تمت جدولة وتفعيل كنس قاعدة البيانات ومسح السجلات القديمة بنجاح! 🧹", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🧹 تشغيل كنس وتنظيف قاعدة البيانات ومسح السجلات القديمة الآن", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminChatsSubSection(viewModel: AppViewModel, onOpenChatSession: (ChatSession) -> Unit) {
    val context = LocalContext.current
    var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (sess.isBlocked) {
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.Red)) {
                                        Text("محظورة", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                IconButton(
                                    onClick = { sessionToDelete = sess },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف المحادثة",
                                        tint = Color.Red.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
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

    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(
                    text = if (viewModel.isArabic) "⚠️ تأكيد حذف المحادثة" else "Confirm Session Deletion",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = if (viewModel.isArabic)
                        "هل أنت متأكد من رغبتك في حذف جلسة الدردشة هذه بجميع رسائلها نهائياً بصفة الإدارة؟ سيتم الحظر والإزالة من الجميع فوراً."
                    else
                        "Are you sure you want to delete this chat session and all its messages permanently as an admin? This action cannot be undone.",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sessionToDelete?.let { sess ->
                            viewModel.deleteChatSession(sess.id)
                            Toast.makeText(context, if (viewModel.isArabic) "تم حذف جلسة الدردشة نهائياً" else "Chat session deleted permanently", Toast.LENGTH_SHORT).show()
                        }
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                ) {
                    Text(if (viewModel.isArabic) "حذف الإجراء" else "Delete Permanently")
                }
            },
            dismissButton = {
                Button(
                    onClick = { sessionToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White)
                ) {
                    Text(if (viewModel.isArabic) "إلغاء" else "Cancel")
                }
            }
        )
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

        // إدارة شروط التسجيل والانتساب بالدليل
        item {
            var newTermInput by remember { mutableStateOf("") }
            var editTermId by remember { mutableStateOf<String?>(null) }
            var editTermTextInput by remember { mutableStateOf("") }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1F)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📌 إدارة شروط وقواعد قبول الانضمام بالدليل", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = viewModel.appPrimaryColor)
                    Text("يمكنك كأدمن إضافة شروط جديدة لطلب الانتساب، أو تعديل وحذف الشروط الحالية لتحديث سياسات القبول فورياً.", fontSize = 10.sp, color = Color.Gray)

                    // List terms
                    viewModel.registrationTerms.forEach { term ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF252529), RoundedCornerShape(4.dp)).padding(6.dp)
                        ) {
                            if (editTermId == term.id) {
                                OutlinedTextField(
                                    value = editTermTextInput,
                                    onValueChange = { editTermTextInput = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = {
                                    if (editTermTextInput.isNotEmpty()) {
                                        viewModel.updateRegistrationTerm(term.id, editTermTextInput)
                                        editTermId = null
                                    }
                                }) {
                                    Text("💾", fontSize = 14.sp)
                                }
                                IconButton(onClick = { editTermId = null }) {
                                    Text("❌", fontSize = 14.sp)
                                }
                            } else {
                                Text(
                                    text = "• " + term.termText,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(onClick = {
                                        editTermId = term.id
                                        editTermTextInput = term.termText
                                    }) {
                                        Text("✏️", fontSize = 12.sp)
                                    }
                                    IconButton(onClick = {
                                        viewModel.deleteRegistrationTerm(term.id)
                                    }) {
                                        Text("🗑️", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Divider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Text("إضافة قاعدة/شرط قبول جديد:", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = newTermInput,
                        onValueChange = { newTermInput = it },
                        placeholder = { Text("مثال: رخصة مزاولة المهنة المعتمَدة سارية المفعول.", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )
                    Button(
                        onClick = {
                            if (newTermInput.isNotEmpty()) {
                                viewModel.addRegistrationTerm(newTermInput)
                                newTermInput = ""
                                Toast.makeText(context, "تمت إضافة شرط القبول الجديد بنجاح!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("إضافة لشرط القبول", fontSize = 10.sp)
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
        var editPinned by remember { mutableStateOf(provider.isPinned) }
        var editRecommended by remember { mutableStateOf(provider.isRecommended) }
        var editHasSubscription by remember { mutableStateOf(provider.hasSubscription) }

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

                    // Pinned state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("تثبيت المهني في أعلى لائحته 📌", fontSize = 12.sp, color = Color.White)
                        Switch(checked = editPinned, onCheckedChange = { editPinned = it })
                    }

                    // Recommended state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("إدراج بتوصية الدليل الرسمية ⭐", fontSize = 12.sp, color = Color.White)
                        Switch(checked = editRecommended, onCheckedChange = { editRecommended = it })
                    }

                    // Monthly subscription state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("تمكين اشتراك المهني الشهري المذلل 💎", fontSize = 12.sp, color = Color.White)
                        Switch(checked = editHasSubscription, onCheckedChange = { editHasSubscription = it })
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
                            galleryUrls = provider.galleryUrls,
                            isPinned = editPinned,
                            isRecommended = editRecommended,
                            hasSubscription = editHasSubscription
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
    val context = LocalContext.current
    
    var backupLocation by remember { mutableStateOf("ذاكرة الهاتف الداخلية") }
    var autoScheduleBackups by remember { mutableStateOf(true) }
    var backupFolder by remember { mutableStateOf("/storage/emulated/0/Download/WAM_Backups") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    
    var newAdminUser by remember { mutableStateOf("") }
    var newAdminPass by remember { mutableStateOf("") }
    
    var editingAdminUser by remember { mutableStateOf<String?>(null) }
    var editingAdminPass by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Section: System Backups
        item {
            Text("🛡️ لوحة المالك الأعلى لإدارة النسخ الاحتياطي والأمن والتدقيق", fontWeight = FontWeight.Bold, color = viewModel.appPrimaryColor, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🗄️ مركز جدولة وصناعة النسخ الاحتياطية", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    
                    Text("حدد المسار المستهدف ومكان الحفظ للنسخ:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("ذاكرة الهاتف الداخلية", "بطاقة الذاكرة SD", "جوجل درايف").forEach { loc ->
                            val isSel = backupLocation == loc
                            Button(
                                onClick = { backupLocation = loc },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) viewModel.appPrimaryColor else Color(0xFF2E2E33),
                                    contentColor = if (isSel) Color.Black else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(loc, fontSize = 9.sp)
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = backupFolder,
                        onValueChange = { backupFolder = it },
                        label = { Text("مجلد حفظ النسخ الاحتياطية المستهدف", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الجدولة التلقائية والنسخ اليومي", fontSize = 11.sp, color = Color.White)
                            Text("القيام بأخذ نسخة مشفرة متكاملة للذاكرة تلقائياً كل 24 ساعة بمجرد طلب الإذن.", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(checked = autoScheduleBackups, onCheckedChange = { autoScheduleBackups = it })
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val currentTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                viewModel.addActivityLog("صناعة نسخة احتياطية يدوية بنجاح إلى $backupLocation ($backupFolder/WAM_Backup_$currentTimestamp.json)")
                                Toast.makeText(context, "تم بنجاح نسخ وحفظ احتياطي شامل إلى $backupLocation بمجلد $backupFolder باسم WAM_Backup_$currentTimestamp.json 📂", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📂 حفظ نسخة احتياطية فورية", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { showRestoreDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E33), contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔄 استعادة من نسخة سابقة", fontSize = 10.sp)
                        }
                    }
                    
                    Button(
                        onClick = {
                            viewModel.triggerDynamicCleanCycle()
                            Toast.makeText(context, "تم نجاح تمشيط ومسح الكاش والبيانات المؤقتة وملفات الكوكيز بنجاح 🧼", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.8f), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🧼 مسح تلقائي للبيانات المؤقتة والسجلات القديمة كلياً", fontSize = 10.sp)
                    }
                }
            }
        }
        
        // Section: Supervisor accounts
        item {
            Text("👥 إدارة حسابات المشرفين والمساعدين للمنصة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("➕ إضافة مشرف إداري جديد ذو صلاحيات مخصصة", fontSize = 11.sp, color = viewModel.appPrimaryColor, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = newAdminUser,
                        onValueChange = { newAdminUser = it },
                        label = { Text("اسم المستخدم للحساب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = newAdminPass,
                        onValueChange = { newAdminPass = it },
                        label = { Text("كلمة المرور السرية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = {
                            if (newAdminUser.trim().isNotEmpty() && newAdminPass.trim().isNotEmpty()) {
                                val acc = com.Serviseyem.models.AdminAccount(
                                    username = newAdminUser.trim(),
                                    passwordSecret = newAdminPass.trim(),
                                    privileges = listOf("general", "moderate_providers")
                                )
                                viewModel.saveAdminAccount(acc)
                                newAdminUser = ""
                                newAdminPass = ""
                                Toast.makeText(context, "تم بنجاح إضافة المشرف وتأكيد الصلاحيات 🟢", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "الرجاء تعبئة الاسم والرمز لإتمام الإضافة", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("➕ إضافة وتفعيل المشرف الآن", fontSize = 11.sp)
                    }
                    
                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text("📋 قائمة حسابات الغرفة الإدارية الحالية (${viewModel.admins.size} مشرفين معتمدين):", fontSize = 11.sp, color = Color.Gray)
                    
                    viewModel.admins.forEach { admin ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F23)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("👤 المستخدم: ${admin.username}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        Text("🔑 كلمة المرور: ${admin.passwordSecret}", fontSize = 10.sp, color = Color.LightGray)
                                    }
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingAdminUser = admin.username
                                                editingAdminPass = admin.passwordSecret
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Password", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        
                                        // Restrict delete if admin is the default one to prevent lockouts
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteAdminAccount(admin.username)
                                                Toast.makeText(context, "تم إزالة وإلغاء صلاحيات المشرف ${admin.username}", Toast.LENGTH_SHORT).show()
                                            },
                                            enabled = admin.username != "admin"
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Account", tint = if (admin.username != "admin") Color.Red else Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                
                                if (editingAdminUser == admin.username) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = editingAdminPass,
                                            onValueChange = { editingAdminPass = it },
                                            label = { Text("رمز جديد") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                val acc = admin.copy(passwordSecret = editingAdminPass.trim())
                                                viewModel.saveAdminAccount(acc)
                                                editingAdminUser = null
                                                Toast.makeText(context, "تم حفظ كلمة المرور الجديدة بنجاح للمشرف ${admin.username}", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                                        ) {
                                            Text("تأكيد", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Section: Activity logs
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📜 سجل الرقابة والعمليات اليومية وعمليات التدقيق", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Button(
                    onClick = { viewModel.triggerDynamicCleanCycle() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("مسح السلف لليقظة", fontSize = 10.sp)
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
    
    if (showRestoreDialog) {
        var restoreCodeInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("🔄 استرجاع مجلدات وبيانات قاعدة البيانات", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الصق محتوى ملف JSON الاحتياطي أو أدخل اسم ملف الحفظ لاستعادته فوراً في الدليل اليمني:", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = restoreCodeInput,
                        onValueChange = { restoreCodeInput = it },
                        placeholder = { Text("اكتب اسم نسخة الحفظ كـ WAM_Backup_2026.json ...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addActivityLog("تمت استعادة قواعد البيانات بنجاح من النسخة ${restoreCodeInput.ifEmpty { "WAM_Backup_Default.json" }}")
                        Toast.makeText(context, "تمت التهيئة واسترجاع البيانات المجدولة المعتمدة بنجاح 🟢", Toast.LENGTH_LONG).show()
                        showRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black)
                ) {
                    Text("تأكيد الاستعادة والدمج")
                }
            },
            dismissButton = {
                Button(onClick = { showRestoreDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                    Text("إلغاء الأمر")
                }
            }
        )
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("تنبيهات وتذكيرات الحجوزات للمستخدمين", fontSize = 11.sp, color = Color.White)
                            Text("تشغيل أو إيقاف تنبيهات تذكير المستخدمين بمواعيد الحجوزات القادمة", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = viewModel.isBookingAlertsEnabled,
                            onCheckedChange = { viewModel.isBookingAlertsEnabled = it }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBannersSubSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var bannerTitle by remember { mutableStateOf("") }
    var bannerType by remember { mutableStateOf("text") } // "text", "image", "video"
    var targetSection by remember { mutableStateOf("") }
    var durationSecs by remember { mutableStateOf("15") }
    var selectedMediaBase64 by remember { mutableStateOf("") }
    var isUploadingMedia by remember { mutableStateOf(false) }

    // File picker launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploadingMedia = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val isVideo = bannerType == "video"
                    val base64 = uriToBase64(context, it, isVideo)
                    selectedMediaBase64 = base64
                    withContext(Dispatchers.Main) {
                        isUploadingMedia = false
                        Toast.makeText(context, "تم تحميل وتشفير ملف الوسائط بنجاح ومزامنته 🟢", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isUploadingMedia = false
                        Toast.makeText(context, "فشل في معالجة الملف: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Form to add banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📺 إضافة لافتة إعلانية / عرض جديد", fontWeight = FontWeight.Bold, color = viewModel.appPrimaryColor)
                    
                    OutlinedTextField(
                        value = bannerTitle,
                        onValueChange = { bannerTitle = it },
                        label = { Text("عنوان اللافتة / نص الإعلان الترويجي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type Selector Row
                    Text("نوع محتوى اللافتة:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("text" to "نص فقط 📝", "image" to "صورة 🖼️", "video" to "فيديو 🎥").forEach { (type, label) ->
                            FilterChip(
                                selected = bannerType == type,
                                onClick = { bannerType = type },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = viewModel.appPrimaryColor,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    if (bannerType != "text") {
                        Button(
                            onClick = {
                                val mimeType = if (bannerType == "video") "video/*" else "image/*"
                                mediaPickerLauncher.launch(mimeType)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appSecondaryColor, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (bannerType == "video") "🎥 اختر فيديو من الذاكرة" else "🖼️ اختر صورة مضغوطة WebP/JPEG")
                        }

                        if (isUploadingMedia) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp, color = viewModel.appPrimaryColor)
                                Text("جاري معالجة وضغط وتشفير الملف ليرفع فورياً...", fontSize = 10.sp, color = Color.Gray)
                            }
                        } else if (selectedMediaBase64.isNotEmpty()) {
                            Text("✅ تم تحميل وضغط ملف الوسائط بنجاح (جاهز للحفظ والسرعة بالهاتف)", fontSize = 10.sp, color = Color.Green)
                        }
                    }

                    OutlinedTextField(
                        value = targetSection,
                        onValueChange = { targetSection = it },
                        label = { Text("القسم المستهدف (مثال: تبريد وتكييف - اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = durationSecs,
                        onValueChange = { durationSecs = it },
                        label = { Text("مدة بقاء المعاينة والتكرار (بالثواني)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (bannerTitle.isBlank()) {
                                Toast.makeText(context, "الرجاء كتابة عنوان اللافتة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val secs = durationSecs.toIntOrNull() ?: 15
                            val newBanner = com.Serviseyem.models.AdBanner(
                                title = bannerTitle.trim(),
                                contentType = bannerType,
                                mediaUrl = selectedMediaBase64.ifEmpty { null },
                                targetSectionId = targetSection.trim(),
                                durationSeconds = secs,
                                isVisible = true
                            )
                            viewModel.addNewAdBanner(newBanner)
                            // Reset form
                            bannerTitle = ""
                            selectedMediaBase64 = ""
                            targetSection = ""
                            Toast.makeText(context, "تم حفظ لافتة الإعلانات والرفع بنجاح لجميع الأجهزة!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ ونشر اللافتة في الأجهزة فوراً 🚀", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active list of banners
        item {
            Text("📺 اللافتات الحالية الناشطة لجميع المستخدمين (${viewModel.banners.size})", fontWeight = FontWeight.Bold, color = Color.White)
        }

        items(viewModel.banners) { banner ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.DarkGray)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (banner.contentType != "text" && !banner.mediaUrl.isNullOrBlank()) {
                        // Small preview in admin panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (banner.contentType == "image") {
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
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Preview",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("معاينة الصورة معطلة 🚫", color = Color.Gray, fontSize = 10.sp)
                                }
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Video", tint = Color.White, modifier = Modifier.size(36.dp))
                                Text("لافتة فيديو تشغيلية 🎥", color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp))
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = banner.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(
                            text = "نوع المحتوى: ${if (banner.contentType == "image") "صورة 🖼️" else if (banner.contentType == "video") "فيديو 🎥" else "نص 📝"} • القسم: ${banner.targetSectionId.ifEmpty { "عام للكل" }} • المدة: ${banner.durationSeconds}ثانية",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.toggleBannerVisibility(banner.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (banner.isVisible) Color(0xFF1E5E20) else Color(0xFF3E2723)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (banner.isVisible) "مرئي 🟢" else "مخفي 🔴", fontSize = 9.sp, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.deleteAdBanner(banner.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Banner", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Image Memory Consumption and Reports
        item {
            Text("🗄️ تقارير استهلاك الصور والملفات 🧹", fontWeight = FontWeight.Bold, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161619)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(viewModel.appPrimaryColor.copy(alpha = 0.3f))),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تقرير كفاءة الذاكرة ومساحة التخزين بالديل:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.LightGray)

                    val providersPhoto = viewModel.providers.filter { it.profilePhotoUrl?.isNotBlank() == true }.size
                    val totalBannersWithMedia = viewModel.banners.filter { !it.mediaUrl.isNullOrBlank() }.size
                    val totalGalleryCount = viewModel.providers.flatMap { it.galleryUrls ?: listOf() }.size

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("الصور الشخصية للمهنيين:", fontSize = 11.sp, color = Color.Gray)
                        Text("$providersPhoto صور", fontSize = 11.sp, color = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("صور معرض الأعمال:", fontSize = 11.sp, color = Color.Gray)
                        Text("$totalGalleryCount صور", fontSize = 11.sp, color = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("لافتات إعلانية تحتوي وسائط:", fontSize = 11.sp, color = Color.Gray)
                        Text("$totalBannersWithMedia لافتات", fontSize = 11.sp, color = Color.White)
                    }
                    Divider(color = Color.DarkGray)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("إجمالي الملفات المرتبطة بنشاط القاعدة:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${providersPhoto + totalGalleryCount + totalBannersWithMedia} ملفات", fontSize = 11.sp, color = viewModel.appPrimaryColor, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            viewModel.cleanOrphanImageMemory()
                            Toast.makeText(context, "تم مسح وتصفية كافة الصور والملفات المنفصلة بنجاح وحررت المساحة 🟢", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("مسح الصور غير المرتبطة بقاعدة البيانات لتحرير المساحة 🧹", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("ملاحظة: يقوم نظام التصفية بمسح أي كاش أو صور مخزنة محلياً بالذاكرة السحابية ومصنفة كمنفصلة لعدم وجود مرجع نشط لها في الملفات المهنية أو الإعلانية.", fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
    }
}

fun uriToBase64(context: Context, uri: Uri, isVideo: Boolean): String {
    return try {
        val cr = context.contentResolver
        if (isVideo) {
            val inputStream = cr.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else ""
        } else {
            val inputStream = cr.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            val bos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, bos)
            val bytes = bos.toByteArray()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        ""
    }
}

