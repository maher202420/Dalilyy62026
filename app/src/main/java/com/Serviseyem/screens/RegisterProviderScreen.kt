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

    val categoriesFromDb by FirebaseService.categoriesList.collectAsState()
    val citiesFromDb by FirebaseService.citiesList.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Dynamic Selectors with Fallback
    val mainCategories = categoriesFromDb.filter { it.parentId.isEmpty() }.map { it.nameAr }
    val categoriesList = if (mainCategories.isNotEmpty()) mainCategories else listOf("كهربائي", "صيانة سباكة", "خدمات إلكترونية", "دعم فني", "عقارية وتجارية")
    
    val fallbackSubServices = mapOf(
        "كهربائي" to listOf("توصيل وتمديد شبكات", "صيانة الأجهزة المنزلية", "تركيب مصابيح وثريات", "أخرى"),
        "صيانة سباكة" to listOf("تأسيس سباكة متكاملة", "صيانة تسريبات المياه", "تركيب مضخات مياه", "أخرى"),
        "خدمات إلكترونية" to listOf("معاملات حكومية سريعة", "تصوير وتصميم إعلانات", "برمجة تطبيقات ومواقع", "أخرى"),
        "دعم فني" to listOf("صيانة كمبيوتر وهواتف", "تركيب شبكات وكاميرات", "دعم تقني فوري", "أخرى"),
        "عقارية وتجارية" to listOf("تسويق عقاري فاخر", "إدارة وتأجير المحلات", "إدارة وتطوير المشاريع", "أخرى")
    )

    var selectedCategory by remember { mutableStateOf(categoriesList.first()) }
    
    val currentSubServices = fallbackSubServices[selectedCategory] ?: listOf("أعمال عامة ومتنوعة", "خدمات VIP سريعة", "أخرى")
    var selectedServiceAr by remember { mutableStateOf(currentSubServices.first()) }

    val citiesNamesList = citiesFromDb.map { it.nameAr }
    val citiesList = if (citiesNamesList.isNotEmpty()) citiesNamesList else listOf("صنعاء", "عدن", "تعز", "حضرموت", "الحديدة")
    var selectedCity by remember { mutableStateOf(citiesList.first()) }
    var neighborhood by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var identityNumber by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    // Dialog Expand controllers
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var showCityDialog by remember { mutableStateOf(false) }

    // High Contrast color values for OutlinedTextField to solve invisible input text completely
    val highContrastTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color.LightGray,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("طلب انضمام مقدم خدمة 🛠️", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
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
                        "يرجى تسجيل بياناتك بشكل دقيق وحرية اختيار القسم والخدمة. سيراجع المشرفون طلبك ويقومون بوضعه في الدليل بشكل مباشر وفوري فور الاعتماد.",
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
                        .background(MaterialTheme.colorScheme.primary)
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

                    // 1. Dynamic Category Selector
                    Text("القسم المهني الرئيسي للعمل:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDialog = true }
                            .border(1.dp, Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCategory, color = Color.White, fontSize = 13.sp)
                            Text("تغيير ⚡", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Specialty/Service Selection Dropdown
                    Text("المهنة أو الخدمة الفرعية المحددة:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showServiceDialog = true }
                            .border(1.dp, Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedServiceAr, color = Color.White, fontSize = 13.sp)
                            Text("تغيير ⚡", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 3. City Selection Dropdown
                    Text("المدينة الرئيسية للخدمات:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCityDialog = true }
                            .border(1.dp, Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCity, color = Color.White, fontSize = 13.sp)
                            Text("تغيير ⚡", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 4. Neighborhood input
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        label = { Text("اسم الحي أو الشارع بالتفصيل (مثال: حي حدة / تقاطع الرقاص)") },
                        modifier = Modifier.fillMaxWidth().testTag("provider_neighborhood_input"),
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

                    // Fake Document Upload UI for Premium Visual appeal
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
                                Text("صورة شخصية 🧔", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, textAlign = TextAlign.Center)
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
                                Text("صورة الهوية 🪪", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, textAlign = TextAlign.Center)
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
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
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
                            if (name.isEmpty() || phone.isEmpty() || identityNumber.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "عذراً الرجاء إدخال كافة الحقول وتصوير المستندات", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!agreed) {
                                Toast.makeText(context, "الرجاء الموافقة على صحة البيانات المكتوبة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val provider = ServiceProvider(
                                id = phone,
                                name = name,
                                phone = phone,
                                specialty = "$selectedCategory - $selectedServiceAr ($selectedCity - $neighborhood)",
                                password = password,
                                identityNumber = identityNumber,
                                status = "أنتظر الموافقة"
                            )
                            FirebaseService.registerProvider(provider, {
                                Toast.makeText(context, "🎉 تم تسجيل طلبك بنجاح ونشره للمراجعة الفورية لدى المشرفين!", Toast.LENGTH_LONG).show()
                                onNavigateBack()
                            }, {
                                Toast.makeText(context, "عذراً فشل الاتصال بالإنترنت للتسجيل", Toast.LENGTH_SHORT).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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

    // Modal List Dialog for Main Categories
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("اختر القسم الرئيسي 🏷️", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesList.forEach { cat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = cat
                                    // Reset service selection matching category
                                    val matchedSubs = fallbackSubServices[cat] ?: listOf("أعمال عامة ومتنوعة", "خدمات VIP سريعة")
                                    selectedServiceAr = matchedSubs.first()
                                    showCategoryDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = if (selectedCategory == cat) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(cat, modifier = Modifier.padding(14.dp), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("إلغاء", color = Color.LightGray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal List Dialog for Sub Services
    if (showServiceDialog) {
        AlertDialog(
            onDismissRequest = { showServiceDialog = false },
            title = { Text("اختر نوع التخصص الفرعي 🛠️", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentSubServices.forEach { sub ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedServiceAr = sub
                                    showServiceDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = if (selectedServiceAr == sub) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(sub, modifier = Modifier.padding(14.dp), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showServiceDialog = false }) {
                    Text("إلغاء", color = Color.LightGray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal List Dialog for Cities
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("اختر المدينة يمنياً 📍", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    citiesList.forEach { city ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCity = city
                                    showCityDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = if (selectedCity == city) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(city, modifier = Modifier.padding(14.dp), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("إلغاء", color = Color.LightGray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
