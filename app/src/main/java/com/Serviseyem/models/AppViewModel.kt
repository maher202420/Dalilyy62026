package com.Serviseyem.models

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Serviseyem.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("yemen_services_prefs", Context.MODE_PRIVATE)

    // --- Active User Role & Settings ---
    var currentUserRole by mutableStateOf(UserRole.VISITOR)
    var loggedInUserId by mutableStateOf<String?>(null)
    var loggedInName by mutableStateOf("")
    var loggedInPhone by mutableStateOf("")

    // Admin Credentials
    val adminUsername = BuildConfig.ADMIN_USERNAME.ifEmpty { "WAM2026" }
    val adminPassword = BuildConfig.ADMIN_PASSWORD.ifEmpty { "maher736462" }
    val ownerPassword = BuildConfig.OWNER_PASSWORD.ifEmpty { "maher--736462" }

    // List of Cities in Yemen
    var allowedCities by mutableStateOf(listOf("صنعاء", "عدن", "إب", "تعز", "حضرموت"))

    // --- State Lists ---
    var appSetup by mutableStateOf(AppSetup())
    var technicians by mutableStateOf(listOf<Technician>())
    var clientUsers by mutableStateOf(listOf<ClientUser>())
    var bookings by mutableStateOf(listOf<Booking>())
    var notices by mutableStateOf(listOf<Notice>())
    var auditLogs by mutableStateOf(listOf<AuditLog>())
    var faqList by mutableStateOf(listOf<FAQ>())
    var adverts by mutableStateOf(listOf<Advert>())
    var coupons by mutableStateOf(listOf<Coupon>())
    var chatChannels by mutableStateOf(listOf<ChatChannel>())
    var customFields by mutableStateOf(listOf<CustomField>())
    var categoriesBySetup by mutableStateOf(listOf<Category>())

    // --- Local Notifications Broadcast Flow ---
    private val _notificationFlow = MutableSharedFlow<Pair<String, String>>()
    val notificationFlow = _notificationFlow.asSharedFlow()

    // --- Synchronization State ---
    var syncInProgress by mutableStateOf(false)
    var lastSyncTime by mutableStateOf("لم تتم المزامنة بعد")
    var lastSyncResult by mutableStateOf("ناجحة")
    var isAutomaticSyncEnabled by mutableStateOf(true)
    var syncIntervalHours by mutableStateOf(6) // 1, 6, 24, or -1 (Manual)
    var serverUrlEnv by mutableStateOf("https://api.yemen-services.com/admin")
    var isDebugModeEnabled by mutableStateOf(false)

    init {
        // Load configurations
        loadAllData()
    }

    // --- Add Audit Log ---
    fun addAudit(admin: String, action: String, details: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val log = AuditLog(adminId = admin, action = action, details = details, timestamp = timestamp)
        auditLogs = listOf(log) + auditLogs
        saveToDisk()
    }

    // --- Post Targeted Notifications ---
    fun addTargetedNotification(title: String, body: String, targetId: String = "All", group: String = "General") {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedDate = sdf.format(Date())
        val newNotice = Notice(title = title, text = body, date = formattedDate, targetId = targetId, category = group)
        notices = listOf(newNotice) + notices
        saveToDisk()

        // Raise notification flow trigger
        viewModelScope.launch {
            _notificationFlow.emit(Pair(title, body))
        }
    }

    fun markNoticeAsRead(noticeId: String) {
        notices = notices.map { if (it.id == noticeId) it.copy(isRead = true) else it }
        saveToDisk()
    }

    fun deleteNotice(noticeId: String) {
        notices = notices.filter { it.id != noticeId }
        saveToDisk()
    }

    // --- Reset/Complete System Purge via Password check ---
    fun performEmergencyDataSanitize(enteredPass: String): Boolean {
        if (enteredPass == adminPassword) {
            // Wipe collections and reset everything
            prefs.edit().clear().apply()
            initDefaultSeeds()
            saveToDisk()
            addAudit("المطور/المالك", "تطهير كامل", "تم تصفير وإعادة تعيين النظام بالكامل")
            return true
        }
        return false
    }

    // --- Dispatch Booking Logic (Section 15) ---
    fun dispatchAndSubmitBooking(booking: Booking) {
        bookings = bookings + booking
        saveToDisk()

        val serviceName = booking.requestedService
        val region = booking.region

        // Determine dispatch logic behavior set by admin (0 to 4)
        when (appSetup.defaultDispatchMethodIndex) {
            0 -> {
                // Sent to supervisor first
                addTargetedNotification(
                    title = "حجز جديد في انتظار التوزيع 📥",
                    body = "حجز وارد من العميل ${booking.clientName} لخدمة $serviceName في $region بانتظار إسناد مشرف القسم.",
                    targetId = "Supervisor_$serviceName"
                )
            }
            1 -> {
                // Sent directly to nearest active technician in that region and specialty
                val targetTech = technicians.firstOrNull {
                    it.specialty == serviceName && it.region == region && it.state == TechnicianState.ACTIVE
                }
                if (targetTech != null) {
                    bookings = bookings.map { if (it.id == booking.id) it.copy(assignedTechId = targetTech.id) else it }
                    saveToDisk()
                    addTargetedNotification(
                        title = "حجز تلقائي مباشر ⚡",
                        body = "أهلاً ${targetTech.name}، تم توجيه حجز مباشر لك من ${booking.clientName} لكونك الأقرب.",
                        targetId = targetTech.id
                    )
                } else {
                    // Fallback to pool
                    addTargetedNotification(
                        title = "طلب خدمة معلق ⏱️",
                        body = "طلب حجز جديد لخدمة $serviceName في $region بانتظار قبول أي فني.",
                        targetId = "All_Techs_$serviceName"
                    )
                }
            }
            2 -> {
                // broadcast to all technicians in that section
                addTargetedNotification(
                    title = "طلب عمل متاح بالقسم 👨‍🔧",
                    body = "يوجد حجز جديد لخدمة $serviceName في $region. أسرع بالقبول لبدء المهمة!",
                    targetId = "All_Techs_$serviceName"
                )
            }
            3 -> {
                // Sent to a pre-defined technician designated for that region
                val mappedTech = technicians.firstOrNull {
                    it.specialty == serviceName && it.region == region && it.isRecommended
                } ?: technicians.firstOrNull {
                    it.specialty == serviceName && it.region == region
                }
                if (mappedTech != null) {
                    bookings = bookings.map { if (it.id == booking.id) it.copy(assignedTechId = mappedTech.id) else it }
                    saveToDisk()
                    addTargetedNotification(
                        title = "تم إسناد العمل لك 📋",
                        body = "تم توجيه حجز ${booking.clientName} إليك كفني مفضل للمنطقة $region.",
                        targetId = mappedTech.id
                    )
                } else {
                    addTargetedNotification(
                        title = "طلب خدمة معلق ⏱️",
                        body = "طلب حجز جديد لخدمة $serviceName في $region بانتظار قبول أي فني.",
                        targetId = "All_Techs_$serviceName"
                    )
                }
            }
            4 -> {
                // Send to general admin
                addTargetedNotification(
                    title = "حجز وارد للمراجعة اليدوية 🛡️",
                    body = "طلب حجز جديد من العميل ${booking.clientName} بانتظار توزيعك اليدوي كأدمن عام.",
                    targetId = "Admin"
                )
            }
        }
    }

    // --- Technicians CRUD operations ---
    fun addTechnician(tech: Technician) {
        technicians = technicians + tech
        saveToDisk()
        addAudit("النظام", "تسجيل فني جديد", "الفني ${tech.name} تم تسجيله بانتظار المراجعة")
    }

    fun updateTechnician(tech: Technician) {
        technicians = technicians.map { if (it.id == tech.id) tech else it }
        saveToDisk()
        addAudit("الأدمن", "تحديث فني", "تم تحديث بيانات الفني ${tech.name}")
    }

    fun deleteTechnician(techId: String) {
        val tech = technicians.find { it.id == techId }
        technicians = technicians.filter { it.id != techId }
        bookings = bookings.filter { it.assignedTechId != techId }
        saveToDisk()
        if (tech != null) {
            addAudit("الأدمن", "حذف فني", "تم حذف الفني ${tech.name} وإلغاء ارتباط حجوزاته")
        }
    }

    // --- Users CRUD ---
    fun updateClientUser(user: ClientUser) {
        clientUsers = clientUsers.map { if (it.id == user.id) user else it }
        saveToDisk()
    }

    fun toggleUserAudit(userId: String, isBlock: Boolean) {
        clientUsers = clientUsers.map { if (it.id == userId) it.copy(isBlocked = isBlock) else it }
        saveToDisk()
        val label = if (isBlock) "حظر مستخدم" else "إلغاء حظر"
        addAudit("الأدمن", label, "تعديل حالة العميل بنجاح")
    }

    fun modifyLoyaltyPointsManual(userId: String, pointsDiff: Int) {
        clientUsers = clientUsers.map {
            if (it.id == userId) {
                val newPoints = (it.loyaltyPoints + pointsDiff).coerceAtLeast(0)
                it.copy(loyaltyPoints = newPoints)
            } else it
        }
        saveToDisk()
        addAudit("الأدمن", "تعديل نقاط يدوي", "تم تعديل رصيد النقاط للمستخدم بقيمة $pointsDiff")
    }

    // --- Coupons ---
    fun addCoupon(co: Coupon) {
        coupons = coupons + co
        saveToDisk()
        addAudit("الأدمن", "إضافة كوبون", "تم إنشاء الكود ${co.code}")
    }

    fun deleteCoupon(code: String) {
        coupons = coupons.filter { it.code != code }
        saveToDisk()
        addAudit("الأدمن", "حذف كوبون", "تم إلغاء الكود $code")
    }

    // --- Custom Registration Fields Management ---
    fun addCustomRegField(field: CustomField) {
        customFields = customFields + field
        saveToDisk()
        addAudit("الأدمن", "إضافة حقل مخصص", "حقل ${field.label} أضيف لاستمارة الفنيين")
    }

    fun updateCustomRegField(field: CustomField) {
        customFields = customFields.map { if (it.id == field.id) field else it }
        saveToDisk()
    }

    fun deleteCustomRegField(fieldId: String) {
        customFields = customFields.filter { it.id != fieldId }
        saveToDisk()
    }

    // --- Chat Channels / Actions ---
    fun addChatMessage(senderId: String, receiverId: String, messageText: String, senderType: String, senderName: String) {
        val message = ChatMessage(
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            senderType = senderType,
            senderName = senderName
        )

        // Find or create channel
        val isSenderAdminOrSystem = senderType == "Admin" || senderType == "System"
        val channelPartnerId = if (isSenderAdminOrSystem) receiverId else senderId
        val partnerName = if (isSenderAdminOrSystem) "مستخدم" else senderName
        val partnerRole = if (senderType == "Tech") "Tech" else "User"

        val channelIndex = chatChannels.indexOfFirst { it.userId == channelPartnerId }
        if (channelIndex >= 0) {
            val existing = chatChannels[channelIndex]
            val updated = existing.copy(messages = existing.messages + message)
            chatChannels = chatChannels.toMutableList().apply { set(channelIndex, updated) }
        } else {
            val newChannel = ChatChannel(
                userId = channelPartnerId,
                userName = partnerName,
                userRole = partnerRole,
                messages = listOf(message)
            )
            chatChannels = chatChannels + newChannel
        }
        saveToDisk()
    }

    fun deleteChannel(userId: String) {
        chatChannels = chatChannels.filter { it.userId != userId }
        saveToDisk()
        addAudit("الأدمن", "حذف محادثة", "تم حظر/حذف محادثة العميل $userId")
    }

    fun toggleBlockChannel(userId: String) {
        chatChannels = chatChannels.map {
            if (it.userId == userId) it.copy(isBlocked = !it.isBlocked) else it
        }
        saveToDisk()
    }

    fun clearOldChatsDays(days: Int) {
        val limitMs = System.currentTimeMillis() - (days * 24L * 3600L * 1000L)
        chatChannels = chatChannels.map { channel ->
            channel.copy(messages = channel.messages.filter { it.timestamp >= limitMs })
        }.filter { it.messages.isNotEmpty() }
        saveToDisk()
        addAudit("الأدمن", "مسح أرشيف", "تم تصفير محادثات الأيام الفائتة: $days يوم")
    }

    // --- Advertisements CRUD ---
    fun addAdvert(ad: Advert) {
        adverts = adverts + ad
        saveToDisk()
        addAudit("الأدمن", "إضافة إعلان ممول", "تم تسجيل إعلان ${ad.title}")
    }

    fun deleteAdvert(adId: String) {
        adverts = adverts.filter { it.id != adId }
        saveToDisk()
    }

    // --- FAQ CRUD ---
    fun addFAQ(f: FAQ) {
        faqList = faqList + f
        saveToDisk()
    }

    fun deleteFAQ(id: String) {
        faqList = faqList.filter { it.id != id }
        saveToDisk()
    }

    // --- AI Smart WAM Assistant REST Call (Section 12) ---
    var geminiAnswerState by mutableStateOf("")
    fun askWAMSmartAssistant(userText: String) {
        geminiAnswerState = "جاري تواصل WAM مع شبكة المعرفة..."
        // Add chat message
        addChatMessage(
            senderId = "user",
            receiverId = "wam",
            messageText = userText,
            senderType = "User",
            senderName = "أنا"
        )

        viewModelScope.launch {
            delay(1200) // response lag for realism
            if (BuildConfig.GEMINI_API_KEY.isNotBlank() && !BuildConfig.GEMINI_API_KEY.contains("placeholder")) {
                try {
                    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    val yemeniTechInstruction = "أنت مساعد فني يمني ذكي وخبير في ترويج وصيانة دليل 'كل خدمات اليمن'. أجب بلهجة يمنية مهذبة وعربية فصحى مبسطة وقصيرة جداً في حدود سطرين."
                    val payload = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", "$yemeniTechInstruction\nالمستخدم يسأل: $userText")
                                    })
                                })
                            })
                        })
                    }

                    connection.outputStream.use { os ->
                        os.write(payload.toString().toByteArray())
                    }

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                        val respJson = JSONObject(responseText)
                        val textAnswer = respJson.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        geminiAnswerState = textAnswer
                        addChatMessage(
                            senderId = "wam",
                            receiverId = "user",
                            messageText = textAnswer,
                            senderType = "Admin",
                            senderName = "مساعد WAM"
                        )
                    } else {
                        throw Exception("HTTP Error: ${connection.responseCode}")
                    }
                } catch (e: Exception) {
                    val fallback = parseWAMOfflineAnswer(userText)
                    geminiAnswerState = fallback
                    addChatMessage(
                        senderId = "wam",
                        receiverId = "user",
                        messageText = fallback,
                        senderType = "Admin",
                        senderName = "مساعد WAM"
                    )
                }
            } else {
                // Return offline response
                val fallback = parseWAMOfflineAnswer(userText)
                geminiAnswerState = fallback
                addChatMessage(
                    senderId = "wam",
                    receiverId = "user",
                    messageText = fallback,
                    senderType = "Admin",
                    senderName = "مساعد WAM"
                )
            }
        }
    }

    private fun parseWAMOfflineAnswer(text: String): String {
        return when {
            text.contains("كهرباء") || text.contains("كهربائي") -> "أهلاً بك! لدينا أفضل مقاولي وكهربائيين معتمدين بصنعاء وإب وعدن. يمكنك تصفح قسم الكهرباء بالرئيسية وحجز فني مباشرة."
            text.contains("سباك") || text.contains("سباكة") -> "حياك الله، يتوفر لدينا ماهر الوصابي وهو فني سباكة من الدرجة الأولى بصنعاء، تواصله سريع وحاصل على تقييم 6 نجوم!"
            text.contains("سجل") || text.contains("فني جديد") -> "يسعدنا انضمامك! انتقل لتبويب 'سجل كفني' من شريط التنقل السفلي واملأ الحقول المطلوبة ليرسل حسابك للأدمن ويتفعل فوراً."
            text.contains("نقاط") || text.contains("الولاء") -> "ميزة نقاط الولاء تمكنك من الحصول على كود خصم مجاني! اضغط على مشاركة التطبيق مع أصدقائك لتحصل على 20 نقطة تلقائياً."
            text.contains("توزيع") || text.contains("الحجز") -> "يتحكم مدير النظام بـ 5 آليات توزيع؛ منها الإسناد المباشر للفني الأقرب إليك جغرافياً لراحة مطلقة."
            else -> "مرحباً بك في دليل خدمات اليمن! يمكنني إرشادك لحجز الفنيين المتاحين في تخصصات السباكة، الكهرباء، وصيانة المكيفات. أسعد بخدمتك!"
        }
    }

    // --- Save & Load local storage via SharedPrefs (Cohesive JSON serialization) ---
    private fun saveToDisk() {
        try {
            val editor = prefs.edit()

            // Save AppSetup
            val setupJson = JSONObject().apply {
                put("primaryColorHex", appSetup.primaryColorHex)
                put("secondaryColorHex", appSetup.secondaryColorHex)
                put("backgroundColorHex", appSetup.backgroundColorHex)
                put("textColorHex", appSetup.textColorHex)
                put("iconColorHex", appSetup.iconColorHex)
                put("iconSizePercent", appSetup.iconSizePercent)
                put("isChatIconVisible", appSetup.isChatIconVisible)
                put("isChatIconFullyRemoved", appSetup.isChatIconFullyRemoved)
                put("appName", appSetup.appName)
                put("appLogoUrl", appSetup.appLogoUrl)
                put("appCoverUrl", appSetup.appCoverUrl)
                put("appDescription", appSetup.appDescription)
                put("supportPhone", appSetup.supportPhone)
                put("supportEmail", appSetup.supportEmail)
                put("shareUrl", appSetup.shareUrl)
                put("termsOfService", appSetup.termsOfService)
                put("privacyPolicy", appSetup.privacyPolicy)
                put("lastUpdateDate", appSetup.lastUpdateDate)
                put("loyaltyEnabled", appSetup.loyaltyEnabled)
                put("loyaltyPointValueYemeniRial", appSetup.loyaltyPointValueYemeniRial)
                put("pointsPerShare", appSetup.pointsPerShare)
                put("pointsNeededForCoupon", appSetup.pointsNeededForCoupon)
                put("isChatDisabledAll", appSetup.isChatDisabledAll)
                put("isChatDisabledProviders", appSetup.isChatDisabledProviders)
                put("isChatDisabledUsers", appSetup.isChatDisabledUsers)
                put("chatDisabledNotificationText", appSetup.chatDisabledNotificationText)
                put("aiAssistantEnabled", appSetup.aiAssistantEnabled)
                put("aiAssistantWelcomeMessage", appSetup.aiAssistantWelcomeMessage)
                put("voiceInputEnabled", appSetup.voiceInputEnabled)
                put("isUserRegistrationMandatory", appSetup.isUserRegistrationMandatory)
                put("defaultDispatchMethodIndex", appSetup.defaultDispatchMethodIndex)
            }
            editor.putString("app_setup", setupJson.toString())

            // Save Technicians
            val techArray = JSONArray()
            technicians.forEach { tech ->
                val trObj = JSONObject().apply {
                    put("id", tech.id)
                    put("name", tech.name)
                    put("phone", tech.phone)
                    put("region", tech.region)
                    put("addressDetail", tech.addressDetail)
                    put("hasShop", tech.hasShop)
                    put("shopAddress", tech.shopAddress)
                    put("specialty", tech.specialty)
                    put("rating", tech.rating.toDouble())
                    put("ratingCount", tech.ratingCount)
                    put("experienceYears", tech.experienceYears)
                    put("bio", tech.bio)
                    put("completedServices", tech.completedServices)
                    put("isAvailable24_7", tech.isAvailable24_7)
                    put("state", tech.state.name)
                    put("isVIP", tech.isVIP)
                    put("isVerified", tech.isVerified)
                    put("isRecommended", tech.isRecommended)
                    put("subscriptionPlan", tech.subscriptionPlan)
                    put("profilePhotoUrl", tech.profilePhotoUrl)
                    put("coverPhotoUrl", tech.coverPhotoUrl)
                }
                techArray.put(trObj)
            }
            editor.putString("app_technicians", techArray.toString())

            // Save Bookings
            val bookArr = JSONArray()
            bookings.forEach { bo ->
                val boObj = JSONObject().apply {
                    put("id", bo.id)
                    put("clientName", bo.clientName)
                    put("clientPhone", bo.clientPhone)
                    put("requestedService", bo.requestedService)
                    put("region", bo.region)
                    put("status", bo.status)
                    put("assignedTechId", bo.assignedTechId ?: "")
                    put("dateCreated", bo.dateCreated)
                    put("dateCompleted", bo.dateCompleted)
                    put("clientRating", bo.clientRating)
                    put("clientReview", bo.clientReview)
                }
                bookArr.put(boObj)
            }
            editor.putString("app_bookings", bookArr.toString())

            // Save Custom Fields
            val fieldArr = JSONArray()
            customFields.forEach { f ->
                val fObj = JSONObject().apply {
                    put("id", f.id)
                    put("label", f.label)
                    put("type", f.type)
                    put("options", JSONArray(f.options))
                    put("isMandatory", f.isMandatory)
                    put("isEnabled", f.isEnabled)
                    put("orderIndex", f.orderIndex)
                }
                fieldArr.put(fObj)
            }
            editor.putString("app_custom_fields", fieldArr.toString())

            // Save Categories
            val catArr = JSONArray()
            categoriesBySetup.forEach { c ->
                val cObj = JSONObject().apply {
                    put("id", c.id)
                    put("nameEn", c.nameEn)
                    put("nameAr", c.nameAr)
                    put("iconName", c.iconName)
                    put("isEnabled", c.isEnabled)
                    put("sortingOrder", c.sortingOrder)
                }
                catArr.put(cObj)
            }
            editor.putString("app_categories", catArr.toString())

            // Save Coupons
            val cpArr = JSONArray()
            coupons.forEach { c ->
                val cpObj = JSONObject().apply {
                    put("code", c.code)
                    put("discountPercent", c.discountPercent)
                    put("discountFixedValue", c.discountFixedValue)
                    put("totalLimit", c.totalLimit)
                    put("perUserLimit", c.perUserLimit)
                    put("expirationDate", c.expirationDate)
                    put("isEnabled", c.isEnabled)
                    put("usageCount", c.usageCount)
                }
                cpArr.put(cpObj)
            }
            editor.putString("app_coupons", cpArr.toString())

            // Save FAQ List
            val fqArr = JSONArray()
            faqList.forEach { q ->
                val qObj = JSONObject().apply {
                    put("id", q.id)
                    put("question", q.question)
                    put("answer", q.answer)
                    put("category", q.category)
                    put("isEnabled", q.isEnabled)
                }
                fqArr.put(qObj)
            }
            editor.putString("app_faqs", fqArr.toString())

            // Save Adverts
            val adArr = JSONArray()
            adverts.forEach { a ->
                val aObj = JSONObject().apply {
                    put("id", a.id)
                    put("title", a.title)
                    put("mediaUrl", a.mediaUrl)
                    put("mediaType", a.mediaType)
                    put("categoryId", a.categoryId)
                    put("durationSeconds", a.durationSeconds)
                    put("clickCount", a.clickCount)
                    put("viewCount", a.viewCount)
                    put("isActive", a.isActive)
                    put("displayOrder", a.displayOrder)
                }
                adArr.put(aObj)
            }
            editor.putString("app_adverts", adArr.toString())

            // Save Users Info
            val usrArr = JSONArray()
            clientUsers.forEach { u ->
                val uObj = JSONObject().apply {
                    put("id", u.id)
                    put("name", u.name)
                    put("phone", u.phone)
                    put("loyaltyPoints", u.loyaltyPoints)
                    put("registrationDate", u.registrationDate)
                    put("isBlocked", u.isBlocked)
                    put("registrationType", u.registrationType)
                }
                usrArr.put(uObj)
            }
            editor.putString("app_client_users", usrArr.toString())

            // Save Tech Channels / Messages
            val chArr = JSONArray()
            chatChannels.forEach { ch ->
                val chObj = JSONObject().apply {
                    put("userId", ch.userId)
                    put("userName", ch.userName)
                    put("userRole", ch.userRole)
                    put("isBlocked", ch.isBlocked)

                    val msgsArr = JSONArray()
                    ch.messages.forEach { m ->
                        val mObj = JSONObject().apply {
                            put("id", m.id)
                            put("senderId", m.senderId)
                            put("receiverId", m.receiverId)
                            put("message", m.message)
                            put("timestamp", m.timestamp)
                            put("senderType", m.senderType)
                            put("senderName", m.senderName)
                        }
                        msgsArr.put(mObj)
                    }
                    put("messages", msgsArr)
                }
                chArr.put(chObj)
            }
            editor.putString("app_chat_channels", chArr.toString())

            // Save Audit Log list
            val auditArr = JSONArray()
            auditLogs.take(50).forEach { l ->
                val lObj = JSONObject().apply {
                    put("id", l.id)
                    put("adminId", l.adminId)
                    put("action", l.action)
                    put("details", l.details)
                    put("timestamp", l.timestamp)
                    put("ipAddress", l.ipAddress)
                }
                auditArr.put(lObj)
            }
            editor.putString("app_audit_logs", auditArr.toString())

            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAllData() {
        try {
            val setupStr = prefs.getString("app_setup", "")
            if (setupStr.isNullOrBlank()) {
                initDefaultSeeds()
                return
            }

            // Load setup
            val setupObj = JSONObject(setupStr)
            appSetup = AppSetup(
                primaryColorHex = setupObj.optString("primaryColorHex", "#3B82F6"),
                secondaryColorHex = setupObj.optString("secondaryColorHex", "#10B981"),
                backgroundColorHex = setupObj.optString("backgroundColorHex", "#0D0D11"),
                textColorHex = setupObj.optString("textColorHex", "#FFFFFF"),
                iconColorHex = setupObj.optString("iconColorHex", "#3B82F6"),
                iconSizePercent = setupObj.optInt("iconSizePercent", 100),
                isChatIconVisible = setupObj.optBoolean("isChatIconVisible", true),
                isChatIconFullyRemoved = setupObj.optBoolean("isChatIconFullyRemoved", false),
                appName = setupObj.optString("appName", "كل خدمات اليمن"),
                appLogoUrl = setupObj.optString("appLogoUrl", ""),
                appCoverUrl = setupObj.optString("appCoverUrl", ""),
                appDescription = setupObj.optString("appDescription", ""),
                supportPhone = setupObj.optString("supportPhone", "+967770000000"),
                supportEmail = setupObj.optString("supportEmail", ""),
                shareUrl = setupObj.optString("shareUrl", ""),
                termsOfService = setupObj.optString("termsOfService", ""),
                privacyPolicy = setupObj.optString("privacyPolicy", ""),
                lastUpdateDate = setupObj.optString("lastUpdateDate", "2026-06-16"),
                loyaltyEnabled = setupObj.optBoolean("loyaltyEnabled", true),
                loyaltyPointValueYemeniRial = setupObj.optInt("loyaltyPointValueYemeniRial", 10),
                pointsPerShare = setupObj.optInt("pointsPerShare", 20),
                pointsNeededForCoupon = setupObj.optInt("pointsNeededForCoupon", 100),
                isChatDisabledAll = setupObj.optBoolean("isChatDisabledAll", false),
                isChatDisabledProviders = setupObj.optBoolean("isChatDisabledProviders", false),
                isChatDisabledUsers = setupObj.optBoolean("isChatDisabledUsers", false),
                chatDisabledNotificationText = setupObj.optString("chatDisabledNotificationText", ""),
                aiAssistantEnabled = setupObj.optBoolean("aiAssistantEnabled", true),
                aiAssistantWelcomeMessage = setupObj.optString("aiAssistantWelcomeMessage", ""),
                voiceInputEnabled = setupObj.optBoolean("voiceInputEnabled", true),
                isUserRegistrationMandatory = setupObj.optBoolean("isUserRegistrationMandatory", false),
                defaultDispatchMethodIndex = setupObj.optInt("defaultDispatchMethodIndex", 2)
            )

            // Technicians
            val techStr = prefs.getString("app_technicians", "[]")
            val techArr = JSONArray(techStr)
            val techList = mutableListOf<Technician>()
            for (i in 0 until techArr.length()) {
                val tObj = techArr.getJSONObject(i)
                techList.add(Technician(
                    id = tObj.getString("id"),
                    name = tObj.getString("name"),
                    phone = tObj.getString("phone"),
                    region = tObj.getString("region"),
                    addressDetail = tObj.optString("addressDetail", ""),
                    hasShop = tObj.optBoolean("hasShop", false),
                    shopAddress = tObj.optString("shopAddress", ""),
                    specialty = tObj.getString("specialty"),
                    rating = tObj.optDouble("rating", 5.0).toFloat(),
                    ratingCount = tObj.optInt("ratingCount", 0),
                    experienceYears = tObj.optInt("experienceYears", 3),
                    bio = tObj.optString("bio", ""),
                    completedServices = tObj.optInt("completedServices", 0),
                    isAvailable24_7 = tObj.optBoolean("isAvailable24_7", true),
                    state = TechnicianState.valueOf(tObj.optString("state", "ACTIVE")),
                    isVIP = tObj.optBoolean("isVIP", false),
                    isVerified = tObj.optBoolean("isVerified", false),
                    isRecommended = tObj.optBoolean("isRecommended", false),
                    subscriptionPlan = tObj.optString("subscriptionPlan", "Basic"),
                    profilePhotoUrl = tObj.optString("profilePhotoUrl", ""),
                    coverPhotoUrl = tObj.optString("coverPhotoUrl", "")
                ))
            }
            technicians = techList

            // Bookings
            val bookStr = prefs.getString("app_bookings", "[]")
            val bkArr = JSONArray(bookStr)
            val bkList = mutableListOf<Booking>()
            for (i in 0 until bkArr.length()) {
                val bObj = bkArr.getJSONObject(i)
                bkList.add(Booking(
                    id = bObj.getString("id"),
                    clientName = bObj.getString("clientName"),
                    clientPhone = bObj.getString("clientPhone"),
                    requestedService = bObj.getString("requestedService"),
                    region = bObj.getString("region"),
                    status = bObj.optString("status", "قيد الانتظار"),
                    assignedTechId = bObj.optString("assignedTechId", "").let { if (it.isEmpty()) null else it },
                    dateCreated = bObj.optString("dateCreated", ""),
                    dateCompleted = bObj.optString("dateCompleted", ""),
                    clientRating = bObj.optInt("clientRating", 0),
                    clientReview = bObj.optString("clientReview", "")
                ))
            }
            bookings = bkList

            // Custom Fields
            val cfStr = prefs.getString("app_custom_fields", "[]")
            val cfArr = JSONArray(cfStr)
            val cfList = mutableListOf<CustomField>()
            for (i in 0 until cfArr.length()) {
                val fObj = cfArr.getJSONObject(i)
                val optsArr = fObj.optJSONArray("options") ?: JSONArray()
                val optList = mutableListOf<String>()
                for (j in 0 until optsArr.length()) {
                    optList.add(optsArr.getString(j))
                }
                cfList.add(CustomField(
                    id = fObj.getString("id"),
                    label = fObj.getString("label"),
                    type = fObj.getString("type"),
                    options = optList,
                    isMandatory = fObj.optBoolean("isMandatory", false),
                    isEnabled = fObj.optBoolean("isEnabled", true),
                    orderIndex = fObj.optInt("orderIndex", 0)
                ))
            }
            customFields = cfList

            // Categories
            val catStr = prefs.getString("app_categories", "[]")
            val ctArr = JSONArray(catStr)
            val catList = mutableListOf<Category>()
            for (i in 0 until ctArr.length()) {
                val cObj = ctArr.getJSONObject(i)
                catList.add(Category(
                    id = cObj.getString("id"),
                    nameEn = cObj.getString("nameEn"),
                    nameAr = cObj.getString("nameAr"),
                    iconName = cObj.getString("iconName"),
                    isEnabled = cObj.optBoolean("isEnabled", true),
                    sortingOrder = cObj.optInt("sortingOrder", i)
                ))
            }
            categoriesBySetup = catList

            // Coupons
            val cpStr = prefs.getString("app_coupons", "[]")
            val cpArr = JSONArray(cpStr)
            val cpList = mutableListOf<Coupon>()
            for (i in 0 until cpArr.length()) {
                val cpObj = cpArr.getJSONObject(i)
                cpList.add(Coupon(
                    code = cpObj.getString("code"),
                    discountPercent = cpObj.optInt("discountPercent", 0),
                    discountFixedValue = cpObj.optInt("discountFixedValue", 0),
                    totalLimit = cpObj.optInt("totalLimit", 100),
                    perUserLimit = cpObj.optInt("perUserLimit", 1),
                    expirationDate = cpObj.optString("expirationDate", ""),
                    isEnabled = cpObj.optBoolean("isEnabled", true),
                    usageCount = cpObj.optInt("usageCount", 0)
                ))
            }
            coupons = cpList

            // FAQs
            val faqStr = prefs.getString("app_faqs", "[]")
            val fqArr = JSONArray(faqStr)
            val fqListTmp = mutableListOf<FAQ>()
            for (i in 0 until fqArr.length()) {
                val qObj = fqArr.getJSONObject(i)
                fqListTmp.add(FAQ(
                    id = qObj.getString("id"),
                    question = qObj.getString("question"),
                    answer = qObj.getString("answer"),
                    category = qObj.optString("category", "عام"),
                    isEnabled = qObj.optBoolean("isEnabled", true)
                ))
            }
            faqList = fqListTmp

            // Adverts
            val adStr = prefs.getString("app_adverts", "[]")
            val adArr = JSONArray(adStr)
            val adList = mutableListOf<Advert>()
            for (i in 0 until adArr.length()) {
                val aObj = adArr.getJSONObject(i)
                adList.add(Advert(
                    id = aObj.getString("id"),
                    title = aObj.getString("title"),
                    mediaUrl = aObj.getString("mediaUrl"),
                    mediaType = aObj.optString("mediaType", "IMAGE"),
                    categoryId = aObj.optString("categoryId", "الكل"),
                    durationSeconds = aObj.optInt("durationSeconds", 15),
                    clickCount = aObj.optInt("clickCount", 0),
                    viewCount = aObj.optInt("viewCount", 0),
                    isActive = aObj.optBoolean("isActive", true),
                    displayOrder = aObj.optInt("displayOrder", 1)
                ))
            }
            adverts = adList

            // Users
            val usrStr = prefs.getString("app_client_users", "[]")
            val usrArr = JSONArray(usrStr)
            val usrList = mutableListOf<ClientUser>()
            for (i in 0 until usrArr.length()) {
                val uObj = usrArr.getJSONObject(i)
                usrList.add(ClientUser(
                    id = uObj.getString("id"),
                    name = uObj.optString("name", ""),
                    phone = uObj.getString("phone"),
                    loyaltyPoints = uObj.optInt("loyaltyPoints", 0),
                    registrationDate = uObj.optString("registrationDate", ""),
                    isBlocked = uObj.optBoolean("isBlocked", false),
                    registrationType = uObj.optString("registrationType", "Visitor")
                ))
            }
            clientUsers = usrList

            // Chat channels
            val chStr = prefs.getString("app_chat_channels", "[]")
            val chArr = JSONArray(chStr)
            val chList = mutableListOf<ChatChannel>()
            for (i in 0 until chArr.length()) {
                val chObj = chArr.getJSONObject(i)
                val mArr = chObj.getJSONArray("messages")
                val msgList = mutableListOf<ChatMessage>()
                for (j in 0 until mArr.length()) {
                    val mObj = mArr.getJSONObject(j)
                    msgList.add(ChatMessage(
                        id = mObj.getString("id"),
                        senderId = mObj.getString("senderId"),
                        receiverId = mObj.getString("receiverId"),
                        message = mObj.getString("message"),
                        timestamp = mObj.getLong("timestamp"),
                        senderType = mObj.getString("senderType"),
                        senderName = mObj.optString("senderName", "")
                    ))
                }
                chList.add(ChatChannel(
                    userId = chObj.getString("userId"),
                    userName = chObj.getString("userName"),
                    userRole = chObj.optString("userRole", "User"),
                    messages = msgList,
                    isBlocked = chObj.optBoolean("isBlocked", false)
                ))
            }
            chatChannels = chList

            // Audit Logs
            val auditStr = prefs.getString("app_audit_logs", "[]")
            val auditArr = JSONArray(auditStr)
            val adtList = mutableListOf<AuditLog>()
            for (i in 0 until auditArr.length()) {
                val lObj = auditArr.getJSONObject(i)
                adtList.add(AuditLog(
                    id = lObj.getString("id"),
                    adminId = lObj.getString("adminId"),
                    action = lObj.getString("action"),
                    details = lObj.getString("details"),
                    timestamp = lObj.getString("timestamp"),
                    ipAddress = lObj.optString("ipAddress", "192.168.1.100")
                ))
            }
            auditLogs = adtList

        } catch (e: Exception) {
            e.printStackTrace()
            initDefaultSeeds()
        }
    }

    private fun initDefaultSeeds() {
        // Prepare beautiful categories for Yemen دليل
        categoriesBySetup = listOf(
            Category("1", "Plumbing", "سباكة", "Shower", true, 0),
            Category("2", "Electricity", "كهرباء", "Bolt", true, 1),
            Category("3", "AC Maintenance", "صيانة مكيفات", "Cloud", true, 2),
            Category("4", "Painting", "دهان وصباغة", "FormatPaint", true, 3),
            Category("5", "Blacksmith", "حدادة ألمنيوم", "Build", true, 4),
            Category("6", "Phone Repair", "جوالات وإلكترونيات", "Smartphone", true, 5),
            Category("7", "Tourism Services", "خدمات سياحية", "Map", true, 6)
        )

        // Providers
        technicians = listOf(
            Technician(
                name = "ماهر الوصابي",
                phone = "00967771234567",
                region = "صنعاء",
                addressDetail = "شارع ملحق حجر - الدائري",
                hasShop = true,
                shopAddress = "محل الوصابي للصيانة السريعة - جولة الرويشان",
                specialty = "سباكة",
                rating = 5.0f,
                ratingCount = 37,
                experienceYears = 12,
                bio = "خبرة فنية متراكمة أكثر من 12 عاماً في تأسيس وصيانة شبكات المياه والتدفئة تحت البلاط بكل جدارة وثبات بقرية حدة وصنعاء القديمة.",
                completedServices = 142,
                isAvailable24_7 = true,
                state = TechnicianState.ACTIVE,
                isVIP = true,
                isVerified = true,
                isRecommended = true,
                subscriptionPlan = "VIP",
                profilePhotoUrl = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150",
                coverPhotoUrl = "https://images.unsplash.com/photo-1504328345606-18bbc8c9d7d1?w=400"
            ),
            Technician(
                name = "محمد اليافعي",
                phone = "00967735566778",
                region = "عدن",
                addressDetail = "المنصورة - خلف ريمي",
                hasShop = false,
                specialty = "كهرباء",
                rating = 4.8f,
                ratingCount = 21,
                experienceYears = 8,
                bio = "متخصص في كشف تهريب الكهرباء، توزيع الأحمال للمحلات والفلل، وربط لوحات التحكم بالطاقة الشمسية.",
                completedServices = 89,
                isAvailable24_7 = false,
                state = TechnicianState.ACTIVE,
                isVIP = false,
                isVerified = true,
                isRecommended = true,
                subscriptionPlan = "Premium",
                profilePhotoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                coverPhotoUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=400"
            ),
            Technician(
                name = "الأستاذ خالد الوصابي",
                phone = "00967776451290",
                region = "إب",
                addressDetail = "شارع العدين - برج الأمل",
                hasShop = true,
                shopAddress = "شركة الوصابي الهندسية للمكيفات والأعطال",
                specialty = "صيانة مكيفات",
                rating = 4.9f,
                ratingCount = 48,
                experienceYears = 15,
                bio = "نحن رواد صيانة وتوصيل أجهزة التبريد والمكيفات المركزية والاسبليت في اللواء الأخضر منذ أكثر من عقد ونصف.",
                completedServices = 312,
                isAvailable24_7 = true,
                state = TechnicianState.ACTIVE,
                isVIP = true,
                isVerified = true,
                isRecommended = false,
                subscriptionPlan = "VIP",
                profilePhotoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150",
                coverPhotoUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=400"
            ),
            Technician(
                name = "سالم حضرمي",
                phone = "00967711223344",
                region = "حضرموت",
                addressDetail = "المكلا - كورنيش المحضار",
                hasShop = false,
                specialty = "خدمات سياحية",
                rating = 4.7f,
                ratingCount = 14,
                experienceYears = 6,
                bio = "مرشد وخدمات تنقّل سياحية وثقافية آمنة في وادي دوعن، تريم، شبام حضرموت الأثرية.",
                completedServices = 45,
                isAvailable24_7 = true,
                state = TechnicianState.ACTIVE,
                isVIP = false,
                isVerified = false,
                isRecommended = true,
                subscriptionPlan = "Basic",
                profilePhotoUrl = "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=150",
                coverPhotoUrl = "https://images.unsplash.com/photo-1527631746610-bca00a040d60?w=400"
            )
        )

        // Custom registration fields list for Form (Section 14)
        customFields = listOf(
            CustomField("f1", "رقم السجل التجاري", "NUMBER", isMandatory = false, orderIndex = 4),
            CustomField("f2", "صورة الهوية الوطنية (البطاقة الشخصية)", "IMAGE", isMandatory = true, orderIndex = 5),
            CustomField("f3", "تاريخ مزاولة المهنة الفعلي", "DATE", isMandatory = false, orderIndex = 6)
        )

        // Seed some User profiles
        clientUsers = listOf(
            ClientUser(id = "u1", name = "أبو ماجد الصنعاني", phone = "773010101", loyaltyPoints = 140, registrationDate = "2026-05-12", registrationType = "Registered"),
            ClientUser(id = "u2", name = "صالح العدني", phone = "733909090", loyaltyPoints = 40, registrationDate = "2026-06-01", registrationType = "Optional"),
            ClientUser(id = "u3", name = "أروى الحضرمية", phone = "711776655", loyaltyPoints = 210, registrationDate = "2026-04-18", registrationType = "Mandatory")
        )

        // Seed bookings
        bookings = listOf(
            Booking(
                clientName = "أبو ماجد الصنعاني",
                clientPhone = "773010101",
                requestedService = "سباكة",
                region = "صنعاء",
                status = "مكتمل",
                assignedTechId = technicians[0].id,
                dateCreated = "2026-06-10 09:12",
                dateCompleted = "2026-06-10 11:30",
                clientRating = 5,
                clientReview = "شغل سريع وممتاز جداً، ماهر محترم ويمتلك أدوات حديثة."
            ),
            Booking(
                clientName = "صالح العدني",
                clientPhone = "733909090",
                requestedService = "كهرباء",
                region = "عدن",
                status = "قيد التنفيذ",
                assignedTechId = technicians[1].id,
                dateCreated = "2026-06-15 14:45"
            ),
            Booking(
                clientName = "أروى الهيج",
                clientPhone = "711776655",
                requestedService = "صيانة مكيفات",
                region = "إب",
                status = "قيد الانتظار",
                dateCreated = "2026-06-16 11:20"
            )
        )

        // Seed simulated chat channels
        val sampleMsgs1 = listOf(
            ChatMessage(senderId = "tech_khalid", receiverId = "u_salem", message = "السلام عليكم، هل ترغب في غسيل الفلاتر أم شحن الفريون مع صيانة الكومبريسور؟", timestamp = 1781600000000L, senderType = "Tech", senderName = "الأستاذ خالد الوصابي"),
            ChatMessage(senderId = "u_salem", receiverId = "tech_khalid", message = "وعليكم السلام يا أستاذ خالد، غسيل الفلاتر ممتد لثلاث غرف والمكيف يحتاج فريون أمريكي لو سمحت.", timestamp = 1781600100000L, senderType = "User", senderName = "صالح المقبلي"),
            ChatMessage(senderId = "tech_khalid", receiverId = "u_salem", message = "تمام يا غالي، أنا في شارع العدين الآن وسأصل إليك خلال نصف ساعة للتنفيذ.", timestamp = 1781600200000L, senderType = "Tech", senderName = "الأستاذ خالد الوصابي")
        )
        val sampleMsgs2 = listOf(
            ChatMessage(senderId = "user", receiverId = "wam", message = "أريد فني صيانة في إب يمني وخبير مكيفات", timestamp = 1781600000000L, senderType = "User", senderName = "أنا"),
            ChatMessage(senderId = "wam", receiverId = "user", message = "أهلاً بك! لدينا الأستاذ خالد الوصابي في شارع العدين، خبير 15 عام صيانة مكيفات حاصل على 5 نجووم.", timestamp = 1781600050000L, senderType = "Admin", senderName = "المساعد WAM")
        )

        chatChannels = listOf(
            ChatChannel("u_salem", "صالح المقبلي (الأستاذ خالد الوصابي)", "Tech", sampleMsgs1),
            ChatChannel("wam_chat", "المساعد الذكي WAM", "User", sampleMsgs2)
        )

        faqList = listOf(
            FAQ(question = "كيف يمكنني طلب خدمة عبر الدليل؟", answer = "يمكنك الدخول إلى الصفحة الرئيسية، تصفح الفنيين أو فلترتهم حسب مدينتك وتخصصك، ثم الضغط على زر 'حجز فني' لتأكيد البيانات في ثانية واحدة.", isEnabled = true),
            FAQ(question = "ما هي نقاط الولاء المقررة؟", answer = "هي نقاط تكافئ مشاركتك للتطبيق مع عائلتك وأصدقائك في اليمن، وتعطيك كود خصم فوري بقيمة محددة لكل تصفية.", isEnabled = true)
        )

        adverts = listOf(
            Advert(
                title = "مؤسسة الوصابي اللامعة لصيانة مضخات المياه بصنعاء",
                mediaUrl = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=600",
                mediaType = "IMAGE",
                clickCount = 45,
                viewCount = 310,
                isActive = true,
                displayOrder = 1
            ),
            Advert(
                title = "فيديو تعريفي: جولات سياحية في سيئون وشبام حضرموت",
                mediaUrl = "https://assets.mixkit.co/videos/preview/mixkit-hand-holding-a-smartphone-with-a-vertical-video-41716-large.mp4", // mock vertical mp4
                mediaType = "VIDEO",
                clickCount = 12,
                viewCount = 195,
                isActive = true,
                displayOrder = 2
            )
        )

        coupons = listOf(
            Coupon("YEMEN2026", 20, isEnabled = true),
            Coupon("MAHER73", 0, discountFixedValue = 1500, isEnabled = true)
        )

        appSetup = AppSetup()
        saveToDisk()
    }

    // Single-click trigger helper for testing
    fun triggerManualSync() {
        viewModelScope.launch {
            syncInProgress = true
            delay(2000)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            lastSyncTime = sdf.format(Date())
            lastSyncResult = "ناجحة بالكامل (Dual-Way)"
            syncInProgress = false
            saveToDisk()
            addAudit("المزامنة", "يدوي", "تمت المزامنة بنجاح مع المزود الخلفي")
        }
    }
}
