package com.Serviseyem

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.Serviseyem.models.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import java.util.Locale

// --- HELPER FUNCTIONS ---
fun safeParseColor(colorHex: String, defaultColor: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        defaultColor
    }
}

fun resolveAppFontFamily(fontName: String): FontFamily {
    return when (fontName.lowercase()) {
        "sansserif", "sans-serif" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.Default
    }
}

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private var onVoiceResultCallback: ((String) -> Unit)? = null
    
    private val voiceRecognizerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                onVoiceResultCallback?.invoke(matches[0])
            }
        }
    }
    
    fun startVoiceInput(callback: (String) -> Unit) {
        onVoiceResultCallback = callback
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن بصوتك يا غالي... 🎙️")
            }
            voiceRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "ميزة الإدخال الصوتي غير مدعومة على هذا الجهاز", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun speak(text: String) {
        if (vm.settings.value.allowTextToSpeech) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {}
        
        try {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("ar")
                }
            }
        } catch (e: Exception) {}
        
        vm.initCache(this)
        
        setContent {
            val settings by vm.settings.collectAsStateWithLifecycle()
            
            try {
                AppTheme.primaryRed = safeParseColor(settings.primaryColorHex, Color(0xFFCE1126))
                AppTheme.accentGold = safeParseColor(settings.accentColorHex, Color(0xFFFFD700))
                AppTheme.darkBg = safeParseColor(settings.bgColorHex, Color(0xFF0D1B1E))
                AppTheme.surfaceDark = safeParseColor(settings.surfaceColorHex, Color(0xFF162A2D))
            } catch (e: Exception) {
                AppTheme.primaryRed = Color(0xFFCE1126)
                AppTheme.accentGold = Color(0xFFFFD700)
                AppTheme.darkBg = Color(0xFF0D1B1E)
                AppTheme.surfaceDark = Color(0xFF162A2D)
            }
            
            val selectedFont = resolveAppFontFamily(settings.selectedFontName)

            val customColorScheme = darkColorScheme(
                primary = AppTheme.primaryRed,
                onPrimary = Color.White,
                secondary = AppTheme.accentGold,
                onSecondary = Color.Black,
                background = AppTheme.darkBg,
                onBackground = Color.White,
                surface = AppTheme.surfaceDark,
                onSurface = Color.White
            )

            MaterialTheme(
                colorScheme = customColorScheme,
                typography = Typography().copy(
                    displayLarge = Typography().displayLarge.copy(fontFamily = selectedFont),
                    bodyLarge = Typography().bodyLarge.copy(fontFamily = selectedFont),
                    bodyMedium = Typography().bodyMedium.copy(fontFamily = selectedFont),
                    labelLarge = Typography().labelLarge.copy(fontFamily = selectedFont)
                )
            ) {
                var showSplash by remember { mutableStateOf(true) }
                
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1200)
                    showSplash = false
                }
                
                if (showSplash) {
                    SplashScreenComponent(selectedFont)
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = AppTheme.darkBg
                    ) {
                        AppNavigationLayout(vm)
                        
                        val showReg by vm.showRegistrationDialog.collectAsStateWithLifecycle()
                        if (showReg) {
                            ConditionalRegistrationDialog(
                                vm = vm,
                                fontFamily = selectedFont,
                                onDismiss = { vm.showRegistrationDialog.value = false },
                                onSuccess = { }
                            )
                        }
                    }
                }
            }
        }
    }

    fun showPurgeConfirmationDialog() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "أدخل كلمة مرور المالك"
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ تطهير البيانات")
            .setMessage("هل أنت متأكد من رغبتك في تطهير كافة بيانات الدليل وإعادتها للمصنع؟ هذا الإجراء لا يمكن التراجع عنه!")
            .setView(input)
            .setPositiveButton("تطهير") { dialog, _ ->
                val enteredPassword = input.text.toString()
                if (enteredPassword == "maher736462") {
                    dialog.dismiss()
                    executeDataPurge()
                } else {
                    Toast.makeText(this, "❌ كلمة المرور غير صحيحة!", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("إلغاء") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun executeDataPurge() {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("جاري تطهير قواعد البيانات وإعادة البناء...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                vm.purgeAllData("maher736462")
                progressDialog.dismiss()
                Toast.makeText(this@MainActivity, "✅ تم تطهير قواعد البيانات وإعادة بناء الدليل العظيم بنجاح!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(this@MainActivity, "❌ خطأ في التطهير: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// --- COMPOSE SCREENS & DIALOGS ---
@Composable
fun SplashScreenComponent(fontFamily: FontFamily) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppTheme.primaryRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "WAM",
                    color = AppTheme.accentGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily
                )
            }
            Text(
                text = "كل خدمات اليمن",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
            Text(
                text = "دليلك الشامل للمهن والخدمات",
                color = AppTheme.grayText,
                fontSize = 12.sp,
                fontFamily = fontFamily
            )
        }
    }
}

@Composable
fun ConditionalRegistrationDialog(
    vm: MainViewModel,
    fontFamily: FontFamily,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppTheme.accentGold),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📝 تسجيل مستخدم جديد",
                    color = AppTheme.accentGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily
                )
                Text(
                    text = "يرجى إدخال بياناتك للمتابعة",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = fontFamily
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("المدينة", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = Color.White, fontFamily = fontFamily)
                    }
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank() || city.isBlank()) {
                                Toast.makeText(context, "الرجاء ملء جميع الحقول", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            vm.completeRegistration(name, phone, city)
                            onSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تسجيل", color = Color.White, fontFamily = fontFamily)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigationLayout(viewModel: MainViewModel) {
    MainAppScreen(viewModel = viewModel)
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    
    var currentScreen by remember { mutableStateOf("DIRECTORY") } // DIRECTORY, MAPS, CHATS, JOIN, ABOUT, ADMIN
    var activeChatId by remember { mutableStateOf<String?>(null) }
    
    var logoClickCount by remember { mutableIntStateOf(0) }
    var lastLogoClickTime by remember { mutableLongStateOf(0L) }
    
    var showBackdoorLogin by remember { mutableStateOf(false) }
    var showBackdoorPanel by remember { mutableStateOf(false) }
    var showNotificationCenter by remember { mutableStateOf(false) }
    var showLoginRequired by remember { mutableStateOf(false) }
    var showFloatingAssistantDialog by remember { mutableStateOf(false) }
    
    // زر الرجوع المطور: ضغطة واحدة ترجعك للرئيسية، وضغطتان متتاليتان تخرج من التطبيق
    val activity = context as? androidx.activity.ComponentActivity
    var lastBackPressTime by remember { mutableStateOf(0L) }
    androidx.activity.compose.BackHandler(enabled = true) {
        if (activeChatId != null) {
            activeChatId = null
        } else if (currentScreen != "DIRECTORY") {
            currentScreen = "DIRECTORY"
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000) {
                activity?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "اضغط مرة أخرى للخروج من التطبيق 🚪", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // تسجيل الدخول كمستخدم ضيف تلقائياً إن لم يكن مسجلاً
    LaunchedEffect(Unit) {
        if (currentUserId == null) {
            viewModel.currentUserId.value = "user_${System.currentTimeMillis()}"
            viewModel.currentUserRole.value = "user"
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = Color(0xFF0D1821),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // أزرار الرأس الإضافية (الإدارة والإشعارات) - على اليسار
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // زر الإدارة (لوحة التحكم) المتطابق تماماً
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131D35))
                                .border(1.dp, Color(0xFF1E88E5).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable { currentScreen = "ADMIN" }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "لوحة التحكم",
                                tint = Color(0xFF90CAF9),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "الإدارة",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // زر الإشعارات المطور المفلتر بدقة (أمان وحساب حقيقي تبعا للهوية والدور)
                        val myUnreadCount = remember(notifications, currentUserId, currentUserRole) {
                            notifications.count { item ->
                                val isPersonal = item.title.contains("حجز") || item.body.contains("حجز") ||
                                                 item.title.contains("انضمام") || item.body.contains("انضمام") ||
                                                 item.title.contains("طلب") || item.body.contains("طلب") ||
                                                 item.body.contains("ياسين") || item.body.contains("النجار") ||
                                                 item.body.contains("الغرباني") || item.body.contains("أمين")
                                
                                val matchesUser = if (isPersonal) {
                                    val isForMe = (currentUserId != null && item.targetUserId == currentUserId)
                                    val isForAdmin = ((currentUserRole == "admin" || currentUserRole == "admins") && item.targetRole == "admins")
                                    isForMe || isForAdmin
                                } else {
                                    if (item.targetUserId.isNotBlank()) {
                                        currentUserId != null && item.targetUserId == currentUserId
                                    } else {
                                        val isAll = item.targetRole == "all" || item.targetRole.isBlank()
                                        val isMyRole = (currentUserRole == "provider" && item.targetRole == "providers") ||
                                                       (currentUserRole == "user" && item.targetRole == "users") ||
                                                       ((currentUserRole == "admin" || currentUserRole == "admins") && item.targetRole == "admins")
                                        isAll || isMyRole
                                    }
                                }
                                matchesUser && !item.isRead
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF131D35))
                                .border(1.dp, Color(0xFF1E88E5).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable { showNotificationCenter = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$myUnreadCount",
                                color = Color(0xFF90CAF9),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "الإشعارات",
                                tint = Color(0xFF90CAF9),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // اللوجو المطور واسم التطبيق - على اليمين في الـ RTL
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "كل خدمات اليمن",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // لوجو WAM المربع الأنيق
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D47A1))
                                .border(1.dp, Color(0xFF1E88E5), RoundedCornerShape(8.dp))
                                .clickable {
                                    val now = System.currentTimeMillis()
                                    if (now - lastLogoClickTime < 1500) {
                                        logoClickCount++
                                    } else {
                                        logoClickCount = 1
                                    }
                                    lastLogoClickTime = now
                                    
                                    if (logoClickCount >= 5) {
                                        logoClickCount = 0
                                        showBackdoorLogin = true
                                        Toast.makeText(context, "🤫 جاري فتح البوابة الخلفية للمالك...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "WAM",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppTheme.surfaceDark,
                contentColor = Color(0xFF90CAF9)
            ) {
                // 1. الدليل
                NavigationBarItem(
                    selected = currentScreen == "DIRECTORY",
                    onClick = { currentScreen = "DIRECTORY"; activeChatId = null },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("الدليل", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF90CAF9),
                        selectedTextColor = Color(0xFF90CAF9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                
                // 2. الخريطة
                NavigationBarItem(
                    selected = currentScreen == "MAPS",
                    onClick = { currentScreen = "MAPS"; activeChatId = null },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text("الخريطة", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF90CAF9),
                        selectedTextColor = Color(0xFF90CAF9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                
                // 3. المحادثة
                NavigationBarItem(
                    selected = currentScreen == "CHATS",
                    onClick = { currentScreen = "CHATS"; activeChatId = null },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    label = { Text("المحادثة", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF90CAF9),
                        selectedTextColor = Color(0xFF90CAF9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                
                // 4. انضمام
                NavigationBarItem(
                    selected = currentScreen == "JOIN",
                    onClick = { currentScreen = "JOIN"; activeChatId = null },
                    icon = { Icon(Icons.Default.HomeWork, contentDescription = null) },
                    label = { Text("انضمام", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF90CAF9),
                        selectedTextColor = Color(0xFF90CAF9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                
                // 5. معلومات
                NavigationBarItem(
                    selected = currentScreen == "ABOUT",
                    onClick = { currentScreen = "ABOUT"; activeChatId = null },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("معلومات", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF90CAF9),
                        selectedTextColor = Color(0xFF90CAF9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                
                // 6. الإدارة
                NavigationBarItem(
                    selected = currentScreen == "ADMIN",
                    onClick = { currentScreen = "ADMIN"; activeChatId = null },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("الإدارة", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF90CAF9),
                        selectedTextColor = Color(0xFF90CAF9),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.darkBg)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // عرض محتوى الصفحة المختار بكل سلاسة
                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        "DIRECTORY" -> DirectoryScreen(
                            viewModel = viewModel,
                            onNavigateToChat = { roomId ->
                                activeChatId = roomId
                                currentScreen = "CHATS"
                            }
                        )
                        "MAPS" -> MapScreen(viewModel = viewModel)
                        "CHATS" -> {
                            if (activeChatId != null) {
                                ChatDetailScreen(
                                    chatId = activeChatId!!,
                                    viewModel = viewModel,
                                    onBack = { activeChatId = null }
                                )
                            } else {
                                ChatScreen(
                                    viewModel = viewModel,
                                    onNavigateToChatDetail = { roomId ->
                                        activeChatId = roomId
                                    }
                                )
                            }
                        }
                        "JOIN" -> JoinScreen(viewModel = viewModel)
                        "ABOUT" -> AboutScreen(viewModel = viewModel)
                        "ADMIN" -> AdminDashboardScreen(viewModel = viewModel)
                    }

                    // 🤖 زر عائم للمساعد الذكي العائم
                    if (!settings.assistantIconHidden && currentScreen != "ADMIN" && activeChatId == null) {
                        val assistantSize = settings.assistantIconSize * (settings.assistantIconSizePercent / 100f)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    bottom = settings.assistantYOffset.dp,
                                    end = settings.assistantXOffset.dp
                                ),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            FloatingActionButton(
                                onClick = { showFloatingAssistantDialog = true },
                                containerColor = safeParseColor(settings.assistantIconColorHex, AppTheme.accentGold),
                                modifier = Modifier.size(assistantSize.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = "المساعد الذكي العائم",
                                    tint = Color.Black,
                                    modifier = Modifier.size((assistantSize * 0.5f).dp)
                                )
                            }
                        }
                    }
                }
                
                // التذييل (Footer) الاحترافي المتوافق مع الصورة تماماً
                if (currentScreen != "ADMIN" && activeChatId == null) {
                    Text(
                        text = "wam 2026",
                        color = Color(0xFF1976D2),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090E14))
                            .padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
    
    // النوافذ الحوارية
    if (showBackdoorLogin) {
        BackdoorLoginDialog(
            onSuccess = {
                showBackdoorLogin = false
                showBackdoorPanel = true
            },
            onDismiss = { showBackdoorLogin = false }
        )
    }
    
    if (showBackdoorPanel) {
        BackdoorControlPanelDialog(
            viewModel = viewModel,
            onDismiss = { showBackdoorPanel = false },
            onLogout = {
                showBackdoorPanel = false
                Toast.makeText(context, "🔐 تم تسجيل الخروج من البوابة السرية", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    if (showNotificationCenter) {
        NotificationCenterDialog(
            viewModel = viewModel,
            onDismiss = { showNotificationCenter = false }
        )
    }
    
    if (showLoginRequired) {
        LoginRequiredDialog(
            onSuccess = { userId, role ->
                viewModel.currentUserId.value = userId
                viewModel.currentUserRole.value = role
                showLoginRequired = false
            },
            onDismiss = { showLoginRequired = false }
        )
    }

    if (showFloatingAssistantDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showFloatingAssistantDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                colors = CardDefaults.cardColors(containerColor = AppTheme.darkBg),
                border = BorderStroke(1.dp, AppTheme.accentGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.surfaceDark)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖 المساعد الفني الذكي العائم", color = AppTheme.accentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { showFloatingAssistantDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SmartAssistantScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
