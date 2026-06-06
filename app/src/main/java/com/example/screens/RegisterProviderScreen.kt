package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Category
import com.example.models.PendingProvider
import com.example.services.FirebaseService
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProviderScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val categoriesState = FirebaseService.categories.collectAsState()
    val citiesState = FirebaseService.cities.collectAsState()

    // Form states
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var workAddress by remember { mutableStateOf("") }
    
    // Dropdown selectors
    val parentCats = categoriesState.value.filter { !it.isSubCategory }
    var selectedParentCat by remember { mutableStateOf<Category?>(null) }
    var selectedSubCat by remember { mutableStateOf<Category?>(null) }
    var selectedCity by remember { mutableStateOf("") }

    var expandedParent by remember { mutableStateOf(false) }
    var expandedSub by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }

    // Upload mocks
    var personalPhotoPath by remember { mutableStateOf("") }
    var idCardPhotoPath by remember { mutableStateOf("") }
    var isFemalePhotoOpt by remember { mutableStateOf(false) }

    val isFormValid = name.count { it == ' ' } >= 2 && 
            phone.length >= 9 && 
            workAddress.isNotEmpty() && 
            selectedParentCat != null && 
            selectedCity.isNotEmpty() && 
            (personalPhotoPath.isNotEmpty() || isFemalePhotoOpt)

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "سجل كمزود خدمة محترف 🛠️",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "انضم إلى دليل النخبة لـ WAM واكسب آلاف العملاء شهرياً مع مزامنة فورية ونقاط ولاء حقيقية.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Full Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم واللقب الثلاثي (إجباري)") },
                placeholder = { Text("مثال: ماهر محمد طاهر") },
                isError = name.isNotEmpty() && name.count { it == ' ' } < 2,
                supportingText = {
                    if (name.isNotEmpty() && name.count { it == ' ' } < 2) {
                        Text("الرجاء كتابة الاسم واللقب الثلاثي للامتثال الأمني")
                    } else {
                        Text("ادخل اسمك الرسمي المعتمد")
                    }
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tech_name_input")
                    .padding(bottom = 12.dp)
            )

            // Phone Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف اليمني النشط (إجباري)") },
                placeholder = { Text("مثال: 777644670") },
                isError = phone.isNotEmpty() && phone.length < 9,
                supportingText = {
                    if (phone.isNotEmpty() && phone.length < 9) {
                        Text("رقم الهاتف غير صالح")
                    }
                },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tech_phone_input")
                    .padding(bottom = 12.dp)
            )

            // Parent Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedParent,
                onExpandedChange = { expandedParent = !expandedParent },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = selectedParentCat?.nameAr ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("اختر التخصص الرئيسي للخدمة (إجباري)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedParent) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedParent,
                    onDismissRequest = { expandedParent = false }
                ) {
                    parentCats.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.nameAr) },
                            onClick = {
                                selectedParentCat = cat
                                selectedSubCat = null // reset sub-category upon change
                                expandedParent = false
                            }
                        )
                    }
                }
            }

            // Sub Category Dropdown
            if (selectedParentCat != null) {
                val subCats = categoriesState.value.filter { it.isSubCategory && it.parentId == selectedParentCat!!.id }
                if (subCats.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedSub,
                        onExpandedChange = { expandedSub = !expandedSub },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedSubCat?.nameAr ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("حدد الخدمة الفرعية التخصصية (إجباري)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSub) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSub,
                            onDismissRequest = { expandedSub = false }
                        ) {
                            subCats.forEach { sCat ->
                                DropdownMenuItem(
                                    text = { Text(sCat.nameAr) },
                                    onClick = {
                                        selectedSubCat = sCat
                                        expandedSub = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // City Governorates Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCity,
                onExpandedChange = { expandedCity = !expandedCity },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = selectedCity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("اختر منطقة السكن أو المحافظة (إجباري)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedCity,
                    onDismissRequest = { expandedCity = false }
                ) {
                    citiesState.value.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                selectedCity = city
                                expandedCity = false
                            }
                        )
                    }
                }
            }

            // Work Location detailed text
            OutlinedTextField(
                value = workAddress,
                onValueChange = { workAddress = it },
                label = { Text("عنوان مركز العمل / المحل بالتفصيل (إجباري)") },
                placeholder = { Text("مثال: صنعاء، شارع الستين الغربي بجوار الدائري") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Gender Photo toggle Option (for females → option to upload work/craft representative photo)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Checkbox(
                    checked = isFemalePhotoOpt,
                    onCheckedChange = { isFemalePhotoOpt = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("التسجيل كفتاة (رفع صورة مهنة ممثلة عِوضاً عن الصورة الشخصية)")
            }

            // Photo upload Mock interfaces
            Text(
                text = "مرفقات الهوية والصور الفنية",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Personal Photo Box
                Card(
                    onClick = {
                        // Mock photoselect camera simulation
                        personalPhotoPath = "https://images.unsplash.com/photo-1570724061671-b0db66c0d0fe?w=200&auto=format&fit=crop"
                        Toast.makeText(context, "📸 تم التقاط وحمل الصورة الشخصية بنجاح من وضع الكاميرا الذاتي", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .border(
                            1.dp,
                            if (personalPhotoPath.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFFD4AF37))
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (personalPhotoPath.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                            contentDescription = "Avatar Upload",
                            tint = if (personalPhotoPath.isNotEmpty()) Color.Green else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFemalePhotoOpt) "صورة بطاقة المهنة" else "صورة شخصية (إجباري)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // National ID card Upload
                Card(
                    onClick = {
                        idCardPhotoPath = "https://images.unsplash.com/photo-1554774853-aae0a22c8aa4?w=200&auto=format&fit=crop"
                        Toast.makeText(context, "📸 تم مسح بطاقة الهوية الذكية وحفظها بنجاح للمراجعة", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .border(
                            1.dp,
                            if (idCardPhotoPath.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFFD4AF37))
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (idCardPhotoPath.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.CreditCard,
                            contentDescription = "ID Card Upload",
                            tint = if (idCardPhotoPath.isNotEmpty()) Color.Green else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "صورة بطاقة الهوية (اختياري)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (isFormValid) {
                        val newPending = PendingProvider(
                            id = "req_" + UUID.randomUUID().toString().take(6),
                            nameAr = name,
                            phone = phone,
                            categoryId = selectedParentCat?.id ?: "",
                            subCategoryId = selectedSubCat?.id ?: "",
                            workAddress = workAddress,
                            residenceAr = selectedCity,
                            avatarUrl = personalPhotoPath,
                            idCardUrl = idCardPhotoPath
                        )
                        FirebaseService.requestRegistration(newPending)
                        Toast.makeText(context, "✅ تم رفع طلبك بنجاح للأدمن وسيتم تفعيله وتوثيقه خلال دقائق!", Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "الرجاء تعبئة كافة الحقول الإجبارية بشكل صحيح", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_reg_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(27.dp),
                enabled = isFormValid
            ) {
                Text(
                    text = "تقديم طلب الانضمام والتوثيق 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
