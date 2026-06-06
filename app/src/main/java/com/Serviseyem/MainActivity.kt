package com.Serviseyem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.Serviseyem.screens.*
import com.Serviseyem.services.FirebaseService
import com.Serviseyem.services.SystemAiAssistant
import com.Serviseyem.ui.theme.WAMServicesTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge To Edge drawing for full-bleed content display
        enableEdgeToEdge()

        try {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:954106721012:android:5fa5e385532b08d5c0e4a1")
                .setApiKey("AIzaSyBq2SEhBADFGVF4sDyV3sC_t2HqQ1m8lC0")
                .setProjectId("serviseyem")
                .setStorageBucket("serviseyem.firebasestorage.app")
                .build()

            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Run the background AI listener for real-time Firebase chat automated processing
        SystemAiAssistant.startListeningForUserMessages()

        setContent {
            WAMServicesTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val settings by FirebaseService.settings.collectAsState()
                
                var showLoginDialog by remember { mutableStateOf(false) }
                
                // Login credentials state fields (high-contrast ready)
                var loginPhone by remember { mutableStateOf("") }
                var loginPass by remember { mutableStateOf("") }

                val highContrastTextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFD4AF37),
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
                    focusedLabelColor = Color(0xFFD4AF37),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFFD4AF37)
                )

                // --- Supervisor/Admin Login Dialog ---
                if (showLoginDialog) {
                    Dialog(onDismissRequest = { showLoginDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, Color(0xFFD4AF37))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Logo", tint = Color(0xFFD4AF37), modifier = Modifier.size(48.dp))
                                
                                Text(
                                    text = "تسجيل دخول مشرف: دليل WAM",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                
                                Text(
                                    text = "المزامنة سريعة وتلقائية بالكامل بين جميع الأجهزة.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )

                                OutlinedTextField(
                                    value = loginPhone,
                                    onValueChange = { loginPhone = it },
                                    label = { Text("اسم المشرف أو رقم الهاتف") },
                                    modifier = Modifier.fillMaxWidth().testTag("login_phone_input"),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = loginPass,
                                    onValueChange = { loginPass = it },
                                    label = { Text("رمز الدخول السري") },
                                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                                    visualTransformation = PasswordVisualTransformation(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (loginPhone.isEmpty() || loginPass.isEmpty()) {
                                                Toast.makeText(context, "يرجى ملء جميع الحقول أولاً", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            // Robust match supporting "admin"/"777644670" user with "123" master password
                                            val isMasterAdmin = (loginPhone.trim() == "admin" || loginPhone.trim() == "777644670") && loginPass.trim() == "123"
                                            val found = if (isMasterAdmin) {
                                                com.Serviseyem.models.SupervisorUser(
                                                    id = "default_wam",
                                                    phone = "777644670",
                                                    name = "المالك العام",
                                                    password = "123",
                                                    isApproved = true,
                                                    notes = "الدخول الافتراضي للمالك العام"
                                                )
                                            } else {
                                                FirebaseService.supervisorsList.value.firstOrNull {
                                                    it.phone == loginPhone.trim() && it.password == loginPass.trim()
                                                }
                                            }
                                            if (found != null) {
                                                FirebaseService.currentSupervisor = found
                                                showLoginDialog = false
                                                loginPhone = ""
                                                loginPass = ""
                                                Toast.makeText(context, "مرحباً بك مجدداً كمشرف: ${found.name} 👑", Toast.LENGTH_LONG).show()
                                                navController.navigate("admin")
                                            } else {
                                                Toast.makeText(context, "يرجى التحقق من صحة رقم الهاتف والمزامنة السحابية", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text("دخول سريع", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { 
                                            showLoginDialog = false
                                            loginPhone = ""
                                            loginPass = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("إلغاء", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Main NavHost with NO global double topBar Scaffold
                NavHost(
                    navController = navController, 
                    startDestination = "main",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("main") {
                        MainScreen(
                            onNavigateToRegister = { navController.navigate("register") },
                            onNavigateToChat = { navController.navigate("chat") },
                            onNavigateToAbout = { navController.navigate("about") },
                            onOpenLoginDialog = { showLoginDialog = true },
                            onNavigateToAdmin = { navController.navigate("admin") }
                        )
                    }
                    composable("register") {
                        RegisterProviderScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("admin") {
                        AdminDashboardScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("chat") {
                        ChatScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
