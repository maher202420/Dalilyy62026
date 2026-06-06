package com.Serviseyem.models

import androidx.compose.ui.graphics.vector.ImageVector

data class AppSettings(
    val appId: String = "primary",
    val appNameAr: String = "دليل WAM للخدمات السحابية",
    val appNameEn: String = "WAM Services",
    val primaryColor: String = "#D4AF37", // Silver/Gold/Green hex
    val secondaryColor: String = "#064E3B", // Accent color
    val baseCanvasColor: String = "#042F2E", // Background color
    val footerText: String = "WAM 777644670",
    val welcomeMsg: String = "مرحباً بكم في خدمات اليمن الفاخرة",
    val supportPhone: String = "777644670",
    val supportWhatsapp: String = "777644670",
    val supportEmail: String = "maher@wam.com",
    val downloadUrl: String = "https://serviseyem.com/download",
    
    // New customized fields for backdoor controls
    val adminPassword: String = "maher736462", // Changeable password for WAM2026
    val themeName: String = "charcoal_gold", // cosmic, charcoal_gold, royal_emerald
    val textColorOption: String = "bright_white", // bright_white, light_gold, vibrant_silver
    val appLogoUrl: String = "", // custom launcher/app logo URL or simulator path
    
    // customizable AI floating button size & stats
    val aiAssistantSize: Int = 48,
    val aiAssistantColor: String = "#D4AF37",
    val aiAssistantVisible: Boolean = true,
    
    // info icon size & stats
    val infoIconSize: Int = 24,
    val infoIconVisible: Boolean = true,
    
    // Sponsored Ads control
    val sponsoredAdUrl: String = "",
    val sponsoredAdText: String = "بوابة WAM السحابية تقدم خدمات فورية وموثوقة على مدار الساعة",
    val sponsoredAdType: String = "نص وصورة", // نص, صورة, فيديو, نص وصورة
    val sponsoredAdDuration: Int = 30, // in days
    val sponsoredAdVisible: Boolean = true,
    
    // Maintenance Mode
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "التطبيق في وضع الصيانة لترقية الخوادم وتحديث قواعد البيانات السحابية فوراً 🛠️"
)

data class ServiceItem(
    val id: String = "",
    val title: String = "",
    val category: String = "", // e.g. "حكومية", "إلكترونية", "دعم فني", "أخرى"
    val description: String = "",
    val price: String = "حسب الاتفاق",
    val executionTime: String = "فوري",
    val providerPhone: String = "777644670",
    val iconName: String = "Star",
    val isPinned: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)

data class SupervisorUser(
    val id: String = "",
    val phone: String = "",
    val name: String = "",
    val password: String = "",
    val isApproved: Boolean = true,
    val notes: String = ""
)

data class ServiceProvider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val specialty: String = "",
    val password: String = "",
    val identityNumber: String = "",
    val status: String = "أنتظر الموافقة", // "أنتظر الموافقة", "مقبول", "مرفوض"
    val registrationDate: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = false, // Set locally at runtime
    val isAdmin: Boolean = false
)

data class PendingProvider(
    val id: String = "",
    val fullName: String = "",
    val phone: String = "",
    val mainCategory: String = "",
    val subCategory: String = "",
    val officeAddress: String = "",
    val district: String = "",
    val gpsCoordinates: String = "", // e.g. "15.3694,44.1910"
    val profilePhotoUrl: String = "",
    val idPhotoUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending", // pending, approved, rejected
    val rejectionReason: String = ""
)

data class ProviderReview(
    val id: String = "",
    val providerId: String = "",
    val userPhone: String = "",
    val userName: String = "مستخدم 익명",
    val rating: Int = 5,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ProviderReport(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val reporterPhone: String = "",
    val reason: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

data class CategoryItem(
    val id: String = "",
    val nameAr: String = "",
    val iconName: String = "Star",
    val parentId: String = "", // Empty for main category, otherwise parent main category ID
    val rankOrder: Int = 0
)

data class LoyaltyTransaction(
    val id: String = "",
    val userPhone: String = "",
    val points: Int = 0,
    val type: String = "earn", // earn, redeem
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class DatabaseCity(
    val id: String = "",
    val nameAr: String = ""
)
