package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.models.ChatMessage
import com.Serviseyem.models.ChatSession
import com.Serviseyem.services.FirebaseService
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by FirebaseService.settings.collectAsState()
    
    // Auth entities
    val isSupervisor = FirebaseService.currentSupervisor != null
    val isProvider = FirebaseService.currentProvider != null
    val isAdmin = isSupervisor

    val currentUserPhone = if (isSupervisor) {
        "ADMIN"
    } else if (isProvider) {
        FirebaseService.currentProvider?.phone ?: "PROVIDER"
    } else {
        "VISITOR_GUEST"
    }

    // Settings validations
    val isChatDisabledByAdmin = if (isSupervisor) {
        false
    } else if (isProvider) {
        !settings.chatEnabledForProviders
    } else {
        !settings.chatEnabledForVisitors
    }

    // Block checks
    val isUserBlocked = if (isSupervisor) {
        false
    } else {
        val blockedList = settings.blockedChatUsers.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        blockedList.contains(currentUserPhone)
    }

    // UI state: selected active channel or lobby listing
    var selectedChannelId by remember { mutableStateOf<String?>(null) }
    var selectedChannelSession by remember { mutableStateOf<ChatSession?>(null) }

    val chatSessions by FirebaseService.chatSessionsList.collectAsState()
    val chatMessages by FirebaseService.chatMessagesList.collectAsState()

    // Trigger loading once on entry
    LaunchedEffect(Unit) {
        FirebaseService.loadInitialCachedData()
        FirebaseService.initListeners()
    }

    // Listen to changing message logs when channel selection modifies
    LaunchedEffect(selectedChannelId) {
        if (selectedChannelId != null) {
            FirebaseService.listenToChatMessages(selectedChannelId!!)
        } else {
            FirebaseService.stopListeningToChatMessages()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            FirebaseService.stopListeningToChatMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Chat Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (selectedChannelId != null) {
                    selectedChannelId = null
                    selectedChannelSession = null
                } else {
                    onNavigateBack()
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (selectedChannelId != null) {
                    if (isSupervisor) {
                        "دردشة: ${selectedChannelSession?.initiatorName ?: "زائر"} ↔ ${selectedChannelSession?.providerName ?: "مقدم"}"
                    } else if (isProvider) {
                        "العميل: ${selectedChannelSession?.initiatorName ?: "زائر"}"
                    } else {
                        "المهني: ${selectedChannelSession?.providerName ?: "مقدم الخدمة"}"
                    }
                } else {
                    "💬 نظام المحادثة المباشر WAM"
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (selectedChannelId != null && isSupervisor) {
                // Admin option to block this phone number
                val initiatorPhone = selectedChannelSession?.initiatorPhone ?: ""
                var showBlockConfirm by remember { mutableStateOf(false) }

                if (initiatorPhone.isNotEmpty() && initiatorPhone != "ADMIN") {
                    IconButton(onClick = { showBlockConfirm = true }) {
                        Icon(Icons.Default.Block, contentDescription = "Block User", tint = Color.Red)
                    }
                }

                if (showBlockConfirm) {
                    AlertDialog(
                        onDismissRequest = { showBlockConfirm = false },
                        title = { Text("⚠️ حظر مستخدم", fontWeight = FontWeight.Bold, color = Color.White) },
                        text = { Text("هل تريد بالتأكيد حظر رقم الهاتف ($initiatorPhone) من استخدام نظام الدردشة المباشرة والخدمات المزامنة؟", color = Color.White) },
                        confirmButton = {
                            TextButton(onClick = {
                                val currentBlocks = settings.blockedChatUsers.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .toMutableList()
                                if (!currentBlocks.contains(initiatorPhone)) {
                                    currentBlocks.add(initiatorPhone)
                                }
                                val newSettings = settings.copy(
                                    blockedChatUsers = currentBlocks.joinToString(",")
                                )
                                FirebaseService.saveSettings(newSettings, {
                                    Toast.makeText(context, "تم حظر المستخدم بنجاح!", Toast.LENGTH_SHORT).show()
                                    showBlockConfirm = false
                                }, {
                                    Toast.makeText(context, "فشل حفظ إعداد الحظر", Toast.LENGTH_SHORT).show()
                                })
                            }) {
                                Text("نعم، حظر", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBlockConfirm = false }) {
                                Text("إلغاء", color = Color.White)
                            }
                        },
                        containerColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Warning alerts if Blocked
        if (isUserBlocked) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                border = BorderStroke(1.dp, Color.Red)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Blocked", tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "🚫 تنبيه أمني: لقد تم حظر رقمك أو حسابك من الإشراك بالدردشة الفورية لمخالفة شروط الإرشاد الفني في WAM.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else if (isChatDisabledByAdmin && selectedChannelId == null) {
            // Screen deactivated notifications
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                border = BorderStroke(1.dp, Color(0xFF60A5FA))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFF93C5FD), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (settings.chatDisabledMessage.isNotEmpty()) {
                            settings.chatDisabledMessage
                        } else {
                            "⚠️ عذراً، تم تعطيل خدمة الدردشة الفورية حالياً من قبل مشرف الإدارة بـ WAM."
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (selectedChannelId != null) {
            // --- CONVERSATION DIALOG VIEW ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chatMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد رسائل سابقة.\nاكتب رسالة لبدء التنسيق واستفسار الخدمات الفورية.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        reverseLayout = false
                    ) {
                        items(chatMessages) { msg ->
                            val isMine = if (isSupervisor) {
                                msg.isAdmin
                            } else if (isProvider) {
                                msg.senderPhone == currentUserPhone
                            } else {
                                msg.senderPhone == currentUserPhone && !msg.isAdmin
                            }
                            
                            ChatBubbleItem(message = msg, isMine = isMine)
                        }
                    }
                }
            }

            // Input Tray controls
            if (!isUserBlocked && !isChatDisabledByAdmin) {
                var messageInputText by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageInputText,
                        onValueChange = { messageInputText = it },
                        placeholder = { Text("اكتب تفاصيل استفسارك الفني هنا بوضوح وسرعة...", color = Color.LightGray, fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .testTag("chat_input_field"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFFD4AF37),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 3,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    FloatingActionButton(
                        onClick = {
                            if (messageInputText.isBlank()) return@FloatingActionButton
                            
                            val name = if (isSupervisor) {
                                "مشرف الدعم WAM"
                            } else if (isProvider) {
                                FirebaseService.currentProvider?.name ?: "مقدم الخدمة"
                            } else {
                                "زائر مستقل"
                            }

                            val activeMsg = ChatMessage(
                                id = "",
                                chatId = selectedChannelId!!,
                                senderName = name,
                                senderPhone = currentUserPhone,
                                messageText = messageInputText.trim(),
                                timestamp = System.currentTimeMillis(),
                                isAdmin = isSupervisor
                            )

                            FirebaseService.sendChatMessage(activeMsg, {
                                messageInputText = ""
                            }, {
                                Toast.makeText(context, "الرجاء التحقق من جودة الإشارة والاتصال", Toast.LENGTH_SHORT).show()
                            })
                        },
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("chat_send_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            // --- CONVERSATION ROOM LOBBY LIST ---
            val matchingSessions = chatSessions.filter { sess ->
                if (isSupervisor) {
                    true // Supervisor views all chat channels!
                } else if (isProvider) {
                    sess.providerPhone == currentUserPhone // Provider views chats built with them
                } else {
                    // Normal visitors see records matching their visitor tag/phone
                    sess.initiatorPhone == currentUserPhone || sess.participants.contains(currentUserPhone)
                }
            }

            if (matchingSessions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد محادثات نشطة حالياً.\nيمكنك فتح محادثة عبر النقر على زر 'بدء محادثة' داخل ملف أو خدمة مقدم المعاملة.",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    items(matchingSessions) { session ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedChannelId = session.id
                                    selectedChannelSession = session
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF064E3B), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFD4AF37)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isSupervisor) {
                                            "${session.initiatorName} ↔ ${session.providerName}"
                                        } else if (isProvider) {
                                            "الزبون: ${session.initiatorName}"
                                        } else {
                                            "أخصائي: ${session.providerName}"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = if (session.lastMessage.isNotEmpty()) session.lastMessage else "لا توجد رسائل مكتوبة بعد.",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }

                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.ROOT).format(Date(session.lastUpdated)),
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage, isMine: Boolean) {
    val bubbleColor = if (isMine) Color(0xFF064E3B) else Color(0xFF1E293B)
    val align = if (isMine) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMine) 12.dp else 0.dp,
                        bottomEnd = if (isMine) 0.dp else 12.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Column {
                if (!isMine) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = message.messageText,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        
        Text(
            text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)),
            color = Color.Gray,
            fontSize = 9.sp,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}
