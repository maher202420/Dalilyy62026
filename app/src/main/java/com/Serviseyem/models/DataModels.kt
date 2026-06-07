package com.Serviseyem.models

data class ServiceItem(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val price: String = "حسب الاتفاق",
    val executionTime: String = "فوري",
    val providerPhone: String = "",
    val isPinned: Boolean = false,
    val iconName: String = "Star",
    val dateAdded: Long = System.currentTimeMillis()
)

data class ServiceProvider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val password: String = "",
    val specialty: String = "",
    val residenceAddress: String = "",
    val businessAddress: String = "",
    val status: String = "أنتظر الموافقة", // "أنتظر الموافقة" or "مقبول"
    val isVerified: Boolean = false,
    val isVip: Boolean = false,
    val gender: String = "ذكر",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rating: Double = 4.8,
    val isMapDisabled: Boolean = false
)

data class AppSettings(
    val appNameAr: String = "دليل خدمات يمن WAM",
    val welcomeMsg: String = "دليل خدمات يمني فاخر يربط المستخدمين بمقدمي الخدمات وبوابة تحكم ذكية",
    val primaryColor: String = "#D4AF37",
    val baseCanvasColor: String = "#0F172A",
    val supportPhone: String = "777644670",
    val supportWhatsapp: String = "777644670",
    val supportEmail: String = "support@wam.com",
    val footerText: String = "MAW 777644670",
    val themeName: String = "Golden Luxury",
    val textColorOption: String = "High Contrast",
    val adminPassword: String = "WAM2026",
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "النظام قيد الموازنة والتحديث السحابي الفوري...",
    val aiAssistantVisible: Boolean = true,
    val aiAssistantSize: Int = 48,
    val aiAssistantColor: String = "#D4AF37",
    val aiAssistantEffect: String = "Pulse",
    val aiAssistantIconUrl: String = "",
    val infoIconVisible: Boolean = true,
    val infoIconSize: Int = 24,
    val sponsoredAdVisible: Boolean = true,
    val sponsoredAdText: String = "خدمات مميزة برعاية دليل WAM الإلكتروني الفوري",
    val sponsoredAdType: String = "Banner",
    
    // Radius & Search settings
    val distanceLimit: Int = 10,
    val voiceSearchEnabled: Boolean = true,
    
    // Dynamic Chat controls
    val chatIconVisible: Boolean = true,
    val chatIconSize: Int = 48,
    val chatIconColor: String = "#D4AF37",
    val chatIconEffect: String = "Pulse",
    val chatIconIconUrl: String = "",
    val chatEnabledForVisitors: Boolean = true,
    val chatEnabledForProviders: Boolean = true,
    val chatDisabledMessage: String = "عذراً، تم تعطيل خدمة الدردشة الفورية مؤقتاً للتحديث الإداري.",
    val blockedChatUsers: String = "" // Comma-separated blacklist of phone numbers
)

data class SupervisorUser(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val password: String = "",
    val canApproveTechs: Boolean = true,
    val canManageCategories: Boolean = true,
    val canManageAds: Boolean = true,
    val canDeleteTechs: Boolean = true,
    val canViewReports: Boolean = true
)

data class CategoryItem(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val isPinned: Boolean = false,
    val parentId: String = "", // Empty means Root Category, else links to another category ID
    val rankOrder: Int = 0
)

data class DatabaseCity(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = ""
)

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false
)

data class ChatSession(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val initiatorName: String = "",
    val initiatorPhone: String = "",
    val providerName: String = "",
    val providerPhone: String = ""
)
