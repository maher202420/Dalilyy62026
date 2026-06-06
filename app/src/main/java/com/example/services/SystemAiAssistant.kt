package com.example.services

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SystemAiAssistant {
    private const val TAG = "SystemAiAssistant"
    
    // Offline knowledge base tailored to Yemeni home services
    private val localKnowledgeBase = mapOf(
        "سباك" to "يربطك دليلنا بأكفأ السباكين في اليمن. مثلاً الفني الحائز على 5 نجوم 'ماهر محمد طاهر' في صنعاء، متخصص في تركيب الخلاطات وحل انسدادات السباكة ومتاح للاتصال عبر: 777644670.",
        "كهربائي" to "تتوفر خدمات الكهربائيين الماهرين لتركيب تمديدات شاشات، إصلاح الغسالات وتمديدات الكهرباء المنزلية. لدينا م. علي عبدالله الحميري في الحديدة وهو معتمد وحاصل على تقييمات ممتازة.",
        "نجار" to "نجارة الأبواب، المطابخ، غرف النوم، وتفصيل المجالس اليمنية الفاخرة متوفرة لدينا. ننصحك بالفني 'وضاح علي مأرب' في إب لسرعة إنجازه وجودة عمله.",
        "تكييف" to "خدمات صيانة مكيفات الهواء وتعبئة الفريون متوفرة بأسعار منافسة. تصفح قسم التبريد والتكييف وابحث عن فنيين متاحين في منطقتك لزيارة فورية.",
        "سعر" to "سعر كشف ومعاينة الخدمة يتم تحديده بوضوح وشفافية من قبل كل فني (مثلاً يبدأ من 1500 ريال يمني للأعمال البسيطة)، وتتحكم المسافة بتكلفة الانتقال.",
        "تواصل" to "للتواصل مع الإدارة أو الدعم الفني، يرجى التكرم بالاتصال على الرقم 777644670 أو مراسلتنا واتساب على نفس الرقم، نحن متاحون لخدمتكم 24 ساعة.",
        "دعم" to "للتواصل مع الإدارة أو الدعم الفني، يرجى التكرم بالاتصال على الرقم 777644670 أو مراسلتنا واتساب على نفس الرقم، نحن متاحون لخدمتكم 24 ساعة.",
        "مشكلة" to "في حال واجهت أي مشكلة مع فني الخدمة، يمكنك رفع شكوى فورية من داخل ملف الفني أو الاتصال بالرقم 777644670 وسيقوم المشرفون بمعالجة شكواك فوراً.",
        "اشتراك" to "يتيح التطبيق شارات مميزة (Verified / VIP ⭐) للفنيين المشتركين باشتراكات شهرية، مما يعطيهم أولوية الظهور في واجهة البحث وتوصية كاملة للجمهور."
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        // Step 1: Check offline knowledge base first for standard keywords
        val lowerPrompt = prompt.lowercase()
        for ((key, value) in localKnowledgeBase) {
            if (lowerPrompt.contains(key)) {
                return@withContext "[مساعد محلي] $value"
            }
        }

        // Step 2: Try online Gemini integration if API key is active and not placeholder
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "null") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                
                val promptPayload = """
                    أنت الآن مساعد ذكي خبير وجزء مدمج من تطبيق "WAM Services" لدليل الخدمات في اليمن. 
                    أجب بإيجاز، بلهجة يمنية ترحيبية مهذبة وودودة واجعل كلامك سهلاً، فصيحاً ومختصراً.
                    سؤال المستخدم هو: $prompt
                """.trimIndent()

                val json = JSONObject().apply {
                    put("contents", JSONObject().apply {
                        put("parts", JSONObject().apply {
                            put("text", promptPayload)
                        })
                    })
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val candidates = jsonResponse.getJSONArray("candidates")
                        val content = candidates.getJSONObject(0).getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        val text = parts.getJSONObject(0).getString("text")
                        return@withContext text.trim()
                    }
                } else {
                    Log.e(TAG, "Gemini call unsuccessful code: ${response.code}, message: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Gemini network call: ${e.message}")
            }
        }

        // Step 3: Default fallback response if offline and no keyword matched
        return@withContext "[مساعد ذكي] أهلاً بك في دليل خدمات اليمن! يمكنني إرشادك للعثور على أقرب نجار، سباك، كهربائي أو فني تبريد وتكييف. اسألني عن 'سباك'، 'سعر المعاينة'، أو 'كيفية تقديم شكوى' وسأجيبك فوراً."
    }
}
