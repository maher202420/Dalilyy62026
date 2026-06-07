package com.Serviseyem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.AppSettings
import com.Serviseyem.screens.*
import com.Serviseyem.services.FirebaseService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to Edge setup
        enableEdgeToEdge()

        // Sync and network recovery handlers
        FirebaseService.loadInitialCachedData()
        FirebaseService.initListeners()
        FirebaseService.registerNetworkMonitoring(this)

        setContent {
            var currentScreen by remember { mutableStateOf("main") }
            var activeSessionId by remember { mutableStateOf<String?>(null) }
            val settings by FirebaseService.settings.collectAsState()

            // Main application theme wrapper
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = parseColorHex(settings.primaryColor, Color(0xFFD4AF37)),
                    background = parseColorHex(settings.baseCanvasColor, Color(0xFF0F172A)),
                    surface = Color(0xFF1E293B)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = parseColorHex(settings.baseCanvasColor, Color(0xFF0F172A))
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(250),
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "main" -> MainScreen(
                                onNavigateToChat = {
                                    activeSessionId = null
                                    currentScreen = "chat"
                                },
                                onNavigateToRegister = {
                                    // Seamless route redirection for secret doorway
                                    currentScreen = "backdoor"
                                },
                                onNavigateToAbout = {
                                    currentScreen = "about"
                                },
                                onNavigateToChatWithSession = { sessId ->
                                    activeSessionId = sessId
                                    currentScreen = "chat"
                                }
                            )

                            "chat" -> ChatScreen(
                                initialChatSessionId = activeSessionId,
                                onNavigateBack = {
                                    currentScreen = "main"
                                }
                            )

                            "about" -> AboutScreen(
                                onNavigateBack = {
                                    currentScreen = "main"
                                }
                            )

                            "register_provider" -> RegisterProviderScreen(
                                onNavigateBack = {
                                    currentScreen = "main"
                                }
                            )

                            "backdoor" -> BackdoorSettingsScreen(
                                onNavigateToAdminDashboard = {
                                    currentScreen = "admin"
                                },
                                onNavigateToRegisterForm = {
                                    currentScreen = "register_provider"
                                },
                                onNavigateBack = {
                                    currentScreen = "main"
                                }
                            )

                            "admin" -> AdminDashboardScreen(
                                onNavigateBack = {
                                    currentScreen = "backdoor"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Robust Color Hex values parsing to guarantee safe compilation.
 */
fun parseColorHex(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

/**
 * Beautiful unified Backdoor Settings Panel.
 * Lets administrators modify identity variables, app name, colors, support numbers,
 * chat indicators, blocker blacklists, etc. synchronously via Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackdoorSettingsScreen(
    onNavigateToAdminDashboard: () -> Unit = {},
    onNavigateToRegisterForm: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by FirebaseService.settings.collectAsState()
    val scrollState = rememberScrollState()

    // Inputs States for AppSettings
    var appNameVal by remember { mutableStateOf("") }
    var welcomeVal by remember { mutableStateOf("") }
    var primaryColorVal by remember { mutableStateOf("") }
    var canvasColorVal by remember { mutableStateOf("") }
    var phoneVal by remember { mutableStateOf("") }
    var whatsappVal by remember { mutableStateOf("") }
    var emailVal by remember { mutableStateOf("") }
    var footerVal by remember { mutableStateOf("") }
    var passVal by remember { mutableStateOf("") }
    var checkMaintenance by remember { mutableStateOf(false) }
    var maintenanceMsgVal by remember { mutableStateOf("") }

    // Chat controls
    var chatSize by remember { mutableStateOf("48") }
    var chatCol by remember { mutableStateOf("") }
    var chatVis by remember { mutableStateOf(true) }
    var chatVisitsActive by remember { mutableStateOf(true) }
    var chatProvsActive by remember { mutableStateOf(true) }
    var chatDisabledMsgVal by remember { mutableStateOf("") }
    var blacklistVal by remember { mutableStateOf("") }

    // Radius Search limit
    var radiusDistanceLimit by remember { mutableStateOf("10") }
    var voiceSearchActive by remember { mutableStateOf(true) }

    // One-time load from global settings
    LaunchedEffect(settings) {
        appNameVal = settings.appNameAr
        welcomeVal = settings.welcomeMsg
        primaryColorVal = settings.primaryColor
        canvasColorVal = settings.baseCanvasColor
        phoneVal = settings.supportPhone
        whatsappVal = settings.supportWhatsapp
        emailVal = settings.supportEmail
        footerVal = settings.footerText
        passVal = settings.adminPassword
        checkMaintenance = settings.isMaintenanceMode
        maintenanceMsgVal = settings.maintenanceMessage

        chatSize = settings.chatIconSize.toString()
        chatCol = settings.chatIconColor
        chatVis = settings.chatIconVisible
        chatVisitsActive = settings.chatEnabledForVisitors
        chatProvsActive = settings.chatEnabledForProviders
        chatDisabledMsgVal = settings.chatDisabledMessage
        blacklistVal = settings.blockedChatUsers

        radiusDistanceLimit = settings.distanceLimit.toString()
        voiceSearchActive = settings.voiceSearchEnabled
    }

    // Supervisor credentials validation
    var supervisorPhone by remember { mutableStateOf("") }
    var supervisorPass by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Topbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "⚙️ الإعدادات السرية وبوابة المزامنة لـ WAM",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مستوى التعديل: المشرف السحابي العام",
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // TRANSITION TRIGGERS
            Button(
                onClick = onNavigateToAdminDashboard,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("الدخول إلى لوحة المبيعات والإشراف (9 أقسام) 💻", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToRegisterForm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.BorderColor, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("طلب تسجيل فني جديد بالدليل 📝", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SUPERVISOR LOGGING CENTER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🔑 بوابة تسجيل دخول وكلاء وموظفي المبيعات:", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = supervisorPhone,
                        onValueChange = { supervisorPhone = it },
                        label = { Text("اسم المستخدم المشرف", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = supervisorPass,
                        onValueChange = { supervisorPass = it },
                        label = { Text("كلمة المرور الخاصة بالمشرف", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val activeSupervisors = FirebaseService.supervisorsList.value
                            val found = activeSupervisors.find { it.name == supervisorPhone.trim() && it.password == supervisorPass.trim() }
                            if (found != null) {
                                FirebaseService.currentSupervisor = found
                                Toast.makeText(context, "تم تسجيل الدخول كمشرف مبيعات بنجاح: ${found.name}", Toast.LENGTH_SHORT).show()
                                onNavigateToAdminDashboard()
                            } else {
                                Toast.makeText(context, "كلمة المرور أو اسم المستخدم غير معتمد!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("دخول كمشرف مبيعات دائم", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // THE GENERAL SETTINGS EDIT FORM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🎨 تخصيص الهوية البصرية والمسميات:", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = appNameVal,
                        onValueChange = { appNameVal = it },
                        label = { Text("اسم التطبيق بالعربية") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = welcomeVal,
                        onValueChange = { welcomeVal = it },
                        label = { Text("رسالة الترحيب والواجهة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = footerVal,
                        onValueChange = { footerVal = it },
                        label = { Text("التذييل الترويجي الدائم") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = primaryColorVal,
                            onValueChange = { primaryColorVal = it },
                            label = { Text("اللون الذهبي والنبض (Hex)") },
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = canvasColorVal,
                            onValueChange = { canvasColorVal = it },
                            label = { Text("لون الكنفاس والخلفية (Hex)") },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("📞 هويات وأرقام الدعم والصيانة المباشرة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = phoneVal,
                        onValueChange = { phoneVal = it },
                        label = { Text("هاتف الاتصال الموحد") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = whatsappVal,
                        onValueChange = { whatsappVal = it },
                        label = { Text("واتساب الشكاوى الفورية") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = emailVal,
                        onValueChange = { emailVal = it },
                        label = { Text("البريد الإلكتروني للإدارة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = passVal,
                        onValueChange = { passVal = it },
                        label = { Text("تحديث رمز البوابة السري الحالي (أدمن)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🛡️ إعدادات البحث القريب والرادار:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = radiusDistanceLimit,
                        onValueChange = { radiusDistanceLimit = it },
                        label = { Text("حد مسافة البحث (Rad بالكم)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = voiceSearchActive, onCheckedChange = { voiceSearchActive = it })
                        Text("تمكين خيار البحث الصوتي الفوري (Mic)", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("💬 التحكم بمنظومة الدردشة الفورية (Chat Settings):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = chatSize,
                            onValueChange = { chatSize = it },
                            label = { Text("حجم الأيقونة (بكسل)") },
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = chatCol,
                            onValueChange = { chatCol = it },
                            label = { Text("لون الأيقونة (Hex)") },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = chatVis, onCheckedChange = { chatVis = it })
                        Text("إظهار أيقونة الدردشة النشطة بالأجهزة", color = Color.White, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = chatVisitsActive, onCheckedChange = { chatVisitsActive = it })
                        Text("تمكين الدردشة للزوار والأجهزة المستقلة", color = Color.White, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = chatProvsActive, onCheckedChange = { chatProvsActive = it })
                        Text("تمكين الدردشة للفنيين ومزودي الخدمات", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = chatDisabledMsgVal,
                        onValueChange = { chatDisabledMsgVal = it },
                        label = { Text("رسالة تعطيل الدردشة (في حال الإغلاق)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = blacklistVal,
                        onValueChange = { blacklistVal = it },
                        label = { Text("قائمة الهواتف المحظورة (مفصولة بفاصلة)") },
                        placeholder = { Text("مثال: 777111222, 733444555") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = checkMaintenance, onCheckedChange = { checkMaintenance = it })
                        Text("تفعيل وضع الصيانة السحابية الموحد 🛡️", color = Color.White, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = maintenanceMsgVal,
                        onValueChange = { maintenanceMsgVal = it },
                        label = { Text("رسالة الصيانة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isSaving) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFD4AF37))
                        }
                    } else {
                        Button(
                            onClick = {
                                isSaving = true
                                val newAppSettings = AppSettings(
                                    appNameAr = appNameVal.trim(),
                                    welcomeMsg = welcomeVal.trim(),
                                    primaryColor = primaryColorVal.trim(),
                                    baseCanvasColor = canvasColorVal.trim(),
                                    supportPhone = phoneVal.trim(),
                                    supportWhatsapp = whatsappVal.trim(),
                                    supportEmail = emailVal.trim(),
                                    footerText = footerVal.trim(),
                                    adminPassword = passVal.trim(),
                                    isMaintenanceMode = checkMaintenance,
                                    maintenanceMessage = maintenanceMsgVal.trim(),

                                    chatIconSize = chatSize.toIntOrNull() ?: 48,
                                    chatIconColor = chatCol.trim(),
                                    chatIconVisible = chatVis,
                                    chatEnabledForVisitors = chatVisitsActive,
                                    chatEnabledForProviders = chatProvsActive,
                                    chatDisabledMessage = chatDisabledMsgVal.trim(),
                                    blockedChatUsers = blacklistVal.trim(),

                                    distanceLimit = radiusDistanceLimit.toIntOrNull() ?: 10,
                                    voiceSearchEnabled = voiceSearchActive
                                )

                                FirebaseService.saveSettings(newAppSettings, {
                                    isSaving = false
                                    Toast.makeText(context, "تم مزامنة وبث كل البيانات الإدارية بنجاح سحابي!", Toast.LENGTH_SHORT).show()
                                }, {
                                    isSaving = false
                                    Toast.makeText(context, "فشل عملية الحفظ والرفع", Toast.LENGTH_SHORT).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("بث وحفظ التعديلات سحابياً 🌍", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
