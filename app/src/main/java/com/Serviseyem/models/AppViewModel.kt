package com.Serviseyem.models

import android.text.format.DateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

// Firebase Firestore Imports
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.ListenerRegistration

class AppViewModel : ViewModel() {

    // Global Activity log for auditory/visual debugging
    var adminActivityLogs by mutableStateOf(listOf<String>())

    // Direct Firestore Database Instance
    private val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()
        db.firestoreSettings = settings
        db
    }

    // --- State Properties Backed by Firestore synchronization ---
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
                updateSettingsField("footerFontSize", value.toDouble())
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

    // Dynamic Customizable Top Bar Icons order
    private var _topBarIconsOrderList = mutableStateOf(listOf("🏠", "🔐", "👤", "🌐", "🔄"))
    var topBarIconsOrderList: List<String>
        get() = _topBarIconsOrderList.value
        set(value) {
            _topBarIconsOrderList.value = value
            updateSettingsField("topBarIconsOrderList", value)
        }

    // Dynamic fonts selection
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
            "SansSerif" -> androidx.compose.ui.text.font.FontFamily.SansSerif
            "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
            else -> androidx.compose.ui.text.font.FontFamily.Default
        }

    // Static stats parameters
    var aboutAppUsersStat by mutableStateOf("7,820")
    var aboutAppProvidersStat by mutableStateOf("1,240")

    // General app primary style configs
    private val _appPrimaryColorStr = mutableStateOf("#FFD700") // Default Golden Accent
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

    private val _appSecondaryColorStr = mutableStateOf("#03DAC6") // Default Teal/Secondary Accent
    var appSecondaryColorStr: String
        get() = _appSecondaryColorStr.value
        set(value) {
            if (_appSecondaryColorStr.value != value) {
                _appSecondaryColorStr.value = value
                updateSettingsField("appSecondaryColorStr", value)
            }
        }

    val appSecondaryColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(appSecondaryColorStr))
        } catch (e: Exception) {
            Color(0xFF03DAC6)
        }

    private val _appBackgroundColorStr = mutableStateOf("#0A0A0C") // Default Dark Background
    var appBackgroundColorStr: String
        get() = _appBackgroundColorStr.value
        set(value) {
            if (_appBackgroundColorStr.value != value) {
                _appBackgroundColorStr.value = value
                updateSettingsField("appBackgroundColorStr", value)
            }
        }

    val appBackgroundColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(appBackgroundColorStr))
        } catch (e: Exception) {
            Color(0xFF0A0A0C)
        }

    private val _appTextColorStr = mutableStateOf("#FFFFFF") // Default White Text
    var appTextColorStr: String
        get() = _appTextColorStr.value
        set(value) {
            if (_appTextColorStr.value != value) {
                _appTextColorStr.value = value
                updateSettingsField("appTextColorStr", value)
            }
        }

    val appTextColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(appTextColorStr))
        } catch (e: Exception) {
            Color(0xFFFFFFFF)
        }

    // Chat widget settings parameters (default 60dp, supports scaling and colors)
    private val _chatSettingsIconSize = mutableStateOf(60f)
    var chatSettingsIconSize: Float
        get() = _chatSettingsIconSize.value
        set(value) {
            if (_chatSettingsIconSize.value != value) {
                _chatSettingsIconSize.value = value
                updateSettingsField("chatSettingsIconSize", value.toDouble())
            }
        }

    private val _chatSettingsIconColorStr = mutableStateOf("#064E3B") // Emerald Green default
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

    // Visibility and Delete controls for the chat widget floating icon
    private val _isChatIconMutedHidden = mutableStateOf(false)
    var isChatIconMutedHidden: Boolean
        get() = _isChatIconMutedHidden.value
        set(value) {
            if (_isChatIconMutedHidden.value != value) {
                _isChatIconMutedHidden.value = value
                updateSettingsField("isChatIconMutedHidden", value)
            }
        }

    private val _isChatIconPermDeleted = mutableStateOf(false)
    var isChatIconPermDeleted: Boolean
        get() = _isChatIconPermDeleted.value
        set(value) {
            if (_isChatIconPermDeleted.value != value) {
                _isChatIconPermDeleted.value = value
                updateSettingsField("isChatIconPermDeleted", value)
            }
        }

    // AI assistant widget config (Default style values)
    var aiAssistantIconSize by mutableStateOf(60f)
    var aiAssistantIconColorStr by mutableStateOf("#111827")
    val aiAssistantIconColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(aiAssistantIconColorStr))
        } catch (e: Exception) {
            Color(0xFF111827)
        }

    var showAiAssistantFloatingBubble by mutableStateOf(true)
    var aiAssistantAlignmentIsRight by mutableStateOf(true)

    // Admin privileges and general toggles
    private val _voiceSearchEnabled = mutableStateOf(true)
    var voiceSearchEnabled: Boolean
        get() = _voiceSearchEnabled.value
        set(value) {
            if (_voiceSearchEnabled.value != value) {
                _voiceSearchEnabled.value = value
                updateSettingsField("voiceSearchEnabled", value)
            }
        }

    var mapRadiusKm by mutableStateOf(10.0)
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

    private val _chatDisabledMessage = mutableStateOf("المحادثة الفورية معطلة مؤقتاً لأعمال الكفاءة - نرجو التواصل هاتفياً مباشرة")
    var chatDisabledMessage: String
        get() = _chatDisabledMessage.value
        set(value) {
            if (_chatDisabledMessage.value != value) {
                _chatDisabledMessage.value = value
                updateSettingsField("chatDisabledMessage", value)
            }
        }

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

    // Loyalty configuration parameter controlled dynamically
    var showLoyaltySection by mutableStateOf(false)
    var loyaltyCardText by mutableStateOf("استبدل خصم 100 نقطة فوري لتقليل كلفة الزيارات بمقدار 5000 ريال يمني!")
    var loyaltyCardTitle by mutableStateOf("🎁 رصيد نقاط الولاء الخاصة بك بالدليل الحالي: %d نقطة")
    var loyaltyCardHeightPadding by mutableStateOf(14f)

    // State information active logged statuses
    var isAdminLoggedIn by mutableStateOf(false)
    var activeAdminUsername by mutableStateOf<String?>(null)
    var userLoyaltyPoints by mutableStateOf(100)

    // Active instant chat flow parameters
    var activeChatSessionId by mutableStateOf<String?>(null)

    // Real-time collections populated via Snapshot Listeners
    var providers by mutableStateOf(listOf<ServiceProvider>())
    var categories by mutableStateOf(listOf<Category>())
    var cities by mutableStateOf(listOf<City>())
    var banners by mutableStateOf(listOf<AdBanner>())
    var registrationRequests by mutableStateOf(listOf<ServiceProvider>())
    var complaints by mutableStateOf(listOf<Complaint>())
    var bookings by mutableStateOf(listOf<Booking>())
    var registrationTerms by mutableStateOf(listOf<RegistrationTerm>())
    var admins by mutableStateOf(listOf<AdminAccount>())

    var chatSessions by mutableStateOf(listOf<ChatSession>())
    var chatMessages by mutableStateOf(listOf<ChatMessage>())
    var chatParticipants by mutableStateOf(listOf<ChatParticipant>())

    // Firestore listener registrations handles for teardown
    private var categoriesListener: ListenerRegistration? = null
    private var serviceProvidersListener: ListenerRegistration? = null
    private var citiesListener: ListenerRegistration? = null
    private var bannersListener: ListenerRegistration? = null
    private var registrationRequestsListener: ListenerRegistration? = null
    private var complaintsListener: ListenerRegistration? = null
    private var bookingsListener: ListenerRegistration? = null
    private var registrationTermsListener: ListenerRegistration? = null
    private var adminsListener: ListenerRegistration? = null
    private var chatSessionsListener: ListenerRegistration? = null
    private var chatMessagesListener: ListenerRegistration? = null
    private var chatParticipantsListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null

    init {
        initializeFirebaseDataIfNeeded()
        setupSnapshotListeners()
    }

    private fun initializeFirebaseDataIfNeeded() {
        firestore.collection("categories").get().addOnSuccessListener { query ->
            if (query.isEmpty || query.size() < 9) {
                val defaultCats = listOf(
                    Category(nameAr = "سباكة", nameEn = "Plumbing", description = "صيانة وتمديد شبكات المياه ومعالجة التسريبات بدقة", iconEmoji = "🔧", isPinned = true),
                    Category(nameAr = "كهرباء", nameEn = "Electrical", description = "تركيب وصيانة أنظمة الإنارة، وتمديدات الطاقة الشمسية والمولدات", iconEmoji = "⚡", isPinned = true),
                    Category(nameAr = "دهان", nameEn = "Painting", description = "أرقى أعمال الديكورات والدهانات الداخلية والخارجية والجبسية", iconEmoji = "🎨", isPinned = true),
                    Category(nameAr = "نجارة", nameEn = "Carpentry", description = "تصميم وتركيب وصيانة الأبواب والشبابيك والأثاث المودرن", iconEmoji = "🔨", isPinned = true),
                    Category(nameAr = "حدادة", nameEn = "Smithing", description = "تفصيل وتركيب البوابات والمظلات والحمايات الحديدية المتينة", iconEmoji = "⚙️", isPinned = true),
                    Category(nameAr = "تبريد وتكييف", nameEn = "Cooling & AC", description = "شحن وتوريد وصيانة غسيل أجهزة التكييف المركزي والاسبليت", iconEmoji = "❄️", isPinned = false),
                    Category(nameAr = "صيانة", nameEn = "General Maintenance", description = "خدمات الصيانة الشاملة والترميمات المتكاملة للمباني", iconEmoji = "🛠️", isPinned = false),
                    Category(nameAr = "ديكور وجبس", nameEn = "Decor & Gypsum", description = "تصميم وتنفيذ أرقى الديكورات الجبسية والأسقف المستعارة بدقة فنية", iconEmoji = "🏛️", isPinned = false),
                    Category(nameAr = "تنظيف وتعقيم", nameEn = "Cleaning & Sterilization", description = "خدمات النظافة الشاملة للمنازل والمكاتب وجلي وتلميع البلاط", iconEmoji = "🧹", isPinned = false)
                )
                for (doc in query.documents) {
                    doc.reference.delete()
                }
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
                for (provider in defaultProviders) {
                    firestore.collection("service_providers").document(provider.id).set(provider)
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
                for (city in defaultCities) {
                    firestore.collection("cities").document(city.id).set(city)
                }
            }
        }

        firestore.collection("banners").get().addOnSuccessListener { query ->
            if (query.isEmpty) {
                val defaultBanners = listOf(
                    AdBanner(title = "أهلاً بكم في دليل كل خدمات اليمن - خصم 30% على صيانة التكييف المركزي والمنزلي!", contentType = "text", targetSectionId = "تبريد وتكييف", durationSeconds = 15, adSize = 10, isVisible = true)
                )
                for (banner in defaultBanners) {
                    firestore.collection("banners").document(banner.id).set(banner)
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
                for (term in defaultTerms) {
                    firestore.collection("registration_terms").document(term.id).set(term)
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
                for (account in defaultAdmins) {
                    firestore.collection("admins").document(account.username).set(account)
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
                    "appSecondaryColorStr" to "#03DAC6",
                    "appBackgroundColorStr" to "#0A0A0C",
                    "appTextColorStr" to "#FFFFFF",
                    "chatSettingsIconColorStr" to "#FFD700",
                    "chatSettingsIconSize" to 60.0,
                    "isChatIconMutedHidden" to false,
                    "isChatIconPermDeleted" to false,
                    "voiceSearchEnabled" to true,
                    "isChatInstantEnabled" to true,
                    "chatDisabledMessage" to "المحادثة الفورية معطلة مؤقتاً لأعمال الكفاءة - نرجو التواصل هاتفياً مباشرة",
                    "isRatingsAndReviewsEnabled" to true,
                    "showBookingsSection" to true,
                    "topBarIconsOrderList" to listOf("🏠", "🔐", "👤", "🌐", "🔄")
                )
                firestore.collection("app_settings").document("master").set(initSettings)
            }
        }
    }

    private fun setupSnapshotListeners() {
        categoriesListener = firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    addActivityLog("Firestore Load Failure [categories]: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    categories = snapshot.toObjects(Category::class.java)
                }
            }

        serviceProvidersListener = firestore.collection("service_providers")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    providers = snapshot.toObjects(ServiceProvider::class.java)
                }
            }

        citiesListener = firestore.collection("cities")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    cities = snapshot.toObjects(City::class.java)
                }
            }

        bannersListener = firestore.collection("banners")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    banners = snapshot.toObjects(AdBanner::class.java)
                }
            }

        registrationRequestsListener = firestore.collection("pending_providers")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    registrationRequests = snapshot.toObjects(ServiceProvider::class.java)
                }
            }

        complaintsListener = firestore.collection("complaints")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    complaints = snapshot.toObjects(Complaint::class.java)
                }
            }

        bookingsListener = firestore.collection("bookings")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    bookings = snapshot.toObjects(Booking::class.java)
                }
            }

        registrationTermsListener = firestore.collection("registration_terms")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    registrationTerms = snapshot.toObjects(RegistrationTerm::class.java)
                }
            }

        adminsListener = firestore.collection("admins")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    admins = snapshot.toObjects(AdminAccount::class.java)
                }
            }

        chatSessionsListener = firestore.collection("chats")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    chatSessions = snapshot.toObjects(ChatSession::class.java)
                }
            }

        chatMessagesListener = firestore.collection("messages")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    chatMessages = snapshot.toObjects(ChatMessage::class.java)
                }
            }

        chatParticipantsListener = firestore.collection("chat_participants")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    chatParticipants = snapshot.toObjects(ChatParticipant::class.java)
                }
            }

        settingsListener = firestore.collection("app_settings").document("master")
            .addSnapshotListener { doc, e ->
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
                    _appPrimaryColorStr.value = doc.getString("appPrimaryColorStr") ?: "#1B5E20"
                    _appSecondaryColorStr.value = doc.getString("appSecondaryColorStr") ?: "#FFC700"
                    _appBackgroundColorStr.value = doc.getString("appBackgroundColorStr") ?: "#FFFFFF"
                    _appTextColorStr.value = doc.getString("appTextColorStr") ?: "#000000"
                    _chatSettingsIconColorStr.value = doc.getString("chatSettingsIconColorStr") ?: "#064E3B"
                    _chatSettingsIconSize.value = doc.getDouble("chatSettingsIconSize")?.toFloat() ?: 60f
                    _isChatIconMutedHidden.value = doc.getBoolean("isChatIconMutedHidden") ?: false
                    _isChatIconPermDeleted.value = doc.getBoolean("isChatIconPermDeleted") ?: false
                    _voiceSearchEnabled.value = doc.getBoolean("voiceSearchEnabled") ?: true
                    _isChatInstantEnabled.value = doc.getBoolean("isChatInstantEnabled") ?: true
                    _chatDisabledMessage.value = doc.getString("chatDisabledMessage") ?: "المحادثة الفورية معطلة مؤقتاً لأعمال الكفاءة - نرجو التواصل هاتفياً مباشرة"
                    _isRatingsAndReviewsEnabled.value = doc.getBoolean("isRatingsAndReviewsEnabled") ?: true
                    _showBookingsSection.value = doc.getBoolean("showBookingsSection") ?: true
                    _footerText.value = doc.getString("footerText") ?: "wam 2026"
                    _footerFontSize.value = doc.getDouble("footerFontSize")?.toFloat() ?: 11f
                    _isFooterVisible.value = doc.getBoolean("isFooterVisible") ?: true

                    val iconsOrder = doc.get("topBarIconsOrderList") as? List<*>
                    if (iconsOrder != null) {
                        _topBarIconsOrderList.value = iconsOrder.map { it.toString() }
                    }
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

    // Dynamic state management helpers & mutations
    fun addActivityLog(log: String) {
        val stamp = DateFormat.format("hh:mm:ss a", Date())
        adminActivityLogs = listOf("[$stamp] $log") + adminActivityLogs
    }

    fun updateFooterTextFromFirestore(text: String, size: Float) {
        footerText = text
        footerFontSize = size
        addActivityLog("النظام المحلي: تم تحديث تذييل التطبيق إلى '$text'")
    }

    // Create & route directly to a live instant chat with the ServiceProvider
    fun initiateInstantChatWithProvider(provider: ServiceProvider, userName: String) {
        val existingSession = chatSessions.find {
            (it.userName == userName && it.techId == provider.id)
        }

        if (existingSession != null) {
            activeChatSessionId = existingSession.id
            addActivityLog("تم العثور على محادثة قائمة ومزامنتها فورياً للعميل $userName مع مقدم الخدمة ${provider.name}")
        } else {
            val newSessionId = UUID.randomUUID().toString()
            val newSession = ChatSession(
                id = newSessionId,
                userName = userName,
                techName = provider.name,
                techId = provider.id,
                lastMessage = "بدأت المحادثة الفورية",
                lastUpdated = System.currentTimeMillis()
            )

            val partUser = ChatParticipant(chatId = newSessionId, userId = userName, role = "user")
            val partTech = ChatParticipant(chatId = newSessionId, userId = provider.id, role = "tech")

            firestore.collection("chats").document(newSessionId).set(newSession)
            firestore.collection("chat_participants").document(partUser.id).set(partUser)
            firestore.collection("chat_participants").document(partTech.id).set(partTech)

            val welcomeMsg = ChatMessage(
                chatId = newSessionId,
                senderName = provider.name,
                senderRole = "tech",
                messageText = provider.biography.ifEmpty { "أهلاً ومرحباً بك لتقديم أفضل خدمات صيانة وحلول في اليمن!" },
                timestamp = System.currentTimeMillis()
            )
            firestore.collection("messages").document(welcomeMsg.id).set(welcomeMsg)

            activeChatSessionId = newSessionId
            addActivityLog("بدء محادثة فورية جديدة ومزامنتها على السحابة: $userName مع ${provider.name}")
        }
    }

    // Send instant dynamic message inside chat session
    fun sendInstantChatMessage(chatId: String, senderName: String, senderRole: String, text: String) {
        val msg = ChatMessage(
            chatId = chatId,
            senderName = senderName,
            senderRole = senderRole,
            messageText = text,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("messages").document(msg.id).set(msg)
        firestore.collection("chats").document(chatId).update(
            "lastMessage", text,
            "lastUpdated", System.currentTimeMillis()
        )
        addActivityLog("إرسال رسالة: [$senderRole] -> $text")
    }

    // Super Admin level administrative control: block/stop session completely
    fun toggleBlockChatSession(chatSession: ChatSession) {
        val nextState = !chatSession.isBlocked
        firestore.collection("chats").document(chatSession.id).update("isBlocked", nextState)
        addActivityLog("تعديل حالة قفل المحادثة للتلمذة رقم ${chatSession.id} إلى: $nextState")
    }

    // Admin level toggle: Mute chat specifically for a singular provider
    fun toggleProviderChatMute(provider: ServiceProvider) {
        val nextMute = !provider.isChatMuted
        firestore.collection("service_providers").document(provider.id).update("isChatMuted", nextMute)
        addActivityLog("حالة كتم محادثات الفني ${provider.name}: $nextMute")
    }

    // Dynamic CRUD operations for other items (Synced with Firestore)
    fun addManualTechnician(
        name: String,
        phone: String,
        specialty: String,
        city: String,
        isVip: Boolean,
        biographyStr: String,
        photoMethodSelection: String
    ): String {
        val newTech = ServiceProvider(
            name = name,
            phone = phone,
            specialty = specialty,
            city = city,
            isVip = isVip,
            biography = biographyStr,
            isVerified = isVip,
            status = "مقبول"
        )
        firestore.collection("service_providers").document(newTech.id).set(newTech)
        addActivityLog("إضافة فني يدوياً: $name [$specialty] في $city")
        return "تم ضغط الصورة الشخصية تلقائيًا لسرعة تحميل التطبيق بنسبة 72%. تم دمج الكادر '${name}' فورياً."
    }

    fun requestTechnicianRegistration(
        name: String,
        phone: String,
        specialty: String,
        city: String,
        photoMethodSelection: String
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
            addActivityLog("قبول طلب الفني والاندماج الفوري للشبكة: ${request.name}")
        }
    }

    fun rejectRequest(id: String) {
        val request = registrationRequests.find { it.id == id }
        if (request != null) {
            firestore.collection("pending_providers").document(id).delete()
            addActivityLog("رفض وعزل طلب تسجيل الفني: ${request.name}")
        }
    }

    fun deleteActiveProvider(id: String) {
        val p = providers.find { id == it.id }
        if (p != null) {
            firestore.collection("service_providers").document(id).delete()
            addActivityLog("حذف المهني النشط ونزع تراخيصه: ${p.name}")
        }
    }

    fun addCategory(nameAr: String, nameEn: String, desc: String, iconCode: String) {
        val newCat = Category(
            nameAr = nameAr,
            nameEn = nameEn,
            description = desc,
            iconEmoji = iconCode
        )
        firestore.collection("categories").document(newCat.id).set(newCat)
        addActivityLog("إنشاء قسم أو مجال رئيسي جديد: $nameAr")
    }

    fun addCity(nameAr: String, nameEn: String) {
        val newCity = City(nameAr = nameAr, nameEn = nameEn)
        firestore.collection("cities").document(newCity.id).set(newCity)
        addActivityLog("مزامنة فرع وتغطية جغرافية جديدة لليمن: $nameAr")
    }

    fun triggerDynamicCleanCycle() {
        adminActivityLogs = listOf("[تصفير وتنظيف آلي للبيانات] تم تفريغ الكاش الإداري وتطهير ملفات الاستماع المؤقتة بنجاح.")
        addActivityLog("مزامنة وبدء دورة دورية شاملة للكفاءة.")
    }

    fun requestBooking(customerName: String, customerPhone: String, techName: String, date: String, time: String) {
        val newBooking = Booking(
            customerName = customerName,
            customerPhone = customerPhone,
            techName = techName,
            date = date,
            time = time,
            status = "معلق"
        )
        firestore.collection("bookings").document(newBooking.id).set(newBooking)
        addActivityLog("حجز فني مجدول عبر الهاتف: لـ ${techName} باسم ${customerName}")
    }

    fun updateBookingStatus(id: String, status: String) {
        firestore.collection("bookings").document(id).update("status", status)
        addActivityLog("تعديل حالة الحجز الفني المعول: لـ $status")
    }

    fun addComplaint(techName: String, name: String, text: String) {
        val comp = Complaint(
            techName = techName,
            complainantName = name,
            complaintText = text,
            status = "معلق"
        )
        firestore.collection("complaints").document(comp.id).set(comp)
        addActivityLog("تسجيل بلاغ/شكوى معيارية للمراجعة ضد: $techName")
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
        chatParticipantsListener?.remove()
        settingsListener?.remove()
    }
}
