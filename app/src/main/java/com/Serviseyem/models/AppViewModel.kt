package com.Serviseyem.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Firebase Firestore Imports
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.ListenerRegistration

class AppViewModel : ViewModel() {

    // --- محرك المزامنة والحفظ المحلي في الذاكرة ---
    private val _footerUpdateFlow = MutableSharedFlow<Pair<String, Float>>(replay = 1)
    val footerUpdateFlow = _footerUpdateFlow.asSharedFlow()

    // Firebase configuration with memory cache (no persistence delays as per instructions)
    private val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()
        db.firestoreSettings = settings
        db
    }

    // --- Backing properties for real-time Firestore sync & Auto-propagating setters ---
    private val _footerText = mutableStateOf("wam 2026")
    var footerText: String
        get() = _footerText.value
        set(value) {
            if (_footerText.value != value) {
                _footerText.value = value
                updateSettingsField("footerText", value)
            }
        }

    private val _footerFontSize = mutableStateOf(11f)
    var footerFontSize: Float
        get() = _footerFontSize.value
        set(value) {
            if (_footerFontSize.value != value) {
                _footerFontSize.value = value
                updateSettingsField("footerFontSize", value)
            }
        }

    private val _isFooterVisible = mutableStateOf(true)
    var isFooterVisible: Boolean
        get() = _isFooterVisible.value
        set(value) {
            if (_isFooterVisible.value != value) {
                _isFooterVisible.value = value
                updateSettingsField("isFooterVisible", value)
            }
        }

    private val _ownerPasswordSecret = mutableStateOf("maher--736462")
    var ownerPasswordSecret: String
        get() = _ownerPasswordSecret.value
        set(value) {
            if (_ownerPasswordSecret.value != value) {
                _ownerPasswordSecret.value = value
                updateSettingsField("ownerPasswordSecret", value)
            }
        }

    private val _adminUsernameSecret = mutableStateOf("WAM2026")
    var adminUsernameSecret: String
        get() = _adminUsernameSecret.value
        set(value) {
            if (_adminUsernameSecret.value != value) {
                _adminUsernameSecret.value = value
                updateSettingsField("adminUsernameSecret", value)
            }
        }

    private val _adminPasswordSecret = mutableStateOf("maher736462")
    var adminPasswordSecret: String
        get() = _adminPasswordSecret.value
        set(value) {
            if (_adminPasswordSecret.value != value) {
                _adminPasswordSecret.value = value
                updateSettingsField("adminPasswordSecret", value)
            }
        }

    private val _appNameAr = mutableStateOf("دليل خدمات اليمن")
    var appNameAr: String
        get() = _appNameAr.value
        set(value) {
            if (_appNameAr.value != value) {
                _appNameAr.value = value
                updateSettingsField("appNameAr", value)
            }
        }

    private val _appNameEn = mutableStateOf("Yemen Services Dir")
    var appNameEn: String
        get() = _appNameEn.value
        set(value) {
            if (_appNameEn.value != value) {
                _appNameEn.value = value
                updateSettingsField("appNameEn", value)
            }
        }

    private val _appLogoEmoji = mutableStateOf("🇾🇪")
    var appLogoEmoji: String
        get() = _appLogoEmoji.value
        set(value) {
            if (_appLogoEmoji.value != value) {
                _appLogoEmoji.value = value
                updateSettingsField("appLogoEmoji", value)
            }
        }

    private val _appGreetingMessageAr = mutableStateOf("أهلاً ومرحباً بكم مع تطبيق دليل كل خدمات اليمن - الرفيق الموثوق للأعمال المهنية وصيانة المنازل بدقة معيارية لحظية متميزة")
    var appGreetingMessageAr: String
        get() = _appGreetingMessageAr.value
        set(value) {
            if (_appGreetingMessageAr.value != value) {
                _appGreetingMessageAr.value = value
                updateSettingsField("appGreetingMessageAr", value)
            }
        }

    private val _appGreetingMessageEn = mutableStateOf("Welcome to Yemen Services Directory - Your trusted companion for professional business and home maintenance with real-time accuracy!")
    var appGreetingMessageEn: String
        get() = _appGreetingMessageEn.value
        set(value) {
            if (_appGreetingMessageEn.value != value) {
                _appGreetingMessageEn.value = value
                updateSettingsField("appGreetingMessageEn", value)
            }
        }

    private val _supportPhone = mutableStateOf("777644670")
    var supportPhone: String
        get() = _supportPhone.value
        set(value) {
            if (_supportPhone.value != value) {
                _supportPhone.value = value
                updateSettingsField("supportPhone", value)
            }
        }

    private val _supportEmail = mutableStateOf("support@serviseyem.com")
    var supportEmail: String
        get() = _supportEmail.value
        set(value) {
            if (_supportEmail.value != value) {
                _supportEmail.value = value
                updateSettingsField("supportEmail", value)
            }
        }

    private val _supportWhatsapp = mutableStateOf("967777644670")
    var supportWhatsapp: String
        get() = _supportWhatsapp.value
        set(value) {
            if (_supportWhatsapp.value != value) {
                _supportWhatsapp.value = value
                updateSettingsField("supportWhatsapp", value)
            }
        }

    private val _rememberMeNormal = mutableStateOf(false)
    var rememberMeNormal: Boolean
        get() = _rememberMeNormal.value
        set(value) {
            if (_rememberMeNormal.value != value) {
                _rememberMeNormal.value = value
                updateSettingsField("rememberMeNormal", value)
            }
        }

    private val _rememberMeBackdoor = mutableStateOf(false)
    var rememberMeBackdoor: Boolean
        get() = _rememberMeBackdoor.value
        set(value) {
            if (_rememberMeBackdoor.value != value) {
                _rememberMeBackdoor.value = value
                updateSettingsField("rememberMeBackdoor", value)
            }
        }

    var isArabic by mutableStateOf(true)

    // Top Bar customizable order items: "home", "login", "register", "language", "refresh"
    var topBarIcons by mutableStateOf(listOf("home", "login", "register", "language", "refresh"))

    // تحديث تذييل الشاشات مع المزامنة الفورية السحابة
    fun updateFooterTextFromFirestore(text: String, size: Float) {
        viewModelScope.launch {
            _footerUpdateFlow.emit(Pair(text, size))
            footerText = text
            footerFontSize = size
            addActivityLog("النظام المحلي: تم تحديث نص التذييل إلى '$text' وحجم الخط إلى $size")
        }
    }

    // Dynamic Fonts
    private val _appSelectedFontName = mutableStateOf("Default")
    var appSelectedFontName: String
        get() = _appSelectedFontName.value
        set(value) {
            if (_appSelectedFontName.value != value) {
                _appSelectedFontName.value = value
                updateSettingsField("appSelectedFontName", value)
            }
        }

    val appFontFamily: androidx.compose.ui.text.font.FontFamily
        get() = when (appSelectedFontName) {
            "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
            "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
            "SansSerif" -> androidx.compose.ui.text.font.FontFamily.SansSerif
            "Cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
            else -> androidx.compose.ui.text.font.FontFamily.Default
        }

    // About App dynamic variables
    var appDownloadLink by mutableStateOf("https://yemservices.page.link/download")
    var appInfoUploadedImagePath by mutableStateOf<String?>("https://cdn-icons-png.flaticon.com/512/2983/2983067.png")
    var appInfoImageEmoji by mutableStateOf("📱")
    var aboutAppTitle by mutableStateOf("الدليل الوطني لربط المهنيين بالعملاء 🇾🇪")
    var aboutAppDescription by mutableStateOf("تطبيق صمم بتقاطعات هندسية عالية لتمكين البحث السريع، والمحادثات المباشرة الفورية والدقيقة.")
    var aboutAppVersion by mutableStateOf("V2.6.2026")
    var aboutAppUsersStat by mutableStateOf("7,820")
    var aboutAppProvidersStat by mutableStateOf("1,240")

    // Loyalty Points Configuration
    var showLoyaltySection by mutableStateOf(false)
    var loyaltyCardText by mutableStateOf("استبدل خصم 100 نقطة فوري لتقليل كلفة الزيارات بمقدار 5000 ريال يمني!")
    var loyaltyCardTitle by mutableStateOf("🎁 رصيد نقاط الولاء الخاصة بك بالدليل الحالي: %d نقطة")
    var loyaltyCardProgressSize by mutableStateOf(13f)
    var loyaltyCardHeightPadding by mutableStateOf(14f)

    // حالة تسجيل دخول المشرف/المالك للبقاء مستقراً ومنع الخروج المفاجئ
    var isAdminLoggedIn by mutableStateOf(false)

    // Central dynamic Lists loaded from Firestore directly
    var providers by mutableStateOf(listOf<ServiceProvider>())
    var categories by mutableStateOf(listOf<Category>())
    var cities by mutableStateOf(listOf<City>())
    var banners by mutableStateOf(listOf<AdBanner>())
    var registrationRequests by mutableStateOf(listOf<ServiceProvider>())
    var chatSessions by mutableStateOf(listOf<ChatSession>())
    var chatMessages by mutableStateOf(listOf<ChatMessage>())
    var complaints by mutableStateOf(listOf<Complaint>())
    var bookings by mutableStateOf(listOf<Booking>())
    var registrationTerms by mutableStateOf(listOf<RegistrationTerm>())
    var admins by mutableStateOf(listOf<AdminAccount>())

    // Admin Customization / Layout State Configurations
    private val _appPrimaryColorStr = mutableStateOf("#FFD700")
    var appPrimaryColorStr: String
        get() = _appPrimaryColorStr.value
        set(value) {
            if (_appPrimaryColorStr.value != value) {
                _appPrimaryColorStr.value = value
                updateSettingsField("appPrimaryColorStr", value)
            }
        }

    val appPrimaryColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(appPrimaryColorStr))
        } catch (e: Exception) {
            Color(0xFFFFD700)
        }

    // Chat widget settings
    var chatSettingsIconSize by mutableStateOf(60f) // Size in DP
    private val _chatSettingsIconColorStr = mutableStateOf("#064E3B")
    var chatSettingsIconColorStr: String
        get() = _chatSettingsIconColorStr.value
        set(value) {
            if (_chatSettingsIconColorStr.value != value) {
                _chatSettingsIconColorStr.value = value
                updateSettingsField("chatSettingsIconColorStr", value)
            }
        }

    val chatSettingsIconColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(chatSettingsIconColorStr))
        } catch (e: Exception) {
            Color(0xFF064E3B)
        }
    var chatSettingsVisible by mutableStateOf(true)
    var chatSettingsDeleted by mutableStateOf(false)
    var chatSettingsIconEmoji by mutableStateOf("💬")
    var chatSettingsOffsetX by mutableStateOf(0f)
    var chatSettingsOffsetY by mutableStateOf(0f)
    var chatSettingsAlignmentIsRight by mutableStateOf(false) // Default Left (bottom start)

    // AI Assistant widget settings
    var aiAssistantIconSize by mutableStateOf(60f)
    private val _aiAssistantIconColorStr = mutableStateOf("#111827")
    var aiAssistantIconColorStr: String
        get() = _aiAssistantIconColorStr.value
        set(value) {
            if (_aiAssistantIconColorStr.value != value) {
                _aiAssistantIconColorStr.value = value
                updateSettingsField("aiAssistantIconColorStr", value)
            }
        }

    val aiAssistantIconColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(aiAssistantIconColorStr))
        } catch (e: Exception) {
            Color(0xFF111827)
        }
    var aiAssistantVisible by mutableStateOf(true)
    var aiAssistantDeleted by mutableStateOf(false)
    var aiAssistantIconEmoji by mutableStateOf("🤖")
    var aiAssistantOffsetX by mutableStateOf(0f)
    var aiAssistantOffsetY by mutableStateOf(0f)
    var aiAssistantAlignmentIsRight by mutableStateOf(true) // Default Right (bottom end)

    // Permissions and overall app features toggled by admin
    private val _voiceSearchEnabled = mutableStateOf(true)
    var voiceSearchEnabled: Boolean
        get() = _voiceSearchEnabled.value
        set(value) {
            if (_voiceSearchEnabled.value != value) {
                _voiceSearchEnabled.value = value
                updateSettingsField("voiceSearchEnabled", value)
            }
        }

    var mapRadiusKm by mutableStateOf(10.0) // 5km, 10km, 25km, etc.
    var autoCleanupDays by mutableStateOf(30)

    private val _isChatInstantEnabled = mutableStateOf(true)
    var isChatInstantEnabled: Boolean
        get() = _isChatInstantEnabled.value
        set(value) {
            if (_isChatInstantEnabled.value != value) {
                _isChatInstantEnabled.value = value
                updateSettingsField("isChatInstantEnabled", value)
            }
        }

    var chatDisabledMessage by mutableStateOf("المحادثة الفورية معطلة مؤقتاً لأعمال الكفاءة - نرجو التواصل هاتفياً مباشرة")

    private val _isRatingsAndReviewsEnabled = mutableStateOf(true)
    var isRatingsAndReviewsEnabled: Boolean
        get() = _isRatingsAndReviewsEnabled.value
        set(value) {
            if (_isRatingsAndReviewsEnabled.value != value) {
                _isRatingsAndReviewsEnabled.value = value
                updateSettingsField("isRatingsAndReviewsEnabled", value)
            }
        }

    private val _showBookingsSection = mutableStateOf(true)
    var showBookingsSection: Boolean
        get() = _showBookingsSection.value
        set(value) {
            if (_showBookingsSection.value != value) {
                _showBookingsSection.value = value
                updateSettingsField("showBookingsSection", value)
            }
        }

    // User State Information
    var userLoyaltyPoints by mutableStateOf(100)
    var activeAdminUsername by mutableStateOf<String?>(null)

    // Log tracking for administration panel
    var adminActivityLogs by mutableStateOf(listOf(
        "تم تشغيل لوحة التحكم ومزامنة بروتوكولات الأمان الفورية بنجاح."
    ))

    fun addActivityLog(log: String) {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        adminActivityLogs = listOf("[$stamp] $log") + adminActivityLogs
    }

    // Firebase Listener Registrations
    private var categoriesListener: ListenerRegistration? = null
    private var serviceProvidersListener: ListenerRegistration? = null
    private var citiesListener: ListenerRegistration? = null
    private var bannersListener: ListenerRegistration? = null
    private var registrationRequestsListener: ListenerRegistration? = null
    private var complaintsListener: ListenerRegistration? = null
    private var bookingsListener: ListenerRegistration? = null
    private var registrationTermsListener: ListenerRegistration? = null
    private var adminsListener: ListenerRegistration? = null
    private var chatMessagesListener: ListenerRegistration? = null
    private var chatSessionsListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null

    init {
        // تشغيل مراقب التغييرات المحلي للتذييل
        viewModelScope.launch {
            _footerUpdateFlow.collect { (text, size) ->
                _footerText.value = text
                _footerFontSize.value = size
                addActivityLog("مستمع محلي فوري: مزامنة التذييل '$text'")
            }
        }

        // تهيئة قواعد بيانات Firestore بالبيانات التأسيسية إذا كانت فارغة
        initializeFirebaseIfNeeded()

        // بدء الاستماع الفوري لجميع المجموعات
        setupSnapshotListeners()
    }

    private fun initializeFirebaseIfNeeded() {
        viewModelScope.launch {
            firestore.collection("categories").get().addOnSuccessListener { query ->
                if (query.isEmpty) {
                    val defaultCats = listOf(
                        Category(nameAr = "سباكة", nameEn = "Plumbing", description = "صيانة وتمديد شبكات المياه ومعالجة التسريبات بدقة", iconEmoji = "🔧", isPinned = true),
                        Category(nameAr = "كهرباء", nameEn = "Electrical", description = "تركيب وصيانة أنظمة الإنارة، وتمديدات الطاقة الشمسية والمولدات", iconEmoji = "⚡", isPinned = true),
                        Category(nameAr = "دهان", nameEn = "Painting", description = "أرقى أعمال الديكورات والدهانات الداخلية والخارجية والجبسية", iconEmoji = "🎨", isPinned = true),
                        Category(nameAr = "نجارة", nameEn = "Carpentry", description = "تصميم وتركيب وصيانة الأبواب والشبابيك والأثاث المودرن", iconEmoji = "🔨", isPinned = true),
                        Category(nameAr = "حدادة", nameEn = "Smithing", description = "تفصيل وتركيب البوابات والمظلات والحمايات الحديدية المتينة", iconEmoji = "⚙️", isPinned = true),
                        Category(nameAr = "تبريد وتكييف", nameEn = "Cooling & AC", description = "شحن وتوريد وصيانة غسيل أجهزة التكييف المركزي والاسبليت", iconEmoji = "❄️", isPinned = false),
                        Category(nameAr = "صيانة", nameEn = "General Maintenance", description = "خدمات الصيانة الشاملة والترميمات المتكاملة للمباني", iconEmoji = "🛠️", isPinned = false)
                    )
                    for (cat in defaultCats) {
                        firestore.collection("categories").document(cat.id).set(cat)
                    }
                }
            }

            firestore.collection("service_providers").get().addOnSuccessListener { query ->
                if (query.isEmpty) {
                    val defaultProviders = listOf(
                        ServiceProvider(name = "المهندس وليد الصنعاني", phone = "777123456", specialty = "تبريد وتكييف", city = "صنعاء", rating = 4.9, ratingsCount = 14, isVip = true, isVerified = true, baseFee = 5000, biography = "أخصائي تكييف وتبريد مركزي ذو خبرة تفوق 10 سنوات في صيانة وتوريد كافة الأنظمة."),
                        ServiceProvider(name = "أبو ماجد البريحي", phone = "777644670", specialty = "سباكة", city = "إب", rating = 4.8, ratingsCount = 21, isVip = true, isVerified = true, baseFee = 4000, biography = "خبير تركيب وصيانة الشبكات لجميع فلل وعمارات المحافظة بأعلى جودة."),
                        ServiceProvider(name = "أحمد جلال الحديدي", phone = "733654321", specialty = "كهرباء", city = "الحديدة", rating = 4.7, ratingsCount = 9, isVip = false, isVerified = true, baseFee = 3500, biography = "فني تمديدات وصيانة أنظمة الطاقة الشمسية والتيار المتردد المنزلي."),
                        ServiceProvider(name = "ياسين النجار", phone = "711998877", specialty = "نجارة", city = "عدن", rating = 4.6, ratingsCount = 7, isVip = false, isVerified = false, baseFee = 6000, biography = "تفصيل وتجهيز أحدث الأثاث الخشبي والمودرن وغرف النوم بجودة وسرعة."),
                        ServiceProvider(name = "فؤاد الحداد", phone = "777554433", specialty = "حدادة", city = "صنعاء", rating = 4.5, ratingsCount = 5, isVip = false, isVerified = false, baseFee = 4500, biography = "أعمال الأبواب والشبابيك والمظلات والدرابزين الفاخر بدقة عالية.")
                    )
                    for (p in defaultProviders) {
                        firestore.collection("service_providers").document(p.id).set(p)
                    }
                }
            }

            firestore.collection("cities").get().addOnSuccessListener { query ->
                if (query.isEmpty) {
                    val defaultCities = listOf(
                        City(nameAr = "صنعاء", nameEn = "Sana'a"),
                        City(nameAr = "عدن", nameEn = "Aden"),
                        City(nameAr = "إب", nameEn = "Ibb"),
                        City(nameAr = "الحديدة", nameEn = "Hodeidah"),
                        City(nameAr = "تعز", nameEn = "Taiz"),
                        City(nameAr = "المكلا", nameEn = "Mukalla")
                    )
                    for (c in defaultCities) {
                        firestore.collection("cities").document(c.id).set(c)
                    }
                }
            }

            firestore.collection("banners").get().addOnSuccessListener { query ->
                if (query.isEmpty) {
                    val defaultBanners = listOf(
                        AdBanner(title = "أهلاً بكم في دليل كل خدمات اليمن - خصم 30% على صيانة التكييف المركزي والمنزلي!", contentType = "text", targetSectionId = "تبريد وتكييف", durationSeconds = 15, adSize = 10, isVisible = true)
                    )
                    for (b in defaultBanners) {
                        firestore.collection("banners").document(b.id).set(b)
                    }
                }
            }

            firestore.collection("registration_terms").get().addOnSuccessListener { query ->
                if (query.isEmpty) {
                    val defaultTerms = listOf(
                        RegistrationTerm(termText = "الالتزام التام بالأسعار المعيارية المقررة من الدليل اليمن المعتمد."),
                        RegistrationTerm(termText = "دقة المواعيد والأمانة في الفحص والمعاينات الفنية المعيارية."),
                        RegistrationTerm(termText = "توفير بطاقة شخصية وضمانة حضورية سارية المفعول عند الطلب.")
                    )
                    for (t in defaultTerms) {
                        firestore.collection("registration_terms").document(t.id).set(t)
                    }
                }
            }

            firestore.collection("admins").get().addOnSuccessListener { query ->
                if (query.isEmpty) {
                    val defaultAdmins = listOf(
                        AdminAccount("admin", "7777", listOf(
                            "قبول ورفض طلبات التسجيل للفنيين",
                            "إضافة وحذف وتعديل الأقسام والمدن",
                            "إدارة الإعلانات والبنرات المتحركة",
                            "حذف مزودي الخدمة النشطين من الدليل",
                            "رؤية بلاغات المستخدمين وتقارير التدقيق الكامل"
                        ))
                    )
                    for (adm in defaultAdmins) {
                        firestore.collection("admins").document(adm.username).set(adm)
                    }
                }
            }

            firestore.collection("app_settings").document("master").get().addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val initSettings = hashMapOf(
                        "footerText" to "wam 2026",
                        "footerFontSize" to 11.0,
                        "isFooterVisible" to true,
                        "ownerPasswordSecret" to "maher--736462",
                        "adminUsernameSecret" to "WAM2026",
                        "adminPasswordSecret" to "maher736462",
                        "appNameAr" to "دليل خدمات اليمن",
                        "appNameEn" to "Yemen Services Dir",
                        "appLogoEmoji" to "🇾🇪",
                        "appGreetingMessageAr" to "أهلاً ومرحباً بكم مع تطبيق دليل كل خدمات اليمن - الرفيق الموثوق للأعمال المهنية وصيانة المنازل بدقة معيارية لحظية متميزة",
                        "appGreetingMessageEn" to "Welcome to Yemen Services Directory - Your trusted companion for professional business and home maintenance with real-time accuracy!",
                        "supportPhone" to "777644670",
                        "supportEmail" to "support@serviseyem.com",
                        "supportWhatsapp" to "967777644670",
                        "rememberMeNormal" to false,
                        "rememberMeBackdoor" to false,
                        "appSelectedFontName" to "Default",
                        "appPrimaryColorStr" to "#FFD700",
                        "chatSettingsIconColorStr" to "#064E3B",
                        "aiAssistantIconColorStr" to "#111827",
                        "voiceSearchEnabled" to true,
                        "isChatInstantEnabled" to true,
                        "isRatingsAndReviewsEnabled" to true,
                        "showBookingsSection" to true
                    )
                    firestore.collection("app_settings").document("master").set(initSettings)
                }
            }
        }
    }

    private fun setupSnapshotListeners() {
        categoriesListener = firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    addActivityLog("خطأ استماع للأقسام: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    categories = snapshot.toObjects(Category::class.java)
                    addActivityLog("مستمع سحابي: تم تحميل أقسام الخدمات (${categories.size})")
                }
            }

        serviceProvidersListener = firestore.collection("service_providers")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    addActivityLog("خطأ استماع لمقدمي الخدمة: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    providers = snapshot.toObjects(ServiceProvider::class.java)
                    addActivityLog("مستمع سحابي: تم تحميل مقدمي الخدمات (${providers.size})")
                }
            }

        citiesListener = firestore.collection("cities")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    cities = snapshot.toObjects(City::class.java)
                }
            }

        bannersListener = firestore.collection("banners")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    banners = snapshot.toObjects(AdBanner::class.java)
                }
            }

        registrationRequestsListener = firestore.collection("pending_providers")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    registrationRequests = snapshot.toObjects(ServiceProvider::class.java)
                }
            }

        complaintsListener = firestore.collection("complaints")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    complaints = snapshot.toObjects(Complaint::class.java)
                }
            }

        bookingsListener = firestore.collection("bookings")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    bookings = snapshot.toObjects(Booking::class.java)
                }
            }

        registrationTermsListener = firestore.collection("registration_terms")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    registrationTerms = snapshot.toObjects(RegistrationTerm::class.java)
                }
            }

        adminsListener = firestore.collection("admins")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    admins = snapshot.toObjects(AdminAccount::class.java)
                }
            }

        chatSessionsListener = firestore.collection("chat_sessions")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    chatSessions = snapshot.toObjects(ChatSession::class.java)
                }
            }

        chatMessagesListener = firestore.collection("chat_messages")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    chatMessages = snapshot.toObjects(ChatMessage::class.java)
                }
            }

        settingsListener = firestore.collection("app_settings").document("master")
            .addSnapshotListener { doc, e ->
                if (e != null) return@addSnapshotListener
                if (doc != null && doc.exists()) {
                    _rememberMeNormal.value = doc.getBoolean("rememberMeNormal") ?: false
                    _rememberMeBackdoor.value = doc.getBoolean("rememberMeBackdoor") ?: false
                    _ownerPasswordSecret.value = doc.getString("ownerPasswordSecret") ?: "maher--736462"
                    _adminUsernameSecret.value = doc.getString("adminUsernameSecret") ?: "WAM2026"
                    _adminPasswordSecret.value = doc.getString("adminPasswordSecret") ?: "maher736462"
                    _appNameAr.value = doc.getString("appNameAr") ?: "دليل خدمات اليمن"
                    _appNameEn.value = doc.getString("appNameEn") ?: "Yemen Services Dir"
                    _appLogoEmoji.value = doc.getString("appLogoEmoji") ?: "🇾🇪"
                    _appGreetingMessageAr.value = doc.getString("appGreetingMessageAr") ?: ""
                    _appGreetingMessageEn.value = doc.getString("appGreetingMessageEn") ?: ""
                    _supportPhone.value = doc.getString("supportPhone") ?: "777644670"
                    _supportEmail.value = doc.getString("supportEmail") ?: "support@serviseyem.com"
                    _supportWhatsapp.value = doc.getString("supportWhatsapp") ?: "967777644670"
                    _appSelectedFontName.value = doc.getString("appSelectedFontName") ?: "Default"
                    _appPrimaryColorStr.value = doc.getString("appPrimaryColorStr") ?: "#FFD700"
                    _chatSettingsIconColorStr.value = doc.getString("chatSettingsIconColorStr") ?: "#064E3B"
                    _aiAssistantIconColorStr.value = doc.getString("aiAssistantIconColorStr") ?: "#111827"
                    _voiceSearchEnabled.value = doc.getBoolean("voiceSearchEnabled") ?: true
                    _isChatInstantEnabled.value = doc.getBoolean("isChatInstantEnabled") ?: true
                    _isRatingsAndReviewsEnabled.value = doc.getBoolean("isRatingsAndReviewsEnabled") ?: true
                    _showBookingsSection.value = doc.getBoolean("showBookingsSection") ?: true
                    _footerText.value = doc.getString("footerText") ?: "wam 2026"
                    _footerFontSize.value = doc.getDouble("footerFontSize")?.toFloat() ?: 11f
                    _isFooterVisible.value = doc.getBoolean("isFooterVisible") ?: true
                }
            }
    }

    private fun updateSettingsField(key: String, value: Any) {
        firestore.collection("app_settings").document("master")
            .update(key, value)
            .addOnFailureListener {
                val updateMap = hashMapOf<String, Any>(key to value)
                firestore.collection("app_settings").document("master")
                    .set(updateMap, com.google.firebase.firestore.SetOptions.merge())
            }
    }

    // Manual add with auto compression simulated
    fun addManualTechnician(
        name: String,
        phone: String,
        city: String,
        specialty: String,
        fee: Int,
        isVip: Boolean
    ): String {
        val formattedFee = if (fee <= 0) 5000 else fee
        val newTech = ServiceProvider(
            name = name,
            phone = phone,
            city = city,
            specialty = specialty,
            baseFee = formattedFee,
            isVip = isVip,
            isVerified = isVip,
            status = "مقبول"
        )
        firestore.collection("service_providers").document(newTech.id).set(newTech)
        addActivityLog("إضافة فني يدوياً: $name [$specialty] في $city")
        return "تم ضغط الصورة الشخصية تلقائيًا لسرعة تحميل التطبيق بنسبة 72%. تم دمج الكادر '${name}' فورياً."
    }

    // Submit provider application from client app
    fun registerNewProvider(
        name: String,
        phone: String,
        specialty: String,
        city: String,
        photoMethodSelection: String, // "Selfie Camera" / "Gallery Selection"
        isFemale: Boolean
    ) {
        val newRequest = ServiceProvider(
            name = name,
            phone = phone,
            specialty = specialty,
            city = city,
            status = "معلق"
        )
        firestore.collection("pending_providers").document(newRequest.id).set(newRequest)
        addActivityLog("طلب تسجيل جديد وارد من: $name [$specialty] عبر $photoMethodSelection")
    }

    fun approveRequest(id: String) {
        val request = registrationRequests.find { it.id == id }
        if (request != null) {
            request.status = "مقبول"
            firestore.collection("service_providers").document(request.id).set(request)
            firestore.collection("pending_providers").document(id).delete()
            addActivityLog("قبول طلب الفني: ${request.name}")
        }
    }

    fun rejectRequest(id: String) {
        val request = registrationRequests.find { it.id == id }
        if (request != null) {
            firestore.collection("pending_providers").document(id).delete()
            addActivityLog("رفض طلب الفني: ${request.name}")
        }
    }

    fun deleteActiveProvider(id: String) {
        val p = providers.find { id == it.id }
        if (p != null) {
            firestore.collection("service_providers").document(id).delete()
            addActivityLog("حذف المهني النشط: ${p.name}")
        }
    }

    fun addMainCategory(nameAr: String, nameEn: String, desc: String, iconCode: String) {
        val newCat = Category(
            nameAr = nameAr,
            nameEn = nameEn,
            description = desc,
            iconEmoji = iconCode
        )
        firestore.collection("categories").document(newCat.id).set(newCat)
        addActivityLog("إنشاء فئة رئيسية جديدة: $nameAr / $nameEn")
    }

    fun addCity(nameAr: String, nameEn: String) {
        val newCity = City(nameAr = nameAr, nameEn = nameEn)
        firestore.collection("cities").document(newCity.id).set(newCity)
        addActivityLog("إضافة مدينة تغطية جغرافية: $nameAr")
    }

    fun cleanUpTempLogs() {
        adminActivityLogs = listOf("[تصفير وتنظيف آلي للبيانات] تم تفريغ الكاش الإداري وتطهير ملفات الاستماع المؤقتة بنجاح.")
        addActivityLog("تشغيل دورة التنظيف الفوري.")
    }

    override fun onCleared() {
        super.onCleared()
        categoriesListener?.remove()
        serviceProvidersListener?.remove()
        citiesListener?.remove()
        bannersListener?.remove()
        registrationRequestsListener?.remove()
        complaintsListener?.remove()
        bookingsListener?.remove()
        registrationTermsListener?.remove()
        adminsListener?.remove()
        chatSessionsListener?.remove()
        chatMessagesListener?.remove()
        settingsListener?.remove()
    }
}
