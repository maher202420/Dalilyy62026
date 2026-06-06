package com.Serviseyem.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.SupervisorUser
import com.Serviseyem.services.FirebaseService

@Composable
fun WamTopBar(
    titleText: String,
    onNavigateHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by FirebaseService.settings.collectAsState()

    var showBackdoorDialog by remember { mutableStateOf(false) }
    var backdoorPasswordInput by remember { mutableStateOf("") }
    var rememberBackdoorPassword by remember { mutableStateOf(false) }

    var showNormalLoginDialog by remember { mutableStateOf(false) }
    var normalUsernameInput by remember { mutableStateOf("") }
    var normalPasswordInput by remember { mutableStateOf("") }

    var homeClickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }

    // Dynamic Theming Colors
    val primaryColor = MaterialTheme.colorScheme.primary

    // Custom Top Bar layout (Edge-to-edge content, RTL Icons arrangement)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Centered/Left branding title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Logo",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = titleText.ifEmpty { settings.appNameAr },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 5 Ordered Icons (RTL flow support: Home, Login, Register, Language, Refresh)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. 🏠 Home Icon with backdoor action
                    IconButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime < 1000) {
                                homeClickCount++
                            } else {
                                homeClickCount = 1
                            }
                            lastClickTime = now

                            if (homeClickCount >= 5) {
                                homeClickCount = 0
                                showBackdoorDialog = true
                            } else {
                                onNavigateHome()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 2. 🔐 Login Lock Icon
                    IconButton(onClick = { showNormalLoginDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = "Login",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 3. 👤 Register Icon
                    IconButton(onClick = onNavigateToRegister) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Register",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 4. 🌐 Globe/Language Icon
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "البرنامج مفعل باللغة العربية كـ لغة افتراضية للوحة التحكم الذكية WAM 🌐", Toast.LENGTH_LONG).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 5. 🔄 Refresh Synced Data Icon
                    IconButton(
                        onClick = {
                            FirebaseService.startRealtimeSynchronization()
                            onRefresh()
                            Toast.makeText(context, "تم مزامنة وتحديث صفحات دليل خدمات WAM سحابياً الآن 🟢", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Underline luxury accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(primaryColor)
            )
        }
    }

    // --- Backdoor Owner Dialog ---
    if (showBackdoorDialog) {
        AlertDialog(
            onDismissRequest = { showBackdoorDialog = false },
            title = {
                Text(
                    text = "👑 البوابة السحابية الخلفية للمالك",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "الرجاء كتم السرية وإدخال رمز الوصول الخلفي للتحكم بالهوية والهيكل الكامل للتطبيق سحابياً:",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = backdoorPasswordInput,
                        onValueChange = { backdoorPasswordInput = it },
                        label = { Text("رمز المرور الخلفي") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberBackdoorPassword = !rememberBackdoorPassword }
                    ) {
                        Checkbox(
                            checked = rememberBackdoorPassword,
                            onCheckedChange = { rememberBackdoorPassword = it },
                            colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                        )
                        Text("حفظ رمز الدخول وتسجيل الدخول تلقائياً للمالك", color = Color.White, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (backdoorPasswordInput == "maher--736462") {
                            FirebaseService.isBackdoorLoggedIn = true
                            FirebaseService.currentSupervisor = SupervisorUser(
                                id = "master_owner",
                                phone = "777644670",
                                name = "المالك العام (بوابة خلفية)",
                                password = "123",
                                isApproved = true,
                                notes = "دخول خلفي آمن"
                            )
                            Toast.makeText(context, "أهلاً بك يا مالك التطبيق 👑! تم فتح كامل الصلاحيات الإدارية والسرية سحابياً.", Toast.LENGTH_LONG).show()
                            showBackdoorDialog = false
                            backdoorPasswordInput = ""
                            onNavigateToAdmin()
                        } else {
                            Toast.makeText(context, "الرمز السري غير صحيح ❌ أعد المحاولة بكتابة الرمز الصحيح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("فتح البوابة الخلفية", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackdoorDialog = false }) {
                    Text("إلغاء", color = Color.LightGray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Normal Admin/Supervisor Login Dialog ---
    if (showNormalLoginDialog) {
        AlertDialog(
            onDismissRequest = { showNormalLoginDialog = false },
            title = {
                Text(
                    text = "🔐 بوابة تسجيل دخول المشرفين والمدراء",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل اسم المستخدم (رقم الهاتف) وكلمة المرور الخاصة بك للوصول إلى لوحة الإدارة والمزامنة السحابية:",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = normalUsernameInput,
                        onValueChange = { normalUsernameInput = it },
                        label = { Text("اسم المستخدم/الهاتف") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    OutlinedTextField(
                        value = normalPasswordInput,
                        onValueChange = { normalPasswordInput = it },
                        label = { Text("كلمة المرور") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedUser = normalUsernameInput.trim()
                        val trimmedPass = normalPasswordInput.trim()

                        // Verify against supervisors list in firestore settings
                        val isMainAdmin = trimmedUser == "WAM2026" && trimmedPass == settings.adminPassword
                        
                        // Check if any supervisor from firestore matches
                        val list = FirebaseService.supervisorsList.value
                        val matchedSup = list.find { it.phone == trimmedUser && it.password == trimmedPass }

                        if (isMainAdmin || matchedSup != null) {
                            val finalSupervisor = matchedSup ?: SupervisorUser(
                                id = "wam_main_admin",
                                phone = "WAM2026",
                                name = "المدير الرئيسي لـ WAM",
                                password = settings.adminPassword,
                                isApproved = true,
                                notes = "المدير الرئيسي العام للتحكم سحابياً"
                            )
                            FirebaseService.currentSupervisor = finalSupervisor
                            FirebaseService.isBackdoorLoggedIn = isMainAdmin || (trimmedUser == "777644670" && trimmedPass == "123")
                            Toast.makeText(context, "أهلاً بك ${finalSupervisor.name}! تم تسجيل الدخول لوحة العمل بنجاح 👑", Toast.LENGTH_LONG).show()
                            showNormalLoginDialog = false
                            normalUsernameInput = ""
                            normalPasswordInput = ""
                            onNavigateToAdmin()
                        } else {
                            Toast.makeText(context, "البيانات المدخلة غير صحيحة ❌ المرجو التأكد من كلمة مرور المشرفين.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("تسجيل دخول", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNormalLoginDialog = false }) {
                    Text("إغاء", color = Color.LightGray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }
}


