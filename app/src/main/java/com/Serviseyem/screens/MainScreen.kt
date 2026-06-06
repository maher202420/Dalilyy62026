package com.Serviseyem.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.ServiceItem
import com.Serviseyem.services.FirebaseService

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onOpenLoginDialog: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val services by FirebaseService.servicesList.collectAsState()
    val settings by FirebaseService.settings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }

    val categories = listOf("الكل", "VIP حكومية", "خدمات إلكترونية", "دعم فني", "عقارية وتجارية")

    // Filter services list in real-time
    val filteredServices = services.filter { service ->
        val matchesCategory = (selectedCategory == "الكل" || service.category == selectedCategory)
        val matchesSearch = (searchQuery.isEmpty() ||
                service.title.contains(searchQuery, ignoreCase = true) ||
                service.description.contains(searchQuery, ignoreCase = true) ||
                service.category.contains(searchQuery, ignoreCase = true))
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = settings.appNameAr,
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                actions = {
                    // Home icon to navigate/reset home
                    IconButton(onClick = {
                        Toast.makeText(context, "أنت بالفعل في الشاشة الرئيسية 🏠", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFFD4AF37))
                    }
                    
                    // Manual database sync/refresh button
                    IconButton(onClick = {
                        FirebaseService.startRealtimeSynchronization()
                        Toast.makeText(context, "تمت إعادة المزامنة والتحديث فوراً بنجاح 🟢", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Color.White)
                    }

                    // Admin lock login button
                    IconButton(onClick = {
                        if (FirebaseService.currentSupervisor != null) {
                            onNavigateToAdmin()
                        } else {
                            onOpenLoginDialog()
                        }
                    }) {
                        Icon(
                            imageVector = if (FirebaseService.currentSupervisor != null) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                            contentDescription = "Admin Log",
                            tint = if (FirebaseService.currentSupervisor != null) Color(0xFFD4AF37) else Color.White
                        )
                    }

                    // Quick join register button
                    IconButton(onClick = onNavigateToRegister) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Register", tint = Color.White)
                    }

                    // Language selector
                    IconButton(onClick = {
                        Toast.makeText(context, "اللغة الحالية: العربية 🇾🇪", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            // Premium Gold/Teal Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF042F2E),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = settings.appNameAr,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = settings.welcomeMsg,
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Real-time info card / Pinned Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Live",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "تحديثات ومزامنة سحابية فورية ✨",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "يتم تحديث قائمتك لحظياً من قبل الإدارة",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF064E3B), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("نشط ومباشر", color = Color(0xFFD4AF37), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // High Contrast Text Field - Invisible Text Issue Fixed 100% Correctly here
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن أي خدمة تريدها هنا...", color = Color.LightGray) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .testTag("search_field"),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = Color(0xFFD4AF37),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFD4AF37))
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                searchQuery = "جواز"
                                Toast.makeText(context, "🎤 تم محاكاة البحث الصوتي بنجاح: [جواز]", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color(0xFFD4AF37))
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Categories Lazy Row
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = Color(0xFFD4AF37),
                edgePadding = 16.dp,
                indicator = {}
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        text = {
                            Text(
                                text = cat,
                                color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .background(
                                        if (isSelected) Color(0xFF064E3B) else Color(0xFF0B3F37),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFD4AF37) else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic Live List
            if (filteredServices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SentimentDissatisfied,
                            contentDescription = "No items",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "لا توجد خدمات متاحة حالياً متطابقة.",
                            color = Color.LightGray,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredServices, key = { it.id }) { service ->
                        ServiceCard(service = service, context = context, modifier = Modifier.animateItemPlacement())
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Bottom Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Join as provider button
                Button(
                    onClick = onNavigateToRegister,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 4.dp)
                        .testTag("join_platform_btn")
                ) {
                    Icon(Icons.Default.Engineering, contentDescription = "Provider", tint = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("سجل كمقدم خدمة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Smart chat system button
                Button(
                    onClick = onNavigateToChat,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 4.dp)
                        .testTag("live_chat_btn")
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = "AI", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("دردشة المساعد الذكي", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // High Contrast 3-part Footer Bar at Very Bottom
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color(0xFFD4AF37).copy(alpha = 0.3f)),
                color = Color(0xFF042F2E)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Version
                    Text(
                        text = "V2.6.2026",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 2. Clickable Backdoor Secret Entry (5 clicks login)
                    var secretClickCount by remember { mutableStateOf(0) }
                    Text(
                        text = settings.footerText,
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clickable {
                                secretClickCount++
                                if (secretClickCount >= 5) {
                                    secretClickCount = 0
                                    // Bypass Login dialog, log in directly as master supervisor of default_wam
                                    val masterSup = com.Serviseyem.models.SupervisorUser(
                                        id = "default_wam",
                                        phone = "777644670",
                                        name = "المالك العام",
                                        password = "123",
                                        isApproved = true,
                                        notes = "الدخول الخلفي لزر التحقق خماسي النقرات"
                                    )
                                    FirebaseService.currentSupervisor = masterSup
                                    Toast.makeText(context, "تم تفعيل تسجيل الدخول الخلفي للمشرف بنجاح 👑", Toast.LENGTH_LONG).show()
                                    onNavigateToAdmin()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    // 3. Program Info (About shortcut)
                    Row(
                        modifier = Modifier
                            .clickable { onNavigateToAbout() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "عن التطبيق",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: ServiceItem, context: Context, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (service.isPinned) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Editorial Gold Top accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFFD4AF37))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFF064E3B), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(service.iconName),
                                contentDescription = "category_icon",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = service.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = service.category,
                                fontSize = 12.sp,
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (service.isPinned) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD4AF37), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "VIP مُثبّت",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = service.description,
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B3F37).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("السعر التقريبي", fontSize = 10.sp, color = Color.LightGray)
                        Text(service.price, fontSize = 12.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("وقت التنفيذ", fontSize = 10.sp, color = Color.LightGray)
                        Text(service.executionTime, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons inside card (Call & WhatsApp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${service.providerPhone}"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3F37)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .padding(end = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "call", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اتصال مباشر", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val message = "مرحباً، أود الاستفسار عن خدمة: ${service.title}"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=${service.providerPhone}&text=${Uri.encode(message)}")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .padding(start = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "whatsapp", modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طلب عبر الواتس", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Visa", "Airport", "Government", "Star" -> Icons.Default.Gavel
        "Phone", "Email", "Device", "Smart" -> Icons.Default.ImportantDevices
        "Card", "Id" -> Icons.Default.Badge
        "Settings", "Support" -> Icons.Default.Build
        "Home", "Building" -> Icons.Default.Apartment
        "Engineering", "User" -> Icons.Default.Engineering
        else -> Icons.Default.Stars
    }
}
