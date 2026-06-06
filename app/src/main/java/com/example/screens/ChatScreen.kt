package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ChatMessage
import com.example.services.FirebaseService
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    techId: String,
    techName: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val chatState = FirebaseService.chats.collectAsState()
    var messageText by remember { mutableStateOf("") }

    // Filter messages for current tech
    val currentMessages = chatState.value.filter {
        (it.senderId == "user" && it.receiverId == techId) || (it.senderId == techId && it.receiverId == "user")
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Info Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "المحادثة مع: $techName",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "متاح ومستعد لقبول المهام • مزامنة فورية",
                            fontSize = 10.sp,
                            color = Color(0xFF25D366)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Chat Messages stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                reverseLayout = false
            ) {
                if (currentMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ابدأ المحادثة الآن بطرح تفاصيل مشكلتك الفنية وسنرد عليك فوراً.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    items(currentMessages) { msg ->
                        val isUser = msg.senderId == "user"
                        ChatBubble(message = msg, isMine = isUser)
                    }
                }
            }

            // Chat Input Controls
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .navigationBarsPadding(), // Account for system navigation bar safe area (Problem 1)
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("اكتب رسالتك تفصيلياً هنا...") },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = false,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (messageText.trim().isNotEmpty()) {
                                val userMessage = ChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    senderId = "user",
                                    senderName = "مستخدم التطبيق",
                                    receiverId = techId,
                                    receiverName = techName,
                                    message = messageText.trim()
                                )
                                FirebaseService.postChatMessage(userMessage)
                                val responseText = messageText // preserve state

                                messageText = ""

                                // Simple mock companion responder
                                val replyMsg = when {
                                    responseText.contains("السلام") || responseText.contains("هلا") -> "وعليكم السلام ورحمة الله وبركاته يا غالي، حياك الله. كيف يمكنني خدمتك الفنية اليوم؟"
                                    responseText.contains("سعر") || responseText.contains("بكم") -> "سعر المعاينة الأولي لدي هو المذكور بالملف، والأعمال الكبرى يُتفق عليها بعد الكشف والمطابقة."
                                    else -> "أهلاً بك أخي الكريم! لقد تم استلام تفاصيل طلبك بنجاح، وسأقوم بالتواصل الكامل معك تفصيلياً عبر تطبيق دليل خدمات WAM للبدء بالعمل."
                                }

                                // Delay responder to look realistic
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val reply = ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        senderId = techId,
                                        senderName = techName,
                                        receiverId = "user",
                                        receiverName = "مستخدم التطبيق",
                                        message = replyMsg
                                    )
                                    FirebaseService.postChatMessage(reply)
                                }, 1500)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isMine) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 0.dp else 16.dp,
                bottomEnd = if (isMine) 16.dp else 0.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    fontSize = 13.sp,
                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
