package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
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
import com.Serviseyem.services.FirebaseService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by FirebaseService.messagesList.collectAsState()
    val settings by FirebaseService.settings.collectAsState()

    var textInput by remember { mutableStateOf("") }
    
    // Check if the current user is logged in as an admin/supervisor
    val isAdmin = FirebaseService.currentSupervisor != null
    val senderDisplayName = if (isAdmin) {
        "المشرف: ${FirebaseService.currentSupervisor?.name}"
    } else {
        "مستخدم دليل WAM"
    }

    // Auto scroll to bottom when newly compiled messages stream in
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "المساعد الذكي والدعم الإداري المالي 🌟",
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isAdmin) "صوت المدير نشط" else "مزامنة لحظية مباشرة بين كافة الأجهزة",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = {
                            FirebaseService.clearAllChats({
                                Toast.makeText(context, "تم تصفير المحادثة بنجاح!", Toast.LENGTH_SHORT).show()
                            }, {})
                        }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = Color.LightGray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            
            // Header Hint Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3F37))
            ) {
                Text(
                    text = "🤖 هل تبحث عن خدمة معينة مثل رخصة قيادة أو طلب دعم أو حجز؟ اطرح سؤالك والمساعد السحابي سيجيبك في ثانية واحدة من خدماتنا المنشورة فورياً!",
                    color = Color.White,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Chat Logs Stream
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد رسائل سابقة.\nابدأ بطرح سؤالك الآن!",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        // Detect mine
                        val isMsgFromAdminViewer = isAdmin && msg.isAdmin
                        val isMsgFromUserViewer = !isAdmin && !msg.isAdmin
                        val isMine = isMsgFromAdminViewer || isMsgFromUserViewer

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMine) Alignment.CenterLeft else Alignment.CenterRight
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMine) 0.dp else 16.dp,
                                    bottomEnd = if (isMine) 16.dp else 0.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMine) {
                                        Color(0xFF064E3B) // Dark teal for user viewer's own msg
                                    } else {
                                        if (msg.isAdmin) Color(0xFFD4AF37) else Color(0xFF0B3F37) // Golden for AI/Admin response
                                    }
                                ),
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isMine && msg.isAdmin) Color.Black else Color(0xFFD4AF37)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = msg.messageText,
                                        fontSize = 13.sp,
                                        color = if (!isMine && msg.isAdmin) Color.Black else Color.White,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Keyboard input tray with 100% visible texts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("اكتب تفاصيل سؤالك هنا بوضوح وسرعة...", color = Color.LightGray) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("chat_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
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
                        if (textInput.isBlank()) return@FloatingActionButton
                        val sendMsg = ChatMessage(
                            senderName = senderDisplayName,
                            senderPhone = if (isAdmin) "ADMIN" else "USER",
                            messageText = textInput.trim(),
                            isAdmin = isAdmin
                        )
                        FirebaseService.sendChatMessage(sendMsg, {
                            textInput = ""
                        }, {
                            Toast.makeText(context, "الرجاء مراجعة شبكة الإنترنت", Toast.LENGTH_SHORT).show()
                        })
                    },
                    containerColor = Color(0xFFD4AF37),
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
