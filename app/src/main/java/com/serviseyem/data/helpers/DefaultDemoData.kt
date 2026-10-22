package com.serviseyem.data.helpers

import com.serviseyem.data.*

/**
 * 📦 Default Demo Data Helper
 * يوفر بيانات أولية متوافقة مع SampleDataInitializer تمثل:
 * 1. فني سباكة
 * 2. متجر إلكترونيات
 * 3. مطعم
 * 4. مركز طبي
 * 5. مكتب عقارات
 */
object DefaultDemoData {

    fun getDemoProviders(): List<ProviderEntity> {
        return listOf(
            ProviderEntity(
                id = "sample_tech_plumber_01",
                name = "فني سباكة وصيانة صحية متقدمة",
                phone = "777123456",
                city = "صنعاء",
                area = "شارع الستين الغربي - جولة مذبح",
                profession = "فني سباكة",
                specialization = "تأسيس وصيانة شبكات المياه وكشف التسريبات",
                rating = 4.9f,
                numReviews = 48,
                isAvailable = true,
                isVerified = true,
                isVip = true,
                latitude = 15.3694,
                longitude = 44.1910
            )
        )
    }

    fun getDemoStores(): List<StoreEntity> {
        return listOf(
            // متجر إلكترونيات
            StoreEntity(
                id = "sample_store_electronics_01",
                name = "متجر البرق للإلكترونيات والأجهزة الذكية",
                phone = "771234567",
                city = "صنعاء",
                area = "حدة - تقاطع شارع صخر",
                category = "إلكترونيات",
                description = "بيع وصيانة أحدث الهواتف الذكية والشاشات وأنظمة المراقبة",
                rating = 4.8f,
                numReviews = 76,
                isVerified = true,
                isVip = true,
                latitude = 15.3520,
                longitude = 44.2050
            ),
            // مطعم
            StoreEntity(
                id = "sample_restaurant_01",
                name = "مطعم البيت اليمني للمأكولات الشعبية",
                phone = "772345678",
                city = "صنعاء",
                area = "شارع حدة - أمام مركز الكميم",
                category = "مطاعم وكافيهات",
                description = "أشهى المأكولات التراثية اليمنية، مندي، فحسة، مشويات وقسم عائلي",
                rating = 4.9f,
                numReviews = 132,
                isVerified = true,
                isVip = true,
                latitude = 15.3340,
                longitude = 44.1870
            ),
            // مركز طبي
            StoreEntity(
                id = "sample_medical_center_01",
                name = "مركز الشفاء الطبي التخصصي",
                phone = "773456789",
                city = "عدن",
                area = "المنصورة - شارع التسعين",
                category = "مراكز طبية وعيادات",
                description = "مجمع عيادات تخصصية، مختبر تحاليل شامل، وصيدلية 24 ساعة",
                rating = 4.95f,
                numReviews = 95,
                isVerified = true,
                isVip = true,
                latitude = 12.8250,
                longitude = 45.0120
            )
        )
    }

    fun getDemoProperties(): List<PropertyEntity> {
        return listOf(
            // مكتب عقارات
            PropertyEntity(
                id = "sample_real_estate_01",
                title = "مكتب الأمل للخدمات والاستشارات العقارية",
                phone = "774567890",
                city = "تعز",
                area = "شارع جمال - برج الأمل",
                propertyType = "عقارات وأراضي",
                description = "تسويق وبيع وشراء الأراضي السكنية والتجارية وتأجير الشقق والفلل",
                price = 0.0,
                isAvailable = true,
                isVip = true,
                latitude = 13.5790,
                longitude = 44.0200
            )
        )
    }

    fun getDemoBanners(): List<BannerEntity> {
        return listOf(
            BannerEntity(
                id = "banner_01",
                title = "منصة خدمات اليمن الشاملة",
                subtitle = "دليلك الأول لكافة الفنيين والمتاجر والخدمات المعتمدة",
                actionUrl = ""
            )
        )
    }
}
