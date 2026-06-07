package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("dashboard") } // "dashboard", "chat_admin", "icon_settings", "logs"

    // Backdoor secret pass gate
    var isOwnerPassed by remember { mutableStateOf(false) }
    var ownerPasswordInput by remember { mutableStateOf("") }

    if (!isOwnerPassed) {
        // Double Premium Backdoor password challenge box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0D0E)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15171B)),
                border = BorderStroke(1.dp, viewModel.appPrimaryColor)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔒 بوابة المالك والمسؤول المعياري", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("الرجاء إدخال رقم المرور السري السري للتوثيق والتحكم:", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = ownerPasswordInput,
                        onValueChange = { ownerPasswordInput = it },
                        placeholder = { Text("أدخل رمز الدخول (مثال: 7777)", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val activeAdminMatch = viewModel.admins.find { it.passwordSecret == ownerPasswordInput }
                            if (activeAdminMatch != null || ownerPasswordInput == "7777") {
                                isOwnerPassed = true
                                viewModel.activeAdminUsername = activeAdminMatch?.username ?: "المالك العام"
                                viewModel.addActivityLog("تسجيل دخول المشرف العام: ${viewModel.activeAdminUsername}")
                                Toast.makeText(context, "🔑 أهلاً بك يا ${viewModel.activeAdminUsername}! تم فتح صلاحيات الدليل بأمان.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "❌ الرمز السري خاطئ! يرجى إعادة التحقق محلياً.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("توثيق الدخول الفوري", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text("العودة للتطبيق العام المباشر", color = Color.White)
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("لوحة التحكم الإدارية وبوابات التخصيص", color = viewModel.appPrimaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("حساب المسؤول النشط: ${viewModel.activeAdminUsername}", color = Color.LightGray, fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = viewModel.appPrimaryColor)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isOwnerPassed = false
                        viewModel.activeAdminUsername = null
                        ownerPasswordInput = ""
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F0F))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0A0A0A)) {
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { activeTab = "dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("الأقسام الـ 10", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = viewModel.appPrimaryColor, selectedTextColor = viewModel.appPrimaryColor, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )
                NavigationBarItem(
                    selected = activeTab == "chat_admin",
                    onClick = { activeTab = "chat_admin" },
                    icon = { Icon(Icons.Default.Message, contentDescription = null) },
                    label = { Text("إدارة الدردشات", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = viewModel.appPrimaryColor, selectedTextColor = viewModel.appPrimaryColor, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )
                NavigationBarItem(
                    selected = activeTab == "icon_settings",
                    onClick = { activeTab = "icon_settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("إعدادات الأيقونات", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = viewModel.appPrimaryColor, selectedTextColor = viewModel.appPrimaryColor, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )
                NavigationBarItem(
                    selected = activeTab == "logs",
                    onClick = { activeTab = "logs" },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("سجل النشاط", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = viewModel.appPrimaryColor, selectedTextColor = viewModel.appPrimaryColor, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                )
            }
        },
        containerColor = Color(0xFF0C0D0E)
    ) { paddingVals ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            when (activeTab) {
                "dashboard" -> AdminSectionsScrollTab(viewModel, context)
                "chat_admin" -> AdminChatRoomsSupervisorTab(viewModel, context)
                "icon_settings" -> AdminFloatingIconsControllerTab(viewModel, context)
                "logs" -> AdminActivityLogsViewerTab(viewModel, context)
            }
        }
    }
}

// ==========================================
// TAB 1: COLLAPSIBLE ACCORDION FOR 10 SECTIONS
// ==========================================
@Composable
fun AdminSectionsScrollTab(viewModel: AppViewModel, context: android.content.Context) {
    var expandedSection by remember { mutableIntStateOf(1) } // Active section ID

    // Data structures for manual tech addition
    var addTechName by remember { mutableStateOf("") }
    var addTechPhone by remember { mutableStateOf("") }
    var addTechCity by remember { mutableStateOf("صنعاء") }
    var addTechSpecialty by remember { mutableStateOf("سباكة") }
    var addTechFee by remember { mutableStateOf("") }
    var addTechVipBadge by remember { mutableStateOf(false) }

    // Data structures for banners
    var bannerTitleInput by remember { mutableStateOf("") }
    var bannerTypeSelection by remember { mutableStateOf("text") } // "text", "image", "video"
    var bannerMediaUrl by remember { mutableStateOf("") }
    var bannerTargetSpecialty by remember { mutableStateOf("سباكة") }
    var bannerAdSize by remember { mutableStateOf("10") }
    var bannerDurationSec by remember { mutableStateOf("15") }

    // Data structures for categories & cities
    var catNameArInput by remember { mutableStateOf("") }
    var catNameEnInput by remember { mutableStateOf("") }
    var catDescInput by remember { mutableStateOf("") }
    var catIconEmoji by remember { mutableStateOf("🔧") }

    var cityArInput by remember { mutableStateOf("") }
    var cityEnInput by remember { mutableStateOf("") }

    // Admin Creation
    var newAdminUsername by remember { mutableStateOf("") }
    var newAdminPasswordSecret by remember { mutableStateOf("") }
    val newAdminPrivileges = remember { mutableStateListOf<String>() }

    // Multi custom colors helper
    val colorPalettes = listOf(
        "#FFD700" to "الذهبي الفاخر",
        "#1D4ED8" to "الأزرق الملكي",
        "#DC2626" to "الأحمر القاني",
        "#16A34A" to "الأخضر الماسي"
    )

    // Security feedback modal state
    var showDeleteConfirmModal by remember { mutableStateOf(false) }
    var providerToDeleteId by remember { mutableStateOf<String?>(null) }

    var cameraPhotoPreviewSimulated by remember { mutableStateOf<String?>(null) }
    var userRegistrationGenderMale by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // --- SECTION 1: REGISTRATION REQUESTS ---
        item {
            AdminSectionHeader(
                id = 1,
                title = "١. طلبات التسجيل للفنيين الجدد المرفوعة",
                badgeValue = viewModel.registrationRequests.size.toString(),
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 1 },
                viewModel = viewModel
            )
            if (expandedSection == 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (viewModel.registrationRequests.isEmpty()) {
                            Text("لا توجد طلبات معلقة مسجلة حالياً.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            viewModel.registrationRequests.forEach { req ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(req.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("الهاتف: ${req.phone} • تخصص # ${req.specialty}", color = Color.LightGray, fontSize = 10.sp)
                                        }
                                        Row {
                                            IconButton(onClick = { viewModel.approveRequest(req.id) }) {
                                                Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
                                            }
                                            IconButton(onClick = { viewModel.rejectRequest(req.id) }) {
                                                Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: ADD TECHNICIAN MANUALLY ---
        item {
            AdminSectionHeader(
                id = 2,
                title = "٢. إضافة فني وتعديله يدوياً للدليل فوراً",
                badgeValue = null,
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 2 },
                viewModel = viewModel
            )
            if (expandedSection == 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        TextField(
                            value = addTechName,
                            onValueChange = { addTechName = it },
                            placeholder = { Text("الاسم الكامل (اختياري للادمن)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = addTechPhone,
                            onValueChange = { addTechPhone = it },
                            placeholder = { Text("رقم الهاتف (اختياري للادمن)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Specialties pick chips
                        Text("حدد تخصص المهني الفني:", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            viewModel.categories.forEach { cat ->
                                val isSelected = addTechSpecialty == cat.nameAr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { addTechSpecialty = cat.nameAr },
                                    label = { Text(cat.nameAr, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = viewModel.appPrimaryColor, selectedLabelColor = Color.Black)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        // Cities selection
                        Text("حدد محافظة/مدينة السكن:", color = Color.LightGray, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            viewModel.cities.forEach { city ->
                                val isSelected = addTechCity == city.nameAr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { addTechCity = city.nameAr },
                                    label = { Text(city.nameAr, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = viewModel.appPrimaryColor, selectedLabelColor = Color.Black)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = addTechFee,
                            onValueChange = { addTechFee = it },
                            placeholder = { Text("سعر الزيارة والمعاينة (اختياري - افتراضي ٥٠٠٠ ريال)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = addTechVipBadge,
                                onCheckedChange = { addTechVipBadge = it },
                                colors = CheckboxDefaults.colors(checkedColor = viewModel.appPrimaryColor)
                            )
                            Text("منح شارة نخبة VIP فورية مع التفعيل المزدوج", color = Color.White, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (addTechName.isEmpty()) {
                                    Toast.makeText(context, "يمكن للادمن الإضافة بدون اسم، سيعين كادر تجريبي عشوائي.", Toast.LENGTH_SHORT).show()
                                }
                                val resultMsg = viewModel.addManualTechnician(
                                    name = if (addTechName.isEmpty()) "فني متعاون" else addTechName,
                                    phone = if (addTechPhone.isEmpty()) "777000000" else addTechPhone,
                                    city = addTechCity,
                                    specialty = addTechSpecialty,
                                    fee = addTechFee.toIntOrNull() ?: 5000,
                                    isVip = addTechVipBadge
                                )
                                Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                                
                                // Reset fields
                                addTechName = ""
                                addTechPhone = ""
                                addTechFee = ""
                                addTechVipBadge = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("أضف الفني فوراً ومزامنته", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SECTION 3: ADS & BANNERS ---
        item {
            AdminSectionHeader(
                id = 3,
                title = "٣. إعلانات وبنرات دعائية ممولة متطورة",
                badgeValue = viewModel.banners.size.toString(),
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 3 },
                viewModel = viewModel
            )
            if (expandedSection == 3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        TextField(
                            value = bannerTitleInput,
                            onValueChange = { bannerTitleInput = it },
                            placeholder = { Text("عنوان البنر الدعائي الترويجي المميز") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Select Content Type
                        Text("نوع المادة الإعلانية:", color = Color.LightGray, fontSize = 11.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("text" to "نص ترويجي", "image" to "صورة دعائية", "video" to "مقطع مرئي").forEach { item ->
                                val isSel = bannerTypeSelection == item.first
                                FilterChip(
                                    selected = isSel,
                                    onClick = { bannerTypeSelection = item.first },
                                    label = { Text(item.second, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = viewModel.appPrimaryColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = bannerMediaUrl,
                            onValueChange = { bannerMediaUrl = it },
                            placeholder = { Text("رابط صورة/فيديو الخلفية الدعائية (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("القسم الموجه إليه بدقة عند النقر:", color = Color.LightGray, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            viewModel.categories.forEach { cat ->
                                val isSelected = bannerTargetSpecialty == cat.nameAr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { bannerTargetSpecialty = cat.nameAr },
                                    label = { Text(cat.nameAr, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = viewModel.appPrimaryColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = bannerAdSize,
                                onValueChange = { bannerAdSize = it },
                                placeholder = { Text("حجم الإعلان") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                            )
                            TextField(
                                value = bannerDurationSec,
                                onValueChange = { bannerDurationSec = it },
                                placeholder = { Text("مدة العرض (ثانية)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (bannerTitleInput.isEmpty()) {
                                    Toast.makeText(context, "يرجى كتابة عنوان للإعلان أولاً.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newAd = AdBanner(
                                        title = bannerTitleInput,
                                        contentType = bannerTypeSelection,
                                        mediaUrl = if (bannerMediaUrl.isEmpty()) null else bannerMediaUrl,
                                        targetSectionId = bannerTargetSpecialty,
                                        adSize = bannerAdSize.toIntOrNull() ?: 10,
                                        durationSeconds = bannerDurationSec.toIntOrNull() ?: 15
                                    )
                                    viewModel.banners = listOf(newAd) + viewModel.banners
                                    viewModel.addActivityLog("إطلاق إعلان ممول جديد: $bannerTitleInput")
                                    bannerTitleInput = ""
                                    bannerMediaUrl = ""
                                    Toast.makeText(context, "📣 تم حفظ وإطلاق البنر الترويجي وحفظه ببطاقة التصفح الفوري!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ وإطلاق البنر الترويجي فوراً", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SECTION 4: MANAGE CATEGORIES & CITIES ---
        item {
            AdminSectionHeader(
                id = 4,
                title = "٤. إدارة تصنيفات المهن التعبيرية والمدن",
                badgeValue = null,
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 4 },
                viewModel = viewModel
            )
            if (expandedSection == 4) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("الجزء أ: إضافة وتأهيل قسم مهني رئيسي فرعي:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = catNameArInput,
                            onValueChange = { catNameArInput = it },
                            placeholder = { Text("الاسم المهني بالعربية (مثال: سباكة)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = catNameEnInput,
                            onValueChange = { catNameEnInput = it },
                            placeholder = { Text("الاسم الإنجليزي (مثال: Plumbing)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = catDescInput,
                            onValueChange = { catDescInput = it },
                            placeholder = { Text("الوصف التعريفي المقتضب للجمهور") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Emoji symbol switcher
                        Text("اختر الرمز المهني التعبيري للقسم:", color = Color.LightGray, fontSize = 11.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("🔧", "⚡", "🪚", "❄️", "🔨", "💻", "🧹").forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (catIconEmoji == emoji) viewModel.appPrimaryColor else Color.Black, CircleShape)
                                        .clickable { catIconEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (catNameArInput.isEmpty() || catNameEnInput.isEmpty()) {
                                    Toast.makeText(context, "يرجى ملء أسماء القسم بالجهتين لضمان المزامنة.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addMainCategory(catNameArInput, catNameEnInput, catDescInput, catIconEmoji)
                                    catNameArInput = ""
                                    catNameEnInput = ""
                                    catDescInput = ""
                                    Toast.makeText(context, "✅ تم إدراج القسم الرئيسي المباشر للتصفح فوراً!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إدراج القسم الرئيسي المباشر للتصفح", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("الجزء ب: تمديد نطاق مدينة / محافظة جغرافية تخديمية:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = cityArInput,
                            onValueChange = { cityArInput = it },
                            placeholder = { Text("المدينة بالعربية (مثال: إب)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = cityEnInput,
                            onValueChange = { cityEnInput = it },
                            placeholder = { Text("المدينة بالإنجليزي (مثال: Ibb)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (cityArInput.isEmpty() || cityEnInput.isEmpty()) {
                                    Toast.makeText(context, "الرجاء تعبئة اسم المحافظة بالعربية والإنجليزية معاً.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addCity(cityArInput, cityEnInput)
                                    cityArInput = ""
                                    cityEnInput = ""
                                    Toast.makeText(context, "📍 تم تشغيل الفرز الجغرافي للمحافظة الجديدة فوراً!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ المدينة الجديدة بالقائمة المزامنة", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SECTION 5: COMPLAINTS & REPORTS AUDITING ---
        item {
            AdminSectionHeader(
                id = 5,
                title = "٥. الإبلاغات وشكاوى الجمهور والتقارير الرقابية",
                badgeValue = viewModel.complaints.size.toString(),
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 5 },
                viewModel = viewModel
            )
            if (expandedSection == 5) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "📥 جاري تصدير التقرير الأسبوعي المعياري PDF المفلتر لوزارة الصناعة والخدمات...", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تصدير أسبوعي PDF 📄", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    Toast.makeText(context, "📥 تم تصدير ملف الإحصاءات CSV المرمّز لتسلسلات الفنيين بنجاح!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تصدير مميز CSV 📊", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("البلاغات النشطة من العملاء (${viewModel.complaints.size}):", color = Color.LightGray, fontSize = 11.sp)
                        viewModel.complaints.forEach { c ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF262626))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("الشاكي: ${c.complainantName}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("ضد: ${c.techName}", color = viewModel.appPrimaryColor, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(c.complaintText, color = Color.LightGray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = {
                                            viewModel.complaints = viewModel.complaints.filter { it.id != c.id }
                                            Toast.makeText(context, "تم حل المشكلة وتصفير السجل بنجاح.", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("تأكيد حل المشكلة وبترها ✓", color = Color.Green, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 6: CHAT PRIVACY HISTORY WIPE ---
        item {
            AdminSectionHeader(
                id = 6,
                title = "٦. إدارة سرية المحادثات ومسح السجل نهائياً",
                badgeValue = null,
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 6 },
                viewModel = viewModel
            )
            if (expandedSection == 6) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "نظام الحماية المعياري بالدليل يسمح بحذف ومسح وإطلاق كافة المحادثات والدردشات الفورية نهائياً من قاعدة البيانات لضمان السرية التامة للمستخدمين.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.chatMessages = emptyList()
                                    viewModel.addActivityLog("مسح كامل سجل وإرشيف المحادثات لضمان الخصوصية.")
                                    Toast.makeText(context, "🧹 تم مسح إرشيف الرسائل نهائياً وتأمين خصوصية الكوادر والعملاء بنجاح!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("مسح السجل نهائياً 🗑️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    Toast.makeText(context, "📤 تم استخراج وتحميل كامل بيانات الدردشات بصيغة CHATS_ARCHIVE.csv", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تصدير المحادثات CSV 📂", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 7: ACTIVE SUPPLIERS DIRECT DELETE ---
        item {
            AdminSectionHeader(
                id = 7,
                title = "٧. الكوادر المهنية النشطة بالخارطة والدليل",
                badgeValue = viewModel.providers.size.toString(),
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 7 },
                viewModel = viewModel
            )
            if (expandedSection == 7) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("قائمة بجميع الفنيين النشطين المزامنين بالخرائط وبوابات الرادار المباشرة:", color = Color.LightGray, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        viewModel.providers.forEach { tech ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(tech.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("تخصص: ${tech.specialty} • ${tech.city}", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            providerToDeleteId = tech.id
                                            showDeleteConfirmModal = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 8: BADGES & VERIFICATIONS & EXCLUSIVES CONTROL ---
        item {
            AdminSectionHeader(
                id = 8,
                title = "٨. ترقيات وشارات المسؤول وتوثيق النجوم",
                badgeValue = null,
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 8 },
                viewModel = viewModel
            )
            if (expandedSection == 8) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("قم بالضغط لتعديل ومنح شارات النخبة والتوثيق للمهنيين فورياً:", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                        viewModel.providers.forEach { tech ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF262626))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tech.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // VIP Toggle Button
                                        Button(
                                            onClick = {
                                                tech.isVip = !tech.isVip
                                                viewModel.addActivityLog("تعديل شارة VIP للفني: ${tech.name} ليكون ${tech.isVip}")
                                                Toast.makeText(context, "🔥 تم تبديل شارة VIP!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (tech.isVip) viewModel.appPrimaryColor else Color.DarkGray),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("👑 VIP", color = if (tech.isVip) Color.Black else Color.White, fontSize = 8.sp)
                                        }

                                        // Blue check verified badge toggle
                                        Button(
                                            onClick = {
                                                tech.isVerified = !tech.isVerified
                                                viewModel.addActivityLog("تعديل شارة التوثيق للفني: ${tech.name} لتصير ${tech.isVerified}")
                                                Toast.makeText(context, "💙 تم تبديل شارة التوثيق والضمانة الزرقاء!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (tech.isVerified) Color(0xFF2563EB) else Color.DarkGray),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("✓ موثق", color = Color.White, fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 9: CREATING MODERATORS & PERMISSIONS ---
        item {
            AdminSectionHeader(
                id = 9,
                title = "٩. إدارة حسابات المشرفين المساعدين والصلاحيات",
                badgeValue = viewModel.admins.size.toString(),
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 9 },
                viewModel = viewModel
            )
            if (expandedSection == 9) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        TextField(
                            value = newAdminUsername,
                            onValueChange = { newAdminUsername = it },
                            placeholder = { Text("اسم المستخدم للمشرف (إنجليزي / عربي)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextField(
                            value = newAdminPasswordSecret,
                            onValueChange = { newAdminPasswordSecret = it },
                            placeholder = { Text("رمز المرور السري (Password)") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("تحديد صلاحيات المشرف المنشأ بالدليل المزدوج:", color = Color.LightGray, fontSize = 11.sp)
                        
                        val privilegeOptions = listOf(
                            "قبول ورفض طلبات التسجيل للفنيين",
                            "إضافة وحذف وتعديل الأقسام والمدن",
                            "إدارة الإعلانات والبنرات المتحركة",
                            "حذف مزودي الخدمة النشطين من الدليل",
                            "رؤية بلاغات المستخدمين وتقارير التدقيق الكامل"
                        )

                        privilegeOptions.forEach { priv ->
                            val isChecked = newAdminPrivileges.contains(priv)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) newAdminPrivileges.remove(priv) else newAdminPrivileges.add(priv)
                                    }
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (isChecked) newAdminPrivileges.remove(priv) else newAdminPrivileges.add(priv)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = viewModel.appPrimaryColor)
                                )
                                Text(priv, color = Color.White, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (newAdminUsername.isEmpty() || newAdminPasswordSecret.isEmpty()) {
                                    Toast.makeText(context, "الرجاء كتابة اسم مستخدم وكلمة مرور للمشرف الجديد.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newAdm = AdminAccount(
                                        newAdminUsername,
                                        newAdminPasswordSecret,
                                        newAdminPrivileges.toList()
                                    )
                                    viewModel.admins = viewModel.admins + newAdm
                                    viewModel.addActivityLog("إنشاء الحساب الإداري المساعد الجديد: $newAdminUsername")
                                    newAdminUsername = ""
                                    newAdminPasswordSecret = ""
                                    newAdminPrivileges.clear()
                                    Toast.makeText(context, "👥 تم إنشاء حساب المشرف المساعد ومزامنة الصلاحيات فوراً لجميع الأجهزة!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إنشاء حساب المشرف فوراً وتوثيقه", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SECTION 10: CUSTOM STYLING & GLOBAL APP FLAGS SWITCHES ---
        item {
            AdminSectionHeader(
                id = 10,
                title = "١٠. تخصيص المظهر والثيم وشروط الكادر الإقليمي",
                badgeValue = null,
                expandedSection = expandedSection,
                onHeaderClick = { expandedSection = 10 },
                viewModel = viewModel
            )
            if (expandedSection == 10) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        
                        // Theme dynamic accent picker
                        Text("أ. التحكم بألوان التطبيق والخطوط المعيارية:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colorPalettes.forEach { item ->
                                val isSelected = viewModel.appPrimaryColorStr == item.first
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .background(Color(android.graphics.Color.parseColor(item.first)), RoundedCornerShape(6.dp))
                                        .border(
                                            2.dp,
                                            if (isSelected) Color.White else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            viewModel.appPrimaryColorStr = item.first
                                            viewModel.addActivityLog("تم تحويل لغة مظهر التطبيق إلى: ${item.second}")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.second,
                                        color = if (item.first == "#FFD700") Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("اختر نوع وحجم الخط الشائع بالدليل الموحد:", color = Color.LightGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val fontOptions = listOf(
                                "Default" to "الافتراضي",
                                "Monospace" to "أحادي",
                                "Serif" to "شريفي",
                                "Cursive" to "رقعة"
                            )
                            fontOptions.forEach { pair ->
                                val isSelected = viewModel.appSelectedFontName == pair.first
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .background(if (isSelected) viewModel.appPrimaryColor else Color(0xFF2E2E3E), RoundedCornerShape(4.dp))
                                        .clickable {
                                            viewModel.appSelectedFontName = pair.first
                                            viewModel.addActivityLog("تعديل الخط المعياري للتطبيق إلى ${pair.second}")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pair.second, color = if (isSelected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggles for features
                        Text("ب. خيارات تمكين قنوات الرادار والتحليل والاتصالات:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        // Toggle Voice speech match
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تمكين البحث الصوتي المعياري (الذكي Voice Search):", color = Color.White, fontSize = 11.sp)
                            Switch(
                                checked = viewModel.voiceSearchEnabled,
                                onCheckedChange = {
                                    viewModel.voiceSearchEnabled = it
                                    viewModel.addActivityLog("تبديل ميزة البحث الصوتي لتصير $it")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = viewModel.appPrimaryColor)
                            )
                        }

                        // Toggle Instant Chat Enabled / Disabled with Custom Message
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تفعيل نظام المحادثة الفورية المباشر (Chat Room):", color = Color.White, fontSize = 11.sp)
                            Switch(
                                checked = viewModel.isChatInstantEnabled,
                                onCheckedChange = {
                                    viewModel.isChatInstantEnabled = it
                                    viewModel.addActivityLog("تبديل تفعيل المحادثات لتصير $it")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = viewModel.appPrimaryColor)
                            )
                        }

                        if (!viewModel.isChatInstantEnabled) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextField(
                                value = viewModel.chatDisabledMessage,
                                onValueChange = { viewModel.chatDisabledMessage = it },
                                placeholder = { Text("أدخل رسالة مخصصة تظهر عند التعطيل") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                            )
                        }

                        // Toggle Reviews Visible
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تمكين خيار التقييم وتعليقات العملاء:", color = Color.White, fontSize = 11.sp)
                            Switch(
                                checked = viewModel.isRatingsAndReviewsEnabled,
                                onCheckedChange = { viewModel.isRatingsAndReviewsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = viewModel.appPrimaryColor)
                            )
                        }

                        // Toggle Bookings
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("عرض تبويب الحجوزات والمواعيد الجارية:", color = Color.White, fontSize = 11.sp)
                            Switch(
                                checked = viewModel.showBookingsSection,
                                onCheckedChange = { viewModel.showBookingsSection = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = viewModel.appPrimaryColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Part C: Selfie and upload repair screen
                        Text("ج. محاكي تسجيل أصحاب الخدمات والمهن (رفع الصور وثائق):", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = userRegistrationGenderMale,
                                onClick = { userRegistrationGenderMale = true },
                                colors = RadioButtonDefaults.colors(selectedColor = viewModel.appPrimaryColor)
                            )
                            Text("متقدم ذكر (يستوجب سيلفي)", color = Color.White, fontSize = 11.sp)
                            RadioButton(
                                selected = !userRegistrationGenderMale,
                                onClick = { userRegistrationGenderMale = false },
                                colors = RadioButtonDefaults.colors(selectedColor = viewModel.appPrimaryColor)
                            )
                            Text("متقدمة أنثى (رمز مهني)", color = Color.White, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (userRegistrationGenderMale) {
                                        cameraPhotoPreviewSimulated = "📸 تم فتح كاميرا الهاتف مباشرة وتصوير سيلفي المتقدم بنجاح!"
                                        Toast.makeText(context, "📸 تم التقاط الصورة وضغطها آلياً بنسبة 72% لتسريع التحميل!", Toast.LENGTH_LONG).show()
                                    } else {
                                        cameraPhotoPreviewSimulated = "🎨 تم اختيار رمز تعبيري للتخصص المهني المعتمد!"
                                        Toast.makeText(context, "تم تحديد باقة الفتاة المهنية للحفاظ على الخصوصية.", Toast.LENGTH_SHORT).show()
                                    }
                                    viewModel.addActivityLog("محاكاة فتح كاميرا الهاتف لالتقاط سيلفي")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("سيلفي كاميرا 📸", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    cameraPhotoPreviewSimulated = "📂 تم تصفح معرض الصور (الاستوديو) واختيار المستند المناسب بنجاح!"
                                    Toast.makeText(context, "📁 تم اختيار المستند وضغطه تلقائياً بنسبة 72%!", Toast.LENGTH_SHORT).show()
                                    viewModel.addActivityLog("تصفح ألبوم مستودع صور الهاتف للحصول على البطاقة")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("اختر من الاستوديو 🖼️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (cameraPhotoPreviewSimulated != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cameraPhotoPreviewSimulated!!,
                                color = Color.Green,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Part D: Edit terms
                        Text("د. إدارة شروط تسجيل مقدمي الخدمات والتوجيهات للتعديل والحذف:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        viewModel.registrationTerms.forEachIndexed { index, term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var currentText by remember(term.termText) { mutableStateOf(term.termText) }
                                TextField(
                                    value = currentText,
                                    onValueChange = { 
                                        currentText = it
                                        val updatedList = viewModel.registrationTerms.toMutableList()
                                        updatedList[index] = term.copy(termText = it)
                                        viewModel.registrationTerms = updatedList
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF1E2129),
                                        unfocusedContainerColor = Color(0xFF14161A)
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = viewModel.appFontFamily)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = {
                                    viewModel.registrationTerms = viewModel.registrationTerms.filter { it.id != term.id }
                                    Toast.makeText(context, "تم إزالة الشرط المذكور بنجاح.", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        var textTermInput by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = textTermInput,
                                onValueChange = { textTermInput = it },
                                placeholder = { Text("أضف شرطاً جديداً لضم الكوادر...", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black, 
                                    unfocusedContainerColor = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (textTermInput.isNotEmpty()) {
                                        val newT = RegistrationTerm(termText = textTermInput)
                                        viewModel.registrationTerms = viewModel.registrationTerms + newT
                                        textTermInput = ""
                                        Toast.makeText(context, "تم إرسال ونشر الشرط الجديد لنموذج التسجيل!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor)
                            ) {
                                Text("أضف +", color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Clean-up databases
                        Text("هـ. عمليات التنظيف التلقائي للبيانات المؤقتة والتقارير:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("دورة الاحتفاظ بالملفات المؤقتة (يوم): ${viewModel.autoCleanupDays} يوماً", color = Color.White, fontSize = 11.sp)
                            Slider(
                                value = viewModel.autoCleanupDays.toFloat(),
                                onValueChange = { viewModel.autoCleanupDays = it.toInt() },
                                valueRange = 7f..90f,
                                steps = 3,
                                modifier = Modifier.width(140.dp),
                                colors = SliderDefaults.colors(viewModel.appPrimaryColor, activeTrackColor = viewModel.appPrimaryColor)
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.cleanUpTempLogs()
                                Toast.makeText(context, "🧹 تم تنظيف التخزين المؤقت وتفريغ ملفات الاستماع فورياً للزيارات المسرّعة!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تطهير الذاكرة العشوائية وسلسلة الكاش الآن", color = Color.White, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Part F: Firestore footer sync settings
                        Text("و. تعديل تذييل التطبيق والمزامنة الفورية مع السحابة (Firestore):", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        var footerTextInputVal by remember(viewModel.footerText) { mutableStateOf(viewModel.footerText) }
                        var footerFontSizeInputVal by remember { mutableFloatStateOf(viewModel.footerFontSize) }

                        TextField(
                            value = footerTextInputVal,
                            onValueChange = {
                                footerTextInputVal = it
                                viewModel.updateFooterTextFromFirestore(it, footerFontSizeInputVal)
                            },
                            label = { Text("محتوى نص التسييل بدلاً من 'wam2026'", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("حجم خط التذييل: ${footerFontSizeInputVal.toInt()} sp", color = Color.LightGray, fontSize = 11.sp)
                            Slider(
                                value = footerFontSizeInputVal,
                                onValueChange = {
                                    footerFontSizeInputVal = it
                                    viewModel.updateFooterTextFromFirestore(footerTextInputVal, it)
                                },
                                valueRange = 8f..24f,
                                modifier = Modifier.width(180.dp),
                                colors = SliderDefaults.colors(viewModel.appPrimaryColor, activeTrackColor = viewModel.appPrimaryColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Part G: About app config
                        Text("ز. إعدادات شاشة (عن التطبيق) ومشاركتها ورابط التحميل:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        TextField(
                            value = viewModel.appDownloadLink,
                            onValueChange = { 
                                viewModel.appDownloadLink = it 
                                viewModel.addActivityLog("تعديل رابط تحميل التطبيق إلى: $it")
                            },
                            label = { Text("رابط تحميل التطبيق المتاح للمشاركة", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("اختر الصورة/اللوجو المعبر عن التطبيق:", color = Color.LightGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val logoPresets = listOf(
                                "📱" to "جوال",
                                "🛠️" to "صيانة",
                                "🇾🇪" to "اليمن",
                                "🌟" to "مميز"
                            )
                            logoPresets.forEach { item ->
                                val isSelected = viewModel.appInfoImageEmoji == item.first && viewModel.appInfoUploadedImagePath == null
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .background(if (isSelected) viewModel.appPrimaryColor else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.appInfoImageEmoji = item.first
                                            viewModel.appInfoUploadedImagePath = null
                                            viewModel.addActivityLog("تصفح واختيار أيقونة المعلومات: ${item.second}")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${item.first} ${item.second}", color = if (isSelected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = viewModel.appInfoUploadedImagePath ?: "",
                            onValueChange = { 
                                viewModel.appInfoUploadedImagePath = if (it.isEmpty()) null else it 
                                viewModel.addActivityLog("تعديل رابط الصورة المخصصة لمعلومات التطبيق")
                            },
                            label = { Text("أو أدخل رابط صورة مخصصة (URL/Path)", fontSize = 10.sp) },
                            placeholder = { Text("مثال: https://yem.com/logo.png") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Part H: Loyalty points toggle and size control
                        Text("ح. لوحة إدارة صندوق الولاء والاستبدال بالرئيسية:", color = viewModel.appPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تنشيط / إظهار قسم نقاط الولاء بالواجهة الرئيسية:", color = Color.White, fontSize = 11.sp)
                            Switch(
                                checked = viewModel.showLoyaltySection,
                                onCheckedChange = { 
                                    viewModel.showLoyaltySection = it 
                                    viewModel.addActivityLog("مزامنة ظهور مربع نقاط الولاء: $it")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = viewModel.appPrimaryColor)
                            )
                        }

                        if (viewModel.showLoyaltySection) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = viewModel.loyaltyCardTitle,
                                onValueChange = { viewModel.loyaltyCardTitle = it },
                                label = { Text("عنوان صندوق الولاء (استخدم %d للإشارة للرصيد)", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            TextField(
                                value = viewModel.loyaltyCardText,
                                onValueChange = { viewModel.loyaltyCardText = it },
                                label = { Text("نص عرض الاستبدال والمكافأة الترويجي", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("حجم خط صندوق الولاء: ${viewModel.loyaltyCardProgressSize.toInt()} sp", color = Color.LightGray, fontSize = 11.sp)
                                Slider(
                                    value = viewModel.loyaltyCardProgressSize,
                                    onValueChange = { viewModel.loyaltyCardProgressSize = it },
                                    valueRange = 10f..22f,
                                    modifier = Modifier.width(160.dp),
                                    colors = SliderDefaults.colors(viewModel.appPrimaryColor, activeTrackColor = viewModel.appPrimaryColor)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("حواف ومسافات الصندوق (Padding): ${viewModel.loyaltyCardHeightPadding.toInt()} dp", color = Color.LightGray, fontSize = 11.sp)
                                Slider(
                                    value = viewModel.loyaltyCardHeightPadding,
                                    onValueChange = { viewModel.loyaltyCardHeightPadding = it },
                                    valueRange = 6f..30f,
                                    modifier = Modifier.width(160.dp),
                                    colors = SliderDefaults.colors(viewModel.appPrimaryColor, activeTrackColor = viewModel.appPrimaryColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- SECURITY MODAL CONFIRMATION: ACTIVE SOURCE DELETE ---
    if (showDeleteConfirmModal && providerToDeleteId != null) {
        val prov = viewModel.providers.find { it.id == providerToDeleteId }
        AlertDialog(
            onDismissRequest = { showDeleteConfirmModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد حذف كادر نشط 🚫", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "تحذير أمني: هل أنت متأكد من رغبتك في ترحيل وحذف ومسح الكادر الفني المهني '${prov?.name ?: ""}' من قاعدة بيانات الدليل والخرائط نهائياً؟ هذا الإجراء فوري ولا يمكن التراجع عنه.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteActiveProvider(providerToDeleteId!!)
                        providerToDeleteId = null
                        showDeleteConfirmModal = false
                        Toast.makeText(context, "تم إقصاء الفني من الدليل والخرائط فوراً.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد الحذف النهائي المباشر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    providerToDeleteId = null
                    showDeleteConfirmModal = false
                }) {
                    Text("إلغاء وتأمين التراجع", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

// ==========================================
// TAB 2: FLOATING MESSAGE supervisor CONTROL (Chat Settings Tab)
// ==========================================
@Composable
fun AdminFloatingIconsControllerTab(viewModel: AppViewModel, context: android.content.Context) {
    var sizeSliderVal by remember { mutableFloatStateOf(viewModel.chatSettingsIconSize) }
    var assistantSizeSliderVal by remember { mutableFloatStateOf(viewModel.aiAssistantIconSize) }

    val iconColors = listOf(
        "#0D1B2A" to "الأسود الأنيق",
        "#064E3B" to "الأخضر المعياري",
        "#1D4ED8" to "الأزرق الفاخر",
        "#EF4444" to "الأحمر المتوهج"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚙️ بوابة إدارة كفاءة وحجم الأيقونات التفاعلية الشائعة:", color = viewModel.appPrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("الأيقونة الأولى: مظهر وحجم فقاعة الدردشة الفورية 💬", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                
                // Toggle hide icon temporarily
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إظهار ميزة الأيقونة بالصفحة الرئيسية:", color = Color.LightGray, fontSize = 11.sp)
                    Switch(
                        checked = viewModel.chatSettingsVisible,
                        onCheckedChange = { viewModel.chatSettingsVisible = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = viewModel.appPrimaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Change size in px / dp slider
                Text(
                    text = "تعديل قطر القطر والحجم للأيقونة: ${sizeSliderVal.toInt()} بكسل/DP",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Slider(
                    value = sizeSliderVal,
                    onValueChange = { 
                        sizeSliderVal = it
                        viewModel.chatSettingsIconSize = it
                    },
                    valueRange = 40f..100f,
                    colors = SliderDefaults.colors(viewModel.appPrimaryColor, activeTrackColor = viewModel.appPrimaryColor)
                )

                Spacer(modifier = Modifier.height(10.dp))
                // Pick color for icon bubble
                Text("اختر لون الأيقونة والفقاعة التفاعلية بالدليل:", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    iconColors.forEach { item ->
                        val isChecked = viewModel.chatSettingsIconColorStr == item.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .background(Color(android.graphics.Color.parseColor(item.first)), RoundedCornerShape(4.dp))
                                .border(2.dp, if (isChecked) Color.White else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.chatSettingsIconColorStr = item.first
                                    viewModel.addActivityLog("تعديل لون فقاعة المحادثة الفورية")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.second, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                // Hard delete icon bubble from app
                Button(
                    onClick = {
                        viewModel.chatSettingsDeleted = true
                        viewModel.addActivityLog("تم محو وحذف ميزة فقاعة الدردشة الفورية بالكامل من التطبيق الرئيسي.")
                        Toast.makeText(context, "🚫 تم حذف ميزة فقاعة الدردشة الفورية من التطبيق بالكامل ولن يعاد برمجتها!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حذف فقاعة الدردشة الفورية نهائياً ⛔", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("الأيقونة الثانية: مظهر وحجم المساعد الذكي AI 🤖", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تنشيط أيقونة المساعد الذكي بالصفحة:", color = Color.LightGray, fontSize = 11.sp)
                    Switch(
                        checked = viewModel.aiAssistantVisible,
                        onCheckedChange = { viewModel.aiAssistantVisible = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = viewModel.appPrimaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "حجم أيقونة الذكاء الاصطناعي: ${assistantSizeSliderVal.toInt()} بكسل/DP",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Slider(
                    value = assistantSizeSliderVal,
                    onValueChange = {
                        assistantSizeSliderVal = it
                        viewModel.aiAssistantIconSize = it
                    },
                    valueRange = 40f..100f,
                    colors = SliderDefaults.colors(viewModel.appPrimaryColor, activeTrackColor = viewModel.appPrimaryColor)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("تلوين أيقونة محاكاة خوادم الذكاء الاصطناعي:", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("#111827" to "الأسود الفخم", "#4F46E5" to "البنفسجي الذكي", "#059669" to "الأخضر الزمردي").forEach { item ->
                        val isChecked = viewModel.aiAssistantIconColorStr == item.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .background(Color(android.graphics.Color.parseColor(item.first)), RoundedCornerShape(4.dp))
                                .border(2.dp, if (isChecked) Color.White else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.aiAssistantIconColorStr = item.first
                                    viewModel.addActivityLog("تعديل لون أيقونة الذكاء الاصطناعي")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.second, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: ADMIN CHAT ROOMS OVERSEER (All chats list)
// ==========================================
@Composable
fun AdminChatRoomsSupervisorTab(viewModel: AppViewModel, context: android.content.Context) {
    var selectedSessionForAudit by remember { mutableStateOf<ChatSession?>(null) }
    var chatOperatorMessageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("💬 بوابة إدارة وفلترة جميع المحادثات والدردشات الفعالة:", color = viewModel.appPrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        if (selectedSessionForAudit == null) {
            Text("جميع المحادثات والاتصالات الإرشادية الجارية (بحد أقصى ٢٠):", color = Color.LightGray, fontSize = 11.sp)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.chatSessions) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSessionForAudit = session },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الدردشة: ${session.userName} ➔ ${session.techName}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (session.isBlocked) {
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.Red)) {
                                        Text("محظور الدردشة", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("آخر رسالة: ${session.lastMessage}", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        } else {
            val ses = selectedSessionForAudit!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { selectedSessionForAudit = null }) {
                            Text("◀ العودة للقائمة", color = viewModel.appPrimaryColor, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                ses.isBlocked = !ses.isBlocked
                                Toast.makeText(context, "تم تعديل حالة الحظر للطرفين بنجاح المباشر!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (ses.isBlocked) Color.Green else Color.Red),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(if (ses.isBlocked) "إلغاء الحظر والفتح الكامل" else "حظر المستخدم ومزامنته 🚫", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("السجل الكامل لتسلسل الدردشة المسترجعة:", color = Color.LightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(viewModel.chatMessages) { m ->
                                val isAdminReply = m.senderRole == "admin"
                                val isUserReply = m.senderRole == "user"
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isUserReply) Alignment.CenterStart else Alignment.CenterEnd
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isAdminReply) Color(0xFFEF4444) else if (isUserReply) viewModel.appPrimaryColor else Color(0xFF1F2937)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(m.senderName, color = if (isUserReply) Color.Black else Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text(m.messageText, color = if (isUserReply) Color.Black else Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("الرد كمشرف ورقابة الدليل (Super Admin Reply):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = chatOperatorMessageText,
                            onValueChange = { chatOperatorMessageText = it },
                            placeholder = { Text("اكتب رد الإدارة التوجيهي للعميل...") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (chatOperatorMessageText.isNotEmpty()) {
                                    val logMsg = ChatMessage(
                                        chatId = "1",
                                        senderName = "مشرف دليل خدمات اليمن 🇾🇪",
                                        senderRole = "admin",
                                        messageText = chatOperatorMessageText
                                    )
                                    viewModel.chatMessages = viewModel.chatMessages + logMsg
                                    ses.lastMessage = chatOperatorMessageText
                                    chatOperatorMessageText = ""
                                    Toast.makeText(context, "✓ تم إرسال الرد الرقابي الحاسم للطرفين!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = viewModel.appPrimaryColor)
                        ) {
                            Text("إرسال", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: SYSTEM AUDITING ACTIVITIES LOG VIEW
// ==========================================
@Composable
fun AdminActivityLogsViewerTab(viewModel: AppViewModel, context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("🖥️ سجل الأنشطة والتدقيق البرمجي المائي ولحظة الترجمات:", color = viewModel.appPrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("لمراقبة ومزامنة المدخلات على بقية الأجهزة فوراً:", color = Color.LightGray, fontSize = 11.sp)
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, Color.DarkGray)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(viewModel.adminActivityLogs) { log ->
                    Text(
                        text = log,
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ---------------- Helper Components for elegance ----------------
@Composable
fun AdminSectionHeader(
    id: Int,
    title: String,
    badgeValue: String?,
    expandedSection: Int,
    onHeaderClick: () -> Unit,
    viewModel: AppViewModel
) {
    val isExpanded = expandedSection == id
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeaderClick() },
        shape = if (isExpanded) RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp) else RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isExpanded) viewModel.appPrimaryColor else Color(0xFF15171B)),
        border = BorderStroke(1.dp, if (isExpanded) viewModel.appPrimaryColor else Color(0xFF262626))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = if (isExpanded) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeValue != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = badgeValue,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isExpanded) Color.Black else Color.White
                )
            }
        }
    }
}
