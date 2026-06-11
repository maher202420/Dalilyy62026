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
    var maxGalleryImages: Int = 10
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
