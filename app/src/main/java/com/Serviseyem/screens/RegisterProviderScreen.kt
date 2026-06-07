package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.ServiceProvider
import com.Serviseyem.services.FirebaseService

@Composable
fun RegisterProviderScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var nameVal by remember { mutableStateOf("") }
    var phoneVal by remember { mutableStateOf("") }
    var passwordVal by remember { mutableStateOf("") }
    var specialtyVal by remember { mutableStateOf("") }
    var residenceAddressVal by remember { mutableStateOf("") }
    var businessAddressVal by remember { mutableStateOf("") }
    var genderVal by remember { mutableStateOf("ذكر") }

    // Geographic Coordinates inputs for Maps
    var latitudeVal by remember { mutableStateOf("15.35") } // Default coordinates (e.g. Sana'a center)
    var longitudeVal by remember { mutableStateOf("44.20") }

    var isSaving by remember { mutableStateOf(false) }

    val citiesFromDb by FirebaseService.citiesList.collectAsState()
    val categoriesFromDb by FirebaseService.categoriesList.collectAsState()

    // Trigger loads to fetch active regions & sections dynamically
    LaunchedEffect(Unit) {
        FirebaseService.loadInitialCachedData()
        FirebaseService.initListeners()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Title Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "📝 طلب انضمام دليل مقدمي الخدمة",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Icon(
            imageVector = Icons.Default.Engineering,
            contentDescription = null,
            tint = Color(0xFFD4AF37),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "انضم إلى دليل WAM الفاخر وزد من زبائنك ومبيعاتك لحظياً!",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Form Fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                
                // Name Input
                Text("الأسم الثلاثي الفاخر:", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = nameVal,
                    onValueChange = { nameVal = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4AF37)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Input
                Text("رقم الهاتف الفوري (المكالمات):", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = phoneVal,
                    onValueChange = { phoneVal = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4AF37)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input
                Text("كلمة المرور الخاصة بلوحتك:", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = passwordVal,
                    onValueChange = { passwordVal = it },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4AF37)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selection specialty list or input
                Text("التخصص الفني أو المهني الرئيسي:", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = specialtyVal,
                    onValueChange = { specialtyVal = it },
                    placeholder = { Text("مثال: كهربائي منازل، سباكة، تعقيب معاملات", color = Color.Gray, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_specialty_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4AF37)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Residence Address dropdown / suggestions
                Text("عنوان السكن (المدينة / المنطقه):", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = residenceAddressVal,
                    onValueChange = { residenceAddressVal = it },
                    placeholder = { Text("مثال: صنعاء - حي الأصبحي", color = Color.Gray, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_residence_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4AF37)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Business Address
                Text("عنوان مقر العمل أو المحل (إن وجد):", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = businessAddressVal,
                    onValueChange = { businessAddressVal = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_business_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4AF37)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gender Selection
                Text("الجنس:", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).clickable { genderVal = "ذكر" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = genderVal == "ذكر", onClick = { genderVal = "ذكر" })
                        Text("ذكر", color = Color.White, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.weight(1f).clickable { genderVal = "أنثى" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = genderVal == "أنثى", onClick = { genderVal = "أنثى" })
                        Text("أنثى", color = Color.White, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Maps Coordinates inputs
                Text("📌 إحداثيات موقعك الجغرافي لدقة الخريطة (Google Maps):", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                        Text("خط العرض (Lat):", color = Color.White, fontSize = 10.sp)
                        OutlinedTextField(
                            value = latitudeVal,
                            onValueChange = { latitudeVal = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD4AF37)
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text("خط الطول (Lng):", color = Color.White, fontSize = 10.sp)
                        OutlinedTextField(
                            value = longitudeVal,
                            onValueChange = { longitudeVal = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD4AF37)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "* يمكنك جلب الإحداثيات ببساطة من تطبيق خرائط Google لزيادة دقة تصفح الزبائن ونظام الاتجاهات المباشر.",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSaving) {
            CircularProgressIndicator(color = Color(0xFFD4AF37))
        } else {
            Button(
                onClick = {
                    if (nameVal.trim().isEmpty() || phoneVal.trim().isEmpty() || passwordVal.trim().isEmpty() || specialtyVal.trim().isEmpty()) {
                        Toast.makeText(context, "الرجاء تعبئة الحقول الأساسية المطلوبة", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    
                    val lat = latitudeVal.toDoubleOrNull()
                    val lng = longitudeVal.toDoubleOrNull()

                    val provider = ServiceProvider(
                        id = "",
                        name = nameVal.trim(),
                        phone = phoneVal.trim(),
                        password = passwordVal.trim(),
                        specialty = specialtyVal.trim(),
                        residenceAddress = residenceAddressVal.trim(),
                        businessAddress = businessAddressVal.trim(),
                        status = "أنتظر الموافقة",
                        isVerified = false,
                        isVip = false,
                        gender = genderVal,
                        latitude = lat,
                        longitude = lng,
                        rating = 4.8
                    )

                    FirebaseService.saveProvider(provider, {
                        isSaving = false
                        Toast.makeText(context, "تم رفع الطلب بنجاح! يرجى التواصل مع الدعم على الرمز 777644670 للموافقة الفورية.", Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    }, {
                        isSaving = false
                        Toast.makeText(context, "فشل إرسال طلب الانضمام: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    })
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_registration_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إرسال طلب الانضمام الآن 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
