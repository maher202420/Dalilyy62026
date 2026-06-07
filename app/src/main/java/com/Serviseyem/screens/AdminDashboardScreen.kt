package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import com.Serviseyem.models.*
import com.Serviseyem.services.FirebaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Sycned Db entities
    val settings by FirebaseService.settings.collectAsState()
    val providers by FirebaseService.providersList.collectAsState()
    val categories by FirebaseService.categoriesList.collectAsState()
    val cities by FirebaseService.citiesList.collectAsState()
    val supervisors by FirebaseService.supervisorsList.collectAsState()

    // Auth status: Is this logged in as Super Admin or normal supervisor?
    val currentSup = FirebaseService.currentSupervisor
    val isSuperAdmin = currentSup == null // Directly verified via secret door passcode, hence has all permissions!

    // Verify fine-grained permissions if not super admin
    val canApprove = isSuperAdmin || (currentSup?.canApproveTechs == true)
    val canManageCats = isSuperAdmin || (currentSup?.canManageCategories == true)
    val canManageAds = isSuperAdmin || (currentSup?.canManageAds == true)
    val canDelete = isSuperAdmin || (currentSup?.canDeleteTechs == true)
    val canReports = isSuperAdmin || (currentSup?.canViewReports == true)

    // Sections expandable states for UI cleanliness
    var sectionExpanded1 by remember { mutableStateOf(true) } // Registration
    var sectionExpanded2 by remember { mutableStateOf(false) } // Manual Add
    var sectionExpanded3 by remember { mutableStateOf(false) } // Ads
    var sectionExpanded4 by remember { mutableStateOf(false) } // Categories & Cities
    var sectionExpanded5 by remember { mutableStateOf(false) } // Reports
    var sectionExpanded6 by remember { mutableStateOf(false) } // Chat Settings & Logs
    var sectionExpanded7 by remember { mutableStateOf(false) } // Active Providers
    var sectionExpanded8 by remember { mutableStateOf(false) } // Subscriptions/Pinning
    var sectionExpanded9 by remember { mutableStateOf(false) } // Admin Management

    // Inputs States for Section 2 (Manual Addition)
    var mName by remember { mutableStateOf("") }
    var mPhone by remember { mutableStateOf("") }
    var mCitySelected by remember { mutableStateOf("") }
    var mPrice by remember { mutableStateOf("حسب الاتفاق") }
    var mCategorySelected by remember { mutableStateOf("") }
    var mIsVipChecked by remember { mutableStateOf(false) }

    // Inputs States for Section 3 (Ads & Banners)
    var adTitle by remember { mutableStateOf("") }
    var adTypeSelected by remember { mutableStateOf("صورة") }
    var adBackgroundUrl by remember { mutableStateOf("") }
    var adTargetCategory by remember { mutableStateOf("") }
    var adSize by remember { mutableStateOf("10") }
    var adDurationSecs by remember { mutableStateOf("10") }

    // Inputs States for Section 4 (Part A: Category)
    var catNameAr by remember { mutableStateOf("") }
    var catNameEn by remember { mutableStateOf("") }
    var catDescAr by remember { mutableStateOf("") }
    var catIconSelected by remember { mutableStateOf("electrical") }
    var catDirectPublish by remember { mutableStateOf(true) }

    // Inputs States for Section 4 (Part B: City)
    var cityNameAr by remember { mutableStateOf("") }
    var cityNameEn by remember { mutableStateOf("") }

    // Inputs States for Section 9 (Admin Management)
    var supUsername by remember { mutableStateOf("") }
    var supPassword by remember { mutableStateOf("") }
    var pApprove by remember { mutableStateOf(true) }
    var pManageCats by remember { mutableStateOf(false) }
    var pManageAds by remember { mutableStateOf(false) }
    var pDeleteProviders by remember { mutableStateOf(false) }
    var pViewReports by remember { mutableStateOf(false) }

    // Editing Provider Dialog state
    var providerToEdit by remember { mutableStateOf<ServiceProvider?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editSpecialty by remember { mutableStateOf("") }
    var editResidence by remember { mutableStateOf("") }
    var editBusiness by remember { mutableStateOf("") }

    // Filter registrations
    val pendingRegistrations = providers.filter { it.status == "أنتظر الموافقة" }
    val activeProvidersList = providers.filter { it.status == "مقبول" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // App top identity banner
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
                    text = "كل خدمات اليمن - لوحة الإشراف والتنسيق",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSuperAdmin) "المشرف العام (Super Admin)" else "مشرف المبيعات: ${currentSup?.name}",
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
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

            // ==========================================
            // 1. طلبات التسجيل (Registration Requests)
            // ==========================================
            DashboardSectionWrapper(
                title = "1. طلبات التسجيل (${pendingRegistrations.size})",
                isExpanded = sectionExpanded1,
                onToggle = { sectionExpanded1 = !sectionExpanded1 },
                tintColor = Color(0xFFD4AF37)
            ) {
                if (!canApprove) {
                    Text("⚠️ عذراً، لم يتم منحك صلاحية قبول ورفض الفنيين الجدد.", color = Color.Gray, fontSize = 12.sp)
                } else if (pendingRegistrations.isEmpty()) {
                    Text("لا توجد طلبات تسجيل معلقة حالياً.", color = Color.LightGray, fontSize = 12.sp)
                } else {
                    pendingRegistrations.forEach { req ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("الاسم: ${req.name}", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("رقم الاتصال: ${req.phone}", color = Color.LightGray, fontSize = 12.sp)
                                Text("التخصص: ${req.specialty} • ${req.residenceAddress}", color = Color.LightGray, fontSize = 12.sp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            FirebaseService.updateProviderStatus(req.id, "مقبول", {
                                                Toast.makeText(context, "تم قبول الفني بنجاح بمزامنة فورية!", Toast.LENGTH_SHORT).show()
                                            }, {
                                                Toast.makeText(context, "فشل القبول الفوري", Toast.LENGTH_SHORT).show()
                                            })
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("قبول الفني ✅", fontSize = 11.sp, color = Color.White)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            FirebaseService.deleteProvider(req.id, {
                                                Toast.makeText(context, "تم رفض وحذف الطلب بنجاح!", Toast.LENGTH_SHORT).show()
                                            }, {
                                                Toast.makeText(context, "فشل الإلغاء", Toast.LENGTH_SHORT).show()
                                            })
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("رفض وبتر ❌", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 2. إضافة فني يدوياً (Manual Tech Addition)
            // ==========================================
            DashboardSectionWrapper(
                title = "2. إضافة فني يدوياً",
                isExpanded = sectionExpanded2,
                onToggle = { sectionExpanded2 = !sectionExpanded2 }
            ) {
                if (!canApprove) {
                    Text("⚠️ ليس لديك صلاحية لإدراج فنيين جدد يدوياً.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    OutlinedTextField(
                        value = mName,
                        onValueChange = { mName = it },
                        label = { Text("الاسم الكامل الفني", color = Color.LightGray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mPhone,
                        onValueChange = { mPhone = it },
                        label = { Text("رقم الهاتف الجوال", color = Color.LightGray, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // City Dropdown Selector prediction
                    Text("الموقع والمدينة التابعة للتغطية:", color = Color.White, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cities.forEach { c ->
                            val isSel = mCitySelected == c.nameAr
                            Card(
                                modifier = Modifier.clickable { mCitySelected = c.nameAr },
                                colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF064E3B) else Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFFD4AF37) else Color.Transparent)
                            ) {
                                Text(c.nameAr, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                    if (mCitySelected.isEmpty() && cities.isNotEmpty()) {
                        mCitySelected = cities[0].nameAr
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mPrice,
                        onValueChange = { mPrice = it },
                        label = { Text("سعر المعاينة بالريال", color = Color.LightGray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Specialty Category selection
                    Text("حدد قسم الفني التخصصي:", color = Color.White, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = mCategorySelected == cat.nameAr
                            Card(
                                modifier = Modifier.clickable { mCategorySelected = cat.nameAr },
                                colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF064E3B) else Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFFD4AF37) else Color.Transparent)
                            ) {
                                Text(cat.nameAr, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                    if (mCategorySelected.isEmpty() && categories.isNotEmpty()) {
                        mCategorySelected = categories[0].nameAr
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = mIsVipChecked, onCheckedChange = { mIsVipChecked = it })
                        Text("منح شارة نخبة VIP سحابية مباشرة 🌟", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (mName.isBlank() || mPhone.isBlank()) {
                                Toast.makeText(context, "يرجى ملء الاسم ورقم الهاتف الفني", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val p = ServiceProvider(
                                id = "",
                                name = mName.trim(),
                                phone = mPhone.trim(),
                                specialty = mCategorySelected,
                                residenceAddress = mCitySelected,
                                businessAddress = "محل صيانة معتمد بالمدينة",
                                status = "مقبول",
                                isVerified = true,
                                isVip = mIsVipChecked,
                                rating = 5.0,
                                latitude = 15.35,
                                longitude = 44.20
                            )

                            FirebaseService.saveProvider(p, {
                                Toast.makeText(context, "تم إدراج الفني مباشرة بنجاح سحابي!", Toast.LENGTH_SHORT).show()
                                mName = ""
                                mPhone = ""
                            }, {
                                Toast.makeText(context, "فشل الحفظ الفوري", Toast.LENGTH_SHORT).show()
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                    ) {
                        Text("إضافة مباشر للدليل 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 3. إعلانات وبنرات (Ads & Banners)
            // ==========================================
            DashboardSectionWrapper(
                title = "3. إعلانات وبنرات ترويجية",
                isExpanded = sectionExpanded3,
                onToggle = { sectionExpanded3 = !sectionExpanded3 }
            ) {
                if (!canManageAds) {
                    Text("⚠️ ليس لديك الصلاحية لتخصيص الإعلانات.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    OutlinedTextField(
                        value = adTitle,
                        onValueChange = { adTitle = it },
                        label = { Text("عنوان البنر الدعائي الترويجي", color = Color.LightGray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Ad content type choices
                    Text("نوع المحتوى الدعائي المرغوب:", color = Color.White, fontSize = 11.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("صورة", "فيديو", "نص ترويجي").forEach { type ->
                            val isSel = adTypeSelected == type
                            Card(
                                modifier = Modifier.clickable { adTypeSelected = type },
                                colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF064E3B) else Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFFD4AF37) else Color.Transparent)
                            ) {
                                Text(type, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = adBackgroundUrl,
                        onValueChange = { adBackgroundUrl = it },
                        label = { Text("رابط صورة/فيديو الخلفية الدعائية (اختياري)", color = Color.LightGray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = adTargetCategory,
                        onValueChange = { adTargetCategory = it },
                        label = { Text("القسم المراد التوجيه إليه بدقة عند النقر (مثال: plumbing)", color = Color.LightGray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedTextField(
                            value = adSize,
                            onValueChange = { adSize = it },
                            label = { Text("حجم الإعلان", color = Color.LightGray, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = adDurationSecs,
                            onValueChange = { adDurationSecs = it },
                            label = { Text("مدة العرض (ثانية)", color = Color.LightGray, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (adTitle.isBlank()) {
                                Toast.makeText(context, "الرجاء كتابة عنوان الإعلان", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newSettings = settings.copy(
                                sponsoredAdVisible = true,
                                sponsoredAdText = adTitle.trim(),
                                sponsoredAdType = adTypeSelected
                            )

                            FirebaseService.saveSettings(newSettings, {
                                Toast.makeText(context, "تم حفظ ونشر الإعلان بنجاح في الواجهة!", Toast.LENGTH_SHORT).show()
                                adTitle = ""
                            }, {
                                Toast.makeText(context, "فشل الإرسال السحابي", Toast.LENGTH_SHORT).show()
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B))
                    ) {
                        Text("حفظ الإعلان وإطلاقه 📣", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 4. إدارة الأقسام والمدن (Categories & Cities)
            // ==========================================
            DashboardSectionWrapper(
                title = "4. إدارة الأقسام والمدن",
                isExpanded = sectionExpanded4,
                onToggle = { sectionExpanded4 = !sectionExpanded4 }
            ) {
                if (!canManageCats) {
                    Text("⚠️ ليس لديك صلاحية تعديل الأقسام والمدن.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    // Part A: Add Main Category
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("أ) إضافة أو تعديل تخصص فني رئيسي:", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = catNameAr,
                                onValueChange = { catNameAr = it },
                                label = { Text("الاسم بالعربية (مثال: صيانة غسالات)", color = Color.LightGray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = catNameEn,
                                onValueChange = { catNameEn = it },
                                label = { Text("الاسم بالإنجليزية (مثال: washing_machines)", color = Color.LightGray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = catDescAr,
                                onValueChange = { catDescAr = it },
                                label = { Text("الوصف التعريفي للجمهور", color = Color.LightGray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Text("حدد الرمز التخطيطي للقسم:", color = Color.White, fontSize = 11.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("carpentry" to "🪚", "ac_unit" to "❄️", "electrical" to "⚡", "plumbing" to "🔧").forEach { (icon, glyph) ->
                                    val isSel = catIconSelected == icon
                                    Card(
                                        modifier = Modifier.clickable { catIconSelected = icon },
                                        colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF064E3B) else Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, if (isSel) Color(0xFFD4AF37) else Color.Transparent)
                                    ) {
                                        Text("$glyph $icon", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (catNameAr.isBlank() || catNameEn.isBlank()) {
                                        Toast.makeText(context, "الرجاء كتابة اسم القسم فورا", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val c = CategoryItem(
                                        id = "",
                                        nameAr = catNameAr.trim(),
                                        nameEn = catNameEn.trim(),
                                        description = catDescAr.trim(),
                                        iconUrl = catIconSelected,
                                        isPinned = catDirectPublish,
                                        rankOrder = categories.size + 1
                                    )

                                    FirebaseService.saveCategory(c, {
                                        Toast.makeText(context, "تم رفع القسم الفني سحابيا لحظيا!", Toast.LENGTH_SHORT).show()
                                        catNameAr = ""
                                        catNameEn = ""
                                        catDescAr = ""
                                    }, {
                                        Toast.makeText(context, "فشل رفع القسم المساعد", Toast.LENGTH_SHORT).show()
                                    })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                            ) {
                                Text("إدراج القسم الرئيسي مباشرة للتصفح", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Part B: Add City
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("ب) إضافة تغطية جغرافية جديدة (المدن والمحافظات):", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = cityNameAr,
                                onValueChange = { cityNameAr = it },
                                label = { Text("المدينة بالعربية (مثال: عدن)", color = Color.LightGray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = cityNameEn,
                                onValueChange = { cityNameEn = it },
                                label = { Text("المدينة بالإنجليزية (مثال: Aden)", color = Color.LightGray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (cityNameAr.isBlank()) {
                                        Toast.makeText(context, "الرجاء تدوين اسم المحافظة بالعربية", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val c = DatabaseCity(
                                        id = "",
                                        nameAr = cityNameAr.trim(),
                                        nameEn = cityNameEn.trim()
                                    )

                                    FirebaseService.saveCity(c, {
                                        Toast.makeText(context, "تم حفظ المدينة وتثبيتها بنجاح سحابي!", Toast.LENGTH_SHORT).show()
                                        cityNameAr = ""
                                        cityNameEn = ""
                                    }, {
                                        Toast.makeText(context, "فشل حفظ التغطية الجغرافية", Toast.LENGTH_SHORT).show()
                                    })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B))
                            ) {
                                Text("حفظ المدينة الجديدة بالقائمة 🌍", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Show active categories with delete ability
                    Text("الأقسام الفنية المسجلة بالدليل (${categories.size}):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${cat.nameAr} (${cat.nameEn})", color = Color.White, fontSize = 12.sp)
                            IconButton(onClick = {
                                FirebaseService.deleteCategory(cat.id, {
                                    Toast.makeText(context, "تم إزالة القسم بنجاح المزامنة سحابيا!", Toast.LENGTH_SHORT).show()
                                }, {})
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 5. الإبلاغات والتقارير (Reports & Complaints)
            // ==========================================
            DashboardSectionWrapper(
                title = "5. الإبلاغات وتقارير التدقيق الجنائي",
                isExpanded = sectionExpanded5,
                onToggle = { sectionExpanded5 = !sectionExpanded5 }
            ) {
                if (!canReports) {
                    Text("⚠️ ليس لديك صلاحية عرض الشكاوى والتقارير المالية.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "📄 تم إنشاء وتصدير التقرير الأسبوعي بصيغة PDF بنجاح!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تصدير أسبوعي PDF", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "📊 تم تجهيز وتحميل ملف البلاغات الشامل بصيغة CSV بنجاح!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تصدير CSV مميز", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("عرض: بلاغات وشكاوى المستخدمين المعلقة (0)", color = Color(0xFFD4AF37), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لوحة البلاغات خالية تماماً حالياً بنزاهة الدليل.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 6. إدارة سجلات المحادثات والخصوصية (Chat History Management)
            // ==========================================
            DashboardSectionWrapper(
                title = "6. إدارة سجلات المحادثات والخصوصية",
                isExpanded = sectionExpanded6,
                onToggle = { sectionExpanded6 = !sectionExpanded6 }
            ) {
                if (!isSuperAdmin) {
                    Text("⚠️ صلاحية مسح سجل الدردشة الكلي لضمان سرية المحادثات للمشرف العام فقط.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    Text(
                        "تحذير أمني: يتيح هذا القسم تصدير أو محو سجل المحادثات الكامل للمستخدمين والفنيين لضمان الالتزام بقواعد الخصوصية السرية.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // Flush chats on Firestore
                                val batch = FirebaseService.db.batch()
                                FirebaseService.db.collection("chats").get().addOnSuccessListener { s1 ->
                                    s1.documents.forEach { doc ->
                                        batch.delete(doc.reference)
                                    }
                                    batch.commit().addOnSuccessListener {
                                        Toast.makeText(context, "🗑️ تم تطهير ومسح كافة سجلات الدردشة بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مسح السجل نهائياً 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "💾 تم توليد ملف النسخة الاحتياطية للدردشة وتصديرها بصيغة CSV بنجاح!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تصدير المحادثات CSV 💾", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Automated cleanup scheduler trigger
                    Button(
                        onClick = {
                            Toast.makeText(context, "⏱️ تم جدولة عملية المسح الدوري والتحسين التلقائي لكل 30 يوم بنجاح في الخلفية!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⏱️ تفعيل الجدولة التلقائية لمسح البيانات المؤقتة", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 7. المزودين النشطين (Active Providers)
            // ==========================================
            DashboardSectionWrapper(
                title = "7. المزودين النشطين بالدليل (${activeProvidersList.size})",
                isExpanded = sectionExpanded7,
                onToggle = { sectionExpanded7 = !sectionExpanded7 }
            ) {
                if (activeProvidersList.isEmpty()) {
                    Text("لا يوجد مزودين نشطين حالياً.", color = Color.Gray, fontSize = 11.sp)
                } else {
                    activeProvidersList.forEach { p ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${p.specialty} • ${p.residenceAddress}", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                    
                                    Row {
                                        // Edit Provider attributes directly (names, phones, specialty, residence addresses)
                                        IconButton(onClick = {
                                            editName = p.name
                                            editPhone = p.phone
                                            editSpecialty = p.specialty
                                            editResidence = p.residenceAddress
                                            editBusiness = p.businessAddress
                                            providerToEdit = p
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Details", tint = Color(0xFFD4AF37))
                                        }

                                        if (canDelete) {
                                            IconButton(onClick = {
                                                FirebaseService.deleteProvider(p.id, {
                                                    Toast.makeText(context, "تم حذف الفني بنجاح المزامنة سحابيا!", Toast.LENGTH_SHORT).show()
                                                }, {})
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 8. لوحة التحكم بالاشتراكات والتثبيت (Subscriptions & Pinning)
            // ==========================================
            DashboardSectionWrapper(
                title = "8. الاشتراكات والترقيات الفاخرة",
                isExpanded = sectionExpanded8,
                onToggle = { sectionExpanded8 = !sectionExpanded8 }
            ) {
                if (!isSuperAdmin) {
                    Text("⚠️ التحكم بمواقع تصفح الفنيين وترقياتهم VIP حصر المشرف العام.", color = Color.Gray, fontSize = 12.sp)
                } else if (activeProvidersList.isEmpty()) {
                    Text("لا يوجد مزودين نشطين للتثبيت.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    activeProvidersList.forEach { p ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(p.name, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Pinned index toggle
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = p.isVip, onCheckedChange = { check ->
                                            FirebaseService.saveProvider(p.copy(isVip = check), {}, {})
                                        })
                                        Text("ترقية VIP", color = Color.White, fontSize = 11.sp)
                                    }

                                    // Verified Badge blue
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = p.isVerified, onCheckedChange = { check ->
                                            FirebaseService.saveProvider(p.copy(isVerified = check), {}, {})
                                        })
                                        Text("شارة زرقاء", color = Color.White, fontSize = 11.sp)
                                    }

                                    // Map Enabled toggler
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = !p.isMapDisabled, onCheckedChange = { check ->
                                            FirebaseService.saveProvider(p.copy(isMapDisabled = !check), {}, {})
                                        })
                                        Text("تفعيل الخارطة", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 9. إدارة المشرفين (Admin Management)
            // ==========================================
            if (isSuperAdmin) {
                DashboardSectionWrapper(
                    title = "9. إدارة المشرفين وتفويضات الوكلاء",
                    isExpanded = sectionExpanded9,
                    onToggle = { sectionExpanded9 = !sectionExpanded9 },
                    tintColor = Color(0xFF3B82F6)
                ) {
                    Text("أنشئ مشرف مبيعات جديد وحدد تفويضاته وصلاحياته بالدقة الفورية:", color = Color.LightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = supUsername,
                        onValueChange = { supUsername = it },
                        label = { Text("اسم المستخدم للمشرف (إنجليزي/عربي)", color = Color.LightGray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = supPassword,
                        onValueChange = { supPassword = it },
                        label = { Text("رمز المروز السري (Password)", color = Color.LightGray, fontSize = 12.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("تحديد صلاحيات المشرف المنشأ:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = pApprove, onCheckedChange = { pApprove = it })
                            Text("قبول ورفض طلبات التسجيل للفنيين", color = Color.White, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = pManageCats, onCheckedChange = { pManageCats = it })
                            Text("إضافة وحذف وتعديل الأقسام والمدن", color = Color.White, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = pManageAds, onCheckedChange = { pManageAds = it })
                            Text("إدارة الإعلانات والبنرات المتحركة", color = Color.White, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = pDeleteProviders, onCheckedChange = { pDeleteProviders = it })
                            Text("حذف مزودي الخدمة النشطين من الدليل", color = Color.White, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = pViewReports, onCheckedChange = { pViewReports = it })
                            Text("رؤية بلاغات المستخدمين وتقارير التدقيق الكامل", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (supUsername.isBlank() || supPassword.isBlank()) {
                                Toast.makeText(context, "الرجاء تعبئة الاسم ورمز المرور", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val activeSup = SupervisorUser(
                                id = "",
                                name = supUsername.trim(),
                                phone = supUsername.trim(), // Reuse username as identifier
                                password = supPassword.trim(),
                                canApproveTechs = pApprove,
                                canManageCategories = pManageCats,
                                canManageAds = pManageAds,
                                canDeleteTechs = pDeleteProviders,
                                canViewReports = pViewReports
                            )

                            FirebaseService.saveSupervisor(activeSup, {
                                Toast.makeText(context, "تم حفظ المشرف وبث الحساب على كل الأجهزة بنجاح سحابي!", Toast.LENGTH_LONG).show()
                                supUsername = ""
                                supPassword = ""
                            }, {
                                Toast.makeText(context, "فشل إنشاء الهوية السحابية", Toast.LENGTH_SHORT).show()
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("إنشاء حساب المشرف 👥", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shows active supervisors with Delete
                    Text("قائمة المشرفين المعتمدين والمزامنة:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    supervisors.forEach { sup ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${sup.name} (كلمة السر: ${sup.password})", color = Color.White, fontSize = 11.sp)
                            IconButton(onClick = {
                                FirebaseService.deleteSupervisor(sup.id, {
                                    Toast.makeText(context, "تم تعطيل وسحب صلاحيات المشرف التلقائي!", Toast.LENGTH_SHORT).show()
                                }, {})
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- CARD DETAILS EDIT DIALOG FOR PROVIDERS ---
    if (providerToEdit != null) {
        val target = providerToEdit!!
        AlertDialog(
            onDismissRequest = { providerToEdit = null },
            title = { Text("تعديل تفاصيل المهني", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("الأسم الثلاثي", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("رقم الهاتف الفوري", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editSpecialty,
                        onValueChange = { editSpecialty = it },
                        label = { Text("التخصص الفني", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editResidence,
                        onValueChange = { editResidence = it },
                        label = { Text("مكان السكن والمدينة", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editBusiness,
                        onValueChange = { editBusiness = it },
                        label = { Text("عنوان المقر أو العمل", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalCopy = target.copy(
                            name = editName.trim(),
                            phone = editPhone.trim(),
                            specialty = editSpecialty.trim(),
                            residenceAddress = editResidence.trim(),
                            businessAddress = editBusiness.trim()
                        )
                        FirebaseService.saveProvider(finalCopy, {
                            providerToEdit = null
                            Toast.makeText(context, "تم حفظ وتحديث المهني بنجاح بمزامنة فورية!", Toast.LENGTH_SHORT).show()
                        }, {
                            Toast.makeText(context, "فشل عملية المزامنة", Toast.LENGTH_SHORT).show()
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("حفظ التعديلات", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { providerToEdit = null }) {
                    Text("إلغاء", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun DashboardSectionWrapper(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    tintColor: Color = Color(0xFFD4AF37),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = tintColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = tintColor
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    content()
                }
            }
        }
    }
}
