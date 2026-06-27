package com.Serviseyem

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.Serviseyem.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel : ViewModel() {
    
    private var firestore: FirebaseFirestore? = null
    
    // State Flows
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers: StateFlow<List<Provider>> = _providers.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()
    
    private val _banners = MutableStateFlow<List<AppBanner>>(emptyList())
    val banners: StateFlow<List<AppBanner>> = _banners.asStateFlow()
    
    val appInstallTime = MutableStateFlow<Long>(0L)
    
    private val _bookings = MutableStateFlow<List<BookingRequest>>(emptyList())
    val bookings: StateFlow<List<BookingRequest>> = _bookings.asStateFlow()
    
    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _pendingRequests = MutableStateFlow<List<Provider>>(emptyList())
    val pendingRequests: StateFlow<List<Provider>> = _pendingRequests.asStateFlow()
    
    private val _adminAccounts = MutableStateFlow<List<AdminAccount>>(listOf(
        AdminAccount(ADMIN_USERNAME, ADMIN_PASSWORD, true, true, true, true, true, true)
    ))
    val adminAccounts: StateFlow<List<AdminAccount>> = _adminAccounts.asStateFlow()
    
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()
    
    val loggedInUsername = MutableStateFlow("")
    val currentUserId = MutableStateFlow<String?>(null)
    val currentUserRole = MutableStateFlow("guest")
    val showRegistrationDialog = MutableStateFlow(false)

    fun completeRegistration(name: String, phone: String, city: String) {
        val uid = "user_${System.currentTimeMillis()}"
        currentUserId.value = uid
        currentUserRole.value = "user"
        showRegistrationDialog.value = false
    }

    private val _geminiMessages = MutableStateFlow<List<Pair<String, Boolean>>>(listOf(
        Pair("مرحباً بك يا غالي! أنا المساعد الذكي لدليل خدمات اليمن 🇾🇪. كيف يمكنني مساعدتك اليوم؟", false)
    ))
    val geminiMessages: StateFlow<List<Pair<String, Boolean>>> = _geminiMessages.asStateFlow()

    fun askGemini(prompt: String) {
        _geminiMessages.value = _geminiMessages.value + Pair(prompt, true)
        viewModelScope.launch {
            try {
                // Get API key from settings first, then check BuildConfig
                val apiKeyFromSettings = _settings.value.geminiApiKey
                val apiKey = if (apiKeyFromSettings.isNotBlank()) apiKeyFromSettings else BuildConfig.GEMINI_API_KEY
                
                if (apiKey.isBlank()) {
                    delay(1000)
                    val localReply = getLocalYemeniResponse(prompt)
                    _geminiMessages.value = _geminiMessages.value + Pair(localReply, false)
                    return@launch
                }
                
                val reply = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    
                    val requestJson = buildJsonObject {
                        putJsonArray("contents") {
                            addJsonObject {
                                putJsonArray("parts") {
                                    addJsonObject {
                                        put("text", "أنت مساعد ذكي ودود جداً وخبير بمنصة 'دليل كل خدمات اليمن'. أجب بلهجة يمنية دافئة ومحترمة وواضحة جداً وتناسب المستخدمين اليمنيين. السؤال أو الرسالة هي: $prompt")
                                    }
                                }
                            }
                        }
                    }
                    
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = requestJson.toString().toRequestBody(mediaType)
                    
                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                        .post(body)
                        .build()
                    
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            val json = Json { ignoreUnknownKeys = true }
                            val element = json.parseToJsonElement(responseBody).jsonObject
                            val candidates = element["candidates"]?.jsonArray
                            val firstCandidate = candidates?.getOrNull(0)?.jsonObject
                            val contentObj = firstCandidate?.get("content")?.jsonObject
                            val parts = contentObj?.get("parts")?.jsonArray
                            parts?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
                        } else {
                            null
                        }
                    }
                }
                
                if (!reply.isNullOrBlank()) {
                    _geminiMessages.value = _geminiMessages.value + Pair(reply, false)
                } else {
                    _geminiMessages.value = _geminiMessages.value + Pair("عذراً، لم أستطع الحصول على إجابة ذكية الآن. هل تود استخدام الدعم المباشر؟", false)
                }
            } catch (e: Exception) {
                val localReply = getLocalYemeniResponse(prompt)
                _geminiMessages.value = _geminiMessages.value + Pair(localReply, false)
            }
        }
    }

    private fun getLocalYemeniResponse(prompt: String): String {
        return when {
            prompt.contains("سباك") || prompt.contains("صباك") -> {
                "أهلاً بك! لدينا العديد من السباكين المتميزين المسجلين في دليلنا في كافة محافظات اليمن (مثل المعلم أحمد صالح في صنعاء والمعلم سعيد في عدن). يمكنك اختيار قسم 'السباكة والشبكات 🚰' من القائمة الرئيسية، والضغط على زر 'حجز موعد' لإرسال طلبك فوراً بدون وسيط!"
            }
            prompt.contains("صيانة") || prompt.contains("تكييف") -> {
                "تتوفر خدمات صيانة التكييف والأجهزة المنزلية تحت قسم 'صيانة الأجهزة 🖥️'. نوصي بالتواصل مع المهندس عادل اليماني لإصلاح التكييف والغسالات بكفاءة عالية وضمان معتمد."
            }
            prompt.contains("كيف") || prompt.contains("حجز") -> {
                "طريقة الحجز بسيطة جداً يا غالي! تصفح قائمة الفنيين في الصفحة الرئيسية، ثم اضغط على زر '📅 احجز الآن' الموجود تحت اسم الفني المطلوب. املأ الاسم ورقم هاتفك والمنطقة ليتواصل معك الفني مباشرة لتأكيد اللقاء."
            }
            prompt.contains("أسعار") || prompt.contains("سعر") -> {
                "تقديم الخدمات وتحديد الأسعار يتم بالاتفاق المباشر والشفاف بينك وبين الفني دون أي عمولات للمنصة. ننصحك باستخدام ميزة 'المحادثة الفورية' للتحدث مع الفني والاتفاق على التكلفة التقريبية قبل بدء العمل."
            }
            else -> {
                "حياك الله يا غالي! دليل خدمات اليمن الذكي سحابياً يوفر لك أفضل الفنيين لخدمات السباكة، الكهرباء، التجارة، والمقاولات في كافة أنحاء اليمن. يمكنك التصفح والحجز مباشرة أو الاتصال بنا لأي استفسار."
            }
        }
    }
    
    private var settingsListener: ListenerRegistration? = null
    
    init {
        setupFirebase()
    }
    
    private fun setupFirebase() {
        firestore = FirebaseFirestore.getInstance()
        setupFirestoreListeners()
    }
    
    private fun setupFirestoreListeners() {
        try {
            // الاستماع للإعدادات
            firestore?.collection("settings")?.document("global")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val settings = snapshot.toObject(AppSettings::class.java) ?: AppSettings()
                            _settings.value = settings
                            
                            // تحديث الألوان
                            try {
                                AppTheme.primaryRed = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(settings.primaryColorHex))
                                AppTheme.accentGold = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(settings.accentColorHex))
                                AppTheme.darkBg = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(settings.bgColorHex))
                                AppTheme.surfaceDark = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(settings.surfaceColorHex))
                            } catch (e: Exception) {}
                        } catch (e: Exception) {}
                    }
                }
            
            // الاستماع لمقدمي الخدمات
            firestore?.collection("providers")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Provider::class.java)
                        _providers.value = list
                        
                        // تحديث تلقائي لدور المستخدم الحالي إذا تم قبوله كمقدم خدمة
                        val uid = currentUserId.value
                        if (uid != null && uid.isNotBlank()) {
                            val isApprovedProvider = list.any { it.deviceId == uid || it.id == uid }
                            if (isApprovedProvider && currentUserRole.value == "user") {
                                currentUserRole.value = "provider"
                                try {
                                    userPrefs?.edit()?.apply {
                                        putString("saved_role", "provider")
                                        apply()
                                    }
                                } catch (e: Exception) {}
                            }
                        }
                    }
                }
            
            // الاستماع للفئات
            firestore?.collection("categories")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Category::class.java)
                        _categories.value = list.sortedBy { it.order }
                    }
                }
            
            // الاستماع للمدن
            firestore?.collection("cities")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(City::class.java)
                        _cities.value = list
                    }
                }

            // الاستماع للبنرات
            firestore?.collection("banners")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(AppBanner::class.java)
                        _banners.value = list
                    }
                }
            
            // الاستماع للحجوزات
            firestore?.collection("bookings")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(BookingRequest::class.java)
                        _bookings.value = list.sortedByDescending { it.timestamp }
                    }
                }
            
            // الاستماع للمحادثات
            firestore?.collection("chats")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(ChatRoom::class.java)
                        _chatRooms.value = list.sortedByDescending { it.lastMessageTime }
                    }
                }
            
            // الاستماع لرسائل المحادثات
            firestore?.collection("messages")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(ChatMessage::class.java)
                        _chatMessages.value = list.sortedBy { it.timestamp }
                    }
                }
            
            // الاستماع للإشعارات
            firestore?.collection("notifications")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Notification::class.java)
                        _notifications.value = list.sortedByDescending { it.timestamp }
                    }
                }
            
            // الاستماع للطلبات المعلقة
            firestore?.collection("pending_requests")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Provider::class.java)
                        _pendingRequests.value = list
                    }
                }
            
        } catch (e: Exception) {
            // No local storage fallback
        }
    }
    
    private var adminPrefs: android.content.SharedPreferences? = null
    private var userPrefs: android.content.SharedPreferences? = null
    
    fun initCache(context: android.content.Context) {
        try {
            adminPrefs = context.getSharedPreferences("admin_session", android.content.Context.MODE_PRIVATE)
            val savedAdmin = adminPrefs?.getString("saved_admin_username", null)
            val isSaved = adminPrefs?.getBoolean("is_admin_saved", false) ?: false
            if (isSaved && savedAdmin != null) {
                _isAdminLoggedIn.value = true
                loggedInUsername.value = savedAdmin
                currentUserRole.value = "admin"
            }
            
            userPrefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            val savedUid = userPrefs?.getString("saved_uid", null)
            val savedRole = userPrefs?.getString("saved_role", "user") ?: "user"
            
            var installTime = userPrefs?.getLong("app_install_time", 0L) ?: 0L
            if (installTime == 0L) {
                installTime = System.currentTimeMillis()
                userPrefs?.edit()?.putLong("app_install_time", installTime)?.apply()
            }
            appInstallTime.value = installTime
            
            if (savedUid != null) {
                currentUserId.value = savedUid
                currentUserRole.value = savedRole
            } else {
                val newUid = "user_${System.currentTimeMillis()}"
                currentUserId.value = newUid
                currentUserRole.value = "user"
                userPrefs?.edit()?.apply {
                    putString("saved_uid", newUid)
                    putString("saved_role", "user")
                    putLong("app_install_time", installTime)
                    apply()
                }
            }
        } catch (e: Exception) {}
    }
    
    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        try {
            firestore?.collection("settings")?.document("global")?.set(newSettings)
        } catch (e: Exception) {}
    }
    
    fun checkAdminLogin(username: String, password: String, remember: Boolean = false): Boolean {
        val matched = _adminAccounts.value.any { 
            it.username == username && it.passwordHash == password 
        } || (username == ADMIN_USERNAME && password == ADMIN_PASSWORD)
        
        if (matched) {
            _isAdminLoggedIn.value = true
            loggedInUsername.value = username
            currentUserRole.value = "admin"
            if (remember) {
                try {
                    adminPrefs?.edit()?.apply {
                        putBoolean("is_admin_saved", true)
                        putString("saved_admin_username", username)
                        apply()
                    }
                } catch (e: Exception) {}
            }
        }
        return matched
    }
    
    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        loggedInUsername.value = ""
        val originalRole = userPrefs?.getString("saved_role", "user") ?: "user"
        currentUserRole.value = originalRole
        try {
            adminPrefs?.edit()?.apply {
                putBoolean("is_admin_saved", false)
                putString("saved_admin_username", null)
                apply()
            }
        } catch (e: Exception) {}
    }
    
    fun addBooking(booking: BookingRequest) {
        _bookings.value = _bookings.value + booking
        try {
            firestore?.collection("bookings")?.document(booking.id)?.set(booking)
        } catch (e: Exception) {}
    }
    
    fun updateBooking(booking: BookingRequest) {
        _bookings.value = _bookings.value.map { if (it.id == booking.id) booking else it }
        try {
            firestore?.collection("bookings")?.document(booking.id)?.set(booking)
        } catch (e: Exception) {}
    }
    
    fun deleteBooking(id: String) {
        _bookings.value = _bookings.value.filter { it.id != id }
        try {
            firestore?.collection("bookings")?.document(id)?.delete()
        } catch (e: Exception) {}
    }
    
    fun sendMessage(chatId: String, message: ChatMessage) {
        _chatMessages.value = _chatMessages.value + message
        try {
            firestore?.collection("messages")?.document(message.id)?.set(message)
            // تحديث آخر رسالة في غرفة المحادثة
            val chatRoom = _chatRooms.value.find { it.id == chatId }
            chatRoom?.let {
                val updated = it.copy(
                    lastMessage = message.message,
                    lastMessageTime = message.timestamp
                )
                _chatRooms.value = _chatRooms.value.map { if (it.id == chatId) updated else it }
                firestore?.collection("chats")?.document(chatId)?.set(updated)
            }
        } catch (e: Exception) {}
    }
    
    fun createChatRoom(room: ChatRoom) {
        _chatRooms.value = _chatRooms.value + room
        try {
            firestore?.collection("chats")?.document(room.id)?.set(room)
        } catch (e: Exception) {}
    }
    
    fun deleteChatRoom(id: String) {
        _chatRooms.value = _chatRooms.value.filter { it.id != id }
        _chatMessages.value = _chatMessages.value.filter { it.chatId != id }
        try {
            firestore?.collection("chats")?.document(id)?.delete()
            firestore?.collection("messages")?.whereEqualTo("chatId", id)?.get()
                ?.addOnSuccessListener { snapshot ->
                    snapshot?.documents?.forEach { doc ->
                        doc.reference.delete()
                    }
                }
        } catch (e: Exception) {}
    }
    
    fun deleteMessage(id: String) {
        _chatMessages.value = _chatMessages.value.filter { it.id != id }
        try {
            firestore?.collection("messages")?.document(id)?.delete()
        } catch (e: Exception) {}
    }
    
    fun addNotification(notification: Notification) {
        _notifications.value = _notifications.value + notification
        try {
            firestore?.collection("notifications")?.document(notification.id)?.set(notification)
        } catch (e: Exception) {}
    }
    
    fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
        try {
            firestore?.collection("notifications")?.document(id)?.delete()
        } catch (e: Exception) {}
    }
    
    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map { 
            if (it.id == id) it.copy(isRead = true) else it 
        }
        try {
            firestore?.collection("notifications")?.document(id)?.update("isRead", true)
        } catch (e: Exception) {}
    }
    
    fun clearAllNotifications() {
        _notifications.value = emptyList()
        try {
            firestore?.collection("notifications")?.get()?.addOnSuccessListener { snapshot ->
                snapshot?.documents?.forEach { doc ->
                    doc.reference.delete()
                }
            }
        } catch (e: Exception) {}
    }
    
    fun addPendingRequest(provider: Provider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentList = _pendingRequests.value.filter { it.deviceId != provider.deviceId && it.id != provider.id }
        _pendingRequests.value = currentList + provider
        try {
            firestore?.collection("pending_requests")?.document(provider.id)?.set(provider)
                ?.addOnSuccessListener {
                    onSuccess()
                    
                    // إرسال إشعار فوري للأدمن والمشرفين بوجود طلب انضمام جديد
                    val adminNotification = Notification(
                        id = "notif_${System.currentTimeMillis()}_admin",
                        title = "طلب انضمام فني جديد 📥",
                        body = "قدم الفني ${provider.name} طلباً للانضمام في مدينة ${provider.city}، منطقة ${provider.area}.",
                        targetUserId = "",
                        targetRole = "admins",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "info"
                    )
                    firestore?.collection("notifications")?.document(adminNotification.id)?.set(adminNotification)
                }
                ?.addOnFailureListener {
                    _pendingRequests.value = _pendingRequests.value.filter { it.id != provider.id }
                    onFailure(it)
                }
        } catch (e: Exception) {
            _pendingRequests.value = _pendingRequests.value.filter { it.id != provider.id }
            onFailure(e)
        }
    }

    fun addProvider(provider: Provider) {
        _providers.value = _providers.value + provider
        try {
            firestore?.collection("providers")?.document(provider.id)?.set(provider)
        } catch (e: Exception) {}
    }
    
    fun updateProvider(provider: Provider) {
        _providers.value = _providers.value.map { if (it.id == provider.id) provider else it }
        try {
            firestore?.collection("providers")?.document(provider.id)?.set(provider)
        } catch (e: Exception) {}
    }
    
    fun deleteProvider(id: String) {
        _providers.value = _providers.value.filter { it.id != id }
        try {
            firestore?.collection("providers")?.document(id)?.delete()
        } catch (e: Exception) {}
    }
    
    fun addCategory(category: Category) {
        _categories.value = _categories.value + category
        try {
            firestore?.collection("categories")?.document(category.id)?.set(category)
        } catch (e: Exception) {}
    }
    
    fun updateCategory(category: Category) {
        _categories.value = _categories.value.map { if (it.id == category.id) category else it }
        try {
            firestore?.collection("categories")?.document(category.id)?.set(category)
        } catch (e: Exception) {}
    }
    
    fun deleteCategory(id: String) {
        _categories.value = _categories.value.filter { it.id != id }
        try {
            firestore?.collection("categories")?.document(id)?.delete()
        } catch (e: Exception) {}
    }
    
    fun addCity(city: City) {
        _cities.value = _cities.value + city
        try {
            firestore?.collection("cities")?.document(city.id)?.set(city)
        } catch (e: Exception) {}
    }
    
    fun deleteCity(id: String) {
        _cities.value = _cities.value.filter { it.id != id }
        try {
            firestore?.collection("cities")?.document(id)?.delete()
        } catch (e: Exception) {}
    }

    fun addBanner(banner: AppBanner) {
        _banners.value = _banners.value + banner
        try {
            firestore?.collection("banners")?.document(banner.id)?.set(banner)
        } catch (e: Exception) {}
    }

    fun updateBanner(banner: AppBanner) {
        _banners.value = _banners.value.map { if (it.id == banner.id) banner else it }
        try {
            firestore?.collection("banners")?.document(banner.id)?.set(banner)
        } catch (e: Exception) {}
    }

    fun deleteBanner(id: String) {
        _banners.value = _banners.value.filter { it.id != id }
        try {
            firestore?.collection("banners")?.document(id)?.delete()
        } catch (e: Exception) {}
    }
    
    fun approvePendingRequest(provider: Provider) {
        val approvedProvider = provider.copy(isVerified = true, verified = true)
        _pendingRequests.value = _pendingRequests.value.filter { it.id != approvedProvider.id }
        _providers.value = _providers.value + approvedProvider
        try {
            firestore?.collection("pending_requests")?.document(approvedProvider.id)?.delete()
            firestore?.collection("providers")?.document(approvedProvider.id)?.set(approvedProvider)
            
            // إرسال إشعار فوري لمقدم الخدمة بأنه تم قبوله
            if (approvedProvider.deviceId.isNotBlank()) {
                val notification = Notification(
                    id = "notif_${System.currentTimeMillis()}",
                    title = "تهانينا! تم قبول طلب انضمامك 🛡️",
                    body = "مرحباً ${approvedProvider.name}، لقد تم قبول طلب انضمامك كمهني معتمد في ${approvedProvider.city} ونشر حسابك بنجاح.",
                    targetUserId = approvedProvider.deviceId,
                    targetRole = "providers",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    type = "success"
                )
                firestore?.collection("notifications")?.document(notification.id)?.set(notification)
            }
        } catch (e: Exception) {}
    }
    
    fun rejectPendingRequest(id: String) {
        val provider = _pendingRequests.value.find { it.id == id }
        _pendingRequests.value = _pendingRequests.value.filter { it.id != id }
        try {
            firestore?.collection("pending_requests")?.document(id)?.delete()
            
            // إرسال إشعار فوري لمقدم الخدمة بالرفض للتوضيح
            if (provider != null && provider.deviceId.isNotBlank()) {
                val notification = Notification(
                    id = "notif_${System.currentTimeMillis()}",
                    title = "تحديث طلب الانضمام ⚠️",
                    body = "مرحباً ${provider.name}، نعتذر منك، لقد تم رفض طلب انضمامك للمنصة لعدم استيفاء الشروط المطلوبة.",
                    targetUserId = provider.deviceId,
                    targetRole = "users",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    type = "warning"
                )
                firestore?.collection("notifications")?.document(notification.id)?.set(notification)
            }
        } catch (e: Exception) {}
    }
    
    fun addAdminAccount(account: AdminAccount) {
        _adminAccounts.value = _adminAccounts.value + account
        try {
            firestore?.collection("admins")?.document(account.username)?.set(account)
        } catch (e: Exception) {}
    }
    
    fun updateAdminAccount(account: AdminAccount) {
        _adminAccounts.value = _adminAccounts.value.map { if (it.username == account.username) account else it }
        try {
            firestore?.collection("admins")?.document(account.username)?.set(account)
        } catch (e: Exception) {}
    }
    
    fun deleteAdminAccount(username: String) {
        _adminAccounts.value = _adminAccounts.value.filter { it.username != username }
        try {
            firestore?.collection("admins")?.document(username)?.delete()
        } catch (e: Exception) {}
    }
    
    fun purgeAllData(password: String): Boolean {
        if (password != ADMIN_PASSWORD) return false
        
        viewModelScope.launch {
            try {
                val collections = listOf(
                    "providers", "categories", "cities", "bookings",
                    "chats", "messages", "notifications", "pending_requests", "banners"
                )
                
                collections.forEach { collection ->
                    val snapshot = firestore?.collection(collection)?.get()?.await()
                    snapshot?.documents?.forEach { doc ->
                        doc.reference.delete().await()
                    }
                }
                
                // إعادة تعيين البيانات المحلية
                _providers.value = emptyList()
                _categories.value = emptyList()
                _cities.value = emptyList()
                _bookings.value = emptyList()
                _chatRooms.value = emptyList()
                _chatMessages.value = emptyList()
                _notifications.value = emptyList()
                _pendingRequests.value = emptyList()
                _banners.value = emptyList()
                
            } catch (e: Exception) {}
        }
        
        return true
    }

    fun purgeSelectedData(
        password: String,
        categories: Boolean,
        providers: Boolean,
        bookings: Boolean,
        chats: Boolean,
        banners: Boolean,
        notifications: Boolean,
        cities: Boolean
    ): Boolean {
        if (password != ADMIN_PASSWORD) return false
        
        viewModelScope.launch {
            try {
                val listToPurge = mutableListOf<String>()
                if (providers) {
                    listToPurge.add("providers")
                    listToPurge.add("pending_requests")
                }
                if (categories) listToPurge.add("categories")
                if (cities) listToPurge.add("cities")
                if (bookings) listToPurge.add("bookings")
                if (chats) {
                    listToPurge.add("chats")
                    listToPurge.add("messages")
                }
                if (notifications) listToPurge.add("notifications")
                if (banners) listToPurge.add("banners")
                
                listToPurge.forEach { collectionName ->
                    val snapshot = firestore?.collection(collectionName)?.get()?.await()
                    snapshot?.documents?.forEach { doc ->
                        doc.reference.delete().await()
                    }
                }
                
                // Reset local states
                if (providers) {
                    _providers.value = emptyList()
                    _pendingRequests.value = emptyList()
                }
                if (categories) _categories.value = emptyList()
                if (cities) _cities.value = emptyList()
                if (bookings) _bookings.value = emptyList()
                if (chats) {
                    _chatRooms.value = emptyList()
                    _chatMessages.value = emptyList()
                }
                if (notifications) _notifications.value = emptyList()
                if (banners) _banners.value = emptyList()
                
            } catch (e: Exception) {}
        }
        return true
    }

    fun backupSelectedData(
        categories: Boolean,
        providers: Boolean,
        bookings: Boolean,
        chats: Boolean,
        banners: Boolean,
        notifications: Boolean,
        cities: Boolean
    ): String {
        val backupObj = JSONObject()
        try {
            if (providers) {
                val providersArray = JSONArray()
                _providers.value.forEach { provider ->
                    val pObj = JSONObject().apply {
                        put("id", provider.id)
                        put("name", provider.name)
                        put("category", provider.category)
                        put("city", provider.city)
                        put("phone", provider.phone)
                        put("description", provider.description)
                        put("area", provider.area)
                        put("rating", provider.rating)
                        put("isVerified", provider.isVerified)
                        put("isSubscribed", provider.isSubscribed)
                        put("isPinned", provider.isPinned)
                        put("isRecommended", provider.isRecommended)
                    }
                    providersArray.put(pObj)
                }
                backupObj.put("providers", providersArray)
            }
            
            if (categories) {
                val catsArray = JSONArray()
                _categories.value.forEach { cat ->
                    val cObj = JSONObject().apply {
                        put("id", cat.id)
                        put("nameAr", cat.nameAr)
                        put("nameEn", cat.nameEn)
                        put("iconUrl", cat.iconUrl)
                    }
                    catsArray.put(cObj)
                }
                backupObj.put("categories", catsArray)
            }
            
            if (cities) {
                val citiesArray = JSONArray()
                _cities.value.forEach { city ->
                    val cObj = JSONObject().apply {
                        put("id", city.id)
                        put("nameAr", city.nameAr)
                        put("nameEn", city.nameEn)
                    }
                    citiesArray.put(cObj)
                }
                backupObj.put("cities", citiesArray)
            }
            
            if (bookings) {
                val bArray = JSONArray()
                _bookings.value.forEach { b ->
                    val bObj = JSONObject().apply {
                        put("id", b.id)
                        put("providerId", b.providerId)
                        put("userId", b.userId)
                        put("customerName", b.userName)
                        put("customerPhone", b.phoneNumber)
                        put("residenceArea", b.residenceArea)
                        put("preferredTime", b.preferredTime)
                        put("description", b.description)
                        put("status", b.status)
                        put("timestamp", b.timestamp)
                    }
                    bArray.put(bObj)
                }
                backupObj.put("bookings", bArray)
            }
            
            if (chats) {
                val chatsArray = JSONArray()
                _chatRooms.value.forEach { room ->
                    val rObj = JSONObject().apply {
                        put("id", room.id)
                        put("lastMessage", room.lastMessage)
                        put("lastMessageTime", room.lastMessageTime)
                        put("isActive", room.isActive)
                    }
                    chatsArray.put(rObj)
                }
                backupObj.put("chats", chatsArray)
            }
            
            if (notifications) {
                val notifsArray = JSONArray()
                _notifications.value.forEach { n ->
                    val nObj = JSONObject().apply {
                        put("id", n.id)
                        put("title", n.title)
                        put("body", n.body)
                        put("targetUserId", n.targetUserId)
                        put("targetRole", n.targetRole)
                        put("timestamp", n.timestamp)
                        put("isRead", n.isRead)
                        put("type", n.type)
                    }
                    notifsArray.put(nObj)
                }
                backupObj.put("notifications", notifsArray)
            }
            
            if (banners) {
                val bArray = JSONArray()
                _banners.value.forEach { b ->
                    val bObj = JSONObject().apply {
                        put("id", b.id)
                        put("imageUrl", b.mediaUrl)
                        put("clickUrl", b.linkUrl)
                        put("title", b.title)
                    }
                    bArray.put(bObj)
                }
                backupObj.put("banners", bArray)
            }
        } catch (e: Exception) {}
        
        return backupObj.toString(4)
    }
}
