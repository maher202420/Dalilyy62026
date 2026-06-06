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

    private val _servicesList = MutableStateFlow<List<ServiceItem>>(emptyList())
    val servicesList: StateFlow<List<ServiceItem>> = _servicesList

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    private val _supervisorsList = MutableStateFlow<List<SupervisorUser>>(emptyList())
    val supervisorsList: StateFlow<List<SupervisorUser>> = _supervisorsList

    private val _providersList = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val providersList: StateFlow<List<ServiceProvider>> = _providersList

    private val _messagesList = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messagesList: StateFlow<List<ChatMessage>> = _messagesList

    // Listener registrations for lifecycle control
    private var servicesListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null
    private var supervisorsListener: ListenerRegistration? = null
    private var providersListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null

    // For keeping track of the current logged in user
    var currentSupervisor: SupervisorUser? = null
    var currentProvider: ServiceProvider? = null

    init {
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
                    _servicesList.value = items.sortedByDescending { it.isPinned }.thenBy { it.dateAdded }
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
                    for (doc in snapshot) {
                        val user = doc.toObject(SupervisorUser::class.java)
                        list.add(user.copy(id = doc.id))
                    }
                    _supervisorsList.value = list
                    
                    // Seed default super supervisor if empty
                    if (list.isEmpty()) {
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
}
