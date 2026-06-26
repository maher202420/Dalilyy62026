@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.Serviseyem

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Serviseyem.models.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush

// ============================================================
// 🌐 فحص حالة الاتصال بالشبكة
// ============================================================
fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// ============================================================
// 📋 شاشة الدليل الرئيسي (المزودين)
// ============================================================
@Composable
fun DirectoryScreen(
    viewModel: MainViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var selectedProviderForBooking by remember { mutableStateOf<Provider?>(null) }
    var selectedProviderForDetails by remember { mutableStateOf<Provider?>(null) }
    
    // تشغيل الإدخال الصوتي بالذكاء الاصطناعي
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                searchQuery = spokenText
            }
        }
    }
    
    val filteredProviders = providers.filter { provider ->
        val matchesSearch = provider.name.contains(searchQuery, ignoreCase = true) || 
                            provider.description.contains(searchQuery, ignoreCase = true) ||
                            provider.area.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory.isEmpty() || provider.category == selectedCategory
        val matchesCity = selectedCity.isEmpty() || provider.city == selectedCity
        provider.isVerified && matchesSearch && matchesCategory && matchesCity
    }.sortedWith(
        compareByDescending<Provider> { it.isPinned }
            .thenByDescending { it.isSubscribed }
            .thenByDescending { it.rating }
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
    ) {
        // شريط البحث المطور
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن سباك، كهربائي، صيانة...", color = Color.Gray, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.accentGold) },
                trailingIcon = {
                    if (settings.allowVoiceInput) {
                        IconButton(onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن للبحث عن فني...")
                                }
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "التعرف على الصوت غير مدعوم في جهازك", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "بحث صوتي", tint = AppTheme.accentGold)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AppTheme.accentGold,
                    unfocusedBorderColor = Color(0xFF223639)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }
        
        // شريط اختيار الأقسام (تصفير الفلترة بالضغط مجدداً)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory.isEmpty(),
                    onClick = { selectedCategory = "" },
                    label = { Text("الكل 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTheme.primaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = AppTheme.surfaceDark,
                        labelColor = Color.White
                    )
                )
            }
            items(categories) { category ->
                val isSelected = selectedCategory == category.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = if (isSelected) "" else category.id },
                    label = { Text("${category.iconUrl} ${category.nameAr}", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTheme.primaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = AppTheme.surfaceDark,
                        labelColor = Color.White
                    )
                )
            }
        }
        
        // شريط اختيار المدن
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCity.isEmpty(),
                    onClick = { selectedCity = "" },
                    label = { Text("كل المدن 🇾🇪", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTheme.primaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = AppTheme.surfaceDark,
                        labelColor = Color.White
                    )
                )
            }
            items(cities) { city ->
                val isSelected = selectedCity == city.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCity = if (isSelected) "" else city.id },
                    label = { Text(city.nameAr, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTheme.primaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = AppTheme.surfaceDark,
                        labelColor = Color.White
                    )
                )
            }
        }
        
        // عرض البنرات الإعلانية المتحركة
        val activeBanners = viewModel.banners.collectAsStateWithLifecycle().value.filter { it.isActive }
        if (activeBanners.isNotEmpty()) {
            var currentBannerIndex by remember { mutableStateOf(0) }
            val currentBanner = activeBanners.getOrNull(currentBannerIndex)
            
            LaunchedEffect(currentBannerIndex, activeBanners) {
                val duration = currentBanner?.displayDurationMs ?: 5000L
                delay(duration)
                currentBannerIndex = (currentBannerIndex + 1) % activeBanners.size
            }
            
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        if (currentBanner != null && currentBanner.linkUrl.isNotBlank()) {
                            try {
                                if (currentBanner.linkUrl.startsWith("http")) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentBanner.linkUrl))
                                    context.startActivity(intent)
                                } else {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentBanner.linkUrl}"))
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {}
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentBanner != null) {
                        val bitmap = rememberBase64Bitmap(currentBanner.mediaUrl)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = currentBanner.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (currentBanner.isVideo) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(36.dp))
                                    Text(currentBanner.title, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                        
                        // غطاء تدرج لوني وعنوان البنر
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                ),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = currentBanner.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        
                        // مؤشرات النقط
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            activeBanners.forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == currentBannerIndex) AppTheme.accentGold else Color.Gray.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // عرض فنيي الدليل المطور
        if (filteredProviders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("عذراً، لم يتم العثور على نتائج مطابقة", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredProviders, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        settings = settings,
                        onBookClick = { selectedProviderForBooking = provider },
                        onDetailsClick = { selectedProviderForDetails = provider },
                        onChatClick = {
                            val userRole = viewModel.currentUserRole.value
                            val userId = viewModel.currentUserId.value
                            if (userId == null) {
                                Toast.makeText(context, "الرجاء تسجيل الدخول أولاً للمحادثة", Toast.LENGTH_SHORT).show()
                            } else {
                                val roomId = "chat_${userId}_${provider.id}"
                                val room = ChatRoom(
                                    id = roomId,
                                    participants = listOf(userId, provider.id),
                                    participantNames = mapOf(userId to "أنا", provider.id to provider.name)
                                )
                                viewModel.createChatRoom(room)
                                onNavigateToChat(roomId)
                            }
                        }
                    )
                }
            }
        }
    }
    
    // نافذة الحجز الفوري
    if (selectedProviderForBooking != null) {
        BookingDialog(
            provider = selectedProviderForBooking!!,
            viewModel = viewModel,
            onDismiss = { selectedProviderForBooking = null }
        )
    }
    
    // نافذة تفاصيل الفني الفاخرة مع معرض الصور السحابي
    if (selectedProviderForDetails != null) {
        ProviderDetailsDialog(
            provider = selectedProviderForDetails!!,
            onDismiss = { selectedProviderForDetails = null }
        )
    }
}

// ============================================================
// 👨‍🔧 بطاقة فني مع خدمات الحجز والاتصال والدردشة
// ============================================================
@Composable
fun ProviderCard(
    provider: Provider,
    settings: AppSettings,
    onBookClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val context = LocalContext.current
    val cardBgColor = remember(settings.cardBackgroundHex) {
        try { Color(android.graphics.Color.parseColor(settings.cardBackgroundHex)) } catch (e: Exception) { AppTheme.surfaceDark }
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        border = if (provider.isPinned) BorderStroke(1.5.dp, AppTheme.accentGold) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // الصورة الشخصية أو الأيقونة الدائرية
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AppTheme.darkBg)
                            .border(1.5.dp, AppTheme.accentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = rememberBase64Bitmap(provider.imageUrl)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(28.dp))
                        }
                    }
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = provider.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (provider.isVerified && settings.showVerifiedBadge) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "موثق",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (provider.isRecommended && settings.showRecommendedBadge) {
                                Icon(
                                    Icons.Default.Stars,
                                    contentDescription = "موصى به",
                                    tint = AppTheme.accentGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "📍 ${provider.city} - ${provider.area}",
                            color = AppTheme.grayText,
                            fontSize = 10.sp
                        )
                    }
                }
                
                // شارة الـ VIP
                if (provider.isSubscribed && settings.showVipBadge) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.accentGold)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "👑 VIP",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (provider.description.isNotBlank()) {
                Text(
                    text = provider.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }
            
            // شريط التقييم والأسعار التقريبية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${provider.rating} (${provider.bookingsCount} تقييم)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "سعر الخدمة يبدأ من: ${provider.price} ر.ي",
                    color = AppTheme.accentGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider(color = Color.White.copy(alpha = 0.1f))
            
            // أزرار التواصل المباشر الأربعة (Call, Whatsapp, Details, Book)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (settings.showCallButton) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "لا يمكن الاتصال الهاتفي من جهازك", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E7D32))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("اتصال", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (settings.showWhatsappButton) {
                    IconButton(
                        onClick = {
                            try {
                                val cleanPhone = provider.phone.replace("+", "").replace(" ", "")
                                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=مرحباً يا غالي، هل أنت متاح لتقديم خدمة؟"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "الواتساب غير مثبت على هذا الجهاز", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF25D366))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "واتساب", tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("واتساب", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (settings.showDetailsButton) {
                    IconButton(
                        onClick = onDetailsClick,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.accentGold)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "تفاصيل", tint = Color.Black, modifier = Modifier.size(14.dp))
                            Text("معلومات", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (settings.showBookButton) {
                    IconButton(
                        onClick = onBookClick,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.primaryRed)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "حجز", tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("احجز الآن", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 📅 نافذة الحجز الفوري المباشر
// ============================================================
@Composable
fun BookingDialog(
    provider: Provider,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }
    
    var areaExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    var activeVoiceTarget by remember { mutableStateOf("") }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                when (activeVoiceTarget) {
                    "name" -> name = text
                    "phone" -> phone = text
                    "time" -> time = text
                    "description" -> description = text
                    "additionalNotes" -> additionalNotes = text
                }
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppTheme.accentGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📅 استمارة طلب حجز خدمة مع: ${provider.name}",
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // حقل الاسم الثلاثي
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل الثلاثي (إجباري) 👤", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    trailingIcon = {
                        if (settings.allowVoiceInput) {
                            IconButton(onClick = {
                                activeVoiceTarget = "name"
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم العميل...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {}
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "تحدث بالاسم", tint = AppTheme.accentGold)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // حقل رقم الهاتف الفعال
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف للتواصل الفعال (إجباري) 📞", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    trailingIcon = {
                        if (settings.allowVoiceInput) {
                            IconButton(onClick = {
                                activeVoiceTarget = "phone"
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث برقم هاتفك...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {}
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "تحدث بالهاتف", tint = AppTheme.accentGold)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                // قائمة منسدلة للمنطقة/السكن
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("المنطقة / السكن (إجباري) 📍", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { areaExpanded = true }
                            .background(AppTheme.darkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, if (selectedArea.isEmpty()) Color(0xFF223639) else AppTheme.accentGold, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedArea.isEmpty()) "اختر منطقتك/مدينتك باليمن..." else selectedArea,
                                color = if (selectedArea.isEmpty()) Color.Gray else Color.White,
                                fontSize = 12.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AppTheme.accentGold)
                        }
                        DropdownMenu(
                            expanded = areaExpanded,
                            onDismissRequest = { areaExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(AppTheme.surfaceDark)
                        ) {
                            val areasList = if (cities.isNotEmpty()) cities.map { it.nameAr } else listOf("صنعاء", "عدن", "تعز", "الحديدة", "إب", "ذمار", "حضرموت", "مأرب")
                            areasList.forEach { cityLabel ->
                                DropdownMenuItem(
                                    text = { Text(cityLabel, color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        selectedArea = cityLabel
                                        areaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // قائمة منسدلة لنوع الخدمة المطلوبة
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("نوع الخدمة المطلوبة (إجباري) 💼", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { serviceExpanded = true }
                            .background(AppTheme.darkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, if (selectedService.isEmpty()) Color(0xFF223639) else AppTheme.accentGold, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedService.isEmpty()) "اختر نوع الخدمة المطلوبة..." else selectedService,
                                color = if (selectedService.isEmpty()) Color.Gray else Color.White,
                                fontSize = 12.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AppTheme.accentGold)
                        }
                        DropdownMenu(
                            expanded = serviceExpanded,
                            onDismissRequest = { serviceExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(AppTheme.surfaceDark)
                        ) {
                            val servicesList = if (categories.isNotEmpty()) categories.map { it.nameAr } else listOf("سباكة", "كهرباء", "صيانة جوالات", "تكييف وتبريد", "صيانة سيارات")
                            servicesList.forEach { catLabel ->
                                DropdownMenuItem(
                                    text = { Text(catLabel, color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        selectedService = catLabel
                                        serviceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // حقل موعد وتاريخ العمل
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("الموعد المفضل للعمل (إجباري) ⏰ (مثال: غداً 9 صباحاً)", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    trailingIcon = {
                        if (settings.allowVoiceInput) {
                            IconButton(onClick = {
                                activeVoiceTarget = "time"
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بالوقت المفضل...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {}
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "تحدث بالوقت", tint = AppTheme.accentGold)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // حقل وصف المشكلة
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف المشكلة بالتفصيل (اختياري) 🔧", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    trailingIcon = {
                        if (settings.allowVoiceInput) {
                            IconButton(onClick = {
                                activeVoiceTarget = "description"
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بتفاصيل المشكلة...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {}
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "تحدث بتفاصيل المشكلة", tint = AppTheme.accentGold)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // حقل ملاحظات إضافية
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text("ملاحظات إضافية (اختياري) 📝", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    trailingIcon = {
                        if (settings.allowVoiceInput) {
                            IconButton(onClick = {
                                activeVoiceTarget = "additionalNotes"
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بملاحظات إضافية...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {}
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "تحدث بملاحظات إضافية", tint = AppTheme.accentGold)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color(0xFF223639)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // أزرار التحكم والتقديم
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
                            if (name.isBlank() || phone.isBlank() || selectedArea.isBlank() || selectedService.isBlank() || time.isBlank()) {
                                Toast.makeText(context, "الرجاء تعبئة جميع الحقول الإلزامية المطلوبة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            showConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("مراجعة وتأكيد الطلب 📅", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    
    // ============================================================
    // 📅 نافذة تأكيد الحجز (Confirmation Dialog)
    // ============================================================
    if (showConfirmDialog) {
        Dialog(
            onDismissRequest = { showConfirmDialog = false },
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppTheme.accentGold,
                        modifier = Modifier.size(56.dp)
                    )
                    
                    Text(
                        text = "🔎 تأكيد تفاصيل حجز الخدمة",
                        color = AppTheme.accentGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "يرجى مراجعة وتأكيد بيانات حجز الخدمة المباشرة قبل إرسالها سحابياً:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppTheme.darkBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "👤 اسم العميل: $name", color = Color.White, fontSize = 12.sp)
                            Text(text = "📞 رقم الهاتف: $phone", color = Color.White, fontSize = 12.sp)
                            Text(text = "📍 المنطقة/السكن: $selectedArea", color = Color.White, fontSize = 12.sp)
                            Text(text = "💼 نوع الخدمة: $selectedService", color = Color.White, fontSize = 12.sp)
                            Text(text = "⏰ الموعد المفضل: $time", color = Color.White, fontSize = 12.sp)
                            if (description.isNotBlank()) {
                                Text(text = "🔧 وصف المشكلة: $description", color = Color.LightGray, fontSize = 11.sp)
                            }
                            if (additionalNotes.isNotBlank()) {
                                Text(text = "📝 ملاحظات إضافية: $additionalNotes", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showConfirmDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تراجع وتعديل", color = Color.White, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = {
                                val booking = BookingRequest(
                                    id = "book_${System.currentTimeMillis()}",
                                    userId = "client_${System.currentTimeMillis()}",
                                    userName = name,
                                    phoneNumber = phone,
                                    serviceType = selectedService,
                                    residenceArea = selectedArea,
                                    preferredTime = time,
                                    description = description,
                                    additionalNotes = additionalNotes,
                                    providerId = provider.id,
                                    providerName = provider.name,
                                    status = "pending",
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.addBooking(booking)
                                Toast.makeText(context, "✅ تم إرسال طلب حجزك وتوجيهه سحابياً بنجاح!", Toast.LENGTH_LONG).show()
                                showConfirmDialog = false
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("تأكيد وإرسال 🚀", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// ℹ️ شاشة تفاصيل الفني الفاخرة مع معرض أعماله
// ============================================================
@Composable
fun ProviderDetailsDialog(
    provider: Provider,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppTheme.accentGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
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
                        text = "ℹ️ تفاصيل ومحفظة الفني",
                        color = AppTheme.accentGold,
                        fontSize = 15.sp,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AppTheme.darkBg)
                                .border(1.5.dp, AppTheme.accentGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = rememberBase64Bitmap(provider.imageUrl)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(34.dp))
                            }
                        }
                        
                        Column {
                            Text(
                                text = provider.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "📍 ${provider.city} - ${provider.area}",
                                color = AppTheme.grayText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "📞 الهاتف: ${provider.phone}",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    Text(
                        text = "توصيف الخدمات والخبرات:",
                        color = AppTheme.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = provider.description.ifBlank { "لم يقم الفني بإضافة تفاصيل خدماته بعد." },
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "📸 معرض صور أعمال الفني السابقة:",
                        color = AppTheme.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (provider.portfolioImages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(AppTheme.darkBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لم يقم الفني بإرفاق صور سابقة لأعماله", color = Color.Gray, fontSize = 10.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            items(provider.portfolioImages) { base64 ->
                                val portfolioBitmap = rememberBase64Bitmap(base64)
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppTheme.darkBg)
                                ) {
                                    if (portfolioBitmap != null) {
                                        Image(
                                            bitmap = portfolioBitmap.asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.align(Alignment.Center))
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

// ============================================================
// 🗺️ شاشة الخارطة والبحث الجغرافي التفاعلية عالية الدقة باليمن
// ============================================================
@Composable
fun MapScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityId by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedDistance by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var showBookingDialogForProvider by remember { mutableStateOf<Provider?>(null) }
    
    // تصفية مزودي الخدمة للخرائط بناءً على المدينة، التخصص، والمسافة الجغرافية الافتراضية
    val filteredProviders = remember(providers, searchQuery, selectedCityId, selectedCategoryId, selectedDistance) {
        providers.filter { provider ->
            val matchesCategory = selectedCategoryId.isBlank() || provider.category == selectedCategoryId
            val hash = kotlin.math.abs(provider.id.hashCode())
            val pctX = ((hash % 80) + 10) / 100f
            val pctY = (((hash / 80) % 80) + 10) / 100f
            val distanceInKm = kotlin.math.sqrt((pctX - 0.5f) * (pctX - 0.5f) + (pctY - 0.5f) * (pctY - 0.5f)) * 40f
            val maxDistance = selectedDistance.toFloatOrNull() ?: Float.MAX_VALUE
            val matchesDistance = distanceInKm <= maxDistance
            
            provider.isVerified &&
            (selectedCityId.isBlank() || provider.city == selectedCityId) &&
            matchesCategory &&
            matchesDistance &&
            (searchQuery.isBlank() || provider.name.contains(searchQuery, true) || provider.area.contains(searchQuery, true))
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // شريط البحث المدمج بالخريطة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث في الخريطة عن فني أو حارة...", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(16.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = AppTheme.surfaceDark,
                        unfocusedContainerColor = AppTheme.surfaceDark,
                        focusedBorderColor = AppTheme.accentGold,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            
            // فلترة المدن اليمنية بشكل أفقي جذاب
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCityId.isBlank(),
                        onClick = { selectedCityId = "" },
                        label = { Text("📍 كل المدن باليمن", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.primaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = AppTheme.surfaceDark,
                            labelColor = Color.White
                        )
                    )
                }
                items(cities) { city ->
                    val isSelected = selectedCityId == city.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCityId = city.id },
                        label = { Text(city.nameAr, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.primaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = AppTheme.surfaceDark,
                            labelColor = Color.White
                        )
                    )
                }
            }

            // فلترة التخصصات بشكل أفقي جذاب (جديد)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId.isBlank(),
                        onClick = { selectedCategoryId = "" },
                        label = { Text("💼 كل التخصصات", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.primaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = AppTheme.surfaceDark,
                            labelColor = Color.White
                        )
                    )
                }
                items(categories) { category ->
                    val isSelected = selectedCategoryId == category.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text("${category.iconUrl} ${category.nameAr}", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.primaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = AppTheme.surfaceDark,
                            labelColor = Color.White
                        )
                    )
                }
            }

            // فلترة المسافة بشكل أفقي جذاب (جديد)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val distances = listOf(
                    Pair("", "🌐 كل المسافات"),
                    Pair("5", "⚡ قريب جداً (< 5 كم)"),
                    Pair("15", "📍 قريب (< 15 كم)"),
                    Pair("30", "🗺️ متوسط (< 30 كم)")
                )
                items(distances) { pair ->
                    val isSelected = selectedDistance == pair.first
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDistance = pair.first },
                        label = { Text(pair.second, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppTheme.primaryRed,
                            selectedLabelColor = Color.White,
                            containerColor = AppTheme.surfaceDark,
                            labelColor = Color.White
                        )
                    )
                }
            }
            
            // لوحة الرسم والخريطة التفاعلية الفخمة
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.surfaceDark)
                    .border(1.dp, AppTheme.accentGold.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            ) {
                // خلفية شبكية ذات طابع راداري وعسكري دقيق
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // رسم دوائر الرادار المركزة
                    drawCircle(
                        color = Color(0xFF1E3A3E).copy(alpha = 0.4f),
                        radius = width * 0.35f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF1E3A3E).copy(alpha = 0.2f),
                        radius = width * 0.2f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                    
                    // رسم خطوط الطول والعرض للشبكة الجغرافية للمدينة
                    val gridLinesCount = 8
                    for (i in 1..gridLinesCount) {
                        val x = (width / (gridLinesCount + 1)) * i
                        drawLine(
                            color = Color(0xFF162A2E).copy(alpha = 0.3f),
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, height),
                            strokeWidth = 1f
                        )
                        
                        val y = (height / (gridLinesCount + 1)) * i
                        drawLine(
                            color = Color(0xFF162A2E).copy(alpha = 0.3f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f
                        )
                    }
                }
                
                // توزيع دبابيس مزودي الخدمة المعتمدين سحابياً على لوحة الرسم
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val maxWidthPx = maxWidth.value
                    val maxHeightPx = maxHeight.value
                    
                    filteredProviders.forEach { provider ->
                        // حساب إحداثيات ثابتة ونظامية فريدة لكل فني للتواجد الدائم بنفس المكان
                        val hash = kotlin.math.abs(provider.id.hashCode())
                        val pctX = ((hash % 80) + 10) / 100f // بين 10% و 90%
                        val pctY = (((hash / 80) % 80) + 10) / 100f // بين 10% و 90%
                        
                        val xOffset = maxWidth * pctX
                        val yOffset = maxHeight * pctY
                        
                        val isSelected = selectedProvider?.id == provider.id
                        
                        Box(
                            modifier = Modifier
                                .offset(x = xOffset - 18.dp, y = yOffset - 36.dp)
                                .size(36.dp)
                                .clickable { selectedProvider = provider },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "دبوس فني",
                                    tint = if (isSelected) AppTheme.primaryRed else AppTheme.accentGold,
                                    modifier = Modifier.size(if (isSelected) 34.dp else 28.dp)
                                )
                            }
                        }
                    }
                }
                
                // شعار جاري التتبع والتزامن
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        Text(
                            text = "GPS سحابي نشط",
                            color = Color.Green,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // بطاقة عرض الفني المحدد أسفل الخريطة مباشرة للتواصل والحجز
        selectedProvider?.let { provider ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, AppTheme.accentGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.darkBg)
                                        .border(1.dp, AppTheme.accentGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = rememberBase64Bitmap(provider.imageUrl)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = AppTheme.accentGold)
                                    }
                                }
                                
                                Column {
                                    Text(
                                        text = provider.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    val catName = categories.find { it.id == provider.category }?.nameAr ?: provider.category
                                    val cityName = cities.find { it.id == provider.city }?.nameAr ?: provider.city
                                    Text(
                                        text = "💼 $catName",
                                        color = AppTheme.accentGold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "📍 $cityName - ${provider.area}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            IconButton(onClick = { selectedProvider = null }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق الكرت", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                        
                        // التقييمات السحابية
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${provider.rating} (${provider.bookingsCount} حجز)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // أزرار الحجز المباشر والاتصال المطور
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showBookingDialogForProvider = provider },
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("حجز فوري", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (settings.showProviderCardPhone && provider.phone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("اتصال", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                            
                            // زر المراسلة السحابية المباشرة
                            Button(
                                onClick = {
                                    if (currentUserId == null) {
                                        Toast.makeText(context, "الرجاء تسجيل الدخول للمراسلة", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val room = ChatRoom(
                                            id = "room_${currentUserId}_${provider.id}",
                                            participants = listOf(currentUserId!!, provider.id),
                                            participantNames = mapOf(currentUserId!! to "عميل", provider.id to provider.name),
                                            lastMessage = "مرحباً، أود التواصل معك بشأن خدمة فنية",
                                            lastMessageTime = System.currentTimeMillis()
                                        )
                                        try {
                                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("chat_rooms")
                                                .document(room.id)
                                                .set(room)
                                            Toast.makeText(context, "💬 تم إنشاء غرفة محادثة مشفرة بنجاح!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {}
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("دردشة", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // نموذج الحجز الفوري المطور عند النقر عليه من كرت الخريطة التفاعلي
    showBookingDialogForProvider?.let { provider ->
        BookingDialog(
            provider = provider,
            viewModel = viewModel,
            onDismiss = { showBookingDialogForProvider = null }
        )
    }
}

// ============================================================
// 💬 شاشة المحادثات المباشرة (قائمة الغرف)
// ============================================================
@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onNavigateToChatDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val chatRooms by viewModel.chatRooms.collectAsStateWithLifecycle()
    
    val myRooms = chatRooms.filter { room ->
        currentUserId != null && room.participants.contains(currentUserId!!)
    }
    
    if (currentUserId == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.darkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(48.dp))
                Text("يرجى تسجيل الدخول لعرض دردشاتك المباشرة", color = Color.White, fontSize = 13.sp)
            }
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.surfaceDark)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬 المحادثات الفورية الفعالة سحابياً",
                color = AppTheme.accentGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (myRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("لا توجد محادثات مباشرة حالية", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(myRooms) { room ->
                    val otherParticipantName = room.participantNames.filterKeys { it != currentUserId }.values.firstOrNull() ?: "فني مجهول"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToChatDetail(room.id) }
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
                                    text = otherParticipantName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = room.lastMessage.ifBlank { "اضغط لبدء المحادثة السحابية..." },
                                    color = AppTheme.grayText,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 💬 تفاصيل المحادثة الفورية مع الفني والترجمة الصوتية
// ============================================================
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val chatRooms by viewModel.chatRooms.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    
    var textMessage by remember { mutableStateOf("") }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    
    // إطلاق محادثة Text-To-Speech الصوتية
    DisposableEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale("ar")
            }
        }
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }
    
    val room = chatRooms.find { it.id == chatId }
    val otherParticipantName = room?.participantNames?.filterKeys { it != currentUserId }?.values?.firstOrNull() ?: "محادثة مباشرة"
    val messagesInThisRoom = chatMessages.filter { it.chatId == chatId }
    
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                textMessage = spokenText
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
    ) {
        // رأس صفحة الدردشة
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.surfaceDark)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }
                Text(
                    text = otherParticipantName,
                    color = AppTheme.accentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // قائمة الرسائل
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messagesInThisRoom, key = { it.id }) { message ->
                val isMe = message.senderId == currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 12.dp
                                )
                            )
                            .background(if (isMe) AppTheme.primaryRed else AppTheme.surfaceDark)
                            .padding(10.dp)
                            .widthIn(max = 260.dp)
                            .combinedClickable(
                                onLongClick = {
                                    if (settings.allowTextToSpeech) {
                                        tts.value?.speak(message.message, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                },
                                onClick = {}
                            )
                    ) {
                        Text(
                            text = message.message,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        // شريط الكتابة المطور مع التعرف الصوتي (Mic)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.surfaceDark)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textMessage,
                onValueChange = { textMessage = it },
                placeholder = { Text("اكتب رسالتك السحابية الفورية...", color = Color.Gray, fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                trailingIcon = {
                    if (settings.allowVoiceInput) {
                        IconButton(onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث وسيترجم صوتك إلى نص...")
                                }
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "التعرف الصوتي غير متوفر", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "تسجيل صوتي", tint = AppTheme.accentGold)
                        }
                    }
                },
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
                    if (textMessage.isNotBlank() && currentUserId != null) {
                        val msg = ChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            chatId = chatId,
                            senderId = currentUserId!!,
                            senderName = "أنا",
                            senderType = "user",
                            message = textMessage
                        )
                        viewModel.sendMessage(chatId, msg)
                        textMessage = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppTheme.accentGold)
            ) {
                Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.Black, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ============================================================
// 🤝 شاشة طلب الانضمام لمقدمي الخدمة (الفنيين)
// ============================================================
@Composable
fun JoinScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    
    val myPendingRequest = pendingRequests.find { currentUserId != null && it.deviceId == currentUserId && it.deviceId.isNotBlank() }
    val myApprovedProvider = providers.find { currentUserId != null && (it.deviceId == currentUserId && it.deviceId.isNotBlank() || (it.id == currentUserId && it.id.isNotBlank())) }
    
    if (myApprovedProvider != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.darkBg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, AppTheme.accentGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppTheme.accentGold,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "🎉 تهانينا! تم تفعيل حسابك بنجاح",
                        color = AppTheme.accentGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "مرحباً بك يا غالي، لقد تم قبول طلب انضمامك كمهني معتمد وأصبحت الآن نشطاً في دليل كل خدمات اليمن! حسابك يظهر الآن لجميع العملاء في القائمة الرئيسية.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // عرض كرت المهني المقبول
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppTheme.darkBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppTheme.surfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                val bitmap = rememberBase64Bitmap(myApprovedProvider.imageUrl)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                }
                            }
                            
                            Column {
                                Text(
                                    text = myApprovedProvider.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val catName = categories.find { it.id == myApprovedProvider.category }?.nameAr ?: myApprovedProvider.category
                                val cityName = cities.find { it.id == myApprovedProvider.city }?.nameAr ?: myApprovedProvider.city
                                Text(
                                    text = "💼 $catName",
                                    color = AppTheme.accentGold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "📍 $cityName - ${myApprovedProvider.area}",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }
    
    if (myPendingRequest != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.darkBg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, AppTheme.accentGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.accentGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "⏳ طلب انضمامك قيد المراجعة السحابية حالياً",
                        color = AppTheme.accentGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "مرحباً يا غالي، لقد استلمنا طلب انضمامك بنجاح وجاري مراجعته وتدقيق البيانات من قبل إدارة المنصة للتأكد من التخصص والخبرة الفنية. ستتلقى إشعاراً في مركز الإشعارات السحابية فور قبول طلبك وتفعيل حسابك مباشرة.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // تفاصيل الطلب المقدم
                    Text(
                        text = "📋 البيانات المقدمة:",
                        color = AppTheme.accentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppTheme.darkBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "• الاسم: ${myPendingRequest.name}", color = Color.White, fontSize = 12.sp)
                            Text(text = "• الهاتف: ${myPendingRequest.phone}", color = Color.White, fontSize = 12.sp)
                            val catName = categories.find { it.id == myPendingRequest.category }?.nameAr ?: myPendingRequest.category
                            val cityName = cities.find { it.id == myPendingRequest.city }?.nameAr ?: myPendingRequest.city
                            Text(text = "• التخصص المهني: $catName", color = Color.White, fontSize = 12.sp)
                            Text(text = "• المدينة والمنطقة: $cityName - ${myPendingRequest.area}", color = Color.White, fontSize = 12.sp)
                            if (myPendingRequest.description.isNotBlank()) {
                                Text(text = "• نبذة عن خبرتك: ${myPendingRequest.description}", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    
    // الإدخال الصوتي المحلي لتعبئة الخانات
    var activeVoiceTarget by remember { mutableStateOf("") }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                when (activeVoiceTarget) {
                    "name" -> name = text
                    "phone" -> phone = text
                    "area" -> area = text
                    "desc" -> desc = text
                }
            }
        }
    }
    
    // تحميل ومعالجة صورة الهوية وصورة البروفايل الشخصية
    var profileImageBase64 by remember { mutableStateOf("") }
    var idCardBase64 by remember { mutableStateOf("") }
    
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val base64 = compressImageBase64(inputStream)
                if (base64 != null) {
                    profileImageBase64 = base64
                    Toast.makeText(context, "✅ تم إرفاق ومعالجة الصورة بنجاح سحابياً!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل معالجة الصورة الشخصية", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val idCardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val base64 = compressImageBase64(inputStream)
                if (base64 != null) {
                    idCardBase64 = base64
                    Toast.makeText(context, "✅ تم إرفاق ومعالجة صورة الهوية بنجاح سحابياً!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل معالجة صورة بطاقة الهوية", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💼 تقديم طلب انضمام كفني جديد",
                    color = AppTheme.accentGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "انضم إلى أكبر دليل خدمات في اليمن وزد أرباحك وتواصلك المباشر مع ملايين العملاء سحابياً!",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
        
        // شروط وقوانين التسجيل
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "📜 شروط وقواعد العمل بالمنصة:",
                    color = AppTheme.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                settings.registrationConditions.forEach { condition ->
                    val label = if (condition.isRequired) "⚠️ (إجباري)" else "✨ (اختياري)"
                    val color = if (condition.isRequired) AppTheme.accentGold else Color.Gray
                    Text(text = "• ${condition.text} $label", color = Color.White, fontSize = 10.sp)
                }
            }
        }
        
        // حقول البيانات الأساسية
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("الاسم الكامل (مطابق للهوية)", color = Color.Gray, fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            trailingIcon = {
                if (settings.allowVoiceInput) {
                    IconButton(onClick = {
                        activeVoiceTarget = "name"
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بالاسم الكامل...")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "تحدث بالاسم", tint = AppTheme.accentGold)
                    }
                }
            },
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
            label = { Text("رقم هاتفك (مع الواتس الفعال)", color = Color.Gray, fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            trailingIcon = {
                if (settings.allowVoiceInput) {
                    IconButton(onClick = {
                        activeVoiceTarget = "phone"
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث برقم الهاتف...")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "تحدث بالهاتف", tint = AppTheme.accentGold)
                    }
                }
            },
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
            value = area,
            onValueChange = { area = it },
            label = { Text("المنطقة السكنية بالتفصيل", color = Color.Gray, fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            trailingIcon = {
                if (settings.allowVoiceInput) {
                    IconButton(onClick = {
                        activeVoiceTarget = "area"
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بالمنطقة السكنية...")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "تحدث بالمنطقة", tint = AppTheme.accentGold)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AppTheme.accentGold,
                unfocusedBorderColor = Color(0xFF223639)
            ),
            shape = RoundedCornerShape(8.dp)
        )
        
        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("أعمالك السابقة ونبذة عن مهاراتك", color = Color.Gray, fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            trailingIcon = {
                if (settings.allowVoiceInput) {
                    IconButton(onClick = {
                        activeVoiceTarget = "desc"
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بنبذة عن مهاراتك...")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "تحدث بنبذة", tint = AppTheme.accentGold)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AppTheme.accentGold,
                unfocusedBorderColor = Color(0xFF223639)
            ),
            shape = RoundedCornerShape(8.dp)
        )
        
        // اختيار التخصص الفني
        Text("اختر تخصصك المهني:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = if (isSelected) "" else category.id },
                    label = { Text("${category.iconUrl} ${category.nameAr}", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTheme.primaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = AppTheme.surfaceDark,
                        labelColor = Color.White
                    )
                )
            }
        }
        
        // اختيار المدينة المهنية
        Text("اختر مدينة خدماتك الرئيسية:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(cities) { city ->
                val isSelected = selectedCity == city.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCity = if (isSelected) "" else city.id },
                    label = { Text(city.nameAr, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppTheme.primaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = AppTheme.surfaceDark,
                        labelColor = Color.White
                    )
                )
            }
        }
        
        // إرفاق الصورة الشخصية السحابية المتقدمة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { imageLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.surfaceDark),
                border = BorderStroke(1.dp, AppTheme.accentGold)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(16.dp))
                    Text("إرفاق الصورة الشخصية", color = Color.White, fontSize = 10.sp)
                }
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.surfaceDark),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = rememberBase64Bitmap(profileImageBase64)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        // إرفاق صورة بطاقة الهوية السحابية (جديد)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { idCardLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.surfaceDark),
                border = BorderStroke(1.dp, AppTheme.accentGold)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = AppTheme.accentGold, modifier = Modifier.size(16.dp))
                    Text("إرفاق بطاقة الهوية/جواز السفر", color = Color.White, fontSize = 10.sp)
                }
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.surfaceDark),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = rememberBase64Bitmap(idCardBase64)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.Gray)
                }
            }
        }
        
        Button(
            onClick = {
                // التحقق من الحقول الأساسية
                if (name.isBlank() || phone.isBlank() || area.isBlank() || selectedCategory.isBlank() || selectedCity.isBlank()) {
                    Toast.makeText(context, "يرجى ملء جميع الحقول المطلوبة للانضمام", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                // التحقق من شروط وقوانين الانضمام التي حددها الآدمن ديناميكياً
                val conditionsMap = settings.registrationConditions.associateBy { it.id }
                
                if (conditionsMap["id_card"]?.isRequired == true && idCardBase64.isBlank()) {
                    Toast.makeText(context, "❌ يرجى إرفاق صورة بطاقة الهوية كشرط إجباري للتسجيل!", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (conditionsMap["personal_photo"]?.isRequired == true && profileImageBase64.isBlank()) {
                    Toast.makeText(context, "❌ يرجى إرفاق صورتك الشخصية كشرط إجباري للتسجيل!", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (conditionsMap["experience_proof"]?.isRequired == true && desc.isBlank()) {
                    Toast.makeText(context, "❌ يرجى كتابة نبذة عن خبراتك كشرط إجباري للتسجيل!", Toast.LENGTH_LONG).show()
                    return@Button
                }
                
                // تقديم طلب انضمام معلق
                val request = Provider(
                    id = "prov_${System.currentTimeMillis()}",
                    name = name,
                    phone = phone,
                    category = selectedCategory,
                    city = selectedCity,
                    area = area,
                    description = desc,
                    imageUrl = profileImageBase64,
                    idCardBase64 = idCardBase64,
                    isVerified = false,
                    deviceId = currentUserId ?: ""
                )
                
                // نقوم بحفظ الطلب سحابياً في الفيو موديل
                viewModel.addPendingRequest(
                    provider = request,
                    onSuccess = {
                        Toast.makeText(context, "✅ تم إرسال طلب انضمامك سحابياً! ستتم مراجعته من الإدارة وقبوله قريباً جداً.", Toast.LENGTH_LONG).show()
                        // إعادة تعيين الحقول
                        name = ""
                        phone = ""
                        selectedCategory = ""
                        selectedCity = ""
                        area = ""
                        desc = ""
                        profileImageBase64 = ""
                        idCardBase64 = ""
                    },
                    onFailure = {
                        Toast.makeText(context, "فشل الاتصال بالشبكة لحفظ طلب الانضمام", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إرسال طلب الانضمام 🚀", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ============================================================
// ℹ️ شاشة "عن التطبيق" والاتصال بالدعم والمشاركة
// ============================================================
@Composable
fun AboutScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // الشعار الرئيسي وتسمية التطبيق المطور
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppTheme.primaryRed),
            contentAlignment = Alignment.Center
        ) {
            Text(settings.appLogoText, color = AppTheme.accentGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Text(
            text = settings.appNameAr,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        // غلاف الصفحة سحابي من الأدمن
        if (settings.aboutPageImageCover.isNotBlank()) {
            val bitmap = remember(settings.aboutPageImageCover) {
                try {
                    val decoded = android.util.Base64.decode(settings.aboutPageImageCover, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                } catch (e: Exception) { null }
            }
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "غلاف دليل الخدمات",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, AppTheme.accentGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                )
            }
        }
        
        // معلومات تفصيلية عن المنصة
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = settings.aboutTitleText,
                    color = AppTheme.accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = settings.aboutPageText,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                
                if (settings.aboutVersionVisible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(settings.aboutVersionLabel, color = Color.Gray, fontSize = 11.sp)
                        Text(settings.aboutVersionValue, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (settings.aboutSecurityVisible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(settings.aboutSecurityLabel, color = Color.Gray, fontSize = 11.sp)
                        Text(settings.aboutSecurityValue, color = AppTheme.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // زر تحميل وتحديث التطبيق
        if (settings.appDownloadUrl.isNotBlank()) {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.appDownloadUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accentGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text("📥 تحميل وتحديث التطبيق المباشر", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // أزرار الدعم والاتصال بإدارة المنصة ومطور النظام
        Text("📞 اتصل وتواصل معنا مباشرة:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val phoneNum = if (settings.supportPhoneNumber.isNotBlank()) settings.supportPhoneNumber else settings.aboutPhone
            if (settings.aboutPhoneVisible) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("اتصال الدعم", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            
            if (settings.aboutWhatsappVisible) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${settings.aboutWhatsapp}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("واتساب الدعم", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ============================================================
// 🤖 شاشة المساعد السحابي الذكي (الدردشة مع Gemini)
// ============================================================
@Composable
fun SmartAssistantScreen(viewModel: MainViewModel) {
    val messages by viewModel.geminiMessages.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val mainActivity = context as? MainActivity
    
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                inputText = matches[0]
            }
        }
    }
    
    // نطق الرسالة الأخيرة تلقائياً عند استلامها من الذكاء الاصطناعي
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastMsg = messages.last()
            if (!lastMsg.second && settings.allowTextToSpeech) {
                mainActivity?.speak(lastMsg.first)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.darkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.surfaceDark)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = AppTheme.accentGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🤖 المساعد الذكي لدليل خدمات اليمن",
                color = AppTheme.accentGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { (text, isUser) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUser && settings.allowTextToSpeech) {
                        IconButton(
                            onClick = { mainActivity?.speak(text) },
                            modifier = Modifier.padding(end = 4.dp).size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "استماع للنص",
                                tint = AppTheme.accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 12.dp
                                )
                            )
                            .background(if (isUser) AppTheme.primaryRed else AppTheme.surfaceDark)
                            .padding(10.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
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
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("اكتب سؤالك الفني والمساعد سيبحث لك...", color = Color.Gray, fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
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
            
            if (settings.allowVoiceInput) {
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن بسؤالك الفني...")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "التعرف الصوتي غير مدعوم بجهازك", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppTheme.accentGold)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "تحدث بصوتك", tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
            
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.askGemini(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppTheme.primaryRed)
            ) {
                Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ============================================================
// 🔔 مركز الإشعارات السحابية المتقدمة
// ============================================================
@Composable
fun NotificationCenterDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    
    // تصفية الإشعارات لضمان الخصوصية والسرية والأمان التام
    val filteredNotifications = remember(notifications, currentUserId, currentUserRole) {
        notifications.filter { item ->
            if (item.targetUserId.isNotBlank()) {
                // إذا كان الإشعار موجه لمستخدم محدد بالـ ID
                currentUserId != null && item.targetUserId == currentUserId
            } else {
                // إذا كان الإشعار عاماً لفئة معينة
                val isAll = item.targetRole == "all" || item.targetRole.isBlank()
                val isMyRole = (currentUserRole == "provider" && item.targetRole == "providers") ||
                               (currentUserRole == "user" && item.targetRole == "users") ||
                               ((currentUserRole == "admin" || currentUserRole == "admins") && item.targetRole == "admins")
                isAll || isMyRole
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.surfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppTheme.accentGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
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
                        text = "🔔 مركز الإشعارات السحابية",
                        color = AppTheme.accentGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }
                
                if (filteredNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد إشعارات جديدة مرسلة", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredNotifications) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AppTheme.darkBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        color = AppTheme.accentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = item.body,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
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
// 📸 ضغط الصور سحابياً ومحلياً
// ============================================================
fun compressImageBase64(inputStream: InputStream?): String? {
    if (inputStream == null) return null
    try {
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return compressBitmapBase64(bitmap)
    } catch (e: Exception) {
        return null
    }
}

fun compressBitmapBase64(bitmap: Bitmap): String? {
    return try {
        val baos = ByteArrayOutputStream()
        // ضغط بنسبة 35% لتقليل استهلاك حزم الـ Database والـ Firestore
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, baos)
        val bytes = baos.toByteArray()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        null
    }
}

// تحويل Base64 إلى Bitmap Compose
@Composable
fun rememberBase64Bitmap(base64String: String?): Bitmap? {
    if (base64String.isNullOrBlank()) return null
    return remember(base64String) {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
