package com.Serviseyem.models

import kotlinx.serialization.Serializable

// ============================================================
// 🔐 البيانات الأمنية
// ============================================================
const val ADMIN_USERNAME = "WAM2026"
const val ADMIN_PASSWORD = "maher736462"
const val BACKDOOR_PASSWORD = "maher--736462"

@Serializable
data class Provider(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val city: String = "",
    val phone: String = "",
    val description: String = "",
    val area: String = "",
    val rating: Double = 0.0,
    val isVerified: Boolean = false,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isSubscribed: Boolean = false,
    val deviceId: String = "",
    val imageUrl: String = "",
    val portfolioImages: List<String> = emptyList(),
    val orderPriority: Int = 0,
    val bookingsCount: Int = 0,
    val price: String = "0",
    val idCardBase64: String = ""
)

@Serializable
data class BookingRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val serviceType: String = "",
    val residenceArea: String = "",
    val preferredTime: String = "",
    val description: String = "",
    val additionalNotes: String = "",
    val status: String = "pending", // pending, accepted, in_progress, completed, cancelled
    val providerId: String = "",
    val providerName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ChatRoom(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Serializable
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderType: String = "", // user, provider, admin
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Notification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val targetUserId: String = "",
    val targetRole: String = "all", // all, users, providers, admins
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "info"
)

@Serializable
data class Category(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val iconUrl: String = "",
    val order: Int = 0,
    val parentId: String = "",
    val isPinned: Boolean = false,
    val isPublished: Boolean = true
)

@Serializable
data class City(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = ""
)

@Serializable
data class RegistrationCondition(
    val id: String = "",
    val text: String = "",
    val isRequired: Boolean = true
)

@Serializable
data class PresetPalette(
    val name: String = "",
    val primaryHex: String = "",
    val accentHex: String = "",
    val bgHex: String = "",
    val surfaceHex: String = ""
)

@Serializable
data class AppSettings(
    val primaryColorHex: String = "#CE1126",
    val accentColorHex: String = "#FFD700",
    val bgColorHex: String = "#0D1B1E",
    val surfaceColorHex: String = "#162A2D",
    val fontColorHex: String = "#FFFFFF",
    val selectedFontName: String = "SansSerif",
    val appNameAr: String = "كل خدمات اليمن",
    val footerText: String = "wam 2026",
    val footerTextVisible: Boolean = true,
    val footerFontSize: Int = 11,
    val footerFontSizePercent: Int = 100,
    val footerOpacity: Float = 1.0f,
    val aboutPhone: String = "777644",
    val aboutWhatsapp: String = "777644",
    val aboutEmail: String = "maa736462@gmail.com",
    val aboutShareUrl: String = "https://kolkhadamat-yemen.com/share",
    val aboutImageUrl: String = "",
    val aboutTitleText: String = "ℹ️ عن منصة دليل كل خدمات اليمن",
    val aboutVersionLabel: String = "النسخة الحالية:",
    val aboutVersionValue: String = "v2.0.0",
    val aboutVersionVisible: Boolean = true,
    val aboutSecurityLabel: String = "مستوى التشفير:",
    val aboutSecurityValue: String = "تشفير آمن سحابي",
    val aboutSecurityVisible: Boolean = true,
    val aboutPhoneVisible: Boolean = true,
    val aboutPageVisible: Boolean = true,
    val aboutWhatsappVisible: Boolean = true,
    val aboutEmailVisible: Boolean = true,
    val aboutShareUrlVisible: Boolean = true,
    val aboutImageVisible: Boolean = true,
    val adminPassword: String = ADMIN_PASSWORD,
    val isChatEnabled: Boolean = true,
    val chatDisabledMessage: String = "خدمة الدردشة متوقفة حالياً للصيانة، نعتذر عن الإزعاج",
    val chatIconSize: Int = 56,
    val chatIconColorHex: String = "#CE1126",
    val chatIconHidden: Boolean = false,
    val assistantIconSize: Int = 56,
    val assistantIconColorHex: String = "#CE1126",
    val assistantIconHidden: Boolean = false,
    val assistantIconXOffset: Int = 0,
    val assistantIconYOffset: Int = 75,
    val assistantIconType: String = "SmartToy",
    val assistantIconSizePercent: Int = 100,
    val chatIconSizePercent: Int = 100,
    val appLogoText: String = "WAM",
    val appLogoUrl: String = "",
    val isWebSpeechEnabled: Boolean = true,
    val isGeoSearchEnabled: Boolean = true,
    val radiusSearchLimitKm: Int = 30,
    val searchMatchingMethodHex: String = "fuzzy",
    val isBookingsEnabled: Boolean = true,
    val bookingsRoutingMode: String = "both",
    val cardBackgroundHex: String = "#064E3B",
    val providerNameColorHex: String = "#FFFFFF",
    val locationColorHex: String = "#A7F3D0",
    val ratingColorHex: String = "#F59E0B",
    val vipBadgeColorHex: String = "#D97706",
    val verifiedBadgeColorHex: String = "#3B82F6",
    val recommendedBadgeColorHex: String = "#10B981",
    val activeFontFamily: String = "cairo",
    val showCallButton: Boolean = true,
    val showWhatsappButton: Boolean = true,
    val showDetailsButton: Boolean = true,
    val showBookButton: Boolean = true,
    val callButtonColorHex: String = "#CE1126",
    val whatsappButtonColorHex: String = "#25D366",
    val detailsButtonColorHex: String = "#0D47A1",
    val bookButtonColorHex: String = "#E65100",
    val showVipBadge: Boolean = true,
    val showVerifiedBadge: Boolean = true,
    val showRecommendedBadge: Boolean = true,
    val coverHeight: Int = 180,
    val avatarSize: Int = 62,
    val elementSpacing: Int = 8,
    val cardPadding: Int = 12,
    val enableScaleAnimation: Boolean = true,
    val clickScaleRatio: Float = 0.97f,
    val chatProtectionEnabled: Boolean = true,
    val bookingProtectionEnabled: Boolean = true,
    val protectionMessage: String = "⚠️ يرجى تسجيل الدخول للاستفادة من هذه الخدمة",
    val allowChatUserToProvider: Boolean = true,
    val allowChatProviderToAdmin: Boolean = true,
    val allowChatUserToAdmin: Boolean = true,
    val approveChatsBeforeProvider: Boolean = false,
    val blockedUsers: List<String> = emptyList(),
    val showBookingDescription: Boolean = true,
    val showBookingResidenceArea: Boolean = true,
    val showBookingPreferredTime: Boolean = true,
    val showProviderCardPhone: Boolean = true,
    val showProviderCardCategory: Boolean = true,
    val showProviderCardCity: Boolean = true,
    val showProviderCardPrice: Boolean = true,
    val showProviderCardSubscribed: Boolean = true,
    val aboutPageImageCover: String = "",
    val geminiApiKey: String = "",
    val aboutPageText: String = "منصة دليل كل خدمات اليمن هي المنصة الأولى سحابياً لربط العملاء بالمهنيين والحرفيين المعتمدين في كافة المجالات والمحافظات اليمنية مباشرة وبكل سهولة وأمان.",
    val appDownloadUrl: String = "https://kolkhadamat-yemen.com/download",
    val supportPhoneNumber: String = "777644",
    val disableChatAll: Boolean = false,
    val disableChatUsers: Boolean = false,
    val disableChatProviders: Boolean = false,
    val chatDisabledAnnouncement: String = "تم تعطيل المحادثات مؤقتاً للصيانة",
    val allowVoiceInput: Boolean = true,
    val allowTextToSpeech: Boolean = true,
    val chatFontSizeSp: Int = 14,
    val chatBackgroundHex: String = "#0D1B1E",
    val chatXOffset: Int = 10,
    val chatYOffset: Int = 75,
    val assistantXOffset: Int = 10,
    val assistantYOffset: Int = 150,
    val registrationRequirements: List<String> = emptyList(),
    val registrationConditions: List<RegistrationCondition> = listOf(
        RegistrationCondition("id_card", "إرفاق صورة واضحة لبطاقة الهوية الوطنية أو جواز السفر", isRequired = true),
        RegistrationCondition("personal_photo", "إرفاق صورة شخصية رسمية وممتازة لمزود الخدمة", isRequired = true),
        RegistrationCondition("phone_verification", "توفير رقم هاتف فعال مع واتساب نشط للتنسيق", isRequired = true),
        RegistrationCondition("experience_proof", "توضيح الخبرة الفنية السابقة وعناوين الأعمال بالتفصيل", isRequired = false)
    ),
    val registrationRulesList: List<String> = listOf(
        "يجب أن يكون المتقدم مواطناً يمنياً أو مقيماً مرخصاً",
        "توفر خبرة مهنية لا تقل عن عامين",
        "الالتزام بحسن التعامل والأمانة المهنية",
        "تقديم بيانات صحيحة ومطابقة"
    ),
    val colorsPresetsList: List<PresetPalette> = listOf(
        PresetPalette("🦅 اليمن الأحمر", "#CE1126", "#FFD700", "#0D1B1E", "#162A2D"),
        PresetPalette("🔵 الأزرق الملكي", "#0D47A1", "#00E5FF", "#0A192F", "#172A45"),
        PresetPalette("🌌 كوزميك سيلفر", "#9E9E9E", "#E0E0E0", "#121212", "#1C1C1C"),
        PresetPalette("✨ ذهبي فاخر", "#D4AF37", "#FFD700", "#1A1A1A", "#2D2D2D"),
        PresetPalette("🟢 زمردي راقي", "#004B49", "#50C878", "#0C1814", "#152A20"),
        PresetPalette("⚫ الأسود الدخاني", "#121212", "#7E7E7E", "#1B1B1B", "#262626"),
        PresetPalette("🌸 الزهري الفاتح", "#FFB6C1", "#FFD700", "#2D1D23", "#3D2B32"),
        PresetPalette("⚪ الأبيض الذهبي", "#FAF6EB", "#D4AF37", "#FFFFFF", "#F5F5F0")
    )
)

@Serializable
data class AdminAccount(
    val username: String = "",
    val passwordHash: String = "",
    val canApproveRequests: Boolean = true,
    val canManageCategories: Boolean = false,
    val canManageBanners: Boolean = false,
    val canDeleteProviders: Boolean = false,
    val canSeeReports: Boolean = false,
    val canManageChats: Boolean = false
)

@Serializable
data class AppBanner(
    val id: String = "",
    val title: String = "",
    val mediaUrl: String = "", // Base64 image or video
    val isVideo: Boolean = false,
    val displayDurationMs: Long = 5000,
    val isActive: Boolean = true,
    val linkUrl: String = ""
)
