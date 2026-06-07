package com.Serviseyem.models

import java.util.UUID

data class ServiceProvider(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var phone: String,
    var specialty: String,
    var city: String,
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
    var biography: String = "أخصائي معتمد ذو خبرة عالية في الدقة والتشخيص المعياري."
)

data class Category(
    val id: String = UUID.randomUUID().toString(),
    var nameAr: String,
    var nameEn: String,
    var description: String,
    var iconEmoji: String = "🔧",
    var isPinned: Boolean = false,
    var isPublished: Boolean = true
)

data class City(
    val id: String = UUID.randomUUID().toString(),
    var nameAr: String,
    var nameEn: String
)

data class AdBanner(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var contentType: String = "text", // "image", "video", "text"
    var mediaUrl: String? = null,
    var targetSectionId: String,
    var adSize: Int = 10,
    var durationSeconds: Int = 15,
    var isVisible: Boolean = true
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val senderName: String,
    val senderRole: String, // "user", "tech", "admin"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val userName: String,
    val techName: String,
    var lastMessage: String,
    var lastUpdated: Long = System.currentTimeMillis(),
    var isBlocked: Boolean = false
)

data class Complaint(
    val id: String = UUID.randomUUID().toString(),
    val techName: String,
    val complainantName: String,
    val complaintText: String,
    var status: String = "معلق", // "معلق" / "تم حلها"
    val timestamp: Long = System.currentTimeMillis()
)

data class Booking(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String,
    val customerPhone: String,
    val techName: String,
    val date: String,
    val time: String,
    var status: String = "معلق" // "معلق", "مقبول", "مرفوض", "مكتمل"
)

data class RegistrationTerm(
    val id: String = UUID.randomUUID().toString(),
    var termText: String
)

data class AdminAccount(
    val username: String,
    val passwordSecret: String,
    val privileges: List<String> = emptyList()
)
