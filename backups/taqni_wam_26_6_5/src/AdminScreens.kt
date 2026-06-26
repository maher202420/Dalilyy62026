@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.Serviseyem

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Serviseyem.models.*
import java.util.UUID
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.InputStream


// ============================================================
// 🛠️ لوحة تحكم الأدمن
// ============================================================
@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val loggedAdmin by viewModel.loggedInUsername.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val chatRooms by viewModel.chatRooms.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val adminAccounts by viewModel.adminAccounts.collectAsStateWithLifecycle()
    
    var activeTab by remember { mutableIntStateOf(0) }
    var showPurgeDialog by remember { mutableStateOf(false) }
    var purgePassword by remember { mutableStateOf("") }
    var purgeError by remember { mutableStateOf(false) }
    
    if (!isAdminLoggedIn) {
        // شاشة تسجيل الدخول للأدمن
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var rememberMe by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf(false) }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.darkBg),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppTheme.accentGold),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = AppTheme.accentGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "🔐 تسجيل دخول الإدارة",
                        color = AppTheme.accentGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; loginError = false },
                        label = { Text("اسم المستخدم", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppTheme.accentGold,
                            unfocusedBorderColor = Color(0xFF223639)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; loginError = false },
                        label = { Text("كلمة المرور", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppTheme.accentGold,
                            unfocusedBorderColor = Color(0xFF223639)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    
                    // تذكر تسجيل الدخول
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberMe = !rememberMe }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppTheme.primaryRed,
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تذكر تسجيل الدخول (حفظ)",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                    
                    if (loginError) {
                        Text(
                            text = "❌ اسم المستخدم أو كلمة المرور غير صحيحة",
                            color = AppTheme.primaryRed,
                            fontSize = 11.sp
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (viewModel.checkAdminLogin(username, password, rememberMe)) {
                                loginError = false
                            } else {
                                loginError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تسجيل الدخول", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }
    
    // لوحة تحكم الأدمن
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
    ) {
        // رأس اللوحة
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.surfaceDark)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛠️ لوحة التحكم - $loggedAdmin",
                color = AppTheme.accentGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { viewModel.logoutAdmin() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.height(32.dp)
            ) {
                Text("خروج", color = Color.White, fontSize = 10.sp)
            }
        }
        
        // التبويبات
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = AppTheme.surfaceDark,
            contentColor = AppTheme.accentGold,
            edgePadding = 4.dp
        ) {
            val tabs = listOf(
                "📊 الإحصائيات",
                "📝 الطلبات",
                "👤 إضافة فني",
                "📢 البنرات",
                "📂 الأقسام",
                "🗺️ المدن",
                "⚠️ البلاغات",
                "💬 الدردشات",
                "👥 المزودين",
                "🏆 الترقيات",
                "👮 المشرفين",
                "📜 شروط الانضمام",
                "🎨 الألوان",
                "🔔 الإشعارات",
                "📅 الحجوزات",
                "🧹 التطهير"
            )
            
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 9.sp,
                            color = if (activeTab == index) AppTheme.accentGold else Color.Gray
                        )
                    }
                )
            }
        }
        
        // محتوى التبويبات
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (activeTab) {
                0 -> AdminStatsTab(viewModel)
                1 -> PendingRequestsTab(viewModel, pendingRequests)
                2 -> AddProviderTab(viewModel)
                3 -> BannersTab(viewModel)
                4 -> CategoriesTab(viewModel, categories)
                5 -> CitiesTab(viewModel, cities)
                6 -> ReportsTab(viewModel)
                7 -> ChatsAdminTab(viewModel, chatRooms)
                8 -> ProvidersListTab(viewModel, providers)
                9 -> SubscriptionsTab(viewModel, providers)
                10 -> AdminsTab(viewModel, adminAccounts)
                11 -> RegistrationConditionsTab(viewModel)
                12 -> ThemeColorsTab(viewModel)
                13 -> NotificationsTab(viewModel, notifications)
                14 -> BookingsTab(viewModel, bookings)
                15 -> PurgeTab(viewModel, showPurgeDialog, purgePassword, purgeError) {
                    showPurgeDialog = true
                }
            }
        }
    }
    
    // نافذة تطهير البيانات
    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = {
                Text(
                    "⚠️ تحذير: تطهير قواعد البيانات",
                    color = AppTheme.primaryRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "سيتم حذف كافة البيانات: الحسابات، المحادثات، الحجوزات، الإشعارات، والبنرات. لا يمكن التراجع عن هذا الإجراء!",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = purgePassword,
                        onValueChange = { 
                            purgePassword = it
                            purgeError = false
                        },
                        label = { Text("أدخل كلمة مرور الأدمن للتأكيد", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppTheme.accentGold,
                            unfocusedBorderColor = Color(0xFF223639)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (purgeError) {
                        Text(
                            text = "❌ كلمة المرور غير صحيحة!",
                            color = AppTheme.primaryRed,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.purgeAllData(purgePassword)) {
                            showPurgeDialog = false
                            purgePassword = ""
                            Toast.makeText(context, "✅ تم تطهير قواعد البيانات بنجاح!", Toast.LENGTH_LONG).show()
                        } else {
                            purgeError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed)
                ) {
                    Text("نعم، تطهير البيانات", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPurgeDialog = false
                    purgePassword = ""
                    purgeError = false
                }) {
                    Text("إلغاء", color = Color.White)
                }
            },
            containerColor = AppTheme.surfaceDark
        )
    }
}

// ============================================================
// 📊 تبويب الإحصائيات
// ============================================================
@Composable
fun AdminStatsTab(viewModel: MainViewModel) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    
    val activeProviders = providers.count { it.isVerified }
    val pendingCount = providers.count { !it.isVerified }
    val bookingsCount = bookings.size
    val pendingBookings = bookings.count { it.status == "pending" }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // بطاقات الإحصائيات
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "👨‍🔧 المزودين",
                value = "${providers.size}",
                subtitle = "نشط: $activeProviders | معلق: $pendingCount"
            )
            StatsCard(
                title = "📅 الحجوزات",
                value = "$bookingsCount",
                subtitle = "قيد الانتظار: $pendingBookings"
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "📂 الأقسام",
                value = "${categories.size}",
                subtitle = "قسم رئيسي"
            )
            StatsCard(
                title = "🔔 الإشعارات",
                value = "${viewModel.notifications.value.size}",
                subtitle = "إشعار مرسل"
            )
        }
        
        // أكثر الأقسام طلباً
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "📊 أكثر الأقسام طلباً",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val categoryCounts = categories.map { cat ->
                    val count = providers.count { it.category == cat.id }
                    cat to count
                }.sortedByDescending { it.second }.take(5)
                
                if (categoryCounts.isEmpty() || categoryCounts.all { it.second == 0 }) {
                    Text(
                        text = "لا توجد بيانات كافية",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                } else {
                    categoryCounts.forEach { (cat, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${cat.iconUrl} ${cat.nameAr}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Badge(
                                containerColor = AppTheme.accentGold
                            ) {
                                Text(
                                    text = "$count",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.StatsCard(
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 10.sp
            )
            Text(
                text = value,
                color = AppTheme.accentGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 8.sp
            )
        }
    }
}

// ============================================================
// 📝 تبويب الطلبات المعلقة
// ============================================================
@Composable
fun PendingRequestsTab(
    viewModel: MainViewModel,
    pendingRequests: List<Provider>
) {
    val context = LocalContext.current
    if (pendingRequests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد طلبات معلقة",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        return
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(pendingRequests) { provider ->
            var showDocuments by remember { mutableStateOf(false) }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                border = BorderStroke(1.dp, Color(0xFF223639)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = provider.name,
                                color = AppTheme.accentGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "📞 ${provider.phone} | 📍 ${provider.city} - ${provider.area}",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "📂 القسم: ${provider.category}",
                                color = AppTheme.grayText,
                                fontSize = 11.sp
                            )
                            if (provider.description.isNotBlank()) {
                                Text(
                                    text = provider.description,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    
                    // زر إخفاء وإظهار المستندات الثبوتية وحماية الخصوصية قبل القرار
                    Button(
                        onClick = { showDocuments = !showDocuments },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.darkBg),
                        border = BorderStroke(1.dp, AppTheme.accentGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (showDocuments) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = AppTheme.accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (showDocuments) "إخفاء الصور والبطاقة الشخصية 👁️" else "عرض الصور وبطاقة الهوية للمتقدم 👁️",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                    
                    if (showDocuments) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // الصورة الشخصية للمتقدم
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الصورة الشخصية", color = Color.Gray, fontSize = 9.sp)
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppTheme.darkBg)
                                        .border(1.dp, Color(0xFF223639), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = com.Serviseyem.rememberBase64Bitmap(provider.imageUrl)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "صورة مقدم الطلب",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = "لا توجد صورة شخصية",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                            
                            // صورة بطاقة الهوية للمتقدم
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("بطاقة الهوية/الجواز", color = Color.Gray, fontSize = 9.sp)
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppTheme.darkBg)
                                        .border(1.dp, Color(0xFF223639), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = com.Serviseyem.rememberBase64Bitmap(provider.idCardBase64)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "بطاقة الهوية",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = "لا توجد بطاقة هوية",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val approved = provider.copy(isVerified = true)
                                viewModel.approvePendingRequest(approved)
                                Toast.makeText(
                                    context,
                                    "✅ تم قبول ${provider.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("قبول ✅", color = Color.White, fontSize = 10.sp)
                        }
                        
                        Button(
                            onClick = {
                                viewModel.rejectPendingRequest(provider.id)
                                Toast.makeText(
                                    context,
                                    "❌ تم رفض ${provider.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("رفض ❌", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 👤 تبويب إضافة فني
// ============================================================
@Composable
fun AddProviderTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var isSubscribed by remember { mutableStateOf(false) }
    var isPinned by remember { mutableStateOf(false) }
    var isRecommended by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "👤 إضافة فني جديد يدوياً",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("المنطقة / الشارع", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("نبذة عن الخدمات", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // اختيار التخصص
                Text(
                    text = "اختر التخصص:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = if (isSelected) "" else category.id },
                            label = {
                                Text(
                                    text = "${category.iconUrl} ${category.nameAr}",
                                    fontSize = 10.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTheme.primaryRed,
                                selectedLabelColor = Color.White,
                                containerColor = AppTheme.surfaceDark,
                                labelColor = Color.White
                            )
                        )
                    }
                }
                
                // اختيار المدينة
                Text(
                    text = "اختر المدينة:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cities) { city ->
                        val isSelected = selectedCity == city.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCity = if (isSelected) "" else city.id },
                            label = {
                                Text(
                                    text = city.nameAr,
                                    fontSize = 10.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTheme.primaryRed,
                                selectedLabelColor = Color.White,
                                containerColor = AppTheme.surfaceDark,
                                labelColor = Color.White
                            )
                        )
                    }
                }
                
                // خيارات إضافية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSubscribed,
                        onCheckedChange = { isSubscribed = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text(
                        text = "👑 منح شارة VIP",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text(
                        text = "📌 تثبيت في المقدمة",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRecommended,
                        onCheckedChange = { isRecommended = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text(
                        text = "⭐ توصية من الإدارة",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || area.isBlank()) {
                            Toast.makeText(context, "الرجاء تعبئة الحقول الأساسية", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedCategory.isBlank() || selectedCity.isBlank()) {
                            Toast.makeText(context, "الرجاء اختيار التخصص والمدينة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val provider = Provider(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            phone = phone,
                            area = area,
                            description = description,
                            category = selectedCategory,
                            city = selectedCity,
                            isVerified = true,
                            isSubscribed = isSubscribed,
                            isPinned = isPinned,
                            isRecommended = isRecommended
                        )
                        viewModel.addProvider(provider)
                        Toast.makeText(context, "✅ تم إضافة ${provider.name} بنجاح", Toast.LENGTH_SHORT).show()
                        
                        // إعادة تعيين الحقول
                        name = ""
                        phone = ""
                        area = ""
                        description = ""
                        selectedCategory = ""
                        selectedCity = ""
                        isSubscribed = false
                        isPinned = false
                        isRecommended = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إضافة الفني", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================
// 📢 تبويب البنرات
// ============================================================
@Composable
fun BannersTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    
    var title by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var isVideo by remember { mutableStateOf(false) }
    var displayDurationSeconds by remember { mutableStateOf("5") }
    var isActive by remember { mutableStateOf(true) }
    var editingBanner by remember { mutableStateOf<AppBanner?>(null) }
    
    // منتقي الوسائط للبنر (يدعم الصور والفيديو القصير)
    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: ""
                isVideo = mimeType.startsWith("video")
                
                val inputStream = context.contentResolver.openInputStream(uri)
                val base64 = com.Serviseyem.compressImageBase64(inputStream)
                if (base64 != null) {
                    mediaUrl = base64
                    Toast.makeText(
                        context, 
                        if (isVideo) "🎥 تم تحميل الفيديو القصير للبنر بنجاح!" else "📸 تم تحميل صورة البنر بنجاح!", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل معالجة ملف الوسائط", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // بطاقة إضافة/تعديل بنر إعلاني
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AppTheme.accentGold.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (editingBanner != null) "✏️ تعديل البنر الإعلاني" else "📢 إضافة بنر إعلاني جديد",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان البنر الإعلاني", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("رابط الإجراء أو رقم الهاتف للتواصل (عند النقر)", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = displayDurationSeconds,
                    onValueChange = { displayDurationSeconds = it },
                    label = { Text("مدة عرض البنر (بالثواني) - الافتراضي 5", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // اختيار ملف الوسائط من المعرض
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("صورة البنر أو ملف الفيديو القصير:", color = Color.Gray, fontSize = 11.sp)
                    Button(
                        onClick = { mediaLauncher.launch("image/* video/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.darkBg),
                        border = BorderStroke(1.dp, AppTheme.accentGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = AppTheme.accentGold)
                            Text("📁 اختر ملفاً من الاستوديو (صورة / فيديو)", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    
                    if (mediaUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppTheme.darkBg, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(if (isVideo) "🎥 نوع الملف: فيديو" else "🖼️ نوع الملف: صورة", color = Color.Gray, fontSize = 10.sp)
                            
                            // معاينة بسيطة إذا كانت صورة
                            if (!isVideo) {
                                val bitmap = com.Serviseyem.rememberBase64Bitmap(mediaUrl)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "معاينة البنر",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            } else {
                                Icon(Icons.Default.VideoLibrary, contentDescription = "معاينة فيديو", tint = AppTheme.accentGold, modifier = Modifier.size(36.dp))
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            IconButton(
                                onClick = { mediaUrl = ""; isVideo = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "حذف", tint = AppTheme.primaryRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.clickable { isActive = !isActive },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text(
                        text = "🟢 تفعيل البنر وعرضه للمستخدمين حالياً",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال عنوان البنر", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (mediaUrl.isBlank()) {
                            Toast.makeText(context, "الرجاء اختيار صورة أو فيديو للبنر", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val durationMs = (displayDurationSeconds.toLongOrNull() ?: 5) * 1000
                        val banner = AppBanner(
                            id = if (editingBanner != null) editingBanner!!.id else UUID.randomUUID().toString(),
                            title = title,
                            mediaUrl = mediaUrl,
                            isVideo = isVideo,
                            displayDurationMs = durationMs,
                            isActive = isActive,
                            linkUrl = linkUrl
                        )
                        
                        if (editingBanner != null) {
                            viewModel.updateBanner(banner)
                            Toast.makeText(context, "✅ تم تعديل البنر بنجاح", Toast.LENGTH_SHORT).show()
                            editingBanner = null
                        } else {
                            viewModel.addBanner(banner)
                            Toast.makeText(context, "✅ تم إضافة البنر بنجاح", Toast.LENGTH_SHORT).show()
                        }
                        
                        // تصفير الحقول
                        title = ""
                        linkUrl = ""
                        mediaUrl = ""
                        isVideo = false
                        displayDurationSeconds = "5"
                        isActive = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (editingBanner != null) AppTheme.accentGold else AppTheme.primaryRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (editingBanner != null) "تحديث البنر الإعلاني ✏️" else "حفظ ونشر البنر الإعلاني 📢",
                        color = if (editingBanner != null) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // قائمة البنرات المضافة حالياً
        Text(
            text = "📋 البنرات واللوحات الإعلانية النشطة",
            color = AppTheme.accentGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        if (banners.isEmpty()) {
            Text(
                text = "لا توجد لوحات إعلانية مضافة بعد",
                color = Color.Gray,
                fontSize = 12.sp
            )
        } else {
            banners.forEach { banner ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                    border = BorderStroke(1.dp, Color(0xFF223639)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // عرض صورة البنر أو إشارة الفيديو
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppTheme.darkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (banner.isVideo) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = "فيديو إعلاني", tint = AppTheme.accentGold, modifier = Modifier.size(24.dp))
                                } else {
                                    val bitmap = com.Serviseyem.rememberBase64Bitmap(banner.mediaUrl)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "صورة البنر",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.BrokenImage, contentDescription = "لا توجد صورة", tint = Color.Gray)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column {
                                Text(
                                    text = banner.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "⏱️ مدة العرض: ${banner.displayDurationMs / 1000} ثوانٍ",
                                    color = AppTheme.grayText,
                                    fontSize = 10.sp
                                )
                                if (banner.linkUrl.isNotBlank()) {
                                    Text(
                                        text = "🔗 الإجراء: ${banner.linkUrl}",
                                        color = AppTheme.accentGold.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = if (banner.isActive) "🟢 نشط ويعرض للزوار" else "🔴 معطل مؤقتاً",
                                    color = if (banner.isActive) Color(0xFF50C878) else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    editingBanner = banner
                                    title = banner.title
                                    linkUrl = banner.linkUrl
                                    mediaUrl = banner.mediaUrl
                                    isVideo = banner.isVideo
                                    displayDurationSeconds = (banner.displayDurationMs / 1000).toString()
                                    isActive = banner.isActive
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "تعديل",
                                    tint = AppTheme.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    viewModel.deleteBanner(banner.id)
                                    Toast.makeText(context, "🗑️ تم حذف البنر بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = AppTheme.primaryRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 📂 تبويب الأقسام
// ============================================================
@Composable
fun CategoriesTab(
    viewModel: MainViewModel,
    categories: List<Category>
) {
    val context = LocalContext.current
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var isPublished by remember { mutableStateOf(true) }
    var isSubcategory by remember { mutableStateOf(false) }
    var selectedParentId by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    
    // منتقي الصور من معرض الصور بالهاتف لتسجيل أيقونة/صورة مخصصة للقسم
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val base64 = com.Serviseyem.compressImageBase64(inputStream)
                if (base64 != null) {
                    iconUrl = base64
                    Toast.makeText(context, "📸 تم اختيار صورة القسم بنجاح!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل في معالجة الصورة المختارة", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // نموذج إضافة/تعديل قسم
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AppTheme.accentGold.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (editingCategory != null) "✏️ تعديل القسم" else "📂 إضافة قسم جديد (رئيسي أو فرعي)",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("الاسم بالعربية", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("الاسم بالإنجليزية (اختياري)", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // خيارات نوع القسم (رئيسي أو فرعي)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isSubcategory = false }
                    ) {
                        RadioButton(
                            selected = !isSubcategory,
                            onClick = { isSubcategory = false },
                            colors = RadioButtonDefaults.colors(selectedColor = AppTheme.accentGold)
                        )
                        Text("📁 قسم رئيسي", color = Color.White, fontSize = 11.sp)
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isSubcategory = true }
                    ) {
                        RadioButton(
                            selected = isSubcategory,
                            onClick = { isSubcategory = true },
                            colors = RadioButtonDefaults.colors(selectedColor = AppTheme.accentGold)
                        )
                        Text("🌿 قسم فرعي", color = Color.White, fontSize = 11.sp)
                    }
                }
                
                // اختيار القسم الرئيسي في حالة اختيار "قسم فرعي"
                if (isSubcategory) {
                    val mainCategories = categories.filter { it.parentId.isBlank() && it.id != editingCategory?.id }
                    if (mainCategories.isEmpty()) {
                        Text(
                            text = "⚠️ لا توجد أقسام رئيسية مضافة بعد لجعل هذا القسم فرعياً منها.",
                            color = AppTheme.primaryRed,
                            fontSize = 10.sp
                        )
                    } else {
                        Text("اختر القسم الرئيسي التابع له:", color = Color.Gray, fontSize = 11.sp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(mainCategories) { mainCat ->
                                val isSelected = selectedParentId == mainCat.id
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) AppTheme.accentGold else AppTheme.darkBg
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) AppTheme.accentGold else Color.Gray.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .clickable { selectedParentId = mainCat.id }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${mainCat.iconUrl} ${mainCat.nameAr}",
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // اختيار الأيقونة أو صورة مخصصة من الاستوديو
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("رمز القسم (Emoji) أو صورة من المعرض:", color = Color.Gray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = if (iconUrl.startsWith("data:image")) "[ صورة مخصصة من الاستوديو ]" else iconUrl,
                            onValueChange = { 
                                if (!it.startsWith("[ صورة")) {
                                    iconUrl = it 
                                }
                            },
                            label = { Text("أيقونة/رمز إيموجي (مثال: 🛠️)", color = Color.Gray, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AppTheme.accentGold,
                                unfocusedBorderColor = Color(0xFF223639)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.darkBg),
                            border = BorderStroke(1.dp, AppTheme.accentGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🖼️ الاستوديو", color = Color.White, fontSize = 10.sp)
                        }
                    }
                    
                    // معاينة الأيقونة أو الصورة المحددة للقسم
                    if (iconUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(AppTheme.darkBg, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text("معاينة:", color = Color.Gray, fontSize = 10.sp)
                            val bitmap = com.Serviseyem.rememberBase64Bitmap(iconUrl)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "معاينة القسم",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            } else {
                                Text(
                                    text = iconUrl,
                                    color = AppTheme.accentGold,
                                    fontSize = 18.sp
                                )
                            }
                            
                            IconButton(
                                onClick = { iconUrl = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = AppTheme.primaryRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable { isPinned = !isPinned },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
                            colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                        )
                        Text(
                            text = "📌 تثبيت القسم",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                    
                    Row(
                        modifier = Modifier.clickable { isPublished = !isPublished },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPublished,
                            onCheckedChange = { isPublished = it },
                            colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                        )
                        Text(
                            text = "✅ نشر القسم",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Button(
                    onClick = {
                        if (nameAr.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال اسم القسم", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isSubcategory && selectedParentId.isBlank()) {
                            Toast.makeText(context, "الرجاء اختيار قسم رئيسي ليتبع له هذا القسم الفرعي", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val category = Category(
                            id = if (editingCategory != null) editingCategory!!.id else UUID.randomUUID().toString(),
                            nameAr = nameAr,
                            nameEn = nameEn,
                            iconUrl = iconUrl.ifBlank { "📌" },
                            parentId = if (isSubcategory) selectedParentId else "",
                            isPinned = isPinned,
                            isPublished = isPublished
                        )
                        
                        if (editingCategory != null) {
                            viewModel.updateCategory(category)
                            Toast.makeText(context, "✅ تم تعديل القسم بنجاح", Toast.LENGTH_SHORT).show()
                            editingCategory = null
                        } else {
                            viewModel.addCategory(category)
                            Toast.makeText(context, "✅ تم إضافة القسم بنجاح", Toast.LENGTH_SHORT).show()
                        }
                        
                        nameAr = ""
                        nameEn = ""
                        iconUrl = ""
                        isSubcategory = false
                        selectedParentId = ""
                        isPinned = false
                        isPublished = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (editingCategory != null) AppTheme.accentGold else AppTheme.primaryRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (editingCategory != null) "تحديث القسم ✏️" else "إضافة القسم 📂",
                        color = if (editingCategory != null) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // قائمة الأقسام
        Text(
            text = "📋 الأقسام الحالية (رئيسية وفرعية)",
            color = AppTheme.accentGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        if (categories.isEmpty()) {
            Text(
                text = "لا توجد أقسام مضافة بعد",
                color = Color.Gray,
                fontSize = 12.sp
            )
        } else {
            categories.sortedBy { it.order }.forEach { category ->
                val parentCat = if (category.parentId.isNotBlank()) categories.find { it.id == category.parentId } else null
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                    border = BorderStroke(1.dp, Color(0xFF223639)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // عرض الأيقونة المخصصة أو الإيموجي
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppTheme.darkBg),
                                contentAlignment = Alignment.Center
                            ) {
                                val bitmap = com.Serviseyem.rememberBase64Bitmap(category.iconUrl)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "صورة القسم",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = category.iconUrl.ifBlank { "📌" },
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column {
                                Text(
                                    text = category.nameAr,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (category.nameEn.isNotBlank()) {
                                    Text(
                                        text = category.nameEn,
                                        color = AppTheme.grayText,
                                        fontSize = 10.sp
                                    )
                                }
                                if (parentCat != null) {
                                    Text(
                                        text = "🌿 فرعي من: ${parentCat.nameAr}",
                                        color = AppTheme.accentGold.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "📁 قسم رئيسي",
                                        color = Color.LightGray.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (category.isPinned) {
                                Text("📌", fontSize = 12.sp)
                            }
                            
                            IconButton(
                                onClick = {
                                    editingCategory = category
                                    nameAr = category.nameAr
                                    nameEn = category.nameEn
                                    iconUrl = category.iconUrl
                                    isSubcategory = category.parentId.isNotBlank()
                                    selectedParentId = category.parentId
                                    isPinned = category.isPinned
                                    isPublished = category.isPublished
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "تعديل",
                                    tint = AppTheme.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    viewModel.deleteCategory(category.id)
                                    Toast.makeText(context, "🗑️ تم حذف القسم بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = AppTheme.primaryRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 🗺️ تبويب المدن
// ============================================================
@Composable
fun CitiesTab(
    viewModel: MainViewModel,
    cities: List<City>
) {
    val context = LocalContext.current
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🗺️ إضافة مدينة جديدة",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم المدينة بالعربية", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("اسم المدينة بالإنجليزية", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Button(
                    onClick = {
                        if (nameAr.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال اسم المدينة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val city = City(
                            id = UUID.randomUUID().toString(),
                            nameAr = nameAr,
                            nameEn = nameEn
                        )
                        viewModel.addCity(city)
                        Toast.makeText(context, "✅ تم إضافة المدينة", Toast.LENGTH_SHORT).show()
                        nameAr = ""
                        nameEn = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إضافة المدينة", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Text(
            text = "📋 المدن الحالية",
            color = AppTheme.accentGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        if (cities.isEmpty()) {
            Text(
                text = "لا توجد مدن",
                color = Color.Gray,
                fontSize = 12.sp
            )
        } else {
            cities.forEach { city ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                    border = BorderStroke(1.dp, Color(0xFF223639)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 ${city.nameAr} (${city.nameEn})",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        
                        IconButton(
                            onClick = {
                                viewModel.deleteCity(city.id)
                                Toast.makeText(context, "🗑️ تم حذف المدينة", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = AppTheme.primaryRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// ⚠️ تبويب البلاغات
// ============================================================
@Composable
fun ReportsTab(viewModel: MainViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⚠️ إدارة البلاغات والتقارير",
            color = AppTheme.accentGold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// 💬 تبويب الدردشات (للأدمن)
// ============================================================
@Composable
fun ChatsAdminTab(
    viewModel: MainViewModel,
    chatRooms: List<ChatRoom>
) {
    val context = LocalContext.current
    var selectedChatId by remember { mutableStateOf<String?>(null) }
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    var adminReply by remember { mutableStateOf("") }
    
    if (selectedChatId != null) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.surfaceDark)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedChatId = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                    Text(
                        text = "📖 تفاصيل المحادثة",
                        color = AppTheme.accentGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = {
                        viewModel.deleteChatRoom(selectedChatId!!)
                        selectedChatId = null
                        Toast.makeText(context, "🗑️ تم حذف المحادثة", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = Color.Red
                    )
                }
            }
            
            val chatMessages = messages.filter { it.chatId == selectedChatId }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages) { message ->
                    val isAdmin = message.senderType == "admin"
                    val isUser = message.senderType == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAdmin || isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isAdmin || isUser) 12.dp else 4.dp,
                                        bottomEnd = if (isAdmin || isUser) 4.dp else 12.dp
                                    )
                                )
                                .background(
                                    when {
                                        isAdmin -> Color(0xFFE65100)
                                        isUser -> AppTheme.primaryRed
                                        else -> AppTheme.surfaceDark
                                    }
                                )
                                .padding(10.dp)
                                .widthIn(max = 260.dp)
                        ) {
                            Column {
                                Text(
                                    text = when {
                                        isAdmin -> "🛡️ مشرف"
                                        isUser -> "👤 مستخدم"
                                        else -> "🔧 مقدم"
                                    },
                                    color = AppTheme.accentGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = message.message,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.surfaceDark)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = adminReply,
                    onValueChange = { adminReply = it },
                    placeholder = { Text("رد المشرف...", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = AppTheme.darkBg,
                        unfocusedContainerColor = AppTheme.darkBg,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                IconButton(
                    onClick = {
                        if (adminReply.isNotBlank()) {
                            val msg = ChatMessage(
                                id = UUID.randomUUID().toString(),
                                chatId = selectedChatId!!,
                                senderId = "admin",
                                senderName = "المشرف العام",
                                senderType = "admin",
                                message = adminReply
                            )
                            viewModel.sendMessage(selectedChatId!!, msg)
                            adminReply = ""
                            Toast.makeText(context, "✅ تم إرسال رد المشرف", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppTheme.accentGold)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "إرسال",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💬 إدارة المحادثات",
                    color = AppTheme.accentGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = {
                        val currentSettings = viewModel.settings.value
                        val updated = currentSettings.copy(
                            isChatEnabled = !currentSettings.isChatEnabled
                        )
                        viewModel.updateSettings(updated)
                        Toast.makeText(
                            context,
                            if (updated.isChatEnabled) "✅ تم تفعيل الدردشة" else "🔒 تم تعطيل الدردشة",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.settings.value.isChatEnabled) 
                            Color(0xFF2E7D32) 
                        else 
                            AppTheme.primaryRed
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (viewModel.settings.value.isChatEnabled) "🔓 مفعل" else "🔒 معطل",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (chatRooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد محادثات نشطة",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chatRooms) { room ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                            border = BorderStroke(1.dp, Color(0xFF223639)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedChatId = room.id }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = room.participantNames.values.joinToString(" ↔ "),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = room.lastMessage,
                                        color = AppTheme.grayText,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        viewModel.deleteChatRoom(room.id)
                                        Toast.makeText(context, "🗑️ تم حذف المحادثة", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 👥 تبويب المزودين
// ============================================================
@Composable
fun ProvidersListTab(
    viewModel: MainViewModel,
    providers: List<Provider>
) {
    val context = LocalContext.current
    
    if (providers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد مزودي خدمات",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        return
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(providers) { provider ->
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                border = BorderStroke(1.dp, Color(0xFF223639)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = provider.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (provider.isVerified) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "موثق",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (provider.isRecommended) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "موصى به",
                                    tint = AppTheme.accentGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (provider.isSubscribed) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AppTheme.accentGold)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "VIP",
                                        color = Color.Black,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "📞 ${provider.phone} | 📍 ${provider.city}",
                            color = AppTheme.grayText,
                            fontSize = 11.sp
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            viewModel.deleteProvider(provider.id)
                            Toast.makeText(context, "🗑️ تم حذف ${provider.name}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = AppTheme.primaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 🏆 تبويب الترقيات
// ============================================================
@Composable
fun SubscriptionsTab(
    viewModel: MainViewModel,
    providers: List<Provider>
) {
    val context = LocalContext.current
    
    if (providers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد مزودي خدمات",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        return
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(providers) { provider ->
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                border = BorderStroke(1.dp, Color(0xFF223639)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = provider.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = provider.isSubscribed,
                                onCheckedChange = {
                                    val updated = provider.copy(isSubscribed = it)
                                    viewModel.updateProvider(updated)
                                    Toast.makeText(context, "✅ تم تعديل الاشتراك", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Text("👑 VIP", color = Color.White, fontSize = 11.sp)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = provider.isPinned,
                                onCheckedChange = {
                                    val updated = provider.copy(isPinned = it)
                                    viewModel.updateProvider(updated)
                                    Toast.makeText(context, "✅ تم تعديل التثبيت", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Text("📌 تثبيت", color = Color.White, fontSize = 11.sp)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = provider.isRecommended,
                                onCheckedChange = {
                                    val updated = provider.copy(isRecommended = it)
                                    viewModel.updateProvider(updated)
                                    Toast.makeText(context, "✅ تم تعديل التوصية", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Text("⭐ توصية", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 👮 تبويب المشرفين
// ============================================================
@Composable
fun AdminsTab(
    viewModel: MainViewModel,
    adminAccounts: List<AdminAccount>
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var canApproveRequests by remember { mutableStateOf(true) }
    var canManageCategories by remember { mutableStateOf(false) }
    var canManageBanners by remember { mutableStateOf(false) }
    var canDeleteProviders by remember { mutableStateOf(false) }
    var canSeeReports by remember { mutableStateOf(false) }
    var canManageChats by remember { mutableStateOf(false) }
    
    var editingAdmin by remember { mutableStateOf<AdminAccount?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (editingAdmin != null) "✏️ تعديل المشرف" else "👮 إضافة مشرف جديد",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Text(
                    text = "الصلاحيات:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canApproveRequests,
                        onCheckedChange = { canApproveRequests = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text("قبول الطلبات", color = Color.White, fontSize = 10.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canManageCategories,
                        onCheckedChange = { canManageCategories = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text("إدارة الأقسام", color = Color.White, fontSize = 10.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canManageBanners,
                        onCheckedChange = { canManageBanners = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text("إدارة البنرات", color = Color.White, fontSize = 10.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canDeleteProviders,
                        onCheckedChange = { canDeleteProviders = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text("حذف المزودين", color = Color.White, fontSize = 10.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canSeeReports,
                        onCheckedChange = { canSeeReports = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text("رؤية البلاغات", color = Color.White, fontSize = 10.sp)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canManageChats,
                        onCheckedChange = { canManageChats = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.accentGold)
                    )
                    Text("إدارة الدردشات", color = Color.White, fontSize = 10.sp)
                }
                
                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "الرجاء تعبئة اسم المستخدم وكلمة المرور", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val admin = AdminAccount(
                            username = username,
                            passwordHash = password,
                            canApproveRequests = canApproveRequests,
                            canManageCategories = canManageCategories,
                            canManageBanners = canManageBanners,
                            canDeleteProviders = canDeleteProviders,
                            canSeeReports = canSeeReports,
                            canManageChats = canManageChats
                        )
                        
                        if (editingAdmin != null) {
                            viewModel.updateAdminAccount(admin)
                            Toast.makeText(context, "✅ تم تعديل المشرف", Toast.LENGTH_SHORT).show()
                            editingAdmin = null
                        } else {
                            viewModel.addAdminAccount(admin)
                            Toast.makeText(context, "✅ تم إضافة المشرف", Toast.LENGTH_SHORT).show()
                        }
                        
                        username = ""
                        password = ""
                        canApproveRequests = true
                        canManageCategories = false
                        canManageBanners = false
                        canDeleteProviders = false
                        canSeeReports = false
                        canManageChats = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (editingAdmin != null) AppTheme.accentGold else AppTheme.primaryRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (editingAdmin != null) "تحديث المشرف" else "إضافة المشرف",
                        color = if (editingAdmin != null) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================
// 🎨 تبويب الألوان والثيم
// ============================================================
@Composable
fun ThemeColorsTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    
    var primaryHex by remember { mutableStateOf(settings.primaryColorHex) }
    var accentHex by remember { mutableStateOf(settings.accentColorHex) }
    var bgHex by remember { mutableStateOf(settings.bgColorHex) }
    var surfaceHex by remember { mutableStateOf(settings.surfaceColorHex) }
    var fontHex by remember { mutableStateOf(settings.fontColorHex) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎨 تخصيص الألوان والثيم",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                ColorPickerField(
                    label = "اللون الأساسي (Primary)",
                    value = primaryHex,
                    onValueChange = { primaryHex = it }
                )
                
                ColorPickerField(
                    label = "اللون الثانوي (Accent)",
                    value = accentHex,
                    onValueChange = { accentHex = it }
                )
                
                ColorPickerField(
                    label = "لون الخلفية (Background)",
                    value = bgHex,
                    onValueChange = { bgHex = it }
                )
                
                ColorPickerField(
                    label = "لون السطح (Surface)",
                    value = surfaceHex,
                    onValueChange = { surfaceHex = it }
                )
                
                ColorPickerField(
                    label = "لون الخطوط (Font)",
                    value = fontHex,
                    onValueChange = { fontHex = it }
                )
                
                Button(
                    onClick = {
                        val updated = settings.copy(
                            primaryColorHex = primaryHex,
                            accentColorHex = accentHex,
                            bgColorHex = bgHex,
                            surfaceColorHex = surfaceHex,
                            fontColorHex = fontHex
                        )
                        viewModel.updateSettings(updated)
                        Toast.makeText(context, "✅ تم حفظ الألوان", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ الألوان", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ColorPickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val presetColors = listOf(
        "#CE1126", "#FFD700", "#0D47A1", "#25D366",
        "#E65100", "#4CAF50", "#9C27B0", "#F44336",
        "#FFFFFF", "#000000", "#607D8B", "#FF6B6B"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AppTheme.accentGold,
                unfocusedBorderColor = Color(0xFF223639)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presetColors.forEach { hex ->
                val isSelected = value == hex
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onValueChange(hex) }
                )
            }
        }
    }
}

// ============================================================
// 🔔 تبويب الإشعارات
// ============================================================
@Composable
fun NotificationsTab(
    viewModel: MainViewModel,
    notifications: List<Notification>
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf("all") }
    
    val targetOptions = listOf(
        "all" to "الجميع",
        "users" to "المستخدمين",
        "providers" to "مقدمي الخدمات",
        "admins" to "المشرفين"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔔 إرسال إشعار جديد",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("المحتوى", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    targetOptions.forEach { (value, label) ->
                        val isSelected = targetRole == value
                        FilterChip(
                            selected = isSelected,
                            onClick = { targetRole = value },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTheme.primaryRed,
                                selectedLabelColor = Color.White,
                                containerColor = AppTheme.surfaceDark,
                                labelColor = Color.White
                            )
                        )
                    }
                }
                
                Button(
                    onClick = {
                        if (title.isBlank() || body.isBlank()) {
                            Toast.makeText(context, "الرجاء كتابة العنوان والمحتوى", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val notification = Notification(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            body = body,
                            targetRole = targetRole,
                            timestamp = System.currentTimeMillis()
                        )
                        viewModel.addNotification(notification)
                        Toast.makeText(context, "✅ تم إرسال الإشعار", Toast.LENGTH_SHORT).show()
                        title = ""
                        body = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إرسال الإشعار", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================
// 📅 تبويب الحجوزات
// ============================================================
@Composable
fun BookingsTab(
    viewModel: MainViewModel,
    bookings: List<BookingRequest>
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var filterStatus by remember { mutableStateOf("") }
    
    val statusOptions = listOf(
        "" to "الكل",
        "pending" to "⏳ قيد الانتظار",
        "accepted" to "✅ مقبول",
        "in_progress" to "🔧 قيد التنفيذ",
        "completed" to "🎉 مكتمل",
        "cancelled" to "❌ ملغي"
    )
    
    val routingOptions = listOf(
        "supervisor" to "لمشرف القسم أولاً 👮",
        "closest" to "لأقرب فني جغرافياً 🗺️",
        "all_section" to "لكل فنيي القسم 👥",
        "preassigned" to "لفني محدد مسبقاً 👤",
        "admin_only" to "للأدمن أولاً 🔐"
    )
    
    val filteredBookings = if (filterStatus.isEmpty()) bookings else bookings.filter { it.status == filterStatus }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // بطاقة التحكم في منطق توزيع الحجوزات
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            border = BorderStroke(1.dp, AppTheme.accentGold.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚙️ منطق توزيع وتوجيه الحجوزات (الذكي):",
                    color = AppTheme.accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "اختر الطريقة المعتمدة لتوزيع طلبات الحجز المباشر على المهنيين في النظام سحابياً:",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    routingOptions.forEach { (mode, label) ->
                        val isSelected = settings.bookingsRoutingMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) AppTheme.primaryRed.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    val updated = settings.copy(bookingsRoutingMode = mode)
                                    viewModel.updateSettings(updated)
                                    Toast.makeText(context, "✅ تم تغيير نظام توزيع الحجوزات إلى: $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    val updated = settings.copy(bookingsRoutingMode = mode)
                                    viewModel.updateSettings(updated)
                                    Toast.makeText(context, "✅ تم تغيير نظام توزيع الحجوزات إلى: $label", Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AppTheme.accentGold,
                                    unselectedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                color = if (isSelected) AppTheme.accentGold else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        
        Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statusOptions.forEach { (value, label) ->
                val isSelected = filterStatus == value
                FilterChip(
                    selected = isSelected,
                    onClick = { filterStatus = if (isSelected) "" else value },
                    label = {
                        Text(
                            text = label,
                            fontSize = 9.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTheme.primaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = AppTheme.surfaceDark,
                        labelColor = Color.White
                    )
                )
            }
        }
        
        Text(
            text = "📅 الحجوزات (${filteredBookings.size})",
            color = AppTheme.accentGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        if (filteredBookings.isEmpty()) {
            Text(
                text = "لا توجد حجوزات",
                color = Color.Gray,
                fontSize = 12.sp
            )
        } else {
            filteredBookings.forEach { booking ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                    border = BorderStroke(1.dp, Color(0xFF223639)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${booking.userName} → ${booking.providerName}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        
                        Text(
                            text = "🔧 ${booking.serviceType} | 📍 ${booking.residenceArea}",
                            color = AppTheme.grayText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 🧹 تبويب التطهير
// ============================================================
@Composable
fun PurgeTab(
    viewModel: MainViewModel,
    showDialog: Boolean,
    password: String,
    error: Boolean,
    onPurgeClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
        border = BorderStroke(2.dp, AppTheme.primaryRed),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = AppTheme.primaryRed,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "⚠️ تطهير قواعد البيانات",
                color = AppTheme.primaryRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onPurgeClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🧹 تطهير البيانات والبدء من جديد",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================
// 🚪 البوابة الخلفية - تسجيل الدخول
// ============================================================
@Composable
fun BackdoorLoginDialog(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, AppTheme.accentGold),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppTheme.accentGold,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "🔐 البوابة الخلفية - المالك",
                    color = AppTheme.accentGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        error = false
                    },
                    label = { Text("كلمة المرور", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = PasswordVisualTransformation()
                )
                
                if (error) {
                    Text(
                        text = "❌ كلمة المرور غير صحيحة!",
                        color = AppTheme.primaryRed,
                        fontSize = 11.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = Color.White)
                    }
                    
                    Button(
                        onClick = {
                            if (password == BACKDOOR_PASSWORD) {
                                onSuccess()
                            } else {
                                error = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accentGold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("دخول", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================================
// 🛠️ البوابة الخلفية - لوحة التحكم
// ============================================================
@Composable
fun BackdoorControlPanelDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var appName by remember { mutableStateOf(settings.appNameAr) }
    var primaryHex by remember { mutableStateOf(settings.primaryColorHex) }
    var accentHex by remember { mutableStateOf(settings.accentColorHex) }
    var bgHex by remember { mutableStateOf(settings.bgColorHex) }
    var surfaceHex by remember { mutableStateOf(settings.surfaceColorHex) }
    var fontHex by remember { mutableStateOf(settings.fontColorHex) }
    var footerText by remember { mutableStateOf(settings.footerText) }
    var aboutPhone by remember { mutableStateOf(settings.aboutPhone) }
    var aboutWhatsapp by remember { mutableStateOf(settings.aboutWhatsapp) }
    var aboutEmail by remember { mutableStateOf(settings.aboutEmail) }
    var adminPassword by remember { mutableStateOf(settings.adminPassword) }
    var isChatEnabled by remember { mutableStateOf(settings.isChatEnabled) }
    var chatIconSize by remember { mutableStateOf(settings.chatIconSize.toFloat()) }
    var assistantIconSize by remember { mutableStateOf(settings.assistantIconSize.toFloat()) }
    var radiusSearchLimit by remember { mutableStateOf(settings.radiusSearchLimitKm.toFloat()) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, AppTheme.accentGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛠️ لوحة التحكم السرية - المالك",
                        color = AppTheme.accentGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("اسم التطبيق", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppTheme.accentGold,
                            unfocusedBorderColor = Color(0xFF223639)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    ColorPickerField(
                        label = "اللون الأساسي",
                        value = primaryHex,
                        onValueChange = { primaryHex = it }
                    )
                    
                    ColorPickerField(
                        label = "اللون الثانوي",
                        value = accentHex,
                        onValueChange = { accentHex = it }
                    )
                    
                    ColorPickerField(
                        label = "لون الخلفية",
                        value = bgHex,
                        onValueChange = { bgHex = it }
                    )
                    
                    ColorPickerField(
                        label = "لون السطح",
                        value = surfaceHex,
                        onValueChange = { surfaceHex = it }
                    )
                    
                    ColorPickerField(
                        label = "لون الخطوط",
                        value = fontHex,
                        onValueChange = { fontHex = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val updated = settings.copy(
                                appNameAr = appName,
                                primaryColorHex = primaryHex,
                                accentColorHex = accentHex,
                                bgColorHex = bgHex,
                                surfaceColorHex = surfaceHex,
                                fontColorHex = fontHex,
                                footerText = footerText,
                                aboutPhone = aboutPhone,
                                aboutWhatsapp = aboutWhatsapp,
                                aboutEmail = aboutEmail,
                                adminPassword = adminPassword,
                                isChatEnabled = isChatEnabled,
                                chatIconSize = chatIconSize.toInt(),
                                assistantIconSize = assistantIconSize.toInt(),
                                radiusSearchLimitKm = radiusSearchLimit.toInt()
                            )
                            viewModel.updateSettings(updated)
                            Toast.makeText(context, "✅ تم حفظ الإعدادات السرية", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("خروج", color = Color.White)
                    }
                }
            }
        }
    }
}

// ============================================================
// 🚪 نافذة تسجيل الدخول الإجبارية
// ============================================================
@Composable
fun LoginRequiredDialog(
    onSuccess: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var residenceArea by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, AppTheme.accentGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🔐 تسجيل الدخول مطلوب",
                    color = AppTheme.accentGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "للاستمرار، الرجاء إدخال بياناتك الأساسية",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("الاسم الثلاثي", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("رقم الهاتف", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                OutlinedTextField(
                    value = residenceArea,
                    onValueChange = { residenceArea = it },
                    label = { Text("منطقة السكن", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إلغاء", color = Color.White, fontSize = 11.sp)
                    }
                    
                    Button(
                        onClick = {
                            if (userName.isBlank() || phoneNumber.isBlank() || residenceArea.isBlank()) {
                                Toast.makeText(context, "الرجاء تعبئة جميع الحقول", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val userId = "user_${System.currentTimeMillis()}"
                            onSuccess(userId, "user")
                            Toast.makeText(context, "✅ تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تسجيل الدخول", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================================
// 📜 شروط وقواعد العمل بالمنصة (إدارة الأدمن)
// ============================================================
@Composable
fun RegistrationConditionsTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var newText by remember { mutableStateOf("") }
    var isNewRequired by remember { mutableStateOf(true) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "📜 إدارة شروط وقواعد الانضمام",
                color = AppTheme.accentGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "يمكنك إضافة شروط جديدة، جعلها إجبارية أو اختيارية، أو حذف الشروط القديمة لضمان جودة مقدمي الخدمة.",
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            
            // إضافة شرط جديد
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    label = { Text("نص الشرط الجديد", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجباري؟", color = Color.White, fontSize = 9.sp)
                    Checkbox(
                        checked = isNewRequired,
                        onCheckedChange = { isNewRequired = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppTheme.primaryRed,
                            checkmarkColor = Color.White
                        )
                    )
                }
                
                Button(
                    onClick = {
                        if (newText.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال نص الشرط أولاً", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newId = "cond_${System.currentTimeMillis()}"
                        val newCondition = RegistrationCondition(
                            id = newId,
                            text = newText,
                            isRequired = isNewRequired
                        )
                        val updatedList = settings.registrationConditions + newCondition
                        viewModel.updateSettings(settings.copy(registrationConditions = updatedList))
                        newText = ""
                        Toast.makeText(context, "✅ تم إضافة الشرط بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("إضافة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            
            // قائمة الشروط الحالية
            Text("الشروط الحالية:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (settings.registrationConditions.isEmpty()) {
                    Text("لا توجد شروط مدخلة حالياً.", color = Color.Gray, fontSize = 11.sp)
                } else {
                    settings.registrationConditions.forEach { condition ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppTheme.darkBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = condition.text, color = Color.White, fontSize = 12.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (condition.isRequired) "⚠️ إجباري" else "✨ اختياري",
                                            color = if (condition.isRequired) AppTheme.accentGold else Color.Gray,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تغيير الحالة",
                                            color = AppTheme.accentGold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.clickable {
                                                val updatedList = settings.registrationConditions.map {
                                                    if (it.id == condition.id) it.copy(isRequired = !it.isRequired) else it
                                                }
                                                viewModel.updateSettings(settings.copy(registrationConditions = updatedList))
                                            }
                                        )
                                    }
                                }
                                
                                IconButton(onClick = {
                                    val updatedList = settings.registrationConditions.filter { it.id != condition.id }
                                    viewModel.updateSettings(settings.copy(registrationConditions = updatedList))
                                    Toast.makeText(context, "🗑️ تم حذف الشرط", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AppTheme.primaryRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

