package com.Serviseyem.services

import android.util.Log
import com.Serviseyem.models.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SystemAiAssistant {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastHandledMessageId = ""

    fun startListeningForUserMessages() {
        scope.launch {
            FirebaseService.messagesList.collect { messages ->
                val lastMsg = messages.lastOrNull() ?: return@collect
                if (!lastMsg.isAdmin && lastMsg.id != lastHandledMessageId) {
                    lastHandledMessageId = lastMsg.id
                    // Generate AI reply with premium Arabic context
                    generateAiReply(lastMsg)
                }
            }
        }
    }

    private fun generateAiReply(userMsg: ChatMessage) {
        scope.launch {
            // Processing/Typing simulation
            delay(1500)
            
            val query = userMsg.messageText.trim().lowercase()
            val services = FirebaseService.servicesList.value
            val settings = FirebaseService.settings.value

            val replyText = when {
                query.contains("السلام") || query.contains("مرحبا") || query.contains("أهلا") || query.contains("هلا") -> {
                    "أهلاً ومرحباً بك في دليل ${settings.appNameAr} الفاخر ✨\nكيف يمكنني مساعدتك اليوم بخصوص الخدمات؟ يمكنك الاستفسار عن المعاملات الحكومية، الخدمات الإلكترونية، أو التواصل مع الإدارة للتوجيه الفوري."
                }
                query.contains("رقم") || query.contains("تواصل") || query.contains("اتصال") || query.contains("واتس") -> {
                    "بإمكانك التواصل المباشر مع الدعم الفني والإداري العام عبر:\n📞 الهاتف: ${settings.supportPhone}\n💬 واتساب: ${settings.supportWhatsapp}\n✉️ البريد الإلكتروني: ${settings.supportEmail}\nأو تفضل بطرح تساؤلك هنا وسنجيبك فوراً!"
                }
                query.contains("سعر") || query.contains("تكلفة") || query.contains("بكم") -> {
                    "أسعار الخدمات لدينا تعتمد على نوع الخدمة والسرعة المطلوبة. معظم الخدمات تتوفر بخيار (حسب الاتفاق) لضمان تقديم أفضل سعر لك. فضلاً حدد الخدمة التي تحتاجها وسنفيدك فوراً بالتقدير المناسب."
                }
                query.contains("مشرف") || query.contains("تسجيل دخول") || query.contains("تسجيل") -> {
                    "لتسجيل الدخول كمشرف، اضغط على أيقونة القفل الفاخرة بقائمة العنوان بالأعلى، ثم أدخل رقم الهاتف والرمز السري الخاص بك للمزامنة والدعم.\nوللتسجيل كمقدم خدمة متاح، استخدم خيار 'تسجيل مقدم خدمة جديد' من القائمة الرئيسية لحسابك!"
                }
                else -> {
                    // Try to search matches in Firestore services
                    val matched = services.filter {
                        it.title.lowercase().contains(query) || it.description.lowercase().contains(query) || it.category.lowercase().contains(query)
                    }
                    if (matched.isNotEmpty()) {
                        val sb = StringBuilder()
                        sb.append("لقد وجدت الخدمات التالية المتطابقة مع طلبك في قاعدة البيانات:\n\n")
                        matched.take(3).forEach {
                            sb.append("⭐ *${it.title}*\n")
                            sb.append("📋 التفاصيل: ${it.description}\n")
                            sb.append("⏱️ مدة التنفيذ: ${it.executionTime}\n")
                            sb.append("💰 السعر المتوقع: ${it.price}\n\n")
                        }
                        sb.append("لتنفيذ أي من هذه الخدمات فوراً، يمكنك التواصل عبر الهاتف الموحد: ${settings.supportPhone}")
                        sb.toString()
                    } else {
                        "شكرًا لتواصلك مع دليل ${settings.appNameAr}. طلبك قيد المتابعة مع الإدارة العامة.\nللاستفسار الشامل المباشر، يمكنك التواصل عبر الواتساب المباشر: ${settings.supportWhatsapp} أو البقاء هنا ريثما يتصل أحد المشرفين للرد عليك فوراً!"
                    }
                }
            }

            val aiMsg = ChatMessage(
                senderName = "مساعد WAM الذكي 🤖",
                senderPhone = "AI_BOT",
                messageText = replyText,
                isAdmin = true
            )

            FirebaseService.sendChatMessage(aiMsg, {}, {})
        }
    }
}
