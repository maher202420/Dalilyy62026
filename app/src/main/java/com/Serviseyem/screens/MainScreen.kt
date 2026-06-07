package com.Serviseyem.screens

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.Serviseyem.models.CategoryItem
import com.Serviseyem.models.DatabaseCity
import com.Serviseyem.models.ServiceProvider
import com.Serviseyem.models.ChatSession
import com.Serviseyem.services.FirebaseService
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToChat: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToChatWithSession: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val settings by FirebaseService.settings.collectAsState()
    val providers by FirebaseService.providersList.collectAsState()
    val categories by FirebaseService.categoriesList.collectAsState()
    val cities by FirebaseService.citiesList.collectAsState()

    // Query states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedCityFilter by remember { mutableStateOf<String?>(null) }
    var minimumRating by remember { mutableFloatStateOf(0f) }
    
    // Radius lookup states
    var simulatedUserLat by remember { mutableDoubleStateOf(15.35) } // Sana'a coordinates
    var simulatedUserLng by remember { mutableDoubleStateOf(44.20) }
    var insideRadiusLimit by remember { mutableDoubleStateOf(0.0) } // 0.0 means disabled
    var simulatedAddressText by remember { mutableStateOf("صنعاء، التحرير") }
    var showAddressAutoComplete by remember { mutableStateOf(false) }

    // Autocomplete predictions for addresses
    val autocompleteSuggestions = listOf(
        "صنعاء، حدة" to (15.3129 to 44.1866),
        "صنعاء، الأصبحي" to (15.2891 to 44.2185),
        "صنعاء، التحرير" to (15.3526 to 44.2058),
        "صنعاء، شارع الستين" to (15.3644 to 44.1678),
        "عدن، كريتر" to (12.7844 to 45.0345),
        "عدن، المنصورة" to (12.8466 to 44.9811),
        "تعز، شارع جمال" to (13.5786 to 44.0134),
        "إب، يريم" to (14.2882 to 44.3789),
        "الحديدة، الحوك" to (14.7925 to 42.9555)
    )

    // Voice search results launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!spoken.isNullOrEmpty()) {
                searchQuery = spoken[0]
                Toast.makeText(context, "البحث الصوتي: $searchQuery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Selected provider details Modal State
    var selectedProviderForDetail by remember { mutableStateOf<ServiceProvider?>(null) }

    // Real-time filtering function
    val filteredProviders = remember(providers, searchQuery, selectedCategoryFilter, selectedCityFilter, minimumRating, insideRadiusLimit, simulatedUserLat, simulatedUserLng) {
        providers.filter { p ->
            // Only verified status
            if (p.status != "مقبول") return@filter false

            // Text search (Name, Specialty, addresses)
            val matchesQuery = searchQuery.isEmpty() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.specialty.contains(searchQuery, ignoreCase = true) ||
                    p.businessAddress.contains(searchQuery, ignoreCase = true) ||
                    p.phone.contains(searchQuery)

            // Category filter
            val matchesCategory = selectedCategoryFilter == null || p.specialty.contains(selectedCategoryFilter!!)

            // City filter
            val matchesCity = selectedCityFilter == null || p.residenceAddress.contains(selectedCityFilter!!)

            // Rating filter
            val matchesRating = p.rating >= minimumRating

            // Distance search (Radius)
            val matchesDistance = if (insideRadiusLimit > 0.0 && p.latitude != null && p.longitude != null) {
                // Haversine formula
                val earthRadius = 6371.0 // kilometers
                val dLat = Math.toRadians(p.latitude - simulatedUserLat)
                val dLng = Math.toRadians(p.longitude - simulatedUserLng)
                val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(simulatedUserLat)) * cos(Math.toRadians(p.latitude)) * sin(dLng / 2).pow(2.0)
                val c = 2 * atan2(sqrt(a), sqrt(1 - a))
                val distance = earthRadius * c
                distance <= insideRadiusLimit
            } else {
                true
            }

            matchesQuery && matchesCategory && matchesCity && matchesRating && matchesDistance
        }
    }

    Scaffold(
        bottomBar = {
            // Keep AI assistant 🤖 and true Chat System without unwanted additions
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Stay on Main */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFFD4AF37)) },
                    label = { Text("الرئيسية", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF334155))
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToChat,
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color.LightGray) },
                    label = { Text("دردشة WAM", color = Color.LightGray, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAbout,
                    icon = { Icon(Icons.Default.Info, contentDescription = "About", tint = Color.LightGray) },
                    label = { Text("عن الدليل", color = Color.LightGray, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(innerPadding)
        ) {
            // Sponsored Ad Banner representation
            if (settings.sponsoredAdVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E3A8A))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = settings.sponsoredAdText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            WamTopBar(
                onInfoClicked = onNavigateToAbout,
                onSecretPortalOpened = {
                    // Navigate to Secret Portal (Triggered automatically)
                    onNavigateToRegister() // Reuse register path or navigation flow in MainActivity
                }
            )

            // Search Triggers & Inputs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Text search bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ابحث بالاسم، الرقم، التخصص...", color = Color.LightGray, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .testTag("main_search_text"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )

                        // Voice Search Button (controlled from AppSettings)
                        if (settings.voiceSearchEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            FilledIconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث للبحث الفوري عن مقدم الخدمة...")
                                        }
                                        voiceLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "البحث الصوتي غير مدعوم على جهازك", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF064E3B)),
                                modifier = Modifier.size(44.dp).testTag("voice_search_mic_btn")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color(0xFFD4AF37))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated Location Autocomplete widget
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "موقعك الحالي للاستعلام: $simulatedAddressText",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { showAddressAutoComplete = !showAddressAutoComplete }
                                    .padding(vertical = 4.dp)
                            )

                            DropdownMenu(
                                expanded = showAddressAutoComplete,
                                onDismissRequest = { showAddressAutoComplete = false },
                                modifier = Modifier.background(Color(0xFF1E293B)),
                            ) {
                                autocompleteSuggestions.forEach { (name, coords) ->
                                    DropdownMenuItem(
                                        text = { Text(name, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                            simulatedAddressText = name
                                            simulatedUserLat = coords.first
                                            simulatedUserLng = coords.second
                                            showAddressAutoComplete = false
                                            Toast.makeText(context, "تم تحديد موقع $name بنجاح!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Radius filter selectors (Limit distance - controlled by secret Admin Settings distance limit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("البحث بالمسأفة القريبة:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        Row {
                            listOf(0.0 to "الكل", 5.0 to "5كم", 10.0 to "10كم", 20.0 to "20كم").forEach { (rad, label) ->
                                val isSelected = insideRadiusLimit == rad
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable { insideRadiusLimit = rad },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF064E3B) else Color(0xFF0F172A)
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFD4AF37) else Color(0xFF334155))
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Categories horizontal slider list
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isAllSelected = selectedCategoryFilter == null
                    Card(
                        modifier = Modifier.clickable { selectedCategoryFilter = null },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAllSelected) Color(0xFFD4AF37) else Color(0xFF1E293B)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = "الكل",
                            color = if (isAllSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat.nameAr
                    Card(
                        modifier = Modifier.clickable { selectedCategoryFilter = cat.nameAr },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFD4AF37) else Color(0xFF1E293B)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = cat.nameAr,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cities filters & matching providers title in Main UI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Engineering, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مزودين الخدمة بـ WAM (${filteredProviders.size})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // City filter selector dropdown trigger
                var showCityMenu by remember { mutableStateOf(false) }
                Button(
                    onClick = { showCityMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (selectedCityFilter == null) "كل المدن 🌍" else "المدينة: $selectedCityFilter",
                        color = Color(0xFFD4AF37),
                        fontSize = 11.sp
                    )
                    DropdownMenu(
                        expanded = showCityMenu,
                        onDismissRequest = { showCityMenu = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        DropdownMenuItem(
                            text = { Text("كل المدن يمن", color = Color.White) },
                            onClick = {
                                selectedCityFilter = null
                                showCityMenu = false
                            }
                        )
                        cities.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.nameAr, color = Color.White) },
                                onClick = {
                                    selectedCityFilter = c.nameAr
                                    showCityMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Providers List view
            if (filteredProviders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "عذراً! لم نجد مقدمين موافقين للمعاير المحددة.\nجرب كتابة عبارة بحث أخرى.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    items(filteredProviders) { provider ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable { selectedProviderForDetail = provider },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, if (provider.isVip) Color(0xFFD4AF37) else Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle or specialist tags
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFF0F172A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (provider.gender == "أنثى") Icons.Default.Face else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (provider.isVip) Color(0xFFD4AF37) else Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = provider.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (provider.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified Tech",
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        if (provider.isVip) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F)),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    "VIP",
                                                    color = Color(0xFFFBBF24),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${provider.specialty} • ${provider.residenceAddress}",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${provider.rating} تقييم الجودة",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "الرجاء الاتصال المباشر على الرقم: ${provider.phone}", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Call Provider",
                                        tint = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Application Bottom descriptive info bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = settings.footerText,
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // --- DIALOG DETAIL VIEW FOR SELECTED PROVIDER ---
    if (selectedProviderForDetail != null) {
        val p = selectedProviderForDetail!!
        AlertDialog(
            onDismissRequest = { selectedProviderForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "بطاقة معطيات المهني", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("اسم المهني: ${p.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("التخصص الفني: ${p.specialty}", color = Color.LightGray, fontSize = 13.sp)
                    Text("رقم التواصل: ${p.phone}", color = Color.LightGray, fontSize = 13.sp)
                    Text("المقر والمنطقه: ${p.residenceAddress}", color = Color.LightGray, fontSize = 13.sp)
                    if (p.businessAddress.isNotEmpty()) {
                        Text("عنوان العمل: ${p.businessAddress}", color = Color.LightGray, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // GOOGLE MAPS CARD IN PROVIDER PAGE
                    Text("🗺️ خريطة الموقع الجغرافي:", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (p.latitude != null && p.longitude != null && !p.isMapDisabled) {
                        GoogleMapView(latitude = p.latitude, longitude = p.longitude, providerName = p.name)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Directions button to open Google Maps navigation!
                        Button(
                            onClick = {
                                try {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${p.latitude},${p.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    // Fallback to web link
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${p.latitude},${p.longitude}"))
                                    context.startActivity(webIntent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الاتجاهات وفتح خرائط Google", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("الموقع الجغرافي غير متوفر لهذا الفني", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // True Chat with Provider Option
                    val visitorPhone = "VISITOR_GUEST"
                    Button(
                        onClick = {
                            if (!settings.chatEnabledForVisitors) {
                                Toast.makeText(context, settings.chatDisabledMessage, Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            // Block verification
                            val blockedList = settings.blockedChatUsers.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            if (blockedList.contains(visitorPhone)) {
                                Toast.makeText(context, "حقوق الدردشة معطلة لحسابك حالياً.", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            // Setup session and open chat screen
                            val sess = ChatSession(
                                id = "",
                                participants = listOf(visitorPhone, p.phone),
                                initiatorName = "زائر الدليل مستقل",
                                initiatorPhone = visitorPhone,
                                providerName = p.name,
                                providerPhone = p.phone,
                                lastMessage = "بدء استفسار تقديم الخدمة",
                                lastUpdated = System.currentTimeMillis()
                            )

                            FirebaseService.createChatSession(sess, { finalId ->
                                selectedProviderForDetail = null
                                onNavigateToChatWithSession(finalId)
                            }, {
                                Toast.makeText(context, "فشل إنشاء مسار المحادثة", Toast.LENGTH_SHORT).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بدء دردشة فورية بالدليل 💬", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedProviderForDetail = null }) {
                    Text("إغلاق", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun GoogleMapView(latitude: Double, longitude: Double, providerName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📍 خريطة موقع مقدم الخدمة الجغرافية ($providerName)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الإحداثيات النشطة: Lat: $latitude، Lng: $longitude",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "انقر فوق زر 'عرض الاتجاهات' أدناه لفتح المسار والملاحة الفورية عبر خرائط Google مباشرة بدقة 100%.",
                color = Color.Gray,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
