package com.Serviseyem.models

data class Category(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val icon: String
)

data class ServiceProvider(
    val id: String,
    val name: String,
    val categoryId: String,
    val specialty: String,
    val city: String,
    val rating: Double,
    val ratingsCount: Int,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val biography: String,
    val baseFee: Int, // in YER (Yemeni Rial)
    val isVip: Boolean = false,
    val isVerified: Boolean = false,
    val isPinned: Boolean = false,
    val hasSubscription: Boolean = false,
    val isChatMuted: Boolean = false,
    val availableHours: String = "8:00 AM - 9:00 PM"
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isFromUser: Boolean
)
