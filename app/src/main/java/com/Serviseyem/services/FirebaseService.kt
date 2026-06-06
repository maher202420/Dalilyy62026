package com.Serviseyem.services

import android.util.Log
import com.Serviseyem.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object FirebaseService {
    private const val TAG = "FirebaseService"
    private val db by lazy { FirebaseFirestore.getInstance() }

    // Global back-door tracking
    var isBackdoorLoggedIn = false

    private val _servicesList = MutableStateFlow<List<ServiceItem>>(emptyList())
    val servicesList: StateFlow<List<ServiceItem>> = _servicesList

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    private val _supervisorsList = MutableStateFlow<List<SupervisorUser>>(
        listOf(
            SupervisorUser(
                id = "default_wam",
                phone = "777644670",
                name = "المالك العام",
                password = "123",
                isApproved = true,
                notes = "حساب افتراضي للمالك العام"
            )
        )
    )
    val supervisorsList: StateFlow<List<SupervisorUser>> = _supervisorsList

    private val _providersList = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val providersList: StateFlow<List<ServiceProvider>> = _providersList

    private val _messagesList = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messagesList: StateFlow<List<ChatMessage>> = _messagesList

    // New State Flows for extensive requirements
    private val _categoriesList = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categoriesList: StateFlow<List<CategoryItem>> = _categoriesList

    private val _pendingProvidersList = MutableStateFlow<List<PendingProvider>>(emptyList())
    val pendingProvidersList: StateFlow<List<PendingProvider>> = _pendingProvidersList

    private val _reviewsList = MutableStateFlow<List<ProviderReview>>(emptyList())
    val reviewsList: StateFlow<List<ProviderReview>> = _reviewsList

    private val _reportsList = MutableStateFlow<List<ProviderReport>>(emptyList())
    val reportsList: StateFlow<List<ProviderReport>> = _reportsList

    private val _citiesList = MutableStateFlow<List<DatabaseCity>>(emptyList())
    val citiesList: StateFlow<List<DatabaseCity>> = _citiesList

    // Listener registrations for lifecycle control
    private var servicesListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null
    private var supervisorsListener: ListenerRegistration? = null
    private var providersListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null
    
    private var categoriesListener: ListenerRegistration? = null
    private var pendingListener: ListenerRegistration? = null
    private var reviewsListener: ListenerRegistration? = null
    private var reportsListener: ListenerRegistration? = null
    private var citiesListener: ListenerRegistration? = null

    // For keeping track of the current logged in user
    var currentSupervisor: SupervisorUser? = null
    var currentProvider: ServiceProvider? = null

    init {
        try {
            db.firestoreSettings = com.google.firebase.firestore.firestoreSettings {
                isPersistenceEnabled = true
            }
        } catch(e: Exception) {
            Log.e(TAG, "Persistence setting error", e)
        }
        startRealtimeSynchronization()
    }

    fun startRealtimeSynchronization() {
        Log.d(TAG, "Starting Realtime Snapshot Synchronization with Firestore")
        
        // 1. App Settings Real-time Listener
        settingsListener?.remove()
        settingsListener = db.collection("settings").document("primary")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen settings", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val incoming = snapshot.toObject(AppSettings::class.java)
                    if (incoming != null) {
                        _settings.value = incoming
                    }
                } else {
                    // Seed settings if they don't exist
                    db.collection("settings").document("primary").set(AppSettings())
                }
            }

        // 2. Services Real-time Listener
        servicesListener?.remove()
        servicesListener = db.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen services", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = mutableListOf<ServiceItem>()
                    for (doc in snapshot) {
                        val item = doc.toObject(ServiceItem::class.java)
                        items.add(item.copy(id = doc.id))
                    }
                    _servicesList.value = items.sortedWith(compareByDescending<ServiceItem> { it.isPinned }.thenBy { it.dateAdded })
                }
            }

        // 3. Supervisors Real-time Listener
        supervisorsListener?.remove()
        supervisorsListener = db.collection("supervisors")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen supervisors", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = mutableListOf<SupervisorUser>()
                    // Always guarantee the first/master supervisor accounts exist
                    list.add(SupervisorUser(
                        id = "default_wam",
                        phone = "777644670",
                        name = "المالك العام",
                        password = "123",
                        isApproved = true,
                        notes = "حساب افتراضي للمالك العام"
                    ))
                    // Supreme Main WAM2026 supervisor
                    list.add(SupervisorUser(
                        id = "wam_main_admin",
                        phone = "WAM2026",
                        name = "المدير الرئيسي لـ WAM",
                        password = _settings.value.adminPassword,
                        isApproved = true,
                        notes = "المدير الرئيسي العام للتحكم سحابياً"
                    ))
                    for (doc in snapshot) {
                        val user = doc.toObject(SupervisorUser::class.java)
                        if (user.phone != "777644670" && user.phone != "WAM2026") {
                            list.add(user.copy(id = doc.id))
                        }
                    }
                    _supervisorsList.value = list
                    
                    if (snapshot.isEmpty) {
                        val defaultSuper = SupervisorUser(
                            id = "default_wam",
                            phone = "777644670",
                            name = "المالك العام",
                            password = "123",
                            isApproved = true,
                            notes = "حساب افتراضي للمالك العام"
                        )
                        db.collection("supervisors").document(defaultSuper.phone).set(defaultSuper)
                    }
                }
            }

        // 4. Service Providers Real-time Listener
        providersListener?.remove()
        providersListener = db.collection("providers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen providers", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = mutableListOf<ServiceProvider>()
                    for (doc in snapshot) {
                        val provider = doc.toObject(ServiceProvider::class.java)
                        list.add(provider.copy(id = doc.id))
                    }
                    _providersList.value = list
                }
            }

        // 5. Chat Communication Real-time Listener
        chatsListener?.remove()
        chatsListener = db.collection("chats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen chats", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chats = mutableListOf<ChatMessage>()
                    for (doc in snapshot) {
                        val msg = doc.toObject(ChatMessage::class.java)
                        chats.add(msg.copy(id = doc.id))
                    }
                    _messagesList.value = chats
                }
            }

        // 6. Categories Real-time Listener (addSnapshotListener on 'categories')
        categoriesListener?.remove()
        categoriesListener = db.collection("categories")
            .orderBy("rankOrder")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen categories", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lists = mutableListOf<CategoryItem>()
                    for (doc in snapshot) {
                        val cat = doc.toObject(CategoryItem::class.java)
                        lists.add(cat.copy(id = doc.id))
                    }
                    _categoriesList.value = lists
                    
                    // Seed categories if database empty
                    if (snapshot.isEmpty) {
                        seedDefaultCategories()
                    }
                }
            }

        // 7. Pending Providers Real-time Listener (addSnapshotListener on 'pending_providers')
        pendingListener?.remove()
        pendingListener = db.collection("pending_providers")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen pending_providers", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lists = mutableListOf<PendingProvider>()
                    for (doc in snapshot) {
                        val p = doc.toObject(PendingProvider::class.java)
                        lists.add(p.copy(id = doc.id))
                    }
                    _pendingProvidersList.value = lists
                }
            }

        // 8. Reviews Real-time Listener (addSnapshotListener on 'reviews')
        reviewsListener?.remove()
        reviewsListener = db.collection("reviews")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen reviews", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lists = mutableListOf<ProviderReview>()
                    for (doc in snapshot) {
                        val r = doc.toObject(ProviderReview::class.java)
                        lists.add(r.copy(id = doc.id))
                    }
                    _reviewsList.value = lists
                }
            }

        // 9. Reports Real-time Listener (addSnapshotListener on 'reports')
        reportsListener?.remove()
        reportsListener = db.collection("reports")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen reports", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lists = mutableListOf<ProviderReport>()
                    for (doc in snapshot) {
                        val rep = doc.toObject(ProviderReport::class.java)
                        lists.add(rep.copy(id = doc.id))
                    }
                    _reportsList.value = lists
                }
            }

        // 10. Cities Real-time Listener (addSnapshotListener on 'cities')
        citiesListener?.remove()
        citiesListener = db.collection("cities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to listen cities", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lists = mutableListOf<DatabaseCity>()
                    for (doc in snapshot) {
                        val city = doc.toObject(DatabaseCity::class.java)
                        lists.add(city.copy(id = doc.id))
                    }
                    _citiesList.value = lists
                    
                    // Seed cities if empty
                    if (snapshot.isEmpty) {
                        seedDefaultCities()
                    }
                }
            }
    }

    private fun seedDefaultCategories() {
        val seed = listOf(
            CategoryItem("cat_1", "صيانة منزلية", "Engineering", "", 1),
            CategoryItem("cat_1_sub1", "كهربائي منازل", "Build", "cat_1", 2),
            CategoryItem("cat_1_sub2", "سباك منازل وحمامات", "Build", "cat_1", 3),
            CategoryItem("cat_1_sub3", "نجارة وأثاث", "Build", "cat_1", 4),
            
            CategoryItem("cat_2", "صحة ورعاية", "Stars", "", 5),
            CategoryItem("cat_2_sub1", "طبيب عام واستشاري", "Stars", "cat_2", 6),
            CategoryItem("cat_2_sub2", "تفتيش بيئي ورش مبيدات", "Stars", "cat_2", 7),
            
            CategoryItem("cat_3", "تعليم وتدريب", "Gavel", "", 8),
            CategoryItem("cat_3_sub1", "مدرس خصوصي لغات", "Gavel", "cat_3", 9),
            CategoryItem("cat_3_sub2", "تعليم البرمجة والذكاء الاصطناعي", "Gavel", "cat_3", 10),
            
            CategoryItem("cat_4", "نقل وخدمات", "Apartment", "", 11),
            CategoryItem("cat_4_sub1", "مشاوير نقل مواد وأثاث", "Apartment", "cat_4", 12),
            CategoryItem("cat_4_sub2", "نقل وايتات ماء صالحة للشرب", "Apartment", "cat_4", 13)
        )
        val batch = db.batch()
        for (item in seed) {
            batch.set(db.collection("categories").document(item.id), item)
        }
        batch.commit()
    }

    private fun seedDefaultCities() {
        val seed = listOf(
            DatabaseCity("city_1", "صنعاء"),
            DatabaseCity("city_2", "عدن"),
            DatabaseCity("city_3", "تعز"),
            DatabaseCity("city_4", "الحديدة"),
            DatabaseCity("city_5", "إب")
        )
        val batch = db.batch()
        for (item in seed) {
            batch.set(db.collection("cities").document(item.id), item)
        }
        batch.commit()
    }

    // --- Services Manipulations ---
    fun saveService(service: ServiceItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (service.id.isEmpty()) {
            db.collection("services").document()
        } else {
            db.collection("services").document(service.id)
        }
        val finalService = service.copy(id = ref.id)
        ref.set(finalService)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteService(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("services").document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- Settings Saving ---
    fun saveSettings(newSettings: AppSettings, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("settings").document("primary")
            .set(newSettings)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- Chat Actions ---
    fun sendChatMessage(msg: ChatMessage, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = db.collection("chats").document()
        val finalMsg = msg.copy(id = ref.id, timestamp = System.currentTimeMillis())
        ref.set(finalMsg)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun clearAllChats(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("chats").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }
                val batch = db.batch()
                for (doc in snapshot) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // --- Providers Manipulations ---
    fun registerProvider(provider: ServiceProvider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = db.collection("providers").document()
        val finalProvider = provider.copy(id = ref.id, registrationDate = System.currentTimeMillis())
        ref.set(finalProvider)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateProviderStatus(id: String, status: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("providers").document(id)
            .update("status", status)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteProvider(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("providers").document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- Supervisors Manipulations ---
    fun saveSupervisor(supervisor: SupervisorUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val targetId = if (supervisor.phone.isNotEmpty()) supervisor.phone else db.collection("supervisors").document().id
        val finalSupervisor = supervisor.copy(id = targetId)
        db.collection("supervisors").document(targetId).set(finalSupervisor)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteSupervisor(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("supervisors").document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- New Manipulations ---
    fun savePendingProvider(p: PendingProvider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (p.id.isEmpty()) db.collection("pending_providers").document() else db.collection("pending_providers").document(p.id)
        val finalP = p.copy(id = ref.id)
        ref.set(finalP)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deletePendingProvider(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("pending_providers").document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun approvePendingProvider(p: PendingProvider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Create matching service item
        val service = ServiceItem(
            id = db.collection("services").document().id,
            title = "أخصائي: " + p.fullName,
            category = if (p.subCategory.isNotEmpty()) p.subCategory else p.mainCategory,
            description = "مقدم خدمة معتمد في ${p.district} - ${p.officeAddress}.\nالموقع الجغرافي: ${p.gpsCoordinates}.",
            price = "حسب الاتفاق",
            executionTime = "فوري",
            providerPhone = p.phone,
            iconName = "User",
            isPinned = false,
            dateAdded = System.currentTimeMillis()
        )
        // Set update status to approved or delete from pending, and save service
        db.collection("services").document(service.id).set(service)
            .addOnSuccessListener {
                db.collection("pending_providers").document(p.id).update("status", "approved")
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveCategory(cat: CategoryItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (cat.id.isEmpty()) db.collection("categories").document() else db.collection("categories").document(cat.id)
        val finalCat = cat.copy(id = ref.id)
        ref.set(finalCat)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteCategory(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("categories").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveReview(r: ProviderReview, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (r.id.isEmpty()) db.collection("reviews").document() else db.collection("reviews").document(r.id)
        val finalR = r.copy(id = ref.id)
        ref.set(finalR)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteReview(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reviews").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveReport(rep: ProviderReport, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (rep.id.isEmpty()) db.collection("reports").document() else db.collection("reports").document(rep.id)
        val finalRep = rep.copy(id = ref.id)
        ref.set(finalRep)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteReport(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reports").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveCity(city: DatabaseCity, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (city.id.isEmpty()) db.collection("cities").document() else db.collection("cities").document(city.id)
        val finalCity = city.copy(id = ref.id)
        ref.set(finalCity)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteCity(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("cities").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
