package com.Serviseyem.models

import java.util.UUID

data class ServiceProvider(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var phone: String = "",
    var specialty: String = "",
    var city: String = "",
    var rating: Double = 4.8,
    var ratingsCount: Int = 9,
    var isVip: Boolean = false,
    var isVerified: Boolean = false,
    var baseFee: Int = 3000,
    var biography: String = "",
    var profilePhotoUrl: String? = null,
    var status: String = "مقبول", // "معلق", "مقبول", "مرفوض"
    var isChatMuted: Boolean = false, // Admin can mute chat for specific providers
    var gender: String = "ذكر", // "ذكر" / "أنثى"
    var photoSource: String = "معرض الصور", // "معرض الصور" / "التقاط بالكاميرا"
    var photoType: String = "صورة شخصية (سيلفي)", // "صورة شخصية (سيلفي)" / "صورة تعبيرية عن المهنة"
    var skills: String = "صيانة عامة، تمديدات حديثة، إصلاح الأعطال الطارئة",
    var galleryUrls: List<String> = emptyList(),
    var galleryEnabled: Boolean = true,
    var maxGalleryImages: Int = 10,
    var experienceYears: Int = 3,
    var contactEmail: String = "tech@serviseyem.com",
    var latitude: Double = 15.3693,
    var longitude: Double = 44.1910,
    var isPinned: Boolean = false,
    var isRecommended: Boolean = false,
    var hasSubscription: Boolean = false,
    var subscriptionStatus: String = "none", // "none", "pending", "active"
    var subscriptionPaymentDetails: String = ""
)

data class Category(
    var id: String = UUID.randomUUID().toString(),
    var nameAr: String = "",
    var nameEn: String = "",
    var description: String = "",
    var iconEmoji: String = "🔧",
    var isPinned: Boolean = false,
    var isPublished: Boolean = true,
    var type: String = "professional", // "professional" (مهني), "service" (خدمي), "commercial" (تجاري)
    var parentId: String? = null, // if it's a sub-category, points to parent Category's id. Otherwise null (main/أساسي).
    var imageUrl: String? = null // custom image URL or icon URL
)

data class ColorPalette(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var primaryColor: String = "#FFD700",
    var secondaryColor: String = "#03DAC6",
    var backgroundColor: String = "#0A0A0C",
    var textColor: String = "#FFFFFF"
)

data class City(
    var id: String = UUID.randomUUID().toString(),
    var nameAr: String = "",
    var nameEn: String = ""
)

data class AdBanner(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var contentType: String = "text", // "image", "video", "text"
    var mediaUrl: String? = null,
    var targetSectionId: String = "",
    var adSize: Int = 10,
    var durationSeconds: Int = 15,
    var isVisible: Boolean = true
)

data class ChatMessage(
    var id: String = UUID.randomUUID().toString(),
    var chatId: String = "",
    var senderName: String = "",
    var senderRole: String = "", // "user", "tech", "admin"
    var messageText: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isApproved: Boolean = true
)

data class ChatSession(
    var id: String = UUID.randomUUID().toString(),
    var userName: String = "",
    var techName: String = "",
    var lastMessage: String = "",
    var lastUpdated: Long = System.currentTimeMillis(),
    var isBlocked: Boolean = false,
    var techId: String = "" // maps to ServiceProvider
)

data class ChatParticipant(
    var id: String = UUID.randomUUID().toString(),
    var chatId: String = "",
    var userId: String = "",
    var role: String = "" // "user", "tech", "admin"
)

data class Complaint(
    var id: String = UUID.randomUUID().toString(),
    var techName: String = "",
    var complainantName: String = "",
    var complaintText: String = "",
    var status: String = "معلق", // "معلق" / "تم حلها"
    var timestamp: Long = System.currentTimeMillis()
)

data class Booking(
    var id: String = UUID.randomUUID().toString(),
    var customerName: String = "",
    var customerPhone: String = "",
    var techName: String = "",
    var date: String = "",
    var time: String = "",
    var status: String = "قيد الانتظار", // "قيد الانتظار", "تم القبول", "قيد التنفيذ", "مكتمل", "ملغي"
    var district: String = "",
    var customFieldsData: Map<String, String> = emptyMap()
)

data class BookingField(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var labelAr: String = "",
    var labelEn: String = "",
    var type: String = "text", // "text", "number", "dropdown", "checkbox"
    var isRequired: Boolean = true,
    var isVisible: Boolean = true,
    var options: List<String> = emptyList(),
    var order: Int = 0
)

data class AuditLog(
    var id: String = UUID.randomUUID().toString(),
    var actor: String = "",
    var action: String = "",
    var details: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

data class RegistrationTerm(
    var id: String = UUID.randomUUID().toString(),
    var termText: String = ""
)

data class AdminAccount(
    var username: String = "",
    var passwordSecret: String = "",
    var privileges: List<String> = emptyList()
)

data class AppNotification(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var body: String = "",
    var recipientName: String? = null, // null means public (عام للجميع)
    var isRead: Boolean = false,
    var timestamp: Long = System.currentTimeMillis(),
    var type: String = "general" // "general", "booking_status", "registration_status"
)
