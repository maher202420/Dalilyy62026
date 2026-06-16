package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.*
import java.text.SimpleDateFormat
import java.util.*

val Gold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreens(
    viewModel: AppViewModel,
    onNavigateToAdminPortal: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Navigation and quick register constraints
    var showUserSignupDialog by remember { mutableStateOf(false) }
    var userPhoneInput by remember { mutableStateOf("") }
    var userNameInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    // Intercept features if registration is mandatory
    fun checkAndRun(action: () -> Unit) {
        if (viewModel.appSetup.isUserRegistrationMandatory && viewModel.loggedInUserId == null) {
            showUserSignupDialog = true
        } else {
            action()
        }
    }

    // Scaffold holding bottom bar
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF131318),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                        unselectedIconColor = Color.LightGray,
                        indicatorColor = Color(0xFF1F1F24)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { checkAndRun { selectedTab = 1 } },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "WAM Assistant") },
                    label = { Text("المساعد WAM", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                        unselectedIconColor = Color.LightGray,
                        indicatorColor = Color(0xFF1F1F24)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { checkAndRun { selectedTab = 2 } },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = "New Tech") },
                    label = { Text("سجل كفني", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                        unselectedIconColor = Color.LightGray,
                        indicatorColor = Color(0xFF1F1F24)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("بوابة نفاذ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                        unselectedIconColor = Color.LightGray,
                        indicatorColor = Color(0xFF1F1F24)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0D0D11))
        ) {
            when (selectedTab) {
                0 -> UserHomeScreen(viewModel)
                1 -> ChatbotWamScreen(viewModel)
                2 -> TechnicianRegisterScreen(viewModel)
                3 -> ProfilePortalSelectorScreen(viewModel, onNavigateToAdminPortal)
            }

            // Quick signup/login Dialog (Optional/Mandatory)
            if (showUserSignupDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!viewModel.appSetup.isUserRegistrationMandatory) showUserSignupDialog = false
                    },
                    containerColor = Color(0xFF1B1B22),
                    shape = RoundedCornerShape(24.dp),
                    title = {
                        Text(
                            text = if (otpSent) "رمز التحقق (OTP) 💬" else "تسجيل مستخدم سريع 🚪",
                            color = Color.White,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!otpSent) {
                                Text(
                                    text = "الرجاء إدخال رقم الهاتف والاسم للاستفادة الكاملة من مميزات التطبيق وحفظ نقاط الولاء الخاصة بك.",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right
                                )
                                OutlinedTextField(
                                    value = userNameInput,
                                    onValueChange = { userNameInput = it },
                                    label = { Text("الاسم الكامل") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                                        focusedLabelColor = Color.White,
                                        unfocusedLabelColor = Color.LightGray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = userPhoneInput,
                                    onValueChange = { userPhoneInput = it },
                                    label = { Text("رقم جوالك اليمني (مثال: 77xxxxxxx)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            } else {
                                Text(
                                    text = "تم إرسال رمز التحقق الافتراضي بنجاح إلى الرقم $userPhoneInput. أدخل الرمز لتأكيد ملكيتك لهاتف الصيانة.",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right
                                )
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it },
                                    label = { Text("رمز التحقق OTP (أدخل 2026)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex))
                            ),
                            onClick = {
                                if (!otpSent) {
                                    if (userNameInput.isNotBlank() && userPhoneInput.length >= 9) {
                                        otpSent = true
                                    } else {
                                        Toast.makeText(context, "الرجاء ملء الاسم ورقم الهاتف بشكل صحيح", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    if (otpInput == "2026" || otpInput == "1234") {
                                        // login user
                                        val existing = viewModel.clientUsers.find { it.phone == userPhoneInput }
                                        if (existing != null) {
                                            viewModel.loggedInUserId = existing.id
                                            viewModel.loggedInName = existing.name
                                            viewModel.loggedInPhone = existing.phone
                                            viewModel.currentUserRole = UserRole.REGISTERED
                                        } else {
                                            val newUser = ClientUser(
                                                name = userNameInput,
                                                phone = userPhoneInput,
                                                loyaltyPoints = 20, // free starter pts
                                                registrationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                                registrationType = "Mandatory"
                                            )
                                            viewModel.clientUsers = viewModel.clientUsers + newUser
                                            viewModel.loggedInUserId = newUser.id
                                            viewModel.loggedInName = newUser.name
                                            viewModel.loggedInPhone = newUser.phone
                                            viewModel.currentUserRole = UserRole.REGISTERED
                                        }
                                        viewModel.addAudit(userNameInput, "تسجيل دخول", "تم تسجيل العميل عبر OTP بنجاح")
                                        showUserSignupDialog = false
                                        otpSent = false
                                        Toast.makeText(context, "أهلاً بك بكامل خدمات اليمن!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "كود التحقق غير صحيح! الرجاء تجربة الكود المعطى [2026]", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text(if (!otpSent) "إرسال رمز OTP" else "تأكيد وتفعيل")
                        }
                    },
                    dismissButton = {
                        if (!viewModel.appSetup.isUserRegistrationMandatory) {
                            TextButton(onClick = { showUserSignupDialog = false }) {
                                Text("إلغاء وتصفح كزائر")
                            }
                        }
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// USER SCREEN 1: UserHomeScreen (Browse providers, search & actions, ads video)
// -------------------------------------------------------------
@Composable
fun UserHomeScreen(viewModel: AppViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityFilter by remember { mutableStateOf("الكل") }

    // Booking trigger states
    var bookingTech by remember { mutableStateOf<Technician?>(null) }
    var clientName by remember { mutableStateOf(viewModel.loggedInName) }
    var clientPhone by remember { mutableStateOf(viewModel.loggedInPhone) }
    var selectedSpecialty by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("صنعاء") }

    var showConfirmationDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(viewModel.loggedInUserId) {
        if (viewModel.loggedInUserId != null) {
            clientName = viewModel.loggedInName
            clientPhone = viewModel.loggedInPhone
        }
    }

    // Filter providers
    val filteredTechs = viewModel.technicians.filter { tech ->
        tech.state == TechnicianState.ACTIVE &&
                (selectedCityFilter == "الكل" || tech.region == selectedCityFilter) &&
                (searchQuery.isBlank() ||
                        tech.name.contains(searchQuery, ignoreCase = true) ||
                        tech.specialty.contains(searchQuery, ignoreCase = true) ||
                        tech.phone.contains(searchQuery) ||
                        tech.region.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Core Header & Welcome
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A22)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.appSetup.appName,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = viewModel.appSetup.appDescription,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = "Logo",
                            tint = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                            modifier = Modifier
                                .padding(12.dp)
                                .size(32.dp)
                        )
                    }
                }
            }
        }

        // Ads Panel with image or simulated Video Player (Section 6 & 27)
        if (viewModel.adverts.isNotEmpty()) {
            item {
                Text(
                    text = "العروض والإعلانات الممولّة 📣",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.adverts.filter { it.isActive }) { ad ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Background static vector simulation or gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFF2E2E38), Color(0xFF13131A))
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Badge(
                                            containerColor = if (ad.mediaType == "VIDEO") Color.Red else Color.DarkGray,
                                            contentColor = Color.White
                                        ) {
                                            Text(if (ad.mediaType == "VIDEO") "فيديو 🎥" else "إعلان 🖼️")
                                        }

                                        Text(
                                            text = "قسم: ${ad.categoryId}",
                                            color = Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Text(
                                        text = ad.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (ad.mediaType == "VIDEO") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            LinearProgressIndicator(
                                                progress = 0.6f,
                                                color = Color.Red,
                                                trackColor = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                            )
                                            Text("0:18 / 0:30", color = Color.White, fontSize = 9.sp)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loyalty Points Card (Mandatory section configuration)
        if (viewModel.appSetup.loyaltyEnabled) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1525)),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = "Points", tint = Color(0xFFD4AF37))
                                Text("برنامج مكافآت صيانة اليمن", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Show Points
                            val userPoints = viewModel.clientUsers.find { it.id == viewModel.loggedInUserId }?.loyaltyPoints ?: 0
                            Text(
                                text = "نقاطك: $userPoints ن",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "احصل على كود خصم مجاني فوري واستثمر كل حجز! بمجرد وصول نقاطك إلى ${viewModel.appSetup.pointsNeededForCoupon} نقطة، يمكنك استبدالها تلقائياً.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B34)),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    val user = viewModel.clientUsers.find { it.id == viewModel.loggedInUserId }
                                    if (user != null && user.loyaltyPoints >= viewModel.appSetup.pointsNeededForCoupon) {
                                        viewModel.modifyLoyaltyPointsManual(user.id, -viewModel.appSetup.pointsNeededForCoupon)
                                        viewModel.addCoupon(Coupon(code = "LOYAL-${UUID.randomUUID().toString().take(5).uppercase()}", discountPercent = 15))
                                        Toast.makeText(context, "تم توليد قسيمة خصم بـ 15% وإضافتها للمدير والمستخدم!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "نقاطك لا تكفي! يرجى حجز فنيين أو مشاركة التطبيق.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("استبدال النقاط 🎟️", fontSize = 11.sp)
                            }

                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C1E7A)),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    if (viewModel.loggedInUserId != null) {
                                        viewModel.modifyLoyaltyPointsManual(viewModel.loggedInUserId!!, viewModel.appSetup.pointsPerShare)
                                        Toast.makeText(context, "تمت مشاركة التطبيق! مبروك حصلت على +${viewModel.appSetup.pointsPerShare} نقطة إضافية.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "الرجاء تسجيل الدخول أولاً لتجميع نقاط المشاركة!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("مشاركة التطبيق (+${viewModel.appSetup.pointsPerShare} ن)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar, filter cities chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم الفني، المهنة، الهاتف، المنطقة...", color = Color.Gray, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Cities horizontal chips list
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCityFilter == "الكل",
                            onClick = { selectedCityFilter = "الكل" },
                            label = { Text("الجمهورية (الكل)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                                selectedLabelColor = Color.Black,
                                labelColor = Color.White
                            )
                        )
                    }
                    items(viewModel.allowedCities) { city ->
                        FilterChip(
                            selected = selectedCityFilter == city,
                            onClick = { selectedCityFilter = city },
                            label = { Text(city) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                                selectedLabelColor = Color.Black,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Technicians List representing the detailed UI card requirements (VIP, Verified, Recommended)
        if (filteredTechs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد نتائج مطابقة لبحثك في الخريطة المحلية حالياً.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredTechs) { tech ->
                TechnicianCardWidget(
                    tech = tech,
                    primaryColorHex = viewModel.appSetup.primaryColorHex,
                    onContact = {
                        Toast.makeText(context, "جاري الإتصال بالرقم الراقي: ${tech.phone}", Toast.LENGTH_LONG).show()
                    },
                    onChat = {
                        viewModel.addChatMessage(
                            senderId = viewModel.loggedInUserId ?: "user",
                            receiverId = tech.id,
                            messageText = "مرحباً يا بطل الصيانة، أرسلت لك طلباً خاصاً لخدمتي.",
                            senderType = "User",
                            senderName = viewModel.loggedInName.ifEmpty { "عميل جديد" }
                        )
                        Toast.makeText(context, "تم بدء محادثة مباشرة مع ${tech.name}", Toast.LENGTH_SHORT).show()
                    },
                    onBook = {
                        bookingTech = tech
                        selectedSpecialty = tech.specialty
                        selectedRegion = tech.region
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // Modal Booking sheet Form (Section 15 & 2): confirmation, dynamic fields, custom options
    if (bookingTech != null) {
        val bTech = bookingTech!!
        AlertDialog(
            onDismissRequest = { bookingTech = null },
            containerColor = Color(0xFF1B1B22),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "حجز صيانة مباشرة مع ${bTech.name} 🛠️",
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "الرجاء تأكيد حقول الطلب للحصول على استجابة سريعة:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("الاسم الثلاثي للعميل") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("رقم الهاتف للاتصال والتحقق") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true
                    )

                    // Region Dropdown representation
                    OutlinedTextField(
                        value = selectedRegion,
                        onValueChange = { selectedRegion = it },
                        label = { Text("منطقة السكن / الإقامة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true
                    )

                    // Requested service label
                    OutlinedTextField(
                        value = selectedSpecialty,
                        onValueChange = { selectedSpecialty = it },
                        label = { Text("الخدمة المطلوبة") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.LightGray,
                            disabledBorderColor = Color.DarkGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex))
                    ),
                    onClick = {
                        if (clientName.length >= 6 && clientPhone.length >= 9) {
                            showConfirmationDialog = true
                        } else {
                            Toast.makeText(context, "الرجاء كتابة الاسم الثلاثي ورقم الهاتف أولاً!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("تقديم الطلب")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingTech = null }) {
                    Text("إلغاء الحجز")
                }
            }
        )
    }

    // Confirmation Pop-up Dialog before saving (Second requirement)
    if (showConfirmationDialog && bookingTech != null) {
        val targetT = bookingTech!!
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            containerColor = Color(0xFF13131A),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "مراجعة وتأكيد بيانات حجزك! 🔬",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "الرجاء مراجعة البيانات بعناية قبل الترحيل والإرسال لشبكة الفنيين في المحافظة:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("الاسم الصادر: $clientName", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                            Text("رقم التواصل: $clientPhone", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                            Text("المنطقة المقصودة: $selectedRegion", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                            Text("مقدم الخدمة: ${targetT.name}", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                            Text("نوع الصيانة: $selectedSpecialty", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    onClick = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val timestamp = sdf.format(Date())

                        // instantiate booking
                        val newBooking = Booking(
                            clientName = clientName,
                            clientPhone = clientPhone,
                            requestedService = selectedSpecialty,
                            region = selectedRegion,
                            assignedTechId = targetT.id,
                            status = "قيد الانتظار",
                            dateCreated = timestamp
                        )

                        viewModel.dispatchAndSubmitBooking(newBooking)

                        // Reward some loyalty points if registered
                        if (viewModel.loggedInUserId != null) {
                            viewModel.modifyLoyaltyPointsManual(viewModel.loggedInUserId!!, 10)
                        }

                        Toast.makeText(context, "تم إرسال طلب الحجز بنجاح! شكراً جزيلاً لتعاملك.", Toast.LENGTH_LONG).show()

                        showConfirmationDialog = false
                        bookingTech = null
                    }
                ) {
                    Text("تأكيد وإرسال الحجز ✅")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("تعديل البيانات")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// USER SCREEN 2: Smart Chatbot WAM (Audio inputs, Offline Suggestions)
// -------------------------------------------------------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatbotWamScreen(viewModel: AppViewModel) {
    var userMessageText by remember { mutableStateOf("") }
    var voiceRecordingSimulated by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // WAM Chat Channel Messages
    val wamChannel = viewModel.chatChannels.find { it.userId == "wam_chat" } ?: ChatChannel("wam_chat", "المساعد الذكي WAM", "User")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D11)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header for intelligent voice bot
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robot",
                        tint = Color.Red,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "المساعد الذكي الصوتي WAM 🎙️",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = if (voiceRecordingSimulated) "جاري الاستماع لصوتك باليمن... 🎧" else "WAM متصل ومستعد للمحادثة والتشخيص الفوري",
                    color = if (voiceRecordingSimulated) Color.Green else Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Suggested offline questions (Section 12)
        if (wamChannel.messages.size <= 2) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "الأسئلة السريعة المقترحة للدليل 💡",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.appSetup.aiSuggestedQuestions) { question ->
                        ElevatedCard(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22)),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                viewModel.askWAMSmartAssistant(question)
                            }
                        ) {
                            Text(
                                text = question,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Interactive messages log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            reverseLayout = true
        ) {
            // Display all messages in reverse order of receipt
            val reversed = wamChannel.messages.reversed()
            items(reversed) { msg ->
                val isUser = msg.senderId == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Red.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = "Bot", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF2B1F4D) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(0.1f, fill = false)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.message,
                                color = Color.White,
                                fontSize = 13.sp,
                                textAlign = if (isUser) TextAlign.Right else TextAlign.Left
                            )
                        }
                    }

                    if (isUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF4C1E7A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "User", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Input bottom bar (Typing & microphone voice input simulated)
        Surface(
            color = Color(0xFF13131A),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone Voice input simulation button
                if (viewModel.appSetup.voiceInputEnabled) {
                    IconButton(
                        onClick = {
                            if (!voiceRecordingSimulated) {
                                voiceRecordingSimulated = true
                                Toast.makeText(context, "جاري فتح ميكروفون دليل اليمن الصوتي للتحليل... تحدث الآن!", Toast.LENGTH_SHORT).show()
                            } else {
                                voiceRecordingSimulated = false
                                userMessageText = "أحتاج فني سباكة ماهر وموثق بصنعاء"
                                Toast.makeText(context, "تم رصد وتحليل النص بنجاح!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (voiceRecordingSimulated) Color.Green else Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = { userMessageText = it },
                    placeholder = { Text("أكتب مشكلة الصيانة هنا لـ WAM...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (userMessageText.isNotBlank()) {
                            viewModel.askWAMSmartAssistant(userMessageText)
                            userMessageText = ""
                        }
                    },
                    modifier = Modifier.background(Color.Red, shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }

                // Delete chats log button (Red trailing tile action requirement)
                IconButton(
                    onClick = {
                        viewModel.deleteChannel("wam_chat")
                        Toast.makeText(context, "تم مسح سجل المحادثة الذكية محلياً بنجاح", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = Color.Red)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// USER SCREEN 3: Technician Register signup Screen (Section 14)
// -------------------------------------------------------------
@Composable
fun TechnicianRegisterScreen(viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var regionSelection by remember { mutableStateOf("صنعاء") }
    var addressDetail by remember { mutableStateOf("") }
    var specialtySelection by remember { mutableStateOf("سباكة") }
    var experienceYears by remember { mutableStateOf("5") }
    var bioText by remember { mutableStateOf("") }
    var hasShop by remember { mutableStateOf(false) }
    var shopAddress by remember { mutableStateOf("") }

    // Custom Fields map
    val customDataMap = remember { mutableStateMapOf<String, String>() }

    val context = LocalContext.current
    var isSubmitted by remember { mutableStateOf(false) }

    if (isSubmitted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Green.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(72.dp)
            ) {
                Icon(Icons.Default.Verified, contentDescription = "Check", tint = Color.Green, modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("طلبك قيد المراجعة الفورية! ⚖️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "جميع وثائق تقديم الخدمة والحقول المخصصة تم استلامها بنجاح بقاعدة البيانات المحفوظة. سيقوم مشرف القسم بمراجعة وتأكيد تسجيل وتفعيل باقتك مباشرة.",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                onClick = { isSubmitted = false }
            ) {
                Text("تسجيل مقدم خدمة آخر")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "استمارة تسجيل محترفي وفنيي اليمن 📋",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "انضم للآلاف من مقدمي الخدمات وكن قريباً من العملاء في محافظتك (صنعاء، إب، تعز، عدن وغيرهم) عبر الدليل العظيم للكهرباء والصيانة والسباكة.",
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم الكامل الثنائي أو الثلاثي") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم هاتفك اليمني (يجب أن يكون فريداً)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            // Region Dropdown representation
            OutlinedTextField(
                value = regionSelection,
                onValueChange = { regionSelection = it },
                label = { Text("منطقة السكن (صنعاء، إب، عدن، تعز، حضرموت...)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = addressDetail,
                onValueChange = { addressDetail = it },
                label = { Text("عنوان الإقامة بالتفصيل (الحي، الشارع)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = specialtySelection,
                onValueChange = { specialtySelection = it },
                label = { Text("المهنة / التخصص (مثال: كهرباء، سباكة، صيانة空调)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = { Text("عدد سنوات الخبرة") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = bioText,
                onValueChange = { bioText = it },
                label = { Text("وصف خبراتك وأعمالك للجمهور") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            // Has Shop option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasShop = !hasShop }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("هل تمتلك محلاً تجارياً لمزاولة المهنة؟", color = Color.White, fontSize = 13.sp)
                Switch(
                    checked = hasShop,
                    onCheckedChange = { hasShop = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Green)
                )
            }

            if (hasShop) {
                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text("عنوان المعرض أو المحل بالكامل") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            // --- ADMIN CONFIGURED CUSTOM REGISTRATION FIELDS ---
            if (viewModel.customFields.isNotEmpty()) {
                Text(
                    text = "متطلبات إضافية من الإدارة العامة 🛡️",
                    color = Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                viewModel.customFields.filter { it.isEnabled }.forEach { field ->
                    var currentVal by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = currentVal,
                        onValueChange = {
                            currentVal = it
                            customDataMap[field.id] = it
                        },
                        label = { Text(field.label + if (field.isMandatory) " (إجباري)" else "") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        Toast.makeText(context, "يرجى ملء الحقول الأساسية: الاسم والهاتف!", Toast.LENGTH_SHORT).show()
                    } else {
                        val newTech = Technician(
                            name = name,
                            phone = phone,
                            region = regionSelection,
                            addressDetail = addressDetail,
                            hasShop = hasShop,
                            shopAddress = shopAddress,
                            specialty = specialtySelection,
                            experienceYears = experienceYears.toIntOrNull() ?: 3,
                            bio = bioText,
                            state = TechnicianState.PENDING, // Pending approval
                            customFieldsData = customDataMap.toMap()
                        )
                        viewModel.addTechnician(newTech)
                        viewModel.addTargetedNotification(
                            title = "طلب تسجيل فني معلق 👨‍🔧",
                            body = "قدم الفني $name طلباً جديداً للانضمام بـ $specialtySelection في $regionSelection. بحاجة لموافقة الأدمن.",
                            targetId = "Admin"
                        )
                        isSubmitted = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text("إرسال استمارة التقديم والوثائق")
            }
        }
    }
}

// -------------------------------------------------------------
// USER SCREEN 4: Profile & Authentication Gateway Selector
// -------------------------------------------------------------
@Composable
fun ProfilePortalSelectorScreen(
    viewModel: AppViewModel,
    onNavigateToAdminPortal: () -> Unit
) {
    var inputTechPhone by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Avatar represent
        Surface(
            color = Color(0xFF1B1B22),
            shape = CircleShape,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User info",
                    tint = Color.LightGray,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        if (viewModel.loggedInUserId != null) {
            Text(
                text = "مرحباً: ${viewModel.loggedInName}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "رقم هاتفك الحافظ: ${viewModel.loggedInPhone}",
                color = Color.LightGray,
                fontSize = 12.sp
            )

            val uPoints = viewModel.clientUsers.find { it.id == viewModel.loggedInUserId }?.loyaltyPoints ?: 0
            Badge(
                containerColor = Color(0xFFD4AF37).copy(alpha = 0.2f),
                contentColor = Color(0xFFD4AF37),
                modifier = Modifier.padding(4.dp)
            ) {
                Text("رصيدك الحالي: $uPoints ن")
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                onClick = {
                    viewModel.loggedInUserId = null
                    viewModel.loggedInName = ""
                    viewModel.loggedInPhone = ""
                    viewModel.currentUserRole = UserRole.VISITOR
                    Toast.makeText(context, "تم تسجيل خروج العميل بنجاح", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("تسجيل خروج العميل")
            }
        } else {
            Text(
                text = "تصفح الدليل الآن كـ 'زائر يمني' 🗺️",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "أنت تتصفح جميع الأقسام والخدمات دون أي حاجة لتسجيل رقم الهاتف. في حال رغبتك بالتمتع بسياسات الحجوزات ونقاط الولاء، تواصل مع إحدى قنوات التفعيل.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // Technician fast portal signin
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1525)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "نظام الفني المحترف والمشرفين 👨‍🔧",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "سجل دخولك برقم هاتفك المسجل لعرض الحجوزات الموجهة إليك وتغيير حالات التقدم، أو تصفح اللوحات الإدارية العليا.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = inputTechPhone,
                    onValueChange = { inputTechPhone = it },
                    placeholder = { Text("أدخل هاتف الفني (مثال: 00967771234567)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C1E7A)),
                    onClick = {
                        val tech = viewModel.technicians.find { it.phone == inputTechPhone }
                        if (tech != null) {
                            viewModel.loggedInUserId = tech.id
                            viewModel.loggedInName = tech.name
                            viewModel.loggedInPhone = tech.phone
                            viewModel.currentUserRole = UserRole.TECHNICIAN
                            Toast.makeText(context, "مرحباً يا بطل الصيانة: ${tech.name}!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "الرقم غير مسجل كفني معتمد! جرب استخدام الرقم الافتراضي [00967771234567]", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("دخول كفني معتمد")
                }
            }
        }

        // Admin Entrance Access
        Button(
            onClick = {
                onNavigateToAdminPortal()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                Text("الدخول الآمن للبوابة الخلفية المشفرة (الأدمن/المالك)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// REUSABLE RESOURCE COMPONENT: Premium Professional Card
// -------------------------------------------------------------
@Composable
fun TechnicianCardWidget(
    tech: Technician,
    primaryColorHex: String,
    onContact: () -> Unit,
    onChat: () -> Unit,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // COVER PHOTO / BANNER (Section Component Requirement)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2E2E38), Color(0xFF141419))
                        )
                    )
            ) {
                // Overlay decorative pattern background
                Text(
                    text = "شبكة مهارات المحترفين بجمهورية اليمن دليل 2026",
                    color = Color.White.copy(alpha = 0.05f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.Center)
                )

                // VIP Gold badge represent in cover (Section components)
                if (tech.isVIP) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color(0xFFD4AF37), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "VIP", tint = Color.Black, modifier = Modifier.size(12.dp))
                        Text(
                            text = "باقة VIP المتميزة 🌟",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // PROFILE PHOTO OVERLAY ROW & INFO
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-30).dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Personal photo represented with custom colored frame border
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1B1B22),
                    border = BorderStroke(2.dp, if (tech.isVIP) Color(0xFFD4AF37) else Color(android.graphics.Color.parseColor(primaryColorHex))),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Photo",
                            tint = Color.LightGray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tech.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Verified Icon (Blue Badge)
                        if (tech.isVerified) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Recommended Star (Green Badge)
                        if (tech.isRecommended) {
                            Icon(
                                imageVector = Icons.Default.Recommend,
                                contentDescription = "Recommended",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "فني ${tech.specialty} بـ ${tech.region}",
                            color = Color(android.graphics.Color.parseColor(primaryColorHex)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text("• 0.0 كم من موقعك", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }

            // BODY: STATISTICS & AVARAGE RATING (Section Component)
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Stars", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Text(
                            text = "${tech.rating} (${tech.ratingCount} تقييم)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Badge(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.LightGray
                    ) {
                        Text("أنجز ${tech.completedServices} خدمة بنجاح", fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                    }

                    Badge(
                        containerColor = if (tech.isAvailable24_7) Color(0xFF1B3B22) else Color(0xFF3B1B1B),
                        contentColor = if (tech.isAvailable24_7) Color.Green else Color.Red
                    ) {
                        Text(
                            text = if (tech.isAvailable24_7) "متاح 24/7" else "متاح فترات العمل",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                Text(
                    text = tech.bio,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // INTERACTIVE ACTION BUTTONS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onContact,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                            Text("إتصال بالهاتف", fontSize = 10.sp)
                        }
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onChat,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", modifier = Modifier.size(14.dp))
                            Text("دردشة سريعة", fontSize = 10.sp)
                        }
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(android.graphics.Color.parseColor(primaryColorHex))),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onBook,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = "Book", modifier = Modifier.size(14.dp))
                            Text("حجز الفني", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
