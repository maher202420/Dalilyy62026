package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.screens.*
import com.example.services.FirebaseService
import com.example.services.SystemAiAssistant
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase and local reactive sync fallback
        FirebaseService.init(this)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigator(
                    onExitApp = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastBackPressTime < 2000) {
                            finish()
                        } else {
                            lastBackPressTime = currentTime
                            Toast.makeText(this, "اضغط مرة أخرى للخروج من التطبيق", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(onExitApp: () -> Unit) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // settings sync
    val settingsState = FirebaseService.settings.collectAsState()
    val settings = settingsState.value

    // Backdoor 5-click counter
    var backdoorClicks by remember { mutableStateOf(0) }
    
    // Admin login dialog state
    var showAdminLogin by remember { mutableStateOf(false) }
    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }

    // AI Assistant state
    var showAiDialog by remember { mutableStateOf(false) }
    var aiQuery by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }
    var aiConversation = remember { mutableStateListOf<Pair<String, Boolean>>() } // query to response (Me, Bot)

    // Current Navigation route tracking for Top/Footer visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    // Universal Arabic directionality wrapper
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        
        // Backpress handler (Problem 5: 1 press → home, 2 presses → close)
        BackHandler {
            if (currentRoute == "home") {
                onExitApp()
            } else {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Top App Bar RTL structure (Home, Admin, Worker registration, language toggles, refresh)
                TopAppBar(
                    title = {
                        Text(
                            text = settings.appNameAr,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .testTag("app_logo_title")
                                .clickable {
                                    // Hidden backup option - click title elements 5 times to unlock backdoor
                                    backdoorClicks++
                                    if (backdoorClicks >= 5) {
                                        backdoorClicks = 0
                                        navController.navigate("backdoor")
                                        Toast.makeText(context, "🔑 تم فتح البوابة الخلفية السرية!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        )
                    },
                    actions = {
                        // 🏠 Home Icon Trigger
                        IconButton(
                            onClick = {
                                backdoorClicks++
                                if (backdoorClicks >= 5) {
                                    backdoorClicks = 0
                                    navController.navigate("backdoor")
                                } else {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("nav_home_btn")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                        }

                        // 🔐 Admin Dashboard Trigger
                        IconButton(
                            onClick = { showAdminLogin = true },
                            modifier = Modifier.testTag("nav_admin_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Admin Area", tint = MaterialTheme.colorScheme.primary)
                        }

                        // 👤 Register Technician Form Link
                        IconButton(
                            onClick = { navController.navigate("register") },
                            modifier = Modifier.testTag("nav_register_btn")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Register Service Provider", tint = MaterialTheme.colorScheme.primary)
                        }

                        // 🌐 Switch Language Toggle Box
                        IconButton(onClick = {
                            Toast.makeText(context, "✓ اللغة الحالية: العربية RTL. تم التحويل التلقائي بنجاح", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Language, contentDescription = "Switch Language", tint = MaterialTheme.colorScheme.primary)
                        }

                        // 🔄 Force Realtime Synchronizer
                        IconButton(onClick = {
                            FirebaseService.init(context)
                            scope.launch {
                                Toast.makeText(context, "🔄 جاري إعادة الاتصال والمزامنة الفورية مع قواعد Firestore...", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Manual sync", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                // FOOTER PANEL WITHsafe bottom padding (Problem 1 Solution)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // Ensures no truncation on notch or gesture zone
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left footer details ℹ️ About Button
                        IconButton(
                            onClick = { navController.navigate("about") },
                            modifier = Modifier.testTag("about_app_footer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About App",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Middle custom trademark copyrights text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = settings.footerText, // e.g. "MAW 777644670"
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp, // Made readable and visible (Problem 1)
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("footer_copyright_text")
                            )
                            Text(
                                text = "دليل خدمات يمن شامل",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 9.sp
                            )
                        }

                        // Right AI Smart Assistant Trigger Button
                        IconButton(
                            onClick = { showAiDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartButton,
                                contentDescription = "AI Assistant",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Navigation Setup holding all Compose Screens
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        MainScreen(
                            onNavigateToChat = { techId, techName ->
                                navController.navigate("chat/$techId/$techName")
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            },
                            onNavigateToAdmin = {
                                navController.navigate("admin")
                            },
                            onLaunchAI = {
                                showAiDialog = true
                            }
                        )
                    }
                    composable("register") {
                        RegisterProviderScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("admin") {
                        AdminDashboardScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("about") {
                        AboutScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("backdoor") {
                        MainSettingsScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable(
                        route = "chat/{techId}/{techName}",
                        arguments = listOf(
                            navArgument("techId") { type = NavType.StringType },
                            navArgument("techName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val techId = backStackEntry.arguments?.getString("techId") ?: ""
                        val techName = backStackEntry.arguments?.getString("techName") ?: ""
                        ChatScreen(techId = techId, techName = techName, onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }

        // Elegant Admin authentication Dialog (Section 1 username and password checker)
        if (showAdminLogin) {
            Dialog(onDismissRequest = { showAdminLogin = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(320.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تسجيل دخول مشرف: الوجبة الأمنية لخدمات WAM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = it },
                            label = { Text("اسم المستخدم") },
                            modifier = Modifier.fillMaxWidth().testTag("admin_username_ui"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("كلمة المرور المشفرة") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_password_ui"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(onClick = { showAdminLogin = false }) {
                                Text("إلغاء")
                            }

                            Button(
                                onClick = {
                                    if (adminUsername == "WAM2026" && adminPassword == "maher736462") {
                                        showAdminLogin = false
                                        adminUsername = ""
                                        adminPassword = ""
                                        navController.navigate("admin")
                                        Toast.makeText(context, "🔓 تم تسجيل دخول المشرف بنجاح!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "❌ البيانات المدخلة غير مسموح لها بالوصول لوحة التحكم!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("admin_login_confirm_btn")
                            ) {
                                Text("دخول المعرض")
                            }
                        }
                    }
                }
            }
        }

        // Intelligent Offline + Online Assistant Dialog UI Window
        if (showAiDialog) {
            Dialog(onDismissRequest = { showAiDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "المساعد الفني الذكي ⚡ (يعمل أوفلاين/أونلاين)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Message bubble listings inside the dialog
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (aiConversation.isEmpty()) {
                                item {
                                    Text(
                                        text = "مرحباً بك! أنا مساعدك التخصصي لحل الأعطال المنزلية في اليمن. اسألني عن تصليح السباكة، الكهرباء، نجار لصيانة الأبواب أو تكاليف الكشف والزيارات.",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            } else {
                                items(aiConversation) { (msg, isMe) ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.widthIn(max = 240.dp)
                                        ) {
                                            Text(
                                                text = msg,
                                                modifier = Modifier.padding(10.dp),
                                                fontSize = 12.sp,
                                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Status loading
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.CenterHorizontally),
                                strokeWidth = 2.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Input control
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = aiQuery,
                                onValueChange = { aiQuery = it },
                                placeholder = { Text("اطرح سؤالك هنا...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(25.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (aiQuery.trim().isNotEmpty()) {
                                        scope.launch {
                                            val query = aiQuery.trim()
                                            aiConversation.add(Pair(query, true))
                                            aiQuery = ""
                                            isAiLoading = true

                                            val ans = SystemAiAssistant.askAssistant(query)
                                            aiConversation.add(Pair(ans, false))
                                            
                                            isAiLoading = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
