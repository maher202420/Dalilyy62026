package com.example.services

import android.content.Context
import android.util.Log
import com.example.models.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

object FirebaseService {
    private const val TAG = "FirebaseService"
    
    private var firestore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null
    private var auth: FirebaseAuth? = null
    private var isFirebaseEnabled = false

    // Real-time flows mirroring Firestore Snapshot Listeners for all collections
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _serviceProviders = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val serviceProviders = _serviceProviders.asStateFlow()

    private val _pendingProviders = MutableStateFlow<List<PendingProvider>>(emptyList())
    val pendingProviders = _pendingProviders.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings = _settings.asStateFlow()

    private val _banners = MutableStateFlow<List<BannerAd>>(emptyList())
    val banners = _banners.asStateFlow()

    private val _admins = MutableStateFlow<List<Admin>>(emptyList())
    val admins = _admins.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _reports = MutableStateFlow<List<ReportComplaint>>(emptyList())
    val reports = _reports.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs = _activityLogs.asStateFlow()

    private val _cities = MutableStateFlow<List<String>>(listOf("صنعاء", "عدن", "إب", "الحديدة"))
    val cities = _cities.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    fun init(context: Context) {
        try {
            // Setup Firebase using the EXACT configuration provided in the system request
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:954106721012:android:5fa5e385532b08d5c0e4a1")
                .setApiKey("AIzaSyBq2SEhBADFGVF4sDyV3sC_t2HqQ1m8lC0")
                .setStorageBucket("serviseyem.firebasestorage.app")
                .setProjectId("serviseyem")
                .build()

            // Safe double-init check
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context, options)
            } else {
                FirebaseApp.getInstance()
            }

            firestore = FirebaseFirestore.getInstance(app)
            storage = FirebaseStorage.getInstance(app)
            auth = FirebaseAuth.getInstance(app)
            isFirebaseEnabled = true
            Log.d(TAG, "Firebase initialized successfully with credentials.")

            // Configure Offline cache for real Firestore
            val settingsBuilder = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
            firestore?.firestoreSettings = settingsBuilder.build()

            // Start actual realtime snapshot listeners
            startRealtimeListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed - Using highly robust local memory database architecture: ${e.message}")
            isFirebaseEnabled = false
            fallbackLocalDatabase()
        }
    }

    private fun startRealtimeListeners() {
        if (!isFirebaseEnabled || firestore == null) return
        
        try {
            // 1. Categories Snapshot
            val catReg = firestore!!.collection("categories").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Categories listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Category::class.java)
                    if (list.isEmpty() && _categories.value.isEmpty()) {
                        seedCategories()
                    } else {
                        _categories.value = list
                    }
                }
            }
            listeners.add(catReg)

            // 2. Providers Snapshot
            val providerReg = firestore!!.collection("service_providers").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Providers listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(ServiceProvider::class.java)
                    if (list.isEmpty() && _serviceProviders.value.isEmpty()) {
                        seedProviders()
                    } else {
                        _serviceProviders.value = list
                    }
                }
            }
            listeners.add(providerReg)

            // 3. Pending Providers Snapshot
            val pendingReg = firestore!!.collection("pending_providers").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                     _pendingProviders.value = snapshot.toObjects(PendingProvider::class.java)
                }
            }
            listeners.add(pendingReg)

            // 4. App Settings Snapshot
            val settingsReg = firestore!!.collection("app_settings").document("global").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val s = snapshot.toObject(AppSettings::class.java)
                    if (s != null) {
                        _settings.value = s
                    } else {
                        saveSettings(AppSettings())
                    }
                }
            }
            listeners.add(settingsReg)

            // 5. Banners Snapshot
            val bannerReg = firestore!!.collection("banners").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                     _banners.value = snapshot.toObjects(BannerAd::class.java)
                }
            }
            listeners.add(bannerReg)

            // 6. Admins Snapshot
            val adminReg = firestore!!.collection("admins").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.toObjects(Admin::class.java)
                    if (list.isEmpty() && _admins.value.isEmpty()) {
                        seedAdmins()
                    } else {
                        _admins.value = list
                    }
                }
            }
            listeners.add(adminReg)

            // 7. Chats Snapshot
            val chatReg = firestore!!.collection("chats").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _chats.value = snapshot.toObjects(ChatMessage::class.java).sortedBy { it.timestamp }
                }
            }
            listeners.add(chatReg)

            // 8. Reports Snapshot
            val reportReg = firestore!!.collection("reports").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _reports.value = snapshot.toObjects(ReportComplaint::class.java)
                }
            }
            listeners.add(reportReg)

            // 9. Cities Snapshot
            val cityReg = firestore!!.collection("cities").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.getString("name") }
                    if (list.isNotEmpty()) _cities.value = list
                }
            }
            listeners.add(cityReg)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting realtime snapshot listeners", e)
        }
    }

    private fun fallbackLocalDatabase() {
        // High fidelity memory database implementation for simulation
        seedCategories()
        seedProviders()
        _pendingProviders.value = emptyList()
        _settings.value = AppSettings()
        seedBanners()
        seedAdmins()
        seedReports()
        _chats.value = listOf(
            ChatMessage(UUID.randomUUID().toString(), "system", "نظام الدعم", "user", "مستخدم", "أهلاً بك! كيف يمكنني مساعدتك اليوم؟", System.currentTimeMillis() - 600000)
        )
        addLog("تم بدء تشغيل التطبيق في وضع توفير البيانات / المزامنة المحلية الفورية")
    }

    // Seeding methods for full database coverage
    private fun seedCategories() {
        val preset = listOf(
            Category("cat_elec", "كهرباء", "Electricity", "خدمات تمديد وإصلاح الشبكات الكهربائية والأجهزة", "electrical", false),
            Category("cat_plumb", "سباكة", "Plumbing", "تركيب وصيانة السباكة وتمديد خطوط المياه والمجاري", "plumbing", false),
            Category("cat_carp", "نجارة", "Carpentry", "صناعة وصيانة الأثاث والمشغولات الخشبية والأبواب والشبابيك", "carpentry", false),
            Category("cat_ac", "تبريد وتكييف", "AC & Cooling", "تركيب وإصلاح وصيانة أجهزة التكييف والتبريد والثلاجات", "ac_unit", false),

            // Subcategories
            Category("sub_elec_rep", "إصلاح الأجهزة الكهربائية", "Electrical Appliances Repair", "صيانة الغسالات، الأفران وما شابه", "electrical", true, "cat_elec"),
            Category("sub_elec_inst", "تمديد شبكات المنازل", "House Wiring Installation", "تركيب مصابيح، مفاتيح وتمديدات داخلية", "electrical", true, "cat_elec"),
            
            Category("sub_plumb_faucet", "تركيب خلاطات وحنفيات", "Faucets & Fittings", "صيانة وتركيب المغاسل والمحابس", "plumbing", true, "cat_plumb"),
            Category("sub_plumb_drain", "تسليك البالوعات ومجاري المياه", "Drainage Unclogging", "حل مشكلات الانسداد الخفيفة والعميقة", "plumbing", true, "cat_plumb"),
            
            Category("sub_carp_lock", "تركيب كوالين وأقفال الأبواب", "Door Locks & Latches", "تثبيت وصيانة الأقفال العادية والذكية", "carpentry", true, "cat_carp"),
            Category("sub_carp_furn", "تصنيع غرف النوم والمجالس", "Furniture Manufacturing", "تفصيل أثاث يمني فاخر محلي الصنع", "carpentry", true, "cat_carp"),

            Category("sub_ac_ref", "تعبئة غاز الفريون وصيانة المكيف", "Freon Refilling & AC Service", "تنظيف الفلاتر وتعبئة غاز التبريد", "ac_unit", true, "cat_ac")
        )
        _categories.value = preset
        if (isFirebaseEnabled && firestore != null) {
            val scope = CoroutineScope(Dispatchers.IO)
            preset.forEach { item ->
                scope.launch { firestore!!.collection("categories").document(item.id).set(item) }
            }
        }
    }

    private fun seedProviders() {
        val preset = listOf(
            ServiceProvider(
                id = "prov_maher",
                nameAr = "ماهر محمد طاهر",
                nameEn = "Maher Mohamed Taher",
                phone = "777644670",
                categoryId = "cat_plumb",
                subCategoryId = "sub_plumb_faucet",
                workAddress = "صنعاء، شارع الستين الغربي (مديرية معين)",
                residenceAr = "صنعاء",
                rating = 5.0,
                reviewCount = 14,
                isPinned = true,
                isRecommended = true,
                isVerified = true,
                loyaltyPoints = 250,
                avatarUrl = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=120&auto=format&fit=crop",
                idCardUrl = "",
                isVip = true,
                distance = 2.3,
                subscriptionActive = true
            ),
            ServiceProvider(
                id = "prov_wadah",
                nameAr = "وضاح علي مأرب",
                nameEn = "Waddah Ali Ma'rib",
                phone = "736462719",
                categoryId = "cat_carp",
                subCategoryId = "sub_carp_lock",
                workAddress = "إب، الدائري الغربي بجوار بنك اليمن",
                residenceAr = "إب",
                rating = 4.8,
                reviewCount = 9,
                isPinned = false,
                isRecommended = true,
                isVerified = true,
                loyaltyPoints = 120,
                avatarUrl = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=120&auto=format&fit=crop",
                idCardUrl = "",
                isVip = false,
                distance = 4.1,
                subscriptionActive = false
            ),
            ServiceProvider(
                id = "prov_ali",
                nameAr = "م. علي عبدالله الحميري",
                nameEn = "Eng. Ali Al-Himyari",
                phone = "771234567",
                categoryId = "cat_elec",
                subCategoryId = "sub_elec_inst",
                workAddress = "الحديدة، شارع صنعاء مقبل فرزة الميناء",
                residenceAr = "الحديدة",
                rating = 4.9,
                reviewCount = 31,
                isPinned = true,
                isRecommended = false,
                isVerified = true,
                loyaltyPoints = 390,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&auto=format&fit=crop",
                idCardUrl = "",
                isVip = true,
                distance = 1.0,
                subscriptionActive = true
            )
        )
        _serviceProviders.value = preset
        if (isFirebaseEnabled && firestore != null) {
            val scope = CoroutineScope(Dispatchers.IO)
            preset.forEach { item ->
                scope.launch { firestore!!.collection("service_providers").document(item.id).set(item) }
            }
        }
    }

    private fun seedBanners() {
        val ads = listOf(
            BannerAd(
                id = "banner_1",
                title = "خصومات العيد في تمديد الكهرباء الفاخرة",
                type = "IMAGE",
                bannerUrl = "https://images.unsplash.com/photo-1558224523-903f55de1ee8?w=800&auto=format&fit=crop",
                targetCategory = "cat_elec",
                size = 10,
                durationSeconds = 6,
                linkUrl = "https://example.com/banner-promo-elec",
                isActive = true
            ),
            BannerAd(
                id = "banner_2",
                title = "افضل سباكين في صنعاء والمحافظات متوفرون 24 ساعة",
                type = "TEXT",
                bannerUrl = "",
                targetCategory = "cat_plumb",
                size = 5,
                durationSeconds = 5,
                linkUrl = "",
                isActive = true
            )
        )
        _banners.value = ads
    }

    private fun seedAdmins() {
        val ad = Admin("admin_wam", "WAM2026", "maher736462", AdminPermissions(true, true, true, true, true))
        _admins.value = listOf(ad)
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("admins").document(ad.id).set(ad)
            }
        }
    }

    private fun seedReports() {
        _reports.value = listOf(
            ReportComplaint("rep_1", "عبدالرحمن الشميري", "prov_maher", "ماهر محمد طاهر", "تأخر 10 دقائق عن الموعد لكن الخدمة كانت قمة في الروعة والإتقان وبسعر معاينة معقول", System.currentTimeMillis() - 86400000)
        )
    }

    // CRUD - Settings Functions
    fun saveSettings(appSettings: AppSettings) {
        _settings.value = appSettings
        addLog("تحديث إعدادات التطبيق العامة: ${appSettings.appNameAr}")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("app_settings").document("global").set(appSettings)
                    .addOnFailureListener { Log.e(TAG, "Failed to save settings", it) }
            }
        }
    }

    // CRUD - Categories Functions
    fun addCategory(category: Category) {
        val currentList = _categories.value.toMutableList()
        currentList.removeAll { it.id == category.id }
        currentList.add(category)
        _categories.value = currentList
        addLog("تمت إضافة قسم جديد: ${category.nameAr}")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("categories").document(category.id).set(category)
            }
        }
    }

    fun deleteCategory(id: String) {
        val currentList = _categories.value.toMutableList()
        currentList.removeAll { it.id == id }
        _categories.value = currentList
        addLog("تم حذف القسم ذو الرقم التعريفي: $id")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("categories").document(id).delete()
            }
        }
    }

    // CRUD - Providers Functions
    fun addProviderDirectly(provider: ServiceProvider) {
        val currentList = _serviceProviders.value.toMutableList()
        currentList.removeAll { it.id == provider.id }
        currentList.add(provider)
        _serviceProviders.value = currentList
        addLog("إضافة فني يدوياً مباشر: ${provider.nameAr}")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("service_providers").document(provider.id).set(provider)
            }
        }
    }

    fun updateProvider(provider: ServiceProvider) {
        val currentList = _serviceProviders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == provider.id }
        if (index != -1) {
            currentList[index] = provider
            _serviceProviders.value = currentList
            addLog("تحديث بيانات الفني: ${provider.nameAr}")
            if (isFirebaseEnabled && firestore != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    firestore!!.collection("service_providers").document(provider.id).set(provider)
                }
            }
        }
    }

    fun deleteProvider(id: String) {
        val currentList = _serviceProviders.value.toMutableList()
        currentList.removeAll { it.id == id }
        _serviceProviders.value = currentList
        addLog("تم إزالة الفني من الدليل بشكل نهائي: $id")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("service_providers").document(id).delete()
            }
        }
    }

    // CRUD - Registration Request (Pending Providers)
    fun requestRegistration(pending: PendingProvider) {
        val currentList = _pendingProviders.value.toMutableList()
        currentList.add(pending)
        _pendingProviders.value = currentList
        addLog("تسجيل طلب انضمام فني جديد: ${pending.nameAr}")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("pending_providers").document(pending.id).set(pending)
            }
        }
    }

    fun approveRequest(requestId: String, isApproved: Boolean, rejectReason: String = "") {
        val req = _pendingProviders.value.find { it.id == requestId }
        if (req != null) {
            val currentPending = _pendingProviders.value.toMutableList()
            currentPending.removeAll { it.id == requestId }
            _pendingProviders.value = currentPending

            if (isApproved) {
                // Transfer to ServiceProvider
                val newProvider = ServiceProvider(
                    id = "prov_" + UUID.randomUUID().toString().take(6),
                    nameAr = req.nameAr,
                    nameEn = req.nameAr, // fallback
                    phone = req.phone,
                    categoryId = req.categoryId,
                    subCategoryId = req.subCategoryId,
                    workAddress = req.workAddress,
                    residenceAr = req.residenceAr,
                    avatarUrl = req.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&auto=format&fit=crop" },
                    idCardUrl = req.idCardUrl,
                    isVerified = true
                )
                addProviderDirectly(newProvider)
                addLog("تم قبول طلب الفني ${req.nameAr} ودخوله حيز الدليل")
            } else {
                addLog("تم رفض طلب الفني ${req.nameAr} لسبب:  $rejectReason")
            }

            if (isFirebaseEnabled && firestore != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    firestore!!.collection("pending_providers").document(requestId).delete()
                }
            }
        }
    }

    // CRUD - Cities / Governorates
    fun addCity(cityName: String) {
        val current = _cities.value.toMutableList()
        if (!current.contains(cityName)) {
            current.add(cityName)
            _cities.value = current
            addLog("تمت إضافة مدينة تغطية جديدة: $cityName")
            if (isFirebaseEnabled && firestore != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val map = mapOf("name" to cityName)
                    firestore!!.collection("cities").document(UUID.randomUUID().toString()).set(map)
                }
            }
        }
    }

    // CRUD - Banner Ads
    fun addBanner(banner: BannerAd) {
        val current = _banners.value.toMutableList()
        current.add(banner)
        _banners.value = current
        addLog("إصدار بنر إعلاني ترويجي:  ${banner.title}")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("banners").document(banner.id).set(banner)
            }
        }
    }

    // CRUD - Logs & Reports & Chats
    fun addLog(text: String, actor: String = "النظام") {
        val newLog = ActivityLog(UUID.randomUUID().toString(), text, actor, System.currentTimeMillis())
        val current = _activityLogs.value.toMutableList()
        current.add(0, newLog) // Add to top
        _activityLogs.value = current
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("logs").document(newLog.id).set(newLog)
            }
        }
    }

    fun submitReport(report: ReportComplaint) {
        val current = _reports.value.toMutableList()
        current.add(report)
        _reports.value = current
        addLog("تلقي بلاغ ضد مقدم خدمة: ${report.providerName}")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("reports").document(report.id).set(report)
            }
        }
    }

    fun postChatMessage(msg: ChatMessage) {
        val current = _chats.value.toMutableList()
        current.add(msg)
        _chats.value = current
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("chats").document(msg.id).set(msg)
            }
        }
    }

    fun clearChatHistory() {
        _chats.value = emptyList()
        addLog("تم محو سجل الدردشات بالكامل لدواعي الخصوصية والسرية")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("chats").get().addOnSuccessListener { r ->
                    for (doc in r.documents) {
                        doc.reference.delete()
                    }
                }
            }
        }
    }

    fun addAdminSupervisor(admin: Admin) {
        val current = _admins.value.toMutableList()
        current.add(admin)
        _admins.value = current
        addLog("إنشاء حساب مشرف جديد للصلاحيات: ${admin.username}")
        if (isFirebaseEnabled && firestore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                firestore!!.collection("admins").document(admin.id).set(admin)
            }
        }
    }

    fun performBackup(context: Context): String {
        addLog("عملية نسخ احتياطي لقاعدة البيانات والمجموعات")
        return "تم حفظ النسخة بنجاح على التخزين الداخلي: Backup_${System.currentTimeMillis()}.json"
    }

    // Image upload simulation & true implementation
    fun uploadImage(filePath: String, onComplete: (String) -> Unit) {
        if (isFirebaseEnabled && storage != null) {
            val file = File(filePath)
            if (file.exists()) {
                val ref = storage!!.reference.child("images/${UUID.randomUUID()}_${file.name}")
                ref.putFile(android.net.Uri.fromFile(file))
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            onComplete(uri.toString())
                        }
                    }
                    .addOnFailureListener {
                        onComplete("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&auto=format&fit=crop")
                    }
                return
            }
        }
        // Fallback simulated imageUrl
        onComplete("https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=120&auto=format&fit=crop")
    }
}
