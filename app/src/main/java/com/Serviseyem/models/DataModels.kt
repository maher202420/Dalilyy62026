package com.Serviseyem.models

import androidx.compose.ui.graphics.vector.ImageVector

data class AppSettings(
    val appId: String = "primary",
    val appNameAr: String = "دليل خدمات يمن WAM",
    val appNameEn: String = "WAM Services",
    val primaryColor: String = "#D4AF37", // Elegant Editorial Gold
    val secondaryColor: String = "#064E3B", // Deep Emerald Gold
    val baseCanvasColor: String = "#042F2E", // Editorial deep-space emerald background
    val footerText: String = "MAW 777644670",
    val welcomeMsg: String = "مرحباً بكم في خدمات اليمن الفاخرة",
    val supportPhone: String = "777644670",
    val supportWhatsapp: String = "777644670",
    val supportEmail: String = "support@serviseyem.com",
    val downloadUrl: String = "https://serviseyem.com/download"
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
