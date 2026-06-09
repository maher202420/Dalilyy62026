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
    var selfieUrl: String? = null,
    var baseFee: Int = 5000,
    var latitude: Double? = 15.348,
    var longitude: Double? = 44.206,
    var isMale: Boolean = true,
    var status: String = "مقبول", // "معلق" / "مقبول" / "مرفوض"
    var biography: String = "أخصائي معتمد ذو خبرة عالية في الدقة والتشخيص المعياري.",
    var rejectionReason: String? = null,
    var subscriptionType: String? = "شهري معتمد",
    var isSubscriptionVerified: Boolean = false,
    var isPinned: Boolean = false
)

data class Category(
    var id: String = UUID.randomUUID().toString(),
    var nameAr: String = "",
    var nameEn: String = "",
    var description: String = "",
    var iconEmoji: String = "🔧",
    var isPinned: Boolean = false,
    var isPublished: Boolean = true
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
    var timestamp: Long = System.currentTimeMillis()
)

data class ChatSession(
    var id: String = UUID.randomUUID().toString(),
    var userName: String = "",
    var techName: String = "",
    var lastMessage: String = "",
    var lastUpdated: Long = System.currentTimeMillis(),
    var isBlocked: Boolean = false
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
    var status: String = "معلق" // "معلق", "مقبول", "مرفوض", "مكتمل"
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
