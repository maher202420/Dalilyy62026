package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.models.*
import com.example.services.FirebaseService
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val pendingState = FirebaseService.pendingProviders.collectAsState()
    val categoriesState = FirebaseService.categories.collectAsState()
    val activeState = FirebaseService.serviceProviders.collectAsState()
    val bannersState = FirebaseService.banners.collectAsState()
    val settingsState = FirebaseService.settings.collectAsState()
    val reportsState = FirebaseService.reports.collectAsState()
    val logState = FirebaseService.activityLogs.collectAsState()
    val adminState = FirebaseService.admins.collectAsState()
    val citiesState = FirebaseService.cities.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("الطلبات والتحكم", "الأقسام والمعلن وتذاكر المشكلات", "سجلات المشرفين والمحادثات")

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "لوحة تحكم المدير: كل خدمات اليمن",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Tab bar
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Tabs Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // SECTION 1: Pending Registrations Requests
                        item {
                            SectionHeader(title = "الأولى: طلبات التسجيل المعلقة (${pendingState.value.size})")
                        }
                        if (pendingState.value.isEmpty()) {
                            item {
                                EmptySectionMessage(message = "لا توجد أي طلبات انضمام فنيين جديدة معلقة حالياً.")
                            }
                        } else {
                            items(pendingState.value) { req ->
                                RegistrationRequestCard(
                                    req = req,
                                    mainCatName = categoriesState.value.find { it.id == req.categoryId }?.nameAr ?: "إنشاء مستند",
                                    subCatName = categoriesState.value.find { it.id == req.subCategoryId }?.nameAr ?: ""
                                )
                            }
                        }

                        // SECTION 2: Add Provider Manually
                        item {
                            SectionHeader(title = "الثانية: إضافة فني يدوياً مباشر")
                            ManualAddTechnicianView(
                                categories = categoriesState.value.filter { !it.isSubCategory },
                                onAdd = { p ->
                                    FirebaseService.addProviderDirectly(p)
                                    Toast.makeText(context, "✅ تم إدراج الفني مباشر في الدليل بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // SECTION 7: Active Providers
                        item {
                            SectionHeader(title = "السابعة: المزودين النشطين بالدليل (${activeState.value.size})")
                        }
                        items(activeState.value) { tech ->
                            ActiveProviderListCard(tech = tech)
                        }

                        // SECTION 8: Pinning & Promotion Panel
                        item {
                            SectionHeader(title = "الثامنة: الاشتراك والتثبيت وتوصية المنصة")
                        }
                        items(activeState.value) { tech ->
                            ActivePromoSettingsCard(tech = tech)
                        }
                    }

                    1 -> {
                        // SECTION 3: Banner Ads Placement
                        item {
                            SectionHeader(title = "الثالثة: البنرات الترويجية والإعلانات")
                            AdBannerManager(
                                onSubmit = { banner ->
                                    FirebaseService.addBanner(banner)
                                    Toast.makeText(context, "✅ تم إطلاق البنر الإعلاني وجاري عرضه فوراً", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // SECTION 4: Categories & Cities
                        item {
                            SectionHeader(title = "الرابعة: إدارة الأقسام وتعديل المدن")
                            CategoriesAndCitiesEditor(
                                onCategoryAdd = { cat ->
                                    FirebaseService.addCategory(cat)
                                    Toast.makeText(context, "✅ قسم جديد مضاف لقاعدة البيانات", Toast.LENGTH_SHORT).show()
                                },
                                onCityAdd = { city ->
                                    FirebaseService.addCity(city)
                                    Toast.makeText(context, "✅ تمت إضافة المدينة الجديدة", Toast.LENGTH_SHORT).show()
                                },
                                cities = citiesState.value
                            )
                        }

                        // SECTION 5: Complaints Reporting and CSV Generation
                        item {
                            SectionHeader(title = "الخامسة: الإبلاغات وتقارير التدقيق المعلقة (${reportsState.value.size})")
                            ComplaintsReportView(
                                reports = reportsState.value,
                                onPDFExport = {
                                    Toast.makeText(context, "📈 تم تصدير التقرير الأسبوعي بصيغة PDF وحفظه في المستندات", Toast.LENGTH_LONG).show()
                                },
                                onCSVExport = {
                                    Toast.makeText(context, "📝 تم تصدير خلاصة التقارير بصيغة CSV بنجاح", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }

                    2 -> {
                        // SECTION 6: Chats Purge System
                        item {
                            SectionHeader(title = "السادسة: إدارة سجلات المحادثات والخصوصية")
                            ChatCleanupView(
                                onWipe = {
                                    FirebaseService.clearChatHistory()
                                    Toast.makeText(context, "🧹 تم مسح كافة قاعدة بيانات الدردشة نهائياً لسرية الخصوصية", Toast.LENGTH_SHORT).show()
                                },
                                onExport = {
                                    Toast.makeText(context, "📂 تم تصدير الدردشات بصيغة CSV بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // SECTION 9: Administrators & Security Access Checker
                        item {
                            SectionHeader(title = "التاسعة: إدارة وتعيين المشرفين والمؤهلين")
                            SupervisorsManagerView(
                                adminsList = adminState.value,
                                onAddSupervisor = { name, pass, permissions ->
                                    val newAdm = Admin("adm_" + UUID.randomUUID().toString().take(5), name, pass, permissions)
                                    FirebaseService.addAdminSupervisor(newAdm)
                                    Toast.makeText(context, "⚙️ تم منح الصلاحيات وإنشاء حساب المشرف $name", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // System Action log viewer
                        item {
                            SectionHeader(title = "سجل الأنشطة والتدقيق المستمر")
                            DatabaseBackupAndLogSection(
                                logs = logState.value,
                                onBackup = {
                                    val res = FirebaseService.performBackup(context)
                                    Toast.makeText(context, res, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(10.dp)
            .padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun EmptySectionMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            modifier = Modifier.padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Subcomponents: Section 1 Card Requests (Approval and Custom Rejections)
@Composable
fun RegistrationRequestCard(req: PendingProvider, mainCatName: String, subCatName: String) {
    var rejectReason by remember { mutableStateOf("") }
    var showRejectInput by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Avatar Review
                if (req.avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = req.avatarUrl,
                        contentDescription = "Avatar Review",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(52.dp).background(Color.Gray, CircleShape))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = req.nameAr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "التخصص: $mainCatName • $subCatName", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "الهاتف: ${req.phone}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "العنوان: ${req.workAddress} (${req.residenceAr})", fontSize = 11.sp)

            if (req.idCardUrl.isNotEmpty()) {
                Text(
                    text = "✓ تم رفع بطاقة الهوية الوطنية للمطابقة السجلية للمدير السري",
                    color = Color(0xFF007AFF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!showRejectInput) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { FirebaseService.approveRequest(req.id, true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("قبول الفني", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = { showRejectInput = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("رفض وانتقاد", fontSize = 11.sp)
                    }
                }
            } else {
                Column {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("سبب الرفض والرد الإداري") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { 
                                FirebaseService.approveRequest(req.id, false, rejectReason)
                                showRejectInput = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("تاكيد الرفض", fontSize = 11.sp)
                        }
                        TextButton(onClick = { showRejectInput = false }) {
                            Text("إلغاء", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// Subcomponent: Section 2 Manual Operator Insterter Form
@Composable
fun ManualAddTechnicianView(categories: List<Category>, onAdd: (ServiceProvider) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("صنعاء") }
    var price by remember { mutableStateOf("1500") }
    var expandedCat by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf<Category?>(null) }
    var assignVip by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم") },
                modifier = Modifier.fillMaxWidth().testTag("add_tech_name")
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف") },
                modifier = Modifier.fillMaxWidth().testTag("add_tech_phone")
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("الموقع والمدينة") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("سعر المعاينة بالريال اليمن") },
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selection
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(
                    onClick = { expandedCat = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = selectedCat?.nameAr ?: "حدد قسم الفني")
                }
                DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.nameAr) },
                            onClick = {
                                selectedCat = cat
                                expandedCat = false
                            }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Checkbox(checked = assignVip, onCheckedChange = { assignVip = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("منح شارة نخبة VIP مباشرة ⭐")
            }

            Button(
                onClick = {
                    if (name.isNotEmpty() && phone.isNotEmpty() && selectedCat != null) {
                        val prov = ServiceProvider(
                            id = "prov_" + UUID.randomUUID().toString().take(6),
                            nameAr = name,
                            nameEn = name,
                            phone = phone,
                            categoryId = selectedCat!!.id,
                            workAddress = "$city، معاينة $price ريال",
                            residenceAr = city,
                            isPinned = assignVip,
                            isVip = assignVip,
                            isVerified = true,
                            rating = 5.0
                        )
                        onAdd(prov)
                        name = ""
                        phone = ""
                        assignVip = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة مباشر للدليل 🛠️")
            }
        }
    }
}

// Subcomponent: Section 3 Banner Ad Deployer Console
@Composable
fun AdBannerManager(onSubmit: (BannerAd) -> Unit) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("IMAGE") }
    var fileUrl by remember { mutableStateOf("") }
    var routeTarget by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("5") }
    var expandedType by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان البنر الدعائي الترويجي") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(onClick = { expandedType = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("نوع المحتوى الدعائي المرغوب: $type")
                }
                DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    listOf("IMAGE", "VIDEO", "TEXT").forEach { t ->
                        DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expandedType = false })
                    }
                }
            }

            OutlinedTextField(
                value = fileUrl,
                onValueChange = { fileUrl = it },
                label = { Text("رابط صورة/فيديو الخلفية الدعائية (اختياري)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = routeTarget,
                onValueChange = { routeTarget = it },
                label = { Text("القسم المراد التوجيه إليه بدقة عند النقر (مثال: ac أو elec)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("مدة العرض بالثواني") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        val banner = BannerAd(
                            id = "banner_" + UUID.randomUUID().toString().take(6),
                            title = title,
                            type = type,
                            bannerUrl = fileUrl,
                            targetCategory = routeTarget,
                            size = 10,
                            durationSeconds = duration.toIntOrNull() ?: 5,
                            isActive = true
                        )
                        onSubmit(banner)
                        title = ""
                        fileUrl = ""
                        routeTarget = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Text("إصدار الإعلان ونشره")
            }
        }
    }
}

// Subcomponent: Section 4 Categories and Cities editor sheets
@Composable
fun CategoriesAndCitiesEditor(
    onCategoryAdd: (Category) -> Unit,
    onCityAdd: (String) -> Unit,
    cities: List<String>
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("carpentry") }
    var expandedIcon by remember { mutableStateOf(false) }

    var cityAr by remember { mutableStateOf("") }

    Column {
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("إضافة قسم رئيسي لتصنيف الدليل:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                OutlinedTextField(value = nameAr, onValueChange = { nameAr = it }, label = { Text("الاسم بالعربية") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("الاسم بالإنجليزية") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف التعريفي للجمهور") }, modifier = Modifier.fillMaxWidth())

                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    OutlinedButton(onClick = { expandedIcon = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("رمز التخطيطي للقسم: $icon")
                    }
                    DropdownMenu(expanded = expandedIcon, onDismissRequest = { expandedIcon = false }) {
                        listOf("carpentry", "ac_unit", "electrical", "plumbing").forEach { i ->
                            DropdownMenuItem(text = { Text(i) }, onClick = { icon = i; expandedIcon = false })
                        }
                    }
                }

                Button(
                    onClick = {
                        if (nameAr.isNotEmpty() && nameEn.isNotEmpty()) {
                            val cat = Category(
                                id = "cat_" + UUID.randomUUID().toString().take(5),
                                nameAr = nameAr,
                                nameEn = nameEn,
                                description = desc,
                                icon = icon,
                                isSubCategory = false
                            )
                            onCategoryAdd(cat)
                            nameAr = ""
                            nameEn = ""
                            desc = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إدراج القسم الرئيسي مباشرة للتصفح")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("إضافة مدينة / محافظة تغطية جديدة جغرافيًا:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                OutlinedTextField(value = cityAr, onValueChange = { cityAr = it }, label = { Text("المدينة بالعربية") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        if (cityAr.isNotEmpty()) {
                            onCityAdd(cityAr)
                            cityAr = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Text("حفظ المدينة الجديدة بالقائمة")
                }

                Text("المدن المسجلة للتصفية الجغرافية:", modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(cities.joinToString(" - "), fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// Subcomponent: Section 5 Grid Complaints
@Composable
fun ComplaintsReportView(reports: List<ReportComplaint>, onPDFExport: () -> Unit, onCSVExport: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onPDFExport, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)), modifier = Modifier.weight(1f)) {
                    Text("تصدير أسبوعي PDF", fontSize = 10.sp, color = Color.White)
                }
                Button(onClick = onCSVExport, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.weight(1f)) {
                    Text("تصدير CSV مميز", fontSize = 10.sp, color = Color.White)
                }
            }

            Text("البلاغات وشكاوى المستخدمين المعلقة (${reports.size}):", modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            
            if (reports.isEmpty()) {
                Text("رائع! السجل ممتثِل ولا توجد شكاوى مستخدمين حالياً ضد أي فني.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                reports.forEach { r ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text("المرسل: ${r.senderName} ضد: ${r.providerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("المتن: ${r.complaintText}", fontSize = 11.sp, color = Color.DarkGray)
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

// Subcomponent: Section 6 chats clearings
@Composable
fun ChatCleanupView(onWipe: () -> Unit, onExport: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "يمكن للمدير تصفية السيرفر ودردشات الفنيين والعملاء بصفة دورية لتقليص مساحة التخزين وحفظ الأمان العام لمحادثات الفنيين والعملاء.",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onWipe, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("مسح السجل نهائياً", fontSize = 11.sp)
                }
                Button(onClick = onExport, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("تصدير المحادثات CSV", fontSize = 11.sp)
                }
            }
        }
    }
}

// Subcomponents: Section 7 & 8 Active Providers Cards
@Composable
fun ActiveProviderListCard(tech: ServiceProvider) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = tech.nameAr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "هاتف: ${tech.phone}", fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = { FirebaseService.deleteProvider(tech.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}

@Composable
fun ActivePromoSettingsCard(tech: ServiceProvider) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "الفني: ${tech.nameAr}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin toggle state
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tech.isPinned, onCheckedChange = { pin ->
                        FirebaseService.updateProvider(tech.copy(isPinned = pin, isVip = pin))
                    })
                    Text(" VIP تثبيت الصدارة", fontSize = 11.sp)
                }

                // Verify toggle state
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tech.isVerified, onCheckedChange = { ver ->
                        FirebaseService.updateProvider(tech.copy(isVerified = ver))
                    })
                    Text(" شارة التحقق الزرقاء", fontSize = 11.sp)
                }

                // Recommend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tech.isRecommended, onCheckedChange = { rec ->
                        FirebaseService.updateProvider(tech.copy(isRecommended = rec))
                    })
                    Text(" موصى به ⭐", fontSize = 11.sp)
                }
            }
        }
    }
}

// Subcomponent: Section 9 Supervisor credentials editor
@Composable
fun SupervisorsManagerView(adminsList: List<Admin>, onAddSupervisor: (String, String, AdminPermissions) -> Unit) {
    var sName by remember { mutableStateOf("") }
    var sPass by remember { mutableStateOf("") }
    
    // Fine-grained Checkboxes Permissions
    var pApprove by remember { mutableStateOf(true) }
    var pCategories by remember { mutableStateOf(false) }
    var pBanners by remember { mutableStateOf(false) }
    var pDelete by remember { mutableStateOf(false) }
    var pReports by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("اسم المستخدم للمشرف (إنجليزي/عربي)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = sPass, onValueChange = { sPass = it }, label = { Text("رمز المرور السري (Password)") }, modifier = Modifier.fillMaxWidth())

            Text("تحديد صلاحيات المشرف المنشأ:", modifier = Modifier.padding(top = 10.dp, bottom = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pApprove, onCheckedChange = { pApprove = it })
                    Text("قبول والتدقيق بطلبات تسجيل الفنيين الجدد", fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pCategories, onCheckedChange = { pCategories = it })
                    Text("إضافة وحذف وتعديل كشافات الأقسام والمدن والمنطقة", fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pBanners, onCheckedChange = { pBanners = it })
                    Text("إدارة الإعلانات والبنرات الدعائية بالصفحة الرئيسية", fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pDelete, onCheckedChange = { pDelete = it })
                    Text("القدرة على حذف وإلغاء فنيين نشطين من المنشور الدليلي", fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pReports, onCheckedChange = { pReports = it })
                    Text("رؤية بلاغات المستخدمين وتقارير التدقيق الكامل والأوراق", fontSize = 11.sp)
                }
            }

            Button(
                onClick = {
                    if (sName.isNotEmpty() && sPass.isNotEmpty()) {
                        val perm = AdminPermissions(
                            canApproveRequests = pApprove,
                            canEditCategories = pCategories,
                            canManageBanners = pBanners,
                            canDeleteProviders = pDelete,
                            canViewReports = pReports
                        )
                        onAddSupervisor(sName, sPass, perm)
                        sName = ""
                        sPass = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Text("إنشاء حساب المشرف وتنزيل بطاقته")
            }

            Text("قائمة المشرفين ذوي الوصول الفني المعين حالياً:", modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            adminsList.forEach { adm ->
                Text("• ${adm.username} (${if (adm.permissions.canApproveRequests) "تدقيق" else ""} ${if (adm.permissions.canDeleteProviders) "شطب" else ""})", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// Logs and Database Backup button
@Composable
fun DatabaseBackupAndLogSection(logs: List<ActivityLog>, onBackup: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onBackup,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("المزامنة وضغط قاعدة البيانات الموقتة (Backup)")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("قائمة حركة سجل الأحداث الزمني:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                logs.forEach { log ->
                    Text("⏱️ [${log.actor}]: ${log.logText}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}
