package com.Serviseyem.models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {

    // Localization
    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    // Search & Filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _selectedCity = MutableStateFlow<String?>(null)
    val selectedCity: StateFlow<String?> = _selectedCity.asStateFlow()

    // Favorite List
    private val _favoriteProviderIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteProviderIds: StateFlow<Set<String>> = _favoriteProviderIds.asStateFlow()

    // Active instant chat state
    private val _activeChatProvider = MutableStateFlow<ServiceProvider?>(null)
    val activeChatProvider: StateFlow<ServiceProvider?> = _activeChatProvider.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // App Visual Settings
    val appPrimaryColor = androidx.compose.ui.graphics.Color(0xFFFFD700) // Yemen Gold
    val chatSettingsIconColor = androidx.compose.ui.graphics.Color(0xFF38BDF8)
    val isFooterVisible = true
    val footerText = "دليل خدمات اليمن الذكي © 2026"
    val footerFontSize = 11
    val isChatInstantEnabled = true
    val chatDisabledMessage = "خدمة المحادثة غير مفعلة مؤقتاً"
    val showBookingsSection = true

    // App message localizations
    val noResultsMessageAr = "لا توجد نتائج مطابقة لبحثك الفني حالياً"
    val noResultsMessageEn = "No matching service providers found in this region."

    // static Categories
    val categories = listOf(
        Category("plumbing", "سباكة", "Plumbing", "🔧"),
        Category("electrical", "كهرباء", "Electrical", "⚡"),
        Category("cleaning", "تنظيف", "Cleaning", "🧹"),
        Category("solar", "طاقة شمسية", "Solar Energy", "☀️"),
        Category("cooling", "تبريد وتكييف", "AC & Cooling", "❄️"),
        Category("tutoring", "تعليم وتدريس", "Tutoring", "📚"),
        Category("electronics", "إلكترونيات", "Electronics", "📱")
    )

    // static Providers
    val providers = listOf(
        ServiceProvider(
            id = "1",
            name = "المهندس عادل الحميري",
            categoryId = "solar",
            specialty = "صيانة وتركيب شبكات الطاقة الشمسية والهجين",
            city = "صنعاء",
            rating = 4.9,
            ratingsCount = 124,
            phone = "777123456",
            latitude = 15.3500,
            longitude = 44.2000,
            biography = "أكثر من 8 سنوات من الخبرة المعتمدة في تصميم وتركيب ألواح ومنظومات الطاقة الشمسية والبطاريات الليثيوم للبيوت والمنشآت التجارية بصنعاء.",
            baseFee = 5000,
            isVip = true,
            isVerified = true,
            isPinned = true,
            hasSubscription = true
        ),
        ServiceProvider(
            id = "2",
            name = "الأستاذ خالد باوزير",
            categoryId = "tutoring",
            specialty = "معلم رياضيات وفيزياء للمرحلة الثانوية والجامعية",
            city = "عدن",
            rating = 4.8,
            ratingsCount = 89,
            phone = "733456789",
            latitude = 12.8000,
            longitude = 45.0333,
            biography = "خبرة وتأسيس ممتاز لطلاب الثانوية العامة والجامعات مع استراتيجيات مبسطة وحل النماذج الوزارية السابقة بمدينة عدن وعبر الإنترنت.",
            baseFee = 3500,
            isVip = false,
            isVerified = true,
            isPinned = false,
            hasSubscription = true
        ),
        ServiceProvider(
            id = "3",
            name = "الفني محمد الحاشدي",
            categoryId = "electrical",
            specialty = "تمديدات وصيانة كهربائية متكاملة للأبنية والفلل",
            city = "صنعاء",
            rating = 4.7,
            ratingsCount = 62,
            phone = "775987654",
            latitude = 15.3620,
            longitude = 44.1870,
            biography = "متخصص في كشف التماس الكهرباء، تركيب المنظومات المنزلية وتصميم لوحات التشغيل والتحكم الإلكتروني لشبكات التغذية.",
            baseFee = 4000,
            isVip = false,
            isVerified = true,
            isPinned = false,
            hasSubscription = false
        ),
        ServiceProvider(
            id = "4",
            name = "المهندس وضاح الكثيري",
            categoryId = "cooling",
            specialty = "صيانة وتنظيف مكيفات اسبليت والمركزية",
            city = "المكلا",
            rating = 4.9,
            ratingsCount = 95,
            phone = "711223344",
            latitude = 14.5422,
            longitude = 49.1242,
            biography = "صيانة وتعبئة غاز فريون أصلي لجميع أنواع المكيفات وغرف التبريد بأسعار مناسبة مع ضمان الأداء والحرص العالي على جودة ونظافة العمل.",
            baseFee = 6000,
            isVip = true,
            isVerified = true,
            isPinned = true,
            hasSubscription = true
        ),
        ServiceProvider(
            id = "5",
            name = "المعلم صالح السباك",
            categoryId = "plumbing",
            specialty = "أعمال سباكة وشبكات مياه وصرف صحي وتصريف أمطار",
            city = "تعز",
            rating = 4.6,
            ratingsCount = 47,
            phone = "771002233",
            latitude = 13.5790,
            longitude = 44.0200,
            biography = "تأسيس وتشطيب سباكة فلل وعمارات بالكامل مع كشف وتصليح تسربات مياه الجدران والأسطح وتركيب المضخات الذكية بكفاءة.",
            baseFee = 3000,
            isVip = false,
            isVerified = false,
            isPinned = false,
            hasSubscription = false
        ),
        ServiceProvider(
            id = "6",
            name = "مؤسسة اللمسة الفضية للمقاولات والخدمات العامة",
            categoryId = "cleaning",
            specialty = "خدمات تنظيف وتلميع منازل ومكاتب وتعقيم متكامل",
            city = "صنعاء",
            rating = 4.8,
            ratingsCount = 76,
            phone = "770112233",
            latitude = 15.3400,
            longitude = 44.2200,
            biography = "فريق فني مجهز بأحدث أدوات ومواد التلميع والتعقيم للواجهات الحجرية، الزجاجية، السجاد والكنبات بأعلى تكنولوجيا النظافة والتعقيم الشامل.",
            baseFee = 8000,
            isVip = false,
            isVerified = true,
            isPinned = false,
            hasSubscription = false
        ),
        ServiceProvider(
            id = "7",
            name = "المهندس مروان لإصلاح الموبايل والشاشات",
            categoryId = "electronics",
            specialty = "صيانة بوردات الآيفون والأندرويد وتغيير إلكتروني للشاشات",
            city = "عدن",
            rating = 4.9,
            ratingsCount = 110,
            phone = "735556677",
            latitude = 12.7833,
            longitude = 45.0167,
            biography = "خبير فني لجميع الهواتف الذكية وتصليح الكرتات الدقيقة واستبدال قطع الغيار المضمونة والقطع الاستهلاكية التالفة باحترافية وسرعة قياسية.",
            baseFee = 3000,
            isVip = true,
            isVerified = true,
            isPinned = false,
            hasSubscription = false
        )
    )

    fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun selectCity(city: String?) {
        _selectedCity.value = if (_selectedCity.value == city) null else city
    }

    fun toggleFavorite(providerId: String) {
        val current = _favoriteProviderIds.value
        _favoriteProviderIds.value = if (current.contains(providerId)) {
            current - providerId
        } else {
            current + providerId
        }
    }

    fun initiateInstantChatWithProvider(provider: ServiceProvider, userName: String) {
        _activeChatProvider.value = provider
        _chatMessages.value = listOf(
            ChatMessage(
                id = "welcome",
                senderName = provider.name,
                text = if (_isArabic.value) {
                    "مرحباً بك يا $userName! أنا المهندس ${provider.name}. كيف يمكنني مساعدتك في مجال وتطبيقات ${provider.specialty} اليوم؟"
                } else {
                    "Hello $userName! I am ${provider.name}. How can I assist you in ${provider.specialty} today?"
                },
                timestamp = System.currentTimeMillis(),
                isFromUser = false
            )
        )
    }

    fun sendUserMessage(text: String, userName: String) {
        if (text.isBlank()) return
        val provider = _activeChatProvider.value ?: return

        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            senderName = userName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromUser = true
        )
        val updated = _chatMessages.value + userMsg
        _chatMessages.value = updated

        // Simulate automatic smart response
        val botMsg = ChatMessage(
            id = (System.currentTimeMillis() + 1).toString(),
            senderName = provider.name,
            text = if (_isArabic.value) {
                "سعدتُ جداً برسالتك! لقد تلقيتُ طلبك وبصفتي متخصصاً في ${provider.specialty}، سأتصل بك مباشرة على رقمك أو يمكنك مكالمتي الآن عبر الهاتف: ${provider.phone} للتنسيق السريع والبدء بالعمل."
            } else {
                "Glad to receive your message! I've got your inquiry. As an expert in ${provider.specialty}, I will call you directly, or feel free to reach me at: ${provider.phone} to coordinate."
            },
            timestamp = System.currentTimeMillis() + 1000,
            isFromUser = false
        )
        // Insert with a small delay
        _chatMessages.value = updated + botMsg
    }

    fun closeChat() {
        _activeChatProvider.value = null
        _chatMessages.value = emptyList()
    }
}
