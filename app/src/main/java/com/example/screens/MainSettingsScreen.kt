package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
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
import com.example.models.AppSettings
import com.example.services.FirebaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val settingsState = FirebaseService.settings.collectAsState()
    val settings = settingsState.value

    // Verification logic
    var isUnlocked by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }

    // Settings fields variables
    var appNameAr by remember { mutableStateOf(settings.appNameAr) }
    var appNameEn by remember { mutableStateOf(settings.appNameEn) }
    var primaryColor by remember { mutableStateOf(settings.primaryColor) }
    var secondaryColor by remember { mutableStateOf(settings.secondaryColor) }
    var bgCanvasColor by remember { mutableStateOf(settings.baseCanvasColor) }
    var footerText by remember { mutableStateOf(settings.footerText) }
    var welcomeMsg by remember { mutableStateOf(settings.welcomeMsg) }
    var supportPhone by remember { mutableStateOf(settings.supportPhone) }
    var supportEmail by remember { mutableStateOf(settings.supportEmail) }
    var supportWhatsapp by remember { mutableStateOf(settings.supportWhatsapp) }
    var updateUrl by remember { mutableStateOf(settings.updateUrl) }

    // Keep fields reactive to background settings modifications
    LaunchedEffect(settings) {
        appNameAr = settings.appNameAr
        appNameEn = settings.appNameEn
        primaryColor = settings.primaryColor
        secondaryColor = settings.secondaryColor
        bgCanvasColor = settings.baseCanvasColor
        footerText = settings.footerText
        welcomeMsg = settings.welcomeMsg
        supportPhone = settings.supportPhone
        supportEmail = settings.supportEmail
        supportWhatsapp = settings.supportWhatsapp
        updateUrl = settings.updateUrl
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        if (!isUnlocked) {
            // Unlock Door screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(320.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock icon",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "البوابة الخلفية الفاخرة السرية لمالك الدليل",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("رمز المرور السري المطلوب", color = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backdoor_pass_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color(0xFFFFD700),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                            ) {
                                Text("إلغاء للخلف")
                            }

                            Button(
                                onClick = {
                                    if (passwordInput == "maher--736462") {
                                        isUnlocked = true
                                        Toast.makeText(context, "🔓 مرحباً يا ماهر! تم تفعيل الصلاحية المطلقة بنجاح", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "❌ الرمز المدخل غير معتمد للمالك السري", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("backdoor_unlock_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                            ) {
                                Text("فتح الإعدادات")
                            }
                        }
                    }
                }
            }
        } else {
            // Unlocked Configuration sheets
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Settings Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "صيانة الألوان والمستندات السرية للمالك",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title section App Name
                    Text("أولاً: تسمية ورسالة واجهة التطبيق:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = appNameAr,
                        onValueChange = { appNameAr = it },
                        label = { Text("اسم التطبيق بالعربية (WAM Services)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = welcomeMsg,
                        onValueChange = { welcomeMsg = it },
                        label = { Text("رسالة ترحيب الشريط العلوي الرئيسي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Color Schemes config (Gold/Silver/Emerald)
                    Text("ثانياً: ألوان الهوية الفاخرة (صيغة Hex):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = primaryColor,
                        onValueChange = { primaryColor = it },
                        label = { Text("اللون اللامع الأساسي (مثال: #FFD700 للذهبي الفاتن)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = secondaryColor,
                        onValueChange = { secondaryColor = it },
                        label = { Text("اللون اللامع الثانوي (مثال: #1B4D3E للزمرد الأخضر)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bgCanvasColor,
                        onValueChange = { bgCanvasColor = it },
                        label = { Text("لون لوحة الخلفية الكلي (الوضع المظلم)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Footer trademark modification
                    Text("ثالثاً: تذييل الصفحة وحقوق الفنيين:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = footerText,
                        onValueChange = { footerText = it },
                        label = { Text("نص التذييل المركزي بالكامل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Support lines change
                    Text("رابعاً: أرقام التواصل والدعم الفني المباشر:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = supportPhone,
                        onValueChange = { supportPhone = it },
                        label = { Text("رقم هاتف الدعم المركزي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supportWhatsapp,
                        onValueChange = { supportWhatsapp = it },
                        label = { Text("رقم واتساب المعتمد") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supportEmail,
                        onValueChange = { supportEmail = it },
                        label = { Text("البريد الإلكتروني المعتمد للدعم") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Direct Update link URL
                    Text("خامساً: عناوين وتحديثات البرنامج الفورية:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = updateUrl,
                        onValueChange = { updateUrl = it },
                        label = { Text("رابط تنزيل وتثبيت آخر نسخة جديدة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Global Save triggers
                    Button(
                        onClick = {
                            val newSet = AppSettings(
                                appNameAr = appNameAr,
                                appNameEn = appNameEn,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                baseCanvasColor = bgCanvasColor,
                                footerText = footerText,
                                welcomeMsg = welcomeMsg,
                                supportPhone = supportPhone,
                                supportEmail = supportEmail,
                                supportWhatsapp = supportWhatsapp,
                                updateUrl = updateUrl
                            )
                            FirebaseService.saveSettings(newSet)
                            Toast.makeText(context, "💾 تم تحديث الإعدادات وحفظها في Firestore ومزامنة كل الأجهزة فوراً!", Toast.LENGTH_LONG).show()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_backdoor_settings"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ وتحديث كل الفروع فوراً", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
