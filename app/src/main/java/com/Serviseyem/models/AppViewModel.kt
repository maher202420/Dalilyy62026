package com.Serviseyem.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel : ViewModel() {

    // Central dynamic Lists
    var providers by mutableStateOf(listOf(
        ServiceProvider(name = "المهندس وليد الصنعاني", phone = "777123456", specialty = "تبريد وتكييف", city = "صنعاء", rating = 4.9, isVip = true, isVerified = true, baseFee = 5000, biography = "أخصائي تكييف وتبريد مركزي ذو خبرة تفوق 10 سنوات."),
        ServiceProvider(name = "أبو ماجد البريحي", phone = "777644670", specialty = "سباكة", city = "إب", rating = 4.8, isVip = true, isVerified = true, baseFee = 4000, biography = "خبير تركيب وصيانة الشبكات لجميع فلل وعمارات المحافظة."),
        ServiceProvider(name = "أحمد جلال الحديدي", phone = "733654321", specialty = "كهرباء", city = "الحديدة", rating = 4.7, isVip = false, isVerified = true, baseFee = 3500, biography = "فني تمديدات وصيانة أنظمة الطاقة الشمسية والتيار المتردد."),
        ServiceProvider(name = "ياسين النجار", phone = "711998877", specialty = "نجارة", city = "عدن", rating = 4.6, isVip = false, isVerified = false, baseFee = 6000, biography = "تفصيل وتجهيز أحدث الأثاث الخشبي والمودرن وغرف النوم."),
        ServiceProvider(name = "فؤاد الحداد", phone = "777554433", specialty = "حدادة", city = "صنعاء", rating = 4.5, isVip = false, isVerified = false, baseFee = 4500, biography = "أعمال الأبواب والشبابيك والمظلات والدرابزين الفاخر.")
    ))

    var categories by mutableStateOf(listOf(
        Category(nameAr = "سباكة", nameEn = "Plumbing", description = "صيانة الحمامات وتمديد الشبكات والمطابخ", iconEmoji = "🔧", isPinned = true),
        Category(nameAr = "كهرباء", nameEn = "Electrical", description = "صيانة وتمديد خطوط الكهرباء وفحص المولدات", iconEmoji = "⚡", isPinned = true),
        Category(nameAr = "نجارة", nameEn = "Carpentry", description = "تصليح الأبواب والمطابخ وتصميم غرف النوم", iconEmoji = "🪚", isPinned = false),
        Category(nameAr = "تبريد وتكييف", nameEn = "Cooling", description = "تعبئة الفريون وغسيل المكيفات المركزية", iconEmoji = "❄️", isPinned = true),
        Category(nameAr = "حدادة", nameEn = "Smithing", description = "تركيب البوابات الحديدية والدرابزين والحماية", iconEmoji = "🔨", isPinned = false)
    ))

    var cities by mutableStateOf(listOf(
        City(nameAr = "صنعاء", nameEn = "Sana'a"),
        City(nameAr = "عدن", nameEn = "Aden"),
        City(nameAr = "إب", nameEn = "Ibb"),
        City(nameAr = "الحديدة", nameEn = "Hodeidah"),
        City(nameAr = "تعز", nameEn = "Taiz"),
        City(nameAr = "المكلا", nameEn = "Mukalla")
    ))

    var banners by mutableStateOf(listOf(
        AdBanner(title = "تخفيضات صيفية 30% على صيانة المكيفات", contentType = "text", targetSectionId = "تبريد وتكييف", durationSeconds = 15, adSize = 10, isVisible = true)
    ))

    var registrationRequests by mutableStateOf(listOf<ServiceProvider>())

    var chatSessions by mutableStateOf(listOf(
        ChatSession(userName = "العميل غسان", techName = "أبو ماجد البريحي", lastMessage = "متى تستطيع الوصول للمنزل؟")
    ))

    var chatMessages by mutableStateOf(listOf(
        ChatMessage(chatId = "1", senderName = "العميل غسان", senderRole = "user", messageText = "السلام عليكم ورحمة الله"),
        ChatMessage(chatId = "1", senderName = "أبو ماجد البريحي", senderRole = "tech", messageText = "وعليكم السلام ورحمة الله، أهلاً بك يا فندم"),
        ChatMessage(chatId = "1", senderName = "العميل غسان", senderRole = "user", messageText = "متى تستطيع الوصول للمنزل؟")
    ))

    var complaints by mutableStateOf(listOf(
        Complaint(techName = "ياسين النجار", complainantName = "العميد رشاد", complaintText = "تأخر عن موعد الإنجاز المالي المتفق عليه ليومين.")
    ))

    var bookings by mutableStateOf(listOf(
        Booking(customerName = "خالد الصبري", customerPhone = "770998811", techName = "أبو ماجد البريحي", date = "2026-06-10", time = "10:00 ص", status = "معلق")
    ))

    var registrationTerms by mutableStateOf(listOf(
        RegistrationTerm(termText = "الالتزام التام بالأسعار المعيارية المقررة من الدليل."),
        RegistrationTerm(termText = "دقة المواعيد والأمانة في معاينات الأجهزة الفنية."),
        RegistrationTerm(termText = "توفير بطاقة شخصية وضمانة حضورية سارية المفعول.")
    ))

    var admins by mutableStateOf(listOf(
        AdminAccount("admin", "7777", listOf(
            "قبول ورفض طلبات التسجيل للفنيين",
            "إضافة وحذف وتعديل الأقسام والمدن",
            "إدارة الإعلانات والبنرات المتحركة",
            "حذف مزودي الخدمة النشطين من الدليل",
            "رؤية بلاغات المستخدمين وتقارير التدقيق الكامل"
        ))
    ))

    // Admin Customization / Layout State Configurations
    var appPrimaryColorStr by mutableStateOf("#FFD700") // Default Gold Accent
    val appPrimaryColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(appPrimaryColorStr))
        } catch (e: Exception) {
            Color(0xFFFFD700)
        }

    // Chat widget settings
    var chatSettingsIconSize by mutableStateOf(60f) // Size in DP
    var chatSettingsIconColorStr by mutableStateOf("#064E3B") // Emerald Green Bubble default
    val chatSettingsIconColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(chatSettingsIconColorStr))
        } catch (e: Exception) {
            Color(0xFF064E3B)
        }
    var chatSettingsVisible by mutableStateOf(true)
    var chatSettingsDeleted by mutableStateOf(false)

    // AI Assistant widget settings
    var aiAssistantIconSize by mutableStateOf(60f)
    var aiAssistantIconColorStr by mutableStateOf("#111827")
    val aiAssistantIconColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(aiAssistantIconColorStr))
        } catch (e: Exception) {
            Color(0xFF111827)
        }
    var aiAssistantVisible by mutableStateOf(true)

    // Permissions and overall app features toggled by admin
    var voiceSearchEnabled by mutableStateOf(true)
    var mapRadiusKm by mutableStateOf(10.0) // 5km, 10km, 25km, etc.
    var autoCleanupDays by mutableStateOf(30)
    var isChatInstantEnabled by mutableStateOf(true)
    var chatDisabledMessage by mutableStateOf("المحادثة الفورية معطلة مؤقتاً لأعمال الكفاءة - نرجو التواصل هاتفياً مباشرة")
    var isRatingsAndReviewsEnabled by mutableStateOf(true)
    var showBookingsSection by mutableStateOf(true)

    // User State Information
    var userLoyaltyPoints by mutableStateOf(100)
    var activeAdminUsername by mutableStateOf<String?>(null)

    // Log tracking for administration panel
    var adminActivityLogs by mutableStateOf(listOf(
        "تم تشغيل لوحة التحكم ومزامنة بروتوكولات الأمان الفورية بنجاح."
    ))

    fun addActivityLog(log: String) {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        adminActivityLogs = listOf("[$stamp] $log") + adminActivityLogs
    }

    // Manual add with auto compression simulated
    fun addManualTechnician(
        name: String,
        phone: String,
        city: String,
        specialty: String,
        fee: Int,
        isVip: Boolean
    ): String {
        val formattedFee = if (fee <= 0) 5000 else fee
        val newTech = ServiceProvider(
            name = name,
            phone = phone,
            city = city,
            specialty = specialty,
            baseFee = formattedFee,
            isVip = isVip,
            isVerified = isVip,
            status = "مقبول"
        )
        providers = providers + newTech
        addActivityLog("إضافة فني يدوياً: ${name} [$specialty] في $city")
        return "تم ضغط الصورة الشخصية تلقائيًا لسرعة تحميل التطبيق بنسبة 72%. تم دمج الكادر '${name}' فورياً."
    }

    // Submit provider application from client app
    fun registerNewProvider(
        name: String,
        phone: String,
        specialty: String,
        city: String,
        photoMethodSelection: String, // "Selfie Camera" / "Gallery Selection"
        isFemale: Boolean
    ) {
        val newRequest = ServiceProvider(
            name = name,
            phone = phone,
            specialty = specialty,
            city = city,
            status = "معلق"
        )
        registrationRequests = registrationRequests + newRequest
        addActivityLog("طلب تسجيل جديد وارد من: $name [$specialty] عبر $photoMethodSelection")
    }

    fun approveRequest(id: String) {
        val request = registrationRequests.find { it.id == id }
        if (request != null) {
            request.status = "مقبول"
            providers = providers + request
            registrationRequests = registrationRequests.filter { it.id != id }
            addActivityLog("قبول طلب الفني: ${request.name}")
        }
    }

    fun rejectRequest(id: String) {
        val request = registrationRequests.find { it.id == id }
        if (request != null) {
            registrationRequests = registrationRequests.filter { it.id != id }
            addActivityLog("رفض طلب الفني: ${request.name}")
        }
    }

    fun deleteActiveProvider(id: String) {
        val p = providers.find { id == it.id }
        if (p != null) {
            providers = providers.filter { it.id != id }
            addActivityLog("حذف المهني النشط: ${p.name}")
        }
    }

    fun addMainCategory(nameAr: String, nameEn: String, desc: String, iconCode: String) {
        val newCat = Category(
            nameAr = nameAr,
            nameEn = nameEn,
            description = desc,
            iconEmoji = iconCode
        )
        categories = categories + newCat
        addActivityLog("إنشاء فئة رئيسية جديدة: $nameAr / $nameEn")
    }

    fun addCity(nameAr: String, nameEn: String) {
        val newCity = City(nameAr = nameAr, nameEn = nameEn)
        cities = cities + newCity
        addActivityLog("إضافة مدينة تغطية جغرافية: $nameAr")
    }

    fun cleanUpTempLogs() {
        adminActivityLogs = listOf("[تصفير وتنظيف آلي للبيانات] تم تفريغ الكاش الإداري وتطهير ملفات الاستماع المؤقتة بنجاح.")
        addActivityLog("تشغيل دورة التنظيف الفوري.")
    }
}
