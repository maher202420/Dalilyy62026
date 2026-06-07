package com.Serviseyem.services

import com.Serviseyem.models.ServiceItem
import java.util.Locale

object SystemAiAssistant {

    /**
     * Responds to user queries about Yemeni services, maintenance, and administrative guidelines in WAM.
     */
    fun getAssistantResponse(userQuery: String, availableServices: List<ServiceItem>): String {
        val query = userQuery.lowercase(Locale.ROOT).trim()
        
        // 1. Plumbing / سباكة
        if (query.contains("سباك") || query.contains("سباكة") || query.contains("مجرى") || query.contains("ماء") || query.contains("تسريب")) {
            val plumbers = availableServices.filter { it.category.contains("سباكة") || it.title.contains("سباك") || it.description.contains("سبا") }
            return if (plumbers.isNotEmpty()) {
                "🔧 تتوفر لدينا خدمة السباكة الفورية في الدليل المعتمد بـ WAM!\n" +
                "إليك أفضل مقترحات السباكة المتوفرة حالياً:\n" +
                plumbers.joinToString("\n") { "• *${it.title}* - هاتف: ${it.providerPhone} (${it.price})" } +
                "\n\nيمكنك الاتصال بهم مباشرة أو الاتجاه لتبويب الأقسام للتفاصيل الجغرافية والموقع."
            } else {
                "🔧 بخصوص مشكلة السباكة، ننصحك بالتحكم الفوري بمحبس المياه الرئيسي لتجنب تسربات أكبر.\n" +
                "نسعى لتحديث الدليل وإضافة فنيين سباكة مؤهلين في منطقتك حالياً. يمكنك الاتصال بنا على 777644670 للدعم الاستثنائي."
            }
        }

        // 2. Electricity / كهرباء
        if (query.contains("كهرباء") || query.contains("كهربائي") || query.contains("ماس") || query.contains("عداد") || query.contains("طاقة")) {
            val electricians = availableServices.filter { it.category.contains("كهرباء") || it.title.contains("كهرب") || it.description.contains("كهرب") }
            return if (electricians.isNotEmpty()) {
                "⚡ دليل الكهرباء المتكامل في خدمتك!\n" +
                "أفضل فنيي الكهرباء والطاقة الشمسية المتوفرون بنظام المزامنة الفوري:\n" +
                electricians.joinToString("\n") { "• *${it.title}* - هاتف: ${it.providerPhone} (${it.price})" } +
                "\n\nيرجى أخذ الحيطة والحذر والابتعاد عن مصادر الماس المائي المباشر!"
            } else {
                "⚡ أهلاً بك. بخصوص أعطال الكهرباء أو منظومات الطاقة الشمسية، يرجى فصل القاطع الرئيسي فوراً لحين وصول فني مؤهل.\n" +
                "دليل WAM يعمل على توفير أفضل المهندسين في أمانة العاصمة والمحافظات."
            }
        }

        // 3. Carpentry & Maintenance / نجارة وصيانة أملاك
        if (query.contains("نجار") || query.contains("نجارة") || query.contains("باب") || query.contains("خشب") || query.contains("مطابخ")) {
            val carpenters = availableServices.filter { it.category.contains("نجارة") || it.title.contains("نجار") || it.description.contains("خشب") }
            return if (carpenters.isNotEmpty()) {
                "🪚 تم تصفية فنيي النجارة والديكور الخشبي وفق رغبتك:\n" +
                carpenters.joinToString("\n") { "• *${it.title}* - هاتف: ${it.providerPhone} (${it.executionTime})" }
            } else {
                "🪚 تتوفر خدمات صيانة الأبواب والنجارة الفورية وقص المطابخ. يرجى مراجعة المهنيين بالأقسام العامة أو عبر الاتصال المباشر بإدارة WAM."
            }
        }

        // 4. Official papers / جواز، رخص، معاملات قانونية
        if (query.contains("جواز") || query.contains("رخصة") || query.contains("سند") || query.contains("معاملة") || query.contains("محامي")) {
            return "📁 معاملات إدارية وقانونية:\n" +
                   "لإتمام معاملات استخراج وتجديد الجوازات، رخص القيادة والسندات العقارية المعتمدة:\n" +
                   "• تأكد من إحضار كافة الوثائق الرسمية وبطاقتك الشخصية وصورة معمدة.\n" +
                   "• يمكنك التواصل مع المستشار في قسم الشؤون والمحاماة مباشرة عبر الدليل للترتيب الفوري."
        }

        // 5. General Greeting & fallbacks
        return "👋 مرحباً بك في المساعد الذكي لدليل خدمات WAM يمن الثقة المتبادلة!\n" +
               "نحن نوفر خدمات سباكة، كهرباء، صيانة منظومات شمسية، تعقيب معاملات، وصيانة عامة برعاية إدارية موحدة.\n\n" +
               "🔑 يمكنك سؤالي عن أي خدمة أو فني تريد، مثال:\n" +
               "- \"أحتاج سباك سريع\"\n" +
               "- \"هل يوجد فني كهربائي في تعز؟\"\n" +
               "- \"كيف أستخرج معاملة جواز؟\"\n\n" +
               "دعمنا الفني جاهز دائماً عبر الهاتف الموحد 777644670."
    }
}
