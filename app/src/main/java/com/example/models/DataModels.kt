package com.example.models

data class Category(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val description: String = "",
    val icon: String = "", // e.g. "carpentry", "ac_unit", "electrical", "plumbing" or image URL
    val isSubCategory: Boolean = false,
    val parentId: String = ""
)

data class ServiceProvider(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val phone: String = "",
    val categoryId: String = "",
    val subCategoryId: String = "",
    val workAddress: String = "",
    val residenceAr: String = "",
    val rating: Double = 5.0,
    val reviewCount: Int = 1,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isVerified: Boolean = false,
    val loyaltyPoints: Int = 0,
    val avatarUrl: String = "",
    val idCardUrl: String = "",
    val isVip: Boolean = false,
    val distance: Double = 1.0, // mockup distance
    val latitude: Double = 15.3694, // Sana'a default coordinates
    val longitude: Double = 44.1910,
    val subscriptionActive: Boolean = false
)

data class PendingProvider(
    val id: String = "",
    val nameAr: String = "",
    val phone: String = "",
    val categoryId: String = "",
    val subCategoryId: String = "",
    val workAddress: String = "",
    val residenceAr: String = "",
    val avatarUrl: String = "",
    val idCardUrl: String = "",
    val dateSubmitted: Long = System.currentTimeMillis()
)

data class Admin(
    val id: String = "",
    val username: String = "",
    val password: String = "",
    val permissions: AdminPermissions = AdminPermissions()
)

data class AdminPermissions(
    val canApproveRequests: Boolean = true,
    val canEditCategories: Boolean = false,
    val canManageBanners: Boolean = false,
    val canDeleteProviders: Boolean = false,
    val canViewReports: Boolean = false
)

data class AppSettings(
    val appNameAr: String = "دليل خدمات يمن WAM",
    val appNameEn: String = "WAM Services",
    val primaryColor: String = "#D4AF37", // Elegant Editorial Gold
    val secondaryColor: String = "#064E3B", // Deep Emerald Green
    val baseCanvasColor: String = "#042F2E", // Editorial deep-space emerald background
    val footerText: String = "MAW 777644670",
    val welcomeMsg: String = "مرحباً بكم في خدمات اليمن الفاخرة",
    val supportPhone: String = "777644670",
    val supportEmail: String = "support@serviseyem.com",
    val supportWhatsapp: String = "777644670",
    val updateUrl: String = "https://example.com/update/v2"
)

data class BannerAd(
    val id: String = "",
    val title: String = "",
    val type: String = "IMAGE", // IMAGE, VIDEO, TEXT
    val bannerUrl: String = "",
    val targetCategory: String = "", // categoryId targeted on click
    val size: Int = 10,
    val durationSeconds: Int = 5,
    val linkUrl: String = "",
    val isActive: Boolean = true
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ReportComplaint(
    val id: String = "",
    val senderName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val complaintText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ActivityLog(
    val id: String = "",
    val logText: String = "",
    val actor: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
