package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.AppSettings
import com.Serviseyem.models.ServiceItem
import com.Serviseyem.models.SupervisorUser
import com.Serviseyem.services.FirebaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val services by FirebaseService.servicesList.collectAsState()
    val settings by FirebaseService.settings.collectAsState()
    val supervisors by FirebaseService.supervisorsList.collectAsState()
    val providers by FirebaseService.providersList.collectAsState()

    var activeTab by remember { mutableStateOf("الخدمات") } // "الخدمات", "مقدمي الخدمة", "المشرفين", "الهوية والألوان"

    // Form States for adding/editing Service
    var showServiceForm by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceItem?>(null) }
    var serviceTitle by remember { mutableStateOf("") }
    var serviceCategory by remember { mutableStateOf("VIP حكومية") }
    var serviceDesc by remember { mutableStateOf("") }
    var servicePrice by remember { mutableStateOf("حسب الاتفاق") }
    var serviceTime by remember { mutableStateOf("فوري") }
    var servicePhone by remember { mutableStateOf("777644670") }
    var servicePinned by remember { mutableStateOf(false) }

    // Form States for adding Supervisor
    var showSupervisorForm by remember { mutableStateOf(false) }
    var superName by remember { mutableStateOf("") }
    var superPhone by remember { mutableStateOf("") }
    var superPassword by remember { mutableStateOf("") }

    // Colors & Identity configuration states
    var appNameArState by remember { mutableStateOf(settings.appNameAr) }
    var welcomeMsgState by remember { mutableStateOf(settings.welcomeMsg) }
    var primaryColorState by remember { mutableStateOf(settings.primaryColor) }
    var canvasColorState by remember { mutableStateOf(settings.baseCanvasColor) }
    var supportPhoneState by remember { mutableStateOf(settings.supportPhone) }
    var supportWhatsappState by remember { mutableStateOf(settings.supportWhatsapp) }
    var footerTextState by remember { mutableStateOf(settings.footerText) }

    // Extra color, control & theme states
    var themeNameState by remember { mutableStateOf(settings.themeName) }
    var textColorOptionState by remember { mutableStateOf(settings.textColorOption) }
    var adminPasswordState by remember { mutableStateOf(settings.adminPassword) }
    
    var isMaintenanceModeState by remember { mutableStateOf(settings.isMaintenanceMode) }
    var maintenanceMessageState by remember { mutableStateOf(settings.maintenanceMessage) }
    
    var aiAssistantVisibleState by remember { mutableStateOf(settings.aiAssistantVisible) }
    var aiAssistantSizeState by remember { mutableStateOf(settings.aiAssistantSize) }
    var aiAssistantColorState by remember { mutableStateOf(settings.aiAssistantColor) }
    
    var infoIconVisibleState by remember { mutableStateOf(settings.infoIconVisible) }
    var infoIconSizeState by remember { mutableStateOf(settings.infoIconSize) }
    
    var sponsoredAdVisibleState by remember { mutableStateOf(settings.sponsoredAdVisible) }
    var sponsoredAdTextState by remember { mutableStateOf(settings.sponsoredAdText) }
    var sponsoredAdTypeState by remember { mutableStateOf(settings.sponsoredAdType) }

    // Sync form states with setting changes
    LaunchedEffect(settings) {
        appNameArState = settings.appNameAr
        welcomeMsgState = settings.welcomeMsg
        primaryColorState = settings.primaryColor
        canvasColorState = settings.baseCanvasColor
        supportPhoneState = settings.supportPhone
        supportWhatsappState = settings.supportWhatsapp
        footerTextState = settings.footerText
        
        // Sync our extra states too
        themeNameState = settings.themeName
        textColorOptionState = settings.textColorOption
        adminPasswordState = settings.adminPassword
        isMaintenanceModeState = settings.isMaintenanceMode
        maintenanceMessageState = settings.maintenanceMessage
        aiAssistantVisibleState = settings.aiAssistantVisible
        aiAssistantSizeState = settings.aiAssistantSize
        aiAssistantColorState = settings.aiAssistantColor
        infoIconVisibleState = settings.infoIconVisible
        infoIconSizeState = settings.infoIconSize
        sponsoredAdVisibleState = settings.sponsoredAdVisible
        sponsoredAdTextState = settings.sponsoredAdText
        sponsoredAdTypeState = settings.sponsoredAdType
    }

    // High contrast text field colors helper
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
                title = { Text("بوابة تحكم دليل WAM 👑", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        FirebaseService.currentSupervisor = null
                        onNavigateBack()
                        Toast.makeText(context, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
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
        ) {
            // Realtime Sync Status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF064E3B))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مزامنة Firestore السحابية فورية ونشطة 🟢",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Tabs layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                listOf("الخدمات", "مقدمي الخدمة", "المشرفين", "الهوية والألوان").forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = tab }
                            .padding(vertical = 12.dp)
                            .background(if (isSelected) Color(0xFF0B3F37) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFFD4AF37) else Color.White
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    "الخدمات" -> {
                        if (showServiceForm) {
                            // Service Add/Edit Form
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFFD4AF37))
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        Text(
                                            text = if (editingService == null) "إضافة خدمة جديدة للخدمة الفاخرة" else "تعديل الخدمة المحددة",
                                            color = Color(0xFFD4AF37),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    item {
                                        OutlinedTextField(
                                            value = serviceTitle,
                                            onValueChange = { serviceTitle = it },
                                            label = { Text("اسم الخدمة") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = highContrastTextFieldColors,
                                            singleLine = true
                                        )
                                    }

                                    item {
                                        Text("التصنيف:", color = Color.LightGray, fontSize = 12.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf("VIP حكومية", "خدمات إلكترونية", "دعم فني", "عقارية وتجارية").forEach { cat ->
                                                val sel = serviceCategory == cat
                                                Box(
                                                    modifier = Modifier
                                                        .border(
                                                            1.dp,
                                                            if (sel) Color(0xFFD4AF37) else Color.Gray,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .background(if (sel) Color(0xFF064E3B) else Color.Transparent)
                                                        .clickable { serviceCategory = cat }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                                ) {
                                                    Text(cat, fontSize = 10.sp, color = if (sel) Color(0xFFD4AF37) else Color.White)
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        OutlinedTextField(
                                            value = serviceDesc,
                                            onValueChange = { serviceDesc = it },
                                            label = { Text("شرح الخدمة بالتفصيل") },
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            colors = highContrastTextFieldColors
                                        )
                                    }

                                    item {
                                        OutlinedTextField(
                                            value = servicePrice,
                                            onValueChange = { servicePrice = it },
                                            label = { Text("سعر التنفيذ (أو حسب الاتفاق)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = highContrastTextFieldColors,
                                            singleLine = true
                                        )
                                    }

                                    item {
                                        OutlinedTextField(
                                            value = serviceTime,
                                            onValueChange = { serviceTime = it },
                                            label = { Text("مدة تنفيذ المعاملة") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = highContrastTextFieldColors,
                                            singleLine = true
                                        )
                                    }

                                    item {
                                        OutlinedTextField(
                                            value = servicePhone,
                                            onValueChange = { servicePhone = it },
                                            label = { Text("برقم هاتف للتواصل (وتس/هاتف)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = highContrastTextFieldColors,
                                            singleLine = true
                                        )
                                    }

                                    item {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = servicePinned,
                                                onCheckedChange = { servicePinned = it },
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD4AF37))
                                            )
                                            Text("تثبيت هذه الخدمة في الأعلى بأول الدليل 📌", color = Color.White, fontSize = 12.sp)
                                        }
                                    }

                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (serviceTitle.isEmpty() || serviceDesc.isEmpty()) {
                                                        Toast.makeText(context, "الرجاء اكمال الحقول الأساسية", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    val finalObj = ServiceItem(
                                                        id = editingService?.id ?: "",
                                                        title = serviceTitle,
                                                        category = serviceCategory,
                                                        description = serviceDesc,
                                                        price = servicePrice,
                                                        executionTime = serviceTime,
                                                        providerPhone = servicePhone,
                                                        isPinned = servicePinned
                                                    )
                                                    FirebaseService.saveService(finalObj, {
                                                        Toast.makeText(context, "تم الحفظ بنجاح وتزامن مع كل الهواتف!", Toast.LENGTH_SHORT).show()
                                                        showServiceForm = false
                                                        editingService = null
                                                    }, {
                                                        Toast.makeText(context, "خطأ بالاتصال بالشبكة لحفظ البيانات", Toast.LENGTH_SHORT).show()
                                                    })
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                                            ) {
                                                Text("حفظ التعديلات فورا", color = Color.Black, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    showServiceForm = false
                                                    editingService = null
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                            ) {
                                                Text("إلغاء الأمر", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Services List Display
                            Column(modifier = Modifier.fillMaxSize()) {
                                Button(
                                    onClick = {
                                        editingService = null
                                        serviceTitle = ""
                                        serviceDesc = ""
                                        serviceCategory = "VIP حكومية"
                                        servicePrice = "حسب الاتفاق"
                                        serviceTime = "فوري"
                                        servicePhone = "777644670"
                                        servicePinned = false
                                        showServiceForm = true
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_service_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                                    border = BorderStroke(1.dp, Color(0xFFD4AF37))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFFD4AF37))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("أضف خدمة جديدة فورية", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(services) { serv ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(serv.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        if (serv.isPinned) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("📌 VIP", color = Color(0xFFD4AF37), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Text(serv.category, color = Color(0xFFD4AF37), fontSize = 11.sp)
                                                }

                                                Row {
                                                    IconButton(onClick = {
                                                        editingService = serv
                                                        serviceTitle = serv.title
                                                        serviceDesc = serv.description
                                                        serviceCategory = serv.category
                                                        servicePrice = serv.price
                                                        serviceTime = serv.executionTime
                                                        servicePhone = serv.providerPhone
                                                        servicePinned = serv.isPinned
                                                        showServiceForm = true
                                                    }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                                                    }

                                                    IconButton(onClick = {
                                                        FirebaseService.deleteService(serv.id, {
                                                            Toast.makeText(context, "تم حذف الخدمة وتزامن الحذف فورا مع كافة الأجهزة المتصلة!", Toast.LENGTH_SHORT).show()
                                                        }, {
                                                            Toast.makeText(context, "الرجاء التحقق من اتصال الانترنت", Toast.LENGTH_SHORT).show()
                                                        })
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

                    "مقدمي الخدمة" -> {
                        // Service Providers Approval / Management
                        if (providers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("لا توجد طلبات تسجيل لمقدمي خدمات حالياً.", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(providers) { prov ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        border = BorderStroke(1.dp, if (prov.status == "أنتظر الموافقة") Color(0xFFD4AF37) else Color.Transparent)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(prov.name, color = Color.White, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("📱 هاتف: ${prov.phone}", color = Color.LightGray, fontSize = 12.sp)
                                                    Text("🛠️ التخصص المتوقع: ${prov.specialty}", color = Color.LightGray, fontSize = 12.sp)
                                                    Text("💳 رقم الهوية: ${prov.identityNumber}", color = Color.LightGray, fontSize = 11.sp)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            when (prov.status) {
                                                                "مقبول" -> Color(0xFF064E3B)
                                                                "مرفوض" -> Color.Red.copy(alpha = 0.2f)
                                                                else -> Color(0xFFD4AF37).copy(alpha = 0.2f)
                                                            },
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        prov.status,
                                                        color = if (prov.status == "أنتظر الموافقة") Color(0xFFD4AF37) else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            if (prov.status == "أنتظر الموافقة") {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            FirebaseService.updateProviderStatus(prov.id, "مقبول", {
                                                                Toast.makeText(context, "تم قبول مقدم الخدمة وتنشيطه فوراً بنجاح!", Toast.LENGTH_SHORT).show()
                                                            }, {})
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                                                        modifier = Modifier.padding(end = 6.dp)
                                                    ) {
                                                        Text("قبول وإعتماد", color = Color.White, fontSize = 11.sp)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            FirebaseService.updateProviderStatus(prov.id, "مرفوض", {
                                                                Toast.makeText(context, "تم رفض الطلب فورا وتحديث البيانات المزامنة", Toast.LENGTH_SHORT).show()
                                                            }, {})
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                        modifier = Modifier.padding(end = 6.dp)
                                                    ) {
                                                        Text("رفض الطلب", color = Color.White, fontSize = 11.sp)
                                                    }

                                                    IconButton(onClick = {
                                                        FirebaseService.deleteProvider(prov.id, {
                                                            Toast.makeText(context, "تم الحذف النهائي", Toast.LENGTH_SHORT).show()
                                                        }, {})
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                    TextButton(onClick = {
                                                        FirebaseService.deleteProvider(prov.id, {
                                                            Toast.makeText(context, "تمت الإزالة بنجاح", Toast.LENGTH_SHORT).show()
                                                        }, {})
                                                    }) {
                                                        Text("حذف السجل", color = Color.Red, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "المشرفين" -> {
                        // Managing Multiple Supervisors with Instant Sync
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "إضافة مشرفين إداريين للوصول من أجهزتهم 👑",
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "أي مشرف تقوم بإضافته هنا يمكنه تسجيل الدخول فوراً من أي هاتف متصل بالإنترنت في نفس اللحظة بفضل مزامنة Firestore الدقيقة.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (showSupervisorForm) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color(0xFFD4AF37))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = superName,
                                            onValueChange = { superName = it },
                                            label = { Text("الاسم الكامل للمشرف") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = highContrastTextFieldColors,
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = superPhone,
                                            onValueChange = { superPhone = it },
                                            label = { Text("هاتف المشرف (يسجل به دخول)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = highContrastTextFieldColors,
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = superPassword,
                                            onValueChange = { superPassword = it },
                                            label = { Text("كلمة السر الخاصة به") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = highContrastTextFieldColors,
                                            singleLine = true
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (superPhone.isEmpty() || superPassword.isEmpty() || superName.isEmpty()) {
                                                        Toast.makeText(context, "الرجاء تعبئة كافة الحقول بشكل كامل", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    val supObj = SupervisorUser(
                                                        phone = superPhone,
                                                        name = superName,
                                                        password = superPassword
                                                    )
                                                    FirebaseService.saveSupervisor(supObj, {
                                                        Toast.makeText(context, "تمت إضافة المشرف بنجاح وهو نشط الان!", Toast.LENGTH_SHORT).show()
                                                        showSupervisorForm = false
                                                        superPhone = ""
                                                        superName = ""
                                                        superPassword = ""
                                                    }, {
                                                        Toast.makeText(context, "خطأ بالاتصال بالشبكة", Toast.LENGTH_SHORT).show()
                                                    })
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                                            ) {
                                                Text("إضافة ونشر فوراً", color = Color.Black, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { showSupervisorForm = false },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                            ) {
                                                Text("إلغاء وأغلاق", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { showSupervisorForm = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                                    border = BorderStroke(1.dp, Color(0xFFD4AF37))
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = "Add supervisor")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تسجيل وإضافة مشرف إداري جديد", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(supervisors) { sup ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(sup.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("📱 هاتف: ${sup.phone} | 🔑 سر: ${sup.password}", color = Color.LightGray, fontSize = 12.sp)
                                            }
                                            
                                            // Non-primary managers deletion
                                            if (sup.phone != "777644670") {
                                                IconButton(onClick = {
                                                    FirebaseService.deleteSupervisor(sup.phone, {
                                                        Toast.makeText(context, "تمت إزالة المشرف وتزامن الحذف فورا!", Toast.LENGTH_SHORT).show()
                                                    }, {})
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFD4AF37), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("المالك", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "الهوية والألوان" -> {
                        // Corporate identity and live system colors management
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Text(
                                    "تحديث الهوية البصرية للبرنامج فورا لأي هاتف متصل ✨",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "قم بتغيير الألوان، الثيمات، شاشات الترحيب وسيتحدث البرنامج بكامله في الهواتف الاخرى في نفس الثانية بفضل المزامنة المباشرة ونظام WAM السحابي.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            // Palette Selection
                            item {
                                Text("اختر ثيم الهوية والباليت الجاهز للبرنامج 🎨:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "cosmic" to "كوزميك سيلفر 🌌",
                                        "charcoal_gold" to "الذهبي الفاخر ⚜️",
                                        "royal_emerald" to "الزمردي الراقي 💚"
                                    ).forEach { (themeKey, label) ->
                                        val isThemeSelected = themeNameState == themeKey
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { themeNameState = themeKey },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isThemeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            border = BorderStroke(1.dp, if (isThemeSelected) Color.White else Color.Transparent)
                                        ) {
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                color = if (isThemeSelected) Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Text Color Options
                            item {
                                Text("اختر لون الخط الموحد بالتطبيق ✍️:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "bright_white" to "أبيض ناصع ⚪",
                                        "light_gold" to "ذهبي فاتح 🟡",
                                        "vibrant_silver" to "فضي لامع 🔘"
                                    ).forEach { (colorKey, label) ->
                                        val isColorSelected = textColorOptionState == colorKey
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { textColorOptionState = colorKey },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isColorSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            border = BorderStroke(1.dp, if (isColorSelected) Color.White else Color.Transparent)
                                        ) {
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                color = if (isColorSelected) Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = appNameArState,
                                    onValueChange = { appNameArState = it },
                                    label = { Text("اسم التطبيق الموحد باللغة العربية") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = welcomeMsgState,
                                    onValueChange = { welcomeMsgState = it },
                                    label = { Text("نص شريط الإعلانات والترحيب البصري") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = adminPasswordState,
                                    onValueChange = { adminPasswordState = it },
                                    label = { Text("رمز الدخول السري والتحكم (للمالك والمخترقين) 🔑") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = primaryColorState,
                                    onValueChange = { primaryColorState = it },
                                    label = { Text("لون الهوية الأساسي المخصص (كود هكس مثل: #D4AF37)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = canvasColorState,
                                    onValueChange = { canvasColorState = it },
                                    label = { Text("لون الخلفية العام المخصص (كود هكس مثل: #042F2E)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = supportPhoneState,
                                    onValueChange = { supportPhoneState = it },
                                    label = { Text("رقم جوال دعم وادارة الدليل الرئيسي") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = supportWhatsappState,
                                    onValueChange = { supportWhatsappState = it },
                                    label = { Text("رقم واتساب الإدارة العام") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = footerTextState,
                                    onValueChange = { footerTextState = it },
                                    label = { Text("حقوق المالك (تظهر بأسفل الـ About والصفحات)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = highContrastTextFieldColors,
                                    singleLine = true
                                )
                            }

                            // Maintenance Mode card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    border = BorderStroke(1.dp, if (isMaintenanceModeState) Color.Red else Color.Gray.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("تفعيل وضع الصيانة العام للبرنامج 🛠️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Switch(
                                                checked = isMaintenanceModeState,
                                                onCheckedChange = { isMaintenanceModeState = it },
                                                colors = SwitchDefaults.colors(checkedIconColor = Color.Red)
                                            )
                                        }
                                        if (isMaintenanceModeState) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = maintenanceMessageState,
                                                onValueChange = { maintenanceMessageState = it },
                                                label = { Text("رسالة تظهر للعملاء أثناء وضع الصيانة") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = highContrastTextFieldColors
                                            )
                                        }
                                    }
                                }
                            }

                            // AI Assistant Controls Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("إظهار زر مساعد الذكاء الاصطناعي 🧠", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Switch(
                                                checked = aiAssistantVisibleState,
                                                onCheckedChange = { aiAssistantVisibleState = it }
                                            )
                                        }
                                        if (aiAssistantVisibleState) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("حجم أيقونة المساعد الذكي (${aiAssistantSizeState}dp):", color = Color.LightGray, fontSize = 11.sp)
                                            Slider(
                                                value = aiAssistantSizeState.toFloat(),
                                                onValueChange = { aiAssistantSizeState = it.toInt() },
                                                valueRange = 32f..72f,
                                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = aiAssistantColorState,
                                                onValueChange = { aiAssistantColorState = it },
                                                label = { Text("لون خلفية زر المساعد (كود هكس مثل: #D4AF37)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = highContrastTextFieldColors,
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }

                            // Info Icon card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("أيقونة معلومات الدليل في صفحات الفوتر ℹ️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Switch(
                                                checked = infoIconVisibleState,
                                                onCheckedChange = { infoIconVisibleState = it }
                                            )
                                        }
                                        if (infoIconVisibleState) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("حجم الأيقونة (${infoIconSizeState}dp):", color = Color.LightGray, fontSize = 11.sp)
                                            Slider(
                                                value = infoIconSizeState.toFloat(),
                                                onValueChange = { infoIconSizeState = it.toInt() },
                                                valueRange = 16f..48f,
                                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            }

                            // Sponsored Ad controls Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("إظهار بانر الإعلانات الممول الفوري 📢", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Switch(
                                                checked = sponsoredAdVisibleState,
                                                onCheckedChange = { sponsoredAdVisibleState = it }
                                            )
                                        }
                                        if (sponsoredAdVisibleState) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = sponsoredAdTextState,
                                                onValueChange = { sponsoredAdTextState = it },
                                                label = { Text("المحتوى النصي للإعلان في الصفحة الرئيسية") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = highContrastTextFieldColors
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("نوع الإعلان ومجال ظهوره:", color = Color.LightGray, fontSize = 11.sp)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf("نص", "صورة", "نص وصورة").forEach { type ->
                                                    val isTypeSelected = sponsoredAdTypeState == type
                                                    Card(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable { sponsoredAdTypeState = type },
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isTypeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
                                                        ),
                                                        border = BorderStroke(1.dp, if (isTypeSelected) Color.White else Color.Transparent)
                                                    ) {
                                                        Text(
                                                            text = type,
                                                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                            color = if (isTypeSelected) Color.Black else Color.White,
                                                            fontSize = 11.sp,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val finalSettings = settings.copy(
                                                appNameAr = appNameArState,
                                                welcomeMsg = welcomeMsgState,
                                                primaryColor = primaryColorState,
                                                baseCanvasColor = canvasColorState,
                                                supportPhone = supportPhoneState,
                                                supportWhatsapp = supportWhatsappState,
                                                footerText = footerTextState,
                                                themeName = themeNameState,
                                                textColorOption = textColorOptionState,
                                                adminPassword = adminPasswordState,
                                                isMaintenanceMode = isMaintenanceModeState,
                                                maintenanceMessage = maintenanceMessageState,
                                                aiAssistantVisible = aiAssistantVisibleState,
                                                aiAssistantSize = aiAssistantSizeState,
                                                aiAssistantColor = aiAssistantColorState,
                                                infoIconVisible = infoIconVisibleState,
                                                infoIconSize = infoIconSizeState,
                                                sponsoredAdVisible = sponsoredAdVisibleState,
                                                sponsoredAdText = sponsoredAdTextState,
                                                sponsoredAdType = sponsoredAdTypeState
                                            )
                                            FirebaseService.saveSettings(finalSettings, {
                                                Toast.makeText(context, "تم النشر والتزامن فورا مع كافة الأجهزة المتصلة!", Toast.LENGTH_SHORT).show()
                                            }, {
                                                Toast.makeText(context, "فشل الحفظ بالإنترنت", Toast.LENGTH_SHORT).show()
                                            })
                                        },
                                        modifier = Modifier.weight(1.5f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("نشر وتطبيق فوراً بكل مكان ✨", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            FirebaseService.clearAllChats({
                                                Toast.makeText(context, "تم مسح سجل الرسائل السحابي بـ Firestore!", Toast.LENGTH_SHORT).show()
                                            }, {})
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                                    ) {
                                        Text("تصفير المحادثة 🗑️", color = Color.White, fontSize = 11.sp)
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
