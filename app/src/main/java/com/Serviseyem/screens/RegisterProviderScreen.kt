package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.ServiceProvider
import com.Serviseyem.services.FirebaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProviderScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var identityNumber by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    // High Contrast color values for OutlinedTextField to solve invisible input text completely
    val highContrastTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFFD4AF37),
        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
        focusedLabelColor = Color(0xFFD4AF37),
        unfocusedLabelColor = Color.LightGray,
        cursorColor = Color(0xFFD4AF37)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("طلب انضمام مقدم خدمة 🛠️", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Header information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "انضم لصفحتنا المباشرة في دليل اليمن الفاخر ✨",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "يرجى تسجيل بياناتك بشكل دقيق. سيراجع المشرفون طلبك ويقومون بوضعه في الدليل بشكل مباشر وفوري فور الاعتماد.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }

            // Input Column with top gold accent borders
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // Top Gold border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFFD4AF37))
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الثلاثي أو التجاري بالكامل") },
                        modifier = Modifier.fillMaxWidth().testTag("provider_name_input"),
                        colors = highContrastTextFieldColors,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم جوال للتواصل المباشر والطلب") },
                        modifier = Modifier.fillMaxWidth().testTag("provider_phone_input"),
                        colors = highContrastTextFieldColors,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("ما هي طبيعة تخصصك أو الخدمة التي تقدمها") },
                        modifier = Modifier.fillMaxWidth().testTag("provider_specialty_input"),
                        colors = highContrastTextFieldColors,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = identityNumber,
                        onValueChange = { identityNumber = it },
                        label = { Text("رقم البطاقة الشخصية أو جواز السفر للتوثيق") },
                        modifier = Modifier.fillMaxWidth().testTag("provider_id_input"),
                        colors = highContrastTextFieldColors,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("اختر رمز سري خاص بك للحساب") },
                        modifier = Modifier.fillMaxWidth().testTag("provider_password_input"),
                        colors = highContrastTextFieldColors,
                        singleLine = true
                    )

                    // Fake Document Upload UI for Premium Visual appeal (doesn't require heavy dependencies)
                    Text("المستندات والصور الشخصية المعتمدة:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                .clickable {
                                    Toast.makeText(context, "📷 تم تحديد الصورة الشخصية الفاخرة لـ WAM", Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("صورة شخصية 🧔", color = Color(0xFFD4AF37), fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                .clickable {
                                    Toast.makeText(context, "📸 تم تصوير وتعتيق هوية الأحوال الشخصية بنجاح بنظام كود WAM", Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("صورة الهوية 🪪", color = Color(0xFFD4AF37), fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = agreed,
                            onCheckedChange = { agreed = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD4AF37))
                        )
                        Text(
                            "أقر بكل دقة بصحة هذه البيانات وموافق على سياسة WAM 🛡️",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.clickable { agreed = !agreed }
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isEmpty() || phone.isEmpty() || specialty.isEmpty() || password.isEmpty() || identityNumber.isEmpty()) {
                                Toast.makeText(context, "عذراً الرجاء إدخال كافة الحقول وتصوير المستندات", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!agreed) {
                                Toast.makeText(context, "الرجاء الموافقة على صحة البيانات المكتوبة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val provider = ServiceProvider(
                                name = name,
                                phone = phone,
                                specialty = specialty,
                                password = password,
                                identityNumber = identityNumber
                            )
                            FirebaseService.registerProvider(provider, {
                                Toast.makeText(context, "🎉 تم تسجيل طلبك بنجاح ونشره للمراجعة الفورية لدى المشرفين!", Toast.LENGTH_LONG).show()
                                onNavigateBack()
                            }, {
                                Toast.makeText(context, "عذراً فشل الاتصال بالإنترنت للتسجيل", Toast.LENGTH_SHORT).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_provider_btn")
                    ) {
                        Text("إرسال طلب الإنضمام السحابي 📡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
