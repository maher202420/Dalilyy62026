package com.Serviseyem.models

import java.util.UUID

// Role Types
enum class UserRole {
    VISITOR,
    USER,
    REGISTERED,
    TECHNICIAN,
    SUPERVISOR,
    ADMIN,
    OWNER
}

// Technician Status
enum class TechnicianState {
    ACTIVE,
    PENDING,
    SUSPENDED
}

// Tech Registration Field configuration
data class CustomField(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val type: String, // "TEXT", "NUMBER", "DROPDOWN", "CHECKBOX", "DATE", "IMAGE"
    val options: List<String> = emptyList(), // for dropdown options
    val isMandatory: Boolean = false,
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0
)

// Technician / Professional Service Provider Model
data class Technician(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val region: String, // e.g., "صنعاء", "إب", "عدن", "تعز", "حضرموت"
    val addressDetail: String = "",
    val hasShop: Boolean = false,
    val shopAddress: String = "",
    val specialty: String, // category/profession
    val rating: Float = 5.0f,
    val ratingCount: Int = 0,
    val experienceYears: Int = 3,
    val bio: String = "",
    val completedServices: Int = 0,
    val isAvailable24_7: Boolean = true,
    val state: TechnicianState = TechnicianState.ACTIVE,
    val isVIP: Boolean = false,
    val isVerified: Boolean = false,
    val isRecommended: Boolean = false,
    val subscriptionPlan: String = "Basic", // "Basic", "Premium", "VIP"
    val profilePhotoUrl: String = "",
    val coverPhotoUrl: String = "",
    val customFieldsData: Map<String, String> = emptyMap() // fieldId -> value
)

// Client / Regular User Model
data class ClientUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String,
    val loyaltyPoints: Int = 0,
    val registrationDate: String = "",
    val isBlocked: Boolean = false,
    val registrationType: String = "Visitor" // "Visitor", "Optional", "Mandatory"
)

// Booking Record Model
data class Booking(
    val id: String = UUID.randomUUID().toString(),
    val clientName: String,
    val clientPhone: String,
    val requestedService: String, // e.g. "سباكة", "كهرباء", "صيانة"
    val region: String,
    val status: String = "قيد الانتظار", // "قيد الانتظار" (Pending), "تم القبول" (Accepted), "قيد التنفيذ" (In Progress), "مكتمل" (Completed), "ملغي" (Cancelled)
    val assignedTechId: String? = null,
    val dateCreated: String = "",
    val dateCompleted: String = "",
    val clientRating: Int = 0,
    val clientReview: String = ""
)

// Notification Model
data class Notice(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val text: String,
    val date: String,
    val isRead: Boolean = false,
    val targetId: String = "All", // "All", or specific userId/techId
    val category: String = "General" // For grouping
)

// Audit Log Model for security
data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val adminId: String,
    val action: String,
    val details: String,
    val timestamp: String,
    val ipAddress: String = "192.168.1.100"
)

// Instant Chat Channel / Message Model
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val senderType: String, // "User", "Tech", "Admin", "System"
    val senderName: String = ""
)

// Active Conversation / Chat info
data class ChatChannel(
    val userId: String,
    val userName: String,
    val userRole: String, // "User" or "Tech"
    val messages: List<ChatMessage> = emptyList(),
    val isBlocked: Boolean = false
)

// App Configuration & Customization Object
data class AppSetup(
    // Theme Colors (represented as hex strings)
    val primaryColorHex: String = "#3B82F6", // Modern Blue
    val secondaryColorHex: String = "#10B981", // Emerald Green
    val backgroundColorHex: String = "#0D0D11", // Sleek dark canvas
    val textColorHex: String = "#FFFFFF", // White

    // Icon customization config
    val iconColorHex: String = "#3B82F6",
    val iconSizePercent: Int = 100, // percentage size of icons
    val isChatIconVisible: Boolean = true,
    val isChatIconFullyRemoved: Boolean = false,

    // App Basic Information
    val appName: String = "كل خدمات اليمن",
    val appLogoUrl: String = "", // empty = default vector logo
    val appCoverUrl: String = "",
    val appDescription: String = "البوابة الذكية لطلب كافة خدمات الصيانة والتوصيل والخدمات السياحية والتقنية من أفضل المحترفين في اليمن.",
    val supportPhone: String = "+967770000000",
    val supportEmail: String = "support@yemen-services.com",
    val shareUrl: String = "https://yemen-services.com/app",
    val termsOfService: String = "الشروط والأحكام الافتراضية للخدمة ودليل صيانة اليمن...",
    val privacyPolicy: String = "سياسة خصوصية حماية بيانات العملاء والفنيين بجمهورية اليمن...",
    val lastUpdateDate: String = "2026-06-16",

    // Loyalty Points Configuration
    val loyaltyEnabled: Boolean = true,
    val loyaltyPointValueYemeniRial: Int = 10, // 1 point = 10 YER
    val pointsPerShare: Int = 20,
    val pointsNeededForCoupon: Int = 100,

    // Chat setup
    val isChatDisabledAll: Boolean = false,
    val isChatDisabledProviders: Boolean = false,
    val isChatDisabledUsers: Boolean = false,
    val chatDisabledNotificationText: String = "خدمة الدردشة متوقفة حالياً للصيانة، نعتذر عن الإزعاج.",

    // AI chatbot assistant configs
    val aiAssistantEnabled: Boolean = true,
    val aiAssistantWelcomeMessage: String = "أهلاً بك! أنا WAM مساعدك الصوتي الذكي في اليمن. كيف يمكنني خدمتك الفنية اليوم؟",
    val aiSuggestedQuestions: List<String> = listOf(
        "كيف أحجز فني كهرباء في صنعاء؟",
        "كم تكلفة خدمة صيانة المكيفات؟",
        "هل تتوفر خدمات لمدينة إب؟",
        "كيف أسجل كفني في المعرض الخدمي؟"
    ),
    val voiceInputEnabled: Boolean = true,

    // Registration and Booking rules
    val isUserRegistrationMandatory: Boolean = false, // If true, requires quick register screen before booking/chatting
    val defaultDispatchMethodIndex: Int = 2 // 0: supervisor first, 1: nearest tech, 2: all tech first acceptance, 3: pre-assigned per region, 4: admin manually dispatch
)

// Active Coupon model
data class Coupon(
    val code: String,
    val discountPercent: Int = 0,
    val discountFixedValue: Int = 0,
    val totalLimit: Int = 100,
    val perUserLimit: Int = 1,
    val expirationDate: String = "",
    val isEnabled: Boolean = true,
    val usageCount: Int = 0
)

// Promotional banners & video advertisements
data class Advert(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val mediaUrl: String, // image URL or Local Resource string or placeholder
    val mediaType: String, // "IMAGE" or "VIDEO"
    val categoryId: String = "الكل", // target category
    val durationSeconds: Int = 15,
    val clickCount: Int = 0,
    val viewCount: Int = 0,
    val isActive: Boolean = true,
    val displayOrder: Int = 1
)

// Service Categories data model
data class Category(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val iconName: String, // material icon identifier
    val isEnabled: Boolean = true,
    val sortingOrder: Int = 0
)

// Shared FAQ items
data class FAQ(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val category: String = "عام", // "حسابي", "الحجوزات", "الدفع", "عام"
    val isEnabled: Boolean = true
)
