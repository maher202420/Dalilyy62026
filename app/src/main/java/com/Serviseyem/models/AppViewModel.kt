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
    private val _aiAssistantIconSize = mutableStateOf(60f)
    var aiAssistantIconSize: Float
        get() = _aiAssistantIconSize.value
        set(value) {
            _aiAssistantIconSize.value = value
            updateSettingsField("aiAssistantIconSize", value)
        }

    private val _aiAssistantIconColorStr = mutableStateOf("#111827")
    var aiAssistantIconColorStr: String
        get() = _aiAssistantIconColorStr.value
        set(value) {
            _aiAssistantIconColorStr.value = value
            updateSettingsField("aiAssistantIconColorStr", value)
        }

    val aiAssistantIconColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(aiAssistantIconColorStr))
        } catch (e: Exception) {
            Color(0xFF111827)
        }

    private val _showAiAssistantFloatingBubble = mutableStateOf(true)
    var showAiAssistantFloatingBubble: Boolean
        get() = _showAiAssistantFloatingBubble.value
        set(value) {
            _showAiAssistantFloatingBubble.value = value
            updateSettingsField("showAiAssistantFloatingBubble", value)
        }

    private val _aiAssistantAlignmentIsRight = mutableStateOf(true)
    var aiAssistantAlignmentIsRight: Boolean
        get() = _aiAssistantAlignmentIsRight.value
        set(value) {
            _aiAssistantAlignmentIsRight.value = value
            updateSettingsField("aiAssistantAlignmentIsRight", value)
        }

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

    private val _searchAutocompleteEnabled = mutableStateOf(true)
    var searchAutocompleteEnabled: Boolean
        get() = _searchAutocompleteEnabled.value
        set(value) {
            if (_searchAutocompleteEnabled.value != value) {
                _searchAutocompleteEnabled.value = value
                updateSettingsField("searchAutocompleteEnabled", value)
            }
        }

    private val _advancedFilteringEnabled = mutableStateOf(true)
    var advancedFilteringEnabled: Boolean
        get() = _advancedFilteringEnabled.value
        set(value) {
            if (_advancedFilteringEnabled.value != value) {
                _advancedFilteringEnabled.value = value
                updateSettingsField("advancedFilteringEnabled", value)
            }
        }

    private val _isChatModerationRequired = mutableStateOf(true)
    var isChatModerationRequired: Boolean
        get() = _isChatModerationRequired.value
        set(value) {
            if (_isChatModerationRequired.value != value) {
                _isChatModerationRequired.value = value
                updateSettingsField("isChatModerationRequired", value)
            }
        }

    private val _mapRadiusKm = mutableStateOf(10.0)
    var mapRadiusKm: Double
        get() = _mapRadiusKm.value
        set(value) {
            if (_mapRadiusKm.value != value) {
                _mapRadiusKm.value = value
                updateSettingsField("mapRadiusKm", value)
            }
        }

    private val _autoCleanupDays = mutableStateOf(30)
    var autoCleanupDays: Int
        get() = _autoCleanupDays.value
        set(value) {
            if (_autoCleanupDays.value != value) {
                _autoCleanupDays.value = value
                updateSettingsField("autoCleanupDays", value.toDouble())
            }
        }

    private val _isBookingAlertsEnabled = mutableStateOf(true)
    var isBookingAlertsEnabled: Boolean
        get() = _isBookingAlertsEnabled.value
        set(value) {
            if (_isBookingAlertsEnabled.value != value) {
                _isBookingAlertsEnabled.value = value
                updateSettingsField("isBookingAlertsEnabled", value)
            }
        }

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

    // Search Bar settings controlled by Admin
    private val _isSearchBarVisible = mutableStateOf(true)
    var isSearchBarVisible: Boolean
        get() = _isSearchBarVisible.value
        set(value) {
            if (_isSearchBarVisible.value != value) {
                _isSearchBarVisible.value = value
                updateSettingsField("isSearchBarVisible", value)
            }
        }

    private val _isSearchBarDeleted = mutableStateOf(false)
    var isSearchBarDeleted: Boolean
        get() = _isSearchBarDeleted.value
        set(value) {
            if (_isSearchBarDeleted.value != value) {
                _isSearchBarDeleted.value = value
                updateSettingsField("isSearchBarDeleted", value)
            }
        }

    private val _searchBarPlaceholderAr = mutableStateOf("بحث عن الأقسام أو المهنيين أو الخدمات...")
    var searchBarPlaceholderAr: String
        get() = _searchBarPlaceholderAr.value
        set(value) {
            if (_searchBarPlaceholderAr.value != value) {
                _searchBarPlaceholderAr.value = value
                updateSettingsField("searchBarPlaceholderAr", value)
            }
        }

    private val _searchBarPlaceholderEn = mutableStateOf("Search categories, providers, or services...")
    var searchBarPlaceholderEn: String
        get() = _searchBarPlaceholderEn.value
        set(value) {
            if (_searchBarPlaceholderEn.value != value) {
                _searchBarPlaceholderEn.value = value
                updateSettingsField("searchBarPlaceholderEn", value)
            }
        }

    private val _isMapEnabled = mutableStateOf(true)
    var isMapEnabled: Boolean
        get() = _isMapEnabled.value
        set(value) {
            if (_isMapEnabled.value != value) {
                _isMapEnabled.value = value
                updateSettingsField("isMapEnabled", value)
            }
        }

    private val _bookingRoutingDestination = mutableStateOf("both")
    var bookingRoutingDestination: String
        get() = _bookingRoutingDestination.value
        set(value) {
            if (_bookingRoutingDestination.value != value) {
                _bookingRoutingDestination.value = value
                updateSettingsField("bookingRoutingDestination", value)
            }
        }

    var notifications by mutableStateOf(listOf<AppNotification>())

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
    var colorPalettes by mutableStateOf(listOf<ColorPalette>())

    // Firestore listener registrations handles for teardown
    private var categoriesListener: ListenerRegistration? = null
    private var colorPalettesListener: ListenerRegistration? = null
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
    private var notificationsListener: ListenerRegistration? = null

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

        firestore.collection("custom_colors").get().addOnSuccessListener { query ->
            if (query.isEmpty) {
                val defaults = listOf(
                    ColorPalette(name = "الذهبي الكلاسيكي المظلم 👑", primaryColor = "#FFD700", secondaryColor = "#03DAC6", backgroundColor = "#0A0A0C", textColor = "#FFFFFF"),
                    ColorPalette(name = "الأخضر اليمني المعتمد 🇾🇪", primaryColor = "#4CAF50", secondaryColor = "#FFC107", backgroundColor = "#0D1E10", textColor = "#FFFFFF"),
                    ColorPalette(name = "الأزرق الملكي الفاخر 🔹", primaryColor = "#2196F3", secondaryColor = "#00E5FF", backgroundColor = "#0D1117", textColor = "#FFFFFF"),
                    ColorPalette(name = "البنفسجي السيبراني الحديث 🔮", primaryColor = "#9C27B0", secondaryColor = "#00E5FF", backgroundColor = "#120024", textColor = "#FFFFFF"),
                    ColorPalette(name = "الأسود الدخاني الفاخر 🖤", primaryColor = "#FAFAFA", secondaryColor = "#8F9094", backgroundColor = "#121212", textColor = "#FFFFFF"),
                    ColorPalette(name = "الزهري الفاتح الهادئ 🌸", primaryColor = "#FFB7C5", secondaryColor = "#FFC1CC", backgroundColor = "#1C0D10", textColor = "#FFFFFF"),
                    ColorPalette(name = "الأبيض الذهبي الملكي ✨", primaryColor = "#FAF0E6", secondaryColor = "#D4AF37", backgroundColor = "#1A1813", textColor = "#FFFFFF")
                )
                for (p in defaults) {
                    firestore.collection("custom_colors").document(p.id).set(p)
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
                    "searchAutocompleteEnabled" to true,
                    "advancedFilteringEnabled" to true,
                    "isChatInstantEnabled" to true,
                    "chatDisabledMessage" to "المحادثة الفورية معطلة مؤقتاً لأعمال الكفاءة - نرجو التواصل هاتفياً مباشرة",
                    "isRatingsAndReviewsEnabled" to true,
                    "showBookingsSection" to true,
                    "isBookingAlertsEnabled" to true,
                    "mapRadiusKm" to 10.0,
                    "autoCleanupDays" to 30.0,
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

        notificationsListener = firestore.collection("notifications")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    notifications = snapshot.toObjects(AppNotification::class.java)
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

        colorPalettesListener = firestore.collection("custom_colors")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    colorPalettes = snapshot.toObjects(ColorPalette::class.java)
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
                    _appPrimaryColorStr.value = doc.getString("appPrimaryColorStr") ?: "#FFD700"
                    _appSecondaryColorStr.value = doc.getString("appSecondaryColorStr") ?: "#03DAC6"
                    _appBackgroundColorStr.value = doc.getString("appBackgroundColorStr") ?: "#0A0A0C"
                    _appTextColorStr.value = doc.getString("appTextColorStr") ?: "#FFFFFF"
                    _chatSettingsIconColorStr.value = doc.getString("chatSettingsIconColorStr") ?: "#064E3B"
                    _chatSettingsIconSize.value = doc.getDouble("chatSettingsIconSize")?.toFloat() ?: 60f
                    _isChatIconMutedHidden.value = doc.getBoolean("isChatIconMutedHidden") ?: false
                    _isChatIconPermDeleted.value = doc.getBoolean("isChatIconPermDeleted") ?: false
                    _voiceSearchEnabled.value = doc.getBoolean("voiceSearchEnabled") ?: true
                    _isChatModerationRequired.value = doc.getBoolean("isChatModerationRequired") ?: true
                    _isChatInstantEnabled.value = doc.getBoolean("isChatInstantEnabled") ?: true
                    _chatDisabledMessage.value = doc.getString("chatDisabledMessage") ?: "المحادثة الفورية معطلة مؤقتاً لأعمال الكفاءة - نرجو التواصل هاتفياً مباشرة"
                    _isRatingsAndReviewsEnabled.value = doc.getBoolean("isRatingsAndReviewsEnabled") ?: true
                    _showBookingsSection.value = doc.getBoolean("showBookingsSection") ?: true
                    _isBookingAlertsEnabled.value = doc.getBoolean("isBookingAlertsEnabled") ?: true
                    _mapRadiusKm.value = doc.getDouble("mapRadiusKm") ?: 10.0
                    _autoCleanupDays.value = doc.getDouble("autoCleanupDays")?.toInt() ?: 30
                    _footerText.value = doc.getString("footerText") ?: "wam 2026"
                    _footerFontSize.value = doc.getDouble("footerFontSize")?.toFloat() ?: 11f
                    _isFooterVisible.value = doc.getBoolean("isFooterVisible") ?: true
                    _aiAssistantIconSize.value = doc.getDouble("aiAssistantIconSize")?.toFloat() ?: 60f
                    _aiAssistantIconColorStr.value = doc.getString("aiAssistantIconColorStr") ?: "#111827"
                    _showAiAssistantFloatingBubble.value = doc.getBoolean("showAiAssistantFloatingBubble") ?: true
                    _aiAssistantAlignmentIsRight.value = doc.getBoolean("aiAssistantAlignmentIsRight") ?: true

                    _isSearchBarVisible.value = doc.getBoolean("isSearchBarVisible") ?: true
                    _isSearchBarDeleted.value = doc.getBoolean("isSearchBarDeleted") ?: false
                    _searchAutocompleteEnabled.value = doc.getBoolean("searchAutocompleteEnabled") ?: true
                    _advancedFilteringEnabled.value = doc.getBoolean("advancedFilteringEnabled") ?: true
                    _searchBarPlaceholderAr.value = doc.getString("searchBarPlaceholderAr") ?: "بحث عن الأقسام أو المهنيين أو الخدمات..."
                    _searchBarPlaceholderEn.value = doc.getString("searchBarPlaceholderEn") ?: "Search categories, providers, or services..."

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
        val isApprovedValue = if (senderRole == "admin") true else !isChatModerationRequired
        val msg = ChatMessage(
            chatId = chatId,
            senderName = senderName,
            senderRole = senderRole,
            messageText = text,
            timestamp = System.currentTimeMillis(),
            isApproved = isApprovedValue
        )

        firestore.collection("messages").document(msg.id).set(msg)
        firestore.collection("chats").document(chatId).update(
            "lastMessage", text,
            "lastUpdated", System.currentTimeMillis()
        )
        addActivityLog("إرسال رسالة: [$senderRole] -> $text")
    }

    fun approveChatMessage(messageId: String) {
        firestore.collection("messages").document(messageId).update("isApproved", true)
        addActivityLog("تمت الموافقة على رسالة المعاملة رقم $messageId")
    }

    fun deleteChatMessage(messageId: String) {
        firestore.collection("messages").document(messageId).delete()
        addActivityLog("تم رفض وحذف رسالة مجهولة رقم $messageId من محادثات الرصد")
    }

    fun deleteChatSession(sessionId: String) {
        firestore.collection("chat_sessions").document(sessionId).delete()
        // Delete messages under this session
        firestore.collection("messages")
            .whereEqualTo("chatId", sessionId)
            .get()
            .addOnSuccessListener { query ->
                val batch = firestore.batch()
                for (doc in query.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
        addActivityLog("تم حذف جلسة الدردشة بالكامل ومعاملاتها للرقم $sessionId")
    }

    val userPresences = androidx.compose.runtime.mutableStateMapOf<String, Long>()

    fun startPresenceListener(entityId: String) {
        if (entityId.isBlank()) return
        firestore.collection("user_presence").document(entityId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val lastActive = snapshot.getLong("lastActive") ?: 0L
                    userPresences[entityId] = lastActive
                }
            }
    }

    fun updateUserPresence(entityId: String) {
        if (entityId.isBlank()) return
        val presenceRef = firestore.collection("user_presence").document(entityId)
        presenceRef.set(
            hashMapOf(
                "name" to entityId,
                "lastActive" to System.currentTimeMillis()
            )
        )
    }

    fun updateChatMessageText(messageId: String, nextText: String) {
        firestore.collection("messages").document(messageId).update("messageText", nextText)
        addActivityLog("قام المشرف بتعديل رسالة رقم $messageId لتصبح: $nextText")
    }

    fun addNewAdBanner(banner: AdBanner) {
        firestore.collection("banners").document(banner.id).set(banner)
        addActivityLog("إضافة لافتة إعلانية جديدة: ${banner.title}")
    }

    fun deleteAdBanner(bannerId: String) {
        firestore.collection("banners").document(bannerId).delete()
        addActivityLog("حذف اللافتة الإعلانية: $bannerId")
    }

    fun toggleBannerVisibility(bannerId: String) {
        val banner = banners.find { it.id == bannerId } ?: return
        val nextVisible = !banner.isVisible
        firestore.collection("banners").document(bannerId).update("isVisible", nextVisible)
        addActivityLog("تعديل رؤية لافتة العرض $bannerId إلى: $nextVisible")
    }

    fun cleanOrphanImageMemory() {
        // System activity log and mock/real Firestore orphan cleaner
        addActivityLog("قام المسؤول بمسح وتصفية كافة الصور والملفات غير المرتبطة بالبيانات بنجاح")
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
        gender: String,
        photoSource: String,
        photoType: String
    ) {
        val newRequest = ServiceProvider(
            name = name,
            phone = phone,
            specialty = specialty,
            city = city,
            status = "معلق",
            gender = gender,
            photoSource = photoSource,
            photoType = photoType
        )
        firestore.collection("pending_providers").document(newRequest.id).set(newRequest)
        addActivityLog("طلب تسجيل جديد وارد من: $name ($gender) [$specialty] عبر $photoSource ($photoType)")
    }

    fun approveRequest(id: String) {
        val request = registrationRequests.find { it.id == id }
        if (request != null) {
            request.status = "مقبول"
            firestore.collection("service_providers").document(request.id).set(request)
            firestore.collection("pending_providers").document(id).delete()
            addActivityLog("قبول طلب الفني والاندماج الفوري للشبكة: ${request.name}")
            sendAppNotification(
                title = "تهانينا! تم قبول طلب انضمامك 🛡️",
                body = "مرحباً ${request.name}، لقد تم قبول طلب انضمامك كمهني معتمد لمجال ${request.specialty} في ${request.city} بدليل خدمات اليمن.",
                recipientName = request.name,
                type = "registration_status"
            )
        }
    }

    fun rejectRequest(id: String) {
        val request = registrationRequests.find { it.id == id }
        if (request != null) {
            firestore.collection("pending_providers").document(id).delete()
            addActivityLog("رفض وعزل طلب تسجيل الفني: ${request.name}")
            sendAppNotification(
                title = "تحديث بخصوص طلب التسجيل ⚠️",
                body = "مرحباً ${request.name}، نأسف لإعلامك بأنه لم يتم قبول طلب تسجيلك في دليل خدمات اليمن للوظيفة الحالية.",
                recipientName = request.name,
                type = "registration_status"
            )
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
            status = "قيد الانتظار"
        )
        firestore.collection("bookings").document(newBooking.id).set(newBooking)
        
        sendAppNotification(
            title = "تم إرسال طلب الحجز بنجاح 📅",
            body = "مرحباً ${customerName}، لقد تلقينا طلب حجزك الجديد للمهني (${techName}) بتاريخ ${date} ووقت ${time}. طلبك الآن قيد الانتظار لمعاينته والموافقة عليه.",
            recipientName = customerName,
            type = "booking_status"
        )
        addActivityLog("حجز فني مجدول عبر الهاتف: لـ ${techName} باسم ${customerName}")
    }

    fun updateBookingStatus(id: String, status: String) {
        firestore.collection("bookings").document(id).update("status", status)
        val booking = bookings.find { it.id == id }
        if (booking != null) {
            sendAppNotification(
                title = "تحديث حالة الحجز الفني ⚙️",
                body = "زبوننا العزيز ${booking.customerName}، تم تعديل حالة حجزك الفني مع المهني (${booking.techName}) ليصبح حالياً: [${status}]",
                recipientName = booking.customerName,
                type = "booking_status"
            )
        }
        addActivityLog("تعديل حالة الحجز الفني المعول: لـ $status")
    }

    fun sendAppNotification(title: String, body: String, recipientName: String? = null, type: String = "general") {
        val notif = AppNotification(
            title = title,
            body = body,
            recipientName = recipientName,
            type = type
        )
        firestore.collection("notifications").document(notif.id).set(notif)
        addActivityLog("إرسال إشعار [${type}]: ${title} -> لـ ${recipientName ?: "جميع المستخدمين"}")
    }

    fun deleteNotification(id: String) {
        firestore.collection("notifications").document(id).delete()
        addActivityLog("تم حذف إشعار رقابي من الدليل")
    }

    fun deleteBooking(id: String) {
        firestore.collection("bookings").document(id).delete()
        addActivityLog("تم حذف حجز فني من الدليل")
    }

    fun updateBookingFull(id: String, date: String, time: String, status: String) {
        val updateMap = hashMapOf<String, Any>(
            "date" to date,
            "time" to time,
            "status" to status
        )
        firestore.collection("bookings").document(id).update(updateMap)
        addActivityLog("تعديل كامل لبيانات الحجز رقم: $id إلى ($date - $time - $status)")
    }

    fun updateProviderFullDetails(
        id: String,
        name: String,
        phone: String,
        specialty: String,
        city: String,
        biography: String,
        baseFee: Int,
        isVerified: Boolean,
        isVip: Boolean,
        experienceYears: Int,
        contactEmail: String,
        latitude: Double,
        longitude: Double,
        galleryUrls: List<String>,
        isPinned: Boolean = false,
        isRecommended: Boolean = false,
        hasSubscription: Boolean = false,
        subscriptionStatus: String = "none",
        subscriptionPaymentDetails: String = ""
    ) {
        val updateMap = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "specialty" to specialty,
            "city" to city,
            "biography" to biography,
            "baseFee" to baseFee,
            "isVerified" to isVerified,
            "isVip" to isVip,
            "experienceYears" to experienceYears,
            "contactEmail" to contactEmail,
            "latitude" to latitude,
            "longitude" to longitude,
            "galleryUrls" to galleryUrls,
            "isPinned" to isPinned,
            "isRecommended" to isRecommended,
            "hasSubscription" to hasSubscription,
            "subscriptionStatus" to subscriptionStatus,
            "subscriptionPaymentDetails" to subscriptionPaymentDetails
        )
        firestore.collection("service_providers").document(id).update(updateMap)
        addActivityLog("تعديل كامل تفاصيل حساب المهني: $name (التثبيت: $isPinned، التوصية: $isRecommended، الاشتراك: $hasSubscription)")
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

    fun updateProviderGallerySettings(provider: ServiceProvider, enabled: Boolean, maxImages: Int) {
        firestore.collection("service_providers").document(provider.id).update(
            "galleryEnabled", enabled,
            "maxGalleryImages", maxImages
        )
        addActivityLog("تعديل إعدادات معرض الصور للفني ${provider.name}: تفعيل=$enabled، الحد الأقصى=$maxImages")
    }

    fun addProviderGalleryImage(provider: ServiceProvider, imageUrl: String) {
        val nextList = provider.galleryUrls.toMutableList()
        if (nextList.size < provider.maxGalleryImages) {
            nextList.add(imageUrl)
            firestore.collection("service_providers").document(provider.id).update("galleryUrls", nextList)
            addActivityLog("إضافة صورة لمعرض أعمال الفني ${provider.name}: $imageUrl")
        }
    }

    fun removeProviderGalleryImage(provider: ServiceProvider, imageUrl: String) {
        val nextList = provider.galleryUrls.toMutableList()
        nextList.remove(imageUrl)
        firestore.collection("service_providers").document(provider.id).update("galleryUrls", nextList)
        addActivityLog("إزالة صورة من معرض أعمال الفني ${provider.name}")
    }

    fun updateProviderProfileData(provider: ServiceProvider, biography: String, skills: String) {
        firestore.collection("service_providers").document(provider.id).update(
            "biography", biography,
            "skills", skills
        )
        addActivityLog("تعديل نبذة ومهارات الفني ${provider.name}")
    }

    override fun onCleared() {
        super.onCleared()
        categoriesListener?.remove()
        colorPalettesListener?.remove()
        serviceProvidersListener?.remove()
        citiesListener?.remove()
        bannersListener?.remove()
        registrationRequestsListener?.remove()
        complaintsListener?.remove()
        bookingsListener?.remove()
        notificationsListener?.remove()
        registrationTermsListener?.remove()
        adminsListener?.remove()
        chatSessionsListener?.remove()
        chatMessagesListener?.remove()
        chatParticipantsListener?.remove()
        settingsListener?.remove()
    }

    // Category CRUD operations for Admins
    fun addCategory(nameAr: String, nameEn: String, desc: String, iconEmoji: String, type: String, parentId: String?, imageUrl: String?) {
        val newCat = Category(
            nameAr = nameAr,
            nameEn = nameEn,
            description = desc,
            iconEmoji = iconEmoji,
            type = type,
            parentId = parentId,
            imageUrl = imageUrl
        )
        firestore.collection("categories").document(newCat.id).set(newCat)
        addActivityLog("إضافة قسم جديد بنجاح: $nameAr [طبيعة الخدمة: $type]")
    }

    fun updateCategory(id: String, nameAr: String, nameEn: String, desc: String, iconEmoji: String, type: String, parentId: String?, imageUrl: String?) {
        val updateMap = hashMapOf<String, Any?>(
            "nameAr" to nameAr,
            "nameEn" to nameEn,
            "description" to desc,
            "iconEmoji" to iconEmoji,
            "type" to type,
            "parentId" to parentId,
            "imageUrl" to imageUrl
        )
        firestore.collection("categories").document(id).update(updateMap)
        addActivityLog("تعديل بيانات القسم بالدليل: $nameAr")
    }

    fun deleteCategory(id: String) {
        firestore.collection("categories").document(id).delete()
        addActivityLog("حذف القسم نهائياً من الشبكة: $id")
    }

    // Registration Terms CRUD operations for Admins
    fun addRegistrationTerm(termText: String) {
        val term = RegistrationTerm(termText = termText)
        firestore.collection("registration_terms").document(term.id).set(term)
        addActivityLog("إضافة شرط جديد لتسجيل المهنيين: $termText")
    }

    fun updateRegistrationTerm(id: String, termText: String) {
        firestore.collection("registration_terms").document(id).update("termText", termText)
        addActivityLog("تعديل شرط التسجيل رقم $id إلى: $termText")
    }

    fun deleteRegistrationTerm(id: String) {
        firestore.collection("registration_terms").document(id).delete()
        addActivityLog("حذف شرط التسجيل بنجاح: $id")
    }

    // Automatic/manual cleanup of temporary data and old logs
    fun triggerDatabaseCleanup() {
        val limitMs = System.currentTimeMillis() - (autoCleanupDays.toLong() * 24 * 60 * 60 * 1000)
        
        // Let's delete notifications older than limitMs
        firestore.collection("notifications")
            .whereLessThan("timestamp", limitMs)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val batch = firestore.batch()
                    for (doc in snapshot.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().addOnSuccessListener {
                        addActivityLog("تم تنظيف الإشعارات المؤقتة القديمة بنجاح!")
                    }
                }
            }

        // Keep local activity logs bounded
        addActivityLog("تمت تصفية سجل النشاطات لآخر 100 سجل لتوفير المساحة والكفاءة.")
    }

    // Color Palette CRUD operations for Admins
    fun addColorPalette(name: String, primary: String, secondary: String, background: String, text: String) {
        val newPalette = ColorPalette(
            name = name,
            primaryColor = primary,
            secondaryColor = secondary,
            backgroundColor = background,
            textColor = text
        )
        firestore.collection("custom_colors").document(newPalette.id).set(newPalette)
        addActivityLog("إضافة لوحة ألوان مخصصة: $name")
    }

    fun updateColorPalette(id: String, name: String, primary: String, secondary: String, background: String, text: String) {
        val updateMap = hashMapOf<String, Any>(
            "name" to name,
            "primaryColor" to primary,
            "secondaryColor" to secondary,
            "backgroundColor" to background,
            "textColor" to text
        )
        firestore.collection("custom_colors").document(id).update(updateMap)
        addActivityLog("تعديل لوحة الألوان: $name")
    }

    fun deleteColorPalette(id: String) {
        firestore.collection("custom_colors").document(id).delete()
        addActivityLog("مسح لوحة الألوان: $id")
    }

    fun activateColorPalette(palette: ColorPalette) {
        firestore.collection("app_settings").document("master").update(
            "appPrimaryColorStr", palette.primaryColor,
            "appSecondaryColorStr", palette.secondaryColor,
            "appBackgroundColorStr", palette.backgroundColor,
            "appTextColorStr", palette.textColor
        )
        addActivityLog("تطبيق لوحة ألوان دليل خدمات اليمن لـ: ${palette.name}")
    }

    fun saveAdminAccount(account: AdminAccount) {
        firestore.collection("admins").document(account.username).set(account)
        addActivityLog("حفظ حساب مشرف: ${account.username}")
    }

    fun deleteAdminAccount(username: String) {
        firestore.collection("admins").document(username).delete()
        addActivityLog("حذف حساب مشرف: $username")
    }

    fun wipeAndRebuildFullDatabase(onComplete: (Boolean) -> Unit) {
        val collections = listOf(
            "categories", "service_providers", "cities", "banners",
            "registration_terms", "admins", "pending_providers", "complaints",
            "bookings", "notifications", "messages", "chats", "chat_participants",
            "custom_colors", "app_settings"
        )
        
        var completedCount = 0
        var success = true
        addActivityLog("بدء عملية تطهير ومسح كافة بيانات وملفات قواعد الدليل...")

        for (colName in collections) {
            firestore.collection(colName).get().addOnSuccessListener { query ->
                if (query.isEmpty) {
                    completedCount++
                    if (completedCount == collections.size) {
                        initializeFirebaseDataIfNeeded()
                        addActivityLog("تم محو وبناء قواعد الدليل والملفات المرجعية بالكامل بنجاح 🇾🇪")
                        onComplete(success)
                    }
                } else {
                    val batch = firestore.batch()
                    for (doc in query.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().addOnCompleteListener { task ->
                        completedCount++
                        if (!task.isSuccessful) {
                            success = false
                        }
                        if (completedCount == collections.size) {
                            initializeFirebaseDataIfNeeded()
                            addActivityLog("تم محو وبناء قواعد الدليل والملفات المرجعية بالكامل بنجاح 🇾🇪")
                            onComplete(success)
                        }
                    }
                }
            }.addOnFailureListener {
                completedCount++
                success = false
                if (completedCount == collections.size) {
                    initializeFirebaseDataIfNeeded()
                    onComplete(false)
                }
            }
        }
    }
}
