package com.Serviseyem.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.Serviseyem.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow

object FirebaseService {
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Flow states synced in real-time
    val servicesList = MutableStateFlow<List<ServiceItem>>(emptyList())
    val settings = MutableStateFlow<AppSettings>(AppSettings())
    val supervisorsList = MutableStateFlow<List<SupervisorUser>>(emptyList())
    val providersList = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val categoriesList = MutableStateFlow<List<CategoryItem>>(emptyList())
    val citiesList = MutableStateFlow<List<DatabaseCity>>(emptyList())
    val chatMessagesList = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatSessionsList = MutableStateFlow<List<ChatSession>>(emptyList())

    // Tracks current session entities
    var currentSupervisor: SupervisorUser? = null
    var currentProvider: ServiceProvider? = null

    // Trackers for active database listeners to prevent leaks
    private var servicesListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null
    private var supervisorsListener: ListenerRegistration? = null
    private var providersListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null
    private var citiesListener: ListenerRegistration? = null
    private var chatMessagesListener: ListenerRegistration? = null
    private var chatSessionsListener: ListenerRegistration? = null

    init {
        try {
            val settingsObj = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            db.firestoreSettings = settingsObj
        } catch (e: Exception) {
            Log.e("FirebaseService", "Firestore already configured: ${e.message}")
        }
    }

    /**
     * Cache-first loading to populate application instantly on initial load, before snapshots trigger.
     */
    fun loadInitialCachedData() {
        db.collection("settings").document("global").get()
            .addOnSuccessListener { sn ->
                val setVal = sn?.toObject(AppSettings::class.java)
                if (setVal != null) {
                    settings.value = setVal
                } else {
                    // Seed if missing
                    db.collection("settings").document("global").set(AppSettings())
                }
            }
        db.collection("services").get()
            .addOnSuccessListener { sn ->
                if (sn != null && !sn.isEmpty) {
                    servicesList.value = sn.toObjects(ServiceItem::class.java)
                }
            }
        db.collection("providers").get()
            .addOnSuccessListener { sn ->
                if (sn != null && !sn.isEmpty) {
                    providersList.value = sn.toObjects(ServiceProvider::class.java)
                }
            }
        db.collection("categories").get()
            .addOnSuccessListener { sn ->
                if (sn != null && !sn.isEmpty) {
                    categoriesList.value = sn.toObjects(CategoryItem::class.java)
                }
            }
        db.collection("cities").get()
            .addOnSuccessListener { sn ->
                if (sn != null && !sn.isEmpty) {
                    citiesList.value = sn.toObjects(DatabaseCity::class.java)
                }
            }
    }

    /**
     * Clear and remove all active Snapshot Listeners explicitly to prevent duplicates.
     */
    fun clearAllListeners() {
        servicesListener?.remove().also { servicesListener = null }
        settingsListener?.remove().also { settingsListener = null }
        supervisorsListener?.remove().also { supervisorsListener = null }
        providersListener?.remove().also { providersListener = null }
        categoriesListener?.remove().also { categoriesListener = null }
        citiesListener?.remove().also { citiesListener = null }
        chatMessagesListener?.remove().also { chatMessagesListener = null }
        chatSessionsListener?.remove().also { chatSessionsListener = null }
    }

    /**
     * Recreate all Snapshot Listeners gracefully.
     */
    fun initListeners() {
        clearAllListeners()

        // 1. Settings listener
        settingsListener = db.collection("settings").document("global")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val value = snapshot.toObject(AppSettings::class.java)
                    if (value != null) {
                        settings.value = value
                    }
                }
            }

        // 2. Services listener
        servicesListener = db.collection("services").orderBy("isPinned", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    servicesList.value = snapshot.toObjects(ServiceItem::class.java)
                }
            }

        // 3. Supervisors listener
        supervisorsListener = db.collection("supervisors")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    supervisorsList.value = snapshot.toObjects(SupervisorUser::class.java)
                }
            }

        // 4. Providers listener
        providersListener = db.collection("providers")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    providersList.value = snapshot.toObjects(ServiceProvider::class.java)
                }
            }

        // 5. Categories listener
        categoriesListener = db.collection("categories").orderBy("rankOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    categoriesList.value = snapshot.toObjects(CategoryItem::class.java)
                }
            }

        // 6. Cities listener
        citiesListener = db.collection("cities")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    citiesList.value = snapshot.toObjects(DatabaseCity::class.java)
                }
            }

        // 7. Chat Channel index lists listener
        chatSessionsListener = db.collection("chats").orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    chatSessionsList.value = snapshot.toObjects(ChatSession::class.java)
                }
            }
    }

    /**
     * Monitor connection state. Unsubscribe and force resubscription immediately upon network return.
     */
    fun registerNetworkMonitoring(context: Context) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()
            cm.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Handler(Looper.getMainLooper()).post {
                        Log.i("FirebaseService", "Network re-established. Force reloading listeners!")
                        initListeners()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to register network monitoring", e)
        }
    }

    // --- ADMINISTRATIVE & DB CRUD FUNCTIONS ---

    fun saveSettings(appSettings: AppSettings, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("settings").document("global").set(appSettings)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveService(service: ServiceItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (service.id.isEmpty()) db.collection("services").document() else db.collection("services").document(service.id)
        val finalService = service.copy(id = ref.id)
        ref.set(finalService)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteService(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("services").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveCategory(category: CategoryItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (category.id.isEmpty()) db.collection("categories").document() else db.collection("categories").document(category.id)
        val finalCategory = category.copy(id = ref.id)
        ref.set(finalCategory)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteCategory(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("categories").document(id).delete()
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

    fun saveProvider(provider: ServiceProvider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (provider.id.isEmpty()) db.collection("providers").document() else db.collection("providers").document(provider.id)
        val finalProvider = provider.copy(id = ref.id)
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
        db.collection("providers").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveSupervisor(supervisor: SupervisorUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = if (supervisor.id.isEmpty()) db.collection("supervisors").document() else db.collection("supervisors").document(supervisor.id)
        val finalSupervisor = supervisor.copy(id = ref.id)
        ref.set(finalSupervisor)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteSupervisor(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("supervisors").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- REALTIME CHAT ENGINE INTERACTION FUNCTIONS ---

    fun createChatSession(session: ChatSession, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        // Query to check if same initiator and provider already have active channel to prevent duplicates
        db.collection("chats")
            .whereEqualTo("initiatorPhone", session.initiatorPhone)
            .whereEqualTo("providerPhone", session.providerPhone)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val existingId = snapshot.documents[0].id
                    onSuccess(existingId)
                } else {
                    val ref = db.collection("chats").document()
                    val finalSession = session.copy(id = ref.id)
                    ref.set(finalSession)
                        .addOnSuccessListener { onSuccess(finalSession.id) }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener {
                val ref = db.collection("chats").document()
                val finalSession = session.copy(id = ref.id)
                ref.set(finalSession)
                    .addOnSuccessListener { onSuccess(finalSession.id) }
                    .addOnFailureListener { onFailure(it) }
            }
    }

    fun sendChatMessage(msg: ChatMessage, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = db.collection("chats").document(msg.chatId).collection("messages").document()
        val finalMsg = msg.copy(id = ref.id)
        
        // Save message inside channel
        ref.set(finalMsg)
            .addOnSuccessListener {
                // Update parent dynamic session last message for quick indexing
                db.collection("chats").document(msg.chatId)
                    .update(
                        "lastMessage", msg.messageText,
                        "lastUpdated", System.currentTimeMillis()
                    )
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * Snapshot list message observer with SINGLE ACTIVE LISTENER logic.
     */
    fun listenToChatMessages(chatId: String) {
        chatMessagesListener?.remove() // Always clear before creating 
        
        chatMessagesListener = db.collection("chats").document(chatId)
            .collection("messages").orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    chatMessagesList.value = snapshot.toObjects(ChatMessage::class.java)
                }
            }
    }

    fun stopListeningToChatMessages() {
        chatMessagesListener?.remove().also { chatMessagesListener = null }
        chatMessagesList.value = emptyList()
    }
}
