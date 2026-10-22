package com.serviseyem.data.local

import android.content.Context
import android.util.Log
import com.serviseyem.data.BusinessType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 🌟 SampleDataInitializer
 * يقوم بإدراج بيانات تجريبية أولية تمثل:
 * 1. فني سباكة (Plumbing Technician)
 * 2. متجر إلكترونيات (Electronics Store)
 * 3. مطعم (Restaurant)
 * 4. مركز طبي (Medical Center)
 * 5. مكتب عقارات (Real Estate Office)
 * 
 * في قاعدة بيانات Room عند بدء التشغيل الأول للتطبيق، مع ضمان تام لعدم تكرار البيانات.
 */
object SampleDataInitializer {

    private const val TAG = "SampleDataInitializer"
    private const val PREFS_NAME = "ys_sample_data_preferences"
    private const val KEY_IS_SEEDED = "is_sample_data_seeded_v1"

    // المعرفات الثابتة والفريدة لضمان عدم التكرار على مستوى المفتاح الأساسي (Primary Key)
    const val ID_PLUMBER = "sample_tech_plumber_01"
    const val ID_ELECTRONICS = "sample_store_electronics_01"
    const val ID_RESTAURANT = "sample_restaurant_01"
    const val ID_MEDICAL = "sample_medical_center_01"
    const val ID_REAL_ESTATE = "sample_real_estate_01"

    /**
     * قائمة العناصر التجريبية الخمسة المحددة بدقة
     */
    val sampleBusinesses: List<BusinessRoomEntity> by lazy {
        listOf(
            // 1. فني سباكة
            BusinessRoomEntity(
                id = ID_PLUMBER,
                name = "فني سباكة وصيانة صحية متقدمة",
                businessType = BusinessType.TECHNICIAN.name,
                categoryId = "services",
                description = "خدمات سباكة احترافية وتأسيس وصيانة شبكات المياه والصرف الصحي، كشف وإصلاح التسريبات بأحدث الأجهزة الإلكترونية وتركيب المضخات والسخانات.",
                phone = "777123456",
                ownerName = "م. أحمد السباعي",
                cityId = "صنعاء",
                neighborhood = "شارع الستين الغربي - جولة مذبح",
                rating = 4.9f,
                numReviews = 48,
                isVerified = true,
                isVip = true,
                workingHours = "24 ساعة / طوارئ متوفرة",
                latitude = 15.3694,
                longitude = 44.1910,
                logoUrl = "",
                coverUrl = "",
                createdAt = 1713500000000L
            ),
            // 2. متجر إلكترونيات
            BusinessRoomEntity(
                id = ID_ELECTRONICS,
                name = "متجر البرق للإلكترونيات والأجهزة الذكية",
                businessType = BusinessType.STORE.name,
                categoryId = "stores",
                description = "بيع وصيانة أحدث الهواتف الذكية والشاشات، أنظمة كاميرات المراقبة، الحواسيب المحمولة ومستلزمات الطاقة الشمسية وبطاريات الليثيوم.",
                phone = "771234567",
                ownerName = "أ. وائل القدسي",
                cityId = "صنعاء",
                neighborhood = "حدة - تقاطع شارع صخر",
                rating = 4.8f,
                numReviews = 76,
                isVerified = true,
                isVip = true,
                workingHours = "9:00 ص - 10:00 م",
                latitude = 15.3520,
                longitude = 44.2050,
                logoUrl = "",
                coverUrl = "",
                createdAt = 1713500100000L
            ),
            // 3. مطعم
            BusinessRoomEntity(
                id = ID_RESTAURANT,
                name = "مطعم البيت اليمني للمأكولات الشعبية",
                businessType = BusinessType.RESTAURANT.name,
                categoryId = "restaurants",
                description = "أشهى وألذ المأكولات التراثية اليمنية، مندي بلدي، حنيذ، فحسة وسلتة ساخنة على الفخار، مشويات طازجة وقسم عائلي متميز وراقي.",
                phone = "772345678",
                ownerName = "الحاج ناصر المفلحي",
                cityId = "صنعاء",
                neighborhood = "شارع حدة - أمام مركز الكميم",
                rating = 4.9f,
                numReviews = 132,
                isVerified = true,
                isVip = true,
                workingHours = "11:00 ص - 1:00 ص",
                latitude = 15.3340,
                longitude = 44.1870,
                logoUrl = "",
                coverUrl = "",
                createdAt = 1713500200000L
            ),
            // 4. مركز طبي
            BusinessRoomEntity(
                id = ID_MEDICAL,
                name = "مركز الشفاء الطبي التخصصي",
                businessType = BusinessType.MEDICAL.name,
                categoryId = "medical",
                description = "مجمع عيادات تخصصية متكامل: باطنية، أطفال، عظام، أسنان، مختبر تحاليل شامل بأحدث التجهيزات، وصيدلية تعمل على مدار 24 ساعة.",
                phone = "773456789",
                ownerName = "د. محمد باذيب",
                cityId = "عدن",
                neighborhood = "المنصورة - شارع التسعين",
                rating = 4.95f,
                numReviews = 95,
                isVerified = true,
                isVip = true,
                workingHours = "24 ساعة - طوارئ وعيادات",
                latitude = 12.8250,
                longitude = 45.0120,
                logoUrl = "",
                coverUrl = "",
                createdAt = 1713500300000L
            ),
            // 5. مكتب عقارات
            BusinessRoomEntity(
                id = ID_REAL_ESTATE,
                name = "مكتب الأمل للخدمات والاستشارات العقارية",
                businessType = BusinessType.REAL_ESTATE.name,
                categoryId = "properties",
                description = "تسويق وبيع وشراء الأراضي السكنية والتجارية، تأجير الشقق والفلل والعمائر، وتوثيق عقود الإيجار والضمانات القانونية الرسمية المعتمدة.",
                phone = "774567890",
                ownerName = "الشيخ فؤاد الشميري",
                cityId = "تعز",
                neighborhood = "شارع جمال - برج الأمل الدور الأول",
                rating = 4.7f,
                numReviews = 62,
                isVerified = true,
                isVip = true,
                workingHours = "8:30 ص - 8:30 م",
                latitude = 13.5790,
                longitude = 44.0200,
                logoUrl = "",
                coverUrl = "",
                createdAt = 1713500400000L
            )
        )
    }

    /**
     * تنفيذ إدراج البيانات التجريبية عبر BusinessDao مع فحص عدم التكرار
     */
    suspend fun initialize(businessDao: BusinessDao): Int = withContext(Dispatchers.IO) {
        var insertedCount = 0
        try {
            for (business in sampleBusinesses) {
                // الفحص الأول: التأكد من عدم وجود السجل مسبقاً في قاعدة بيانات Room
                val exists = businessDao.exists(business.id)
                if (!exists) {
                    val result = businessDao.insertIfNotExist(business)
                    if (result > 0) {
                        insertedCount++
                        Log.d(TAG, "✅ [Room] تم إدراج بيان تجريبي بنجاح: ${business.name} (${business.businessType})")
                    }
                } else {
                    Log.d(TAG, "ℹ️ [Room] البيان التجريبي موجود مسبقاً (تم منع التكرار): ${business.id}")
                }
            }
            Log.i(TAG, "🏁 اكتمال فحص وتهيئة البيانات التجريبية. عدد السجلات الجديدة المضافة: $insertedCount")
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطأ أثناء إدراج البيانات التجريبية في Room", e)
        }
        insertedCount
    }

    /**
     * تنفيذ التهيئة عبر كائن قاعدة بيانات AppDatabase
     */
    suspend fun initialize(database: AppDatabase): Int {
        return initialize(database.businessDao())
    }

    /**
     * تنفيذ التهيئة عبر Context مع حفظ مؤشر التشغيل الأول في SharedPreferences
     */
    suspend fun initialize(context: Context): Int = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val db = AppDatabase.getInstance(context)
        val inserted = initialize(db.businessDao())
        prefs.edit().putBoolean(KEY_IS_SEEDED, true).apply()
        inserted
    }

    /**
     * دالة غير متزامنة لتشغيل التهيئة بأمان في الخلفية عند بدء تشغيل التطبيق
     * دون تجميد واجهة المستخدم (UI) إطلاقاً
     */
    fun initializeAsync(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                initialize(context)
            } catch (e: Exception) {
                Log.e(TAG, "❌ خطأ في التهيئة غير المتزامنة للبيانات التجريبية: ${e.message}", e)
            }
        }
    }
}

/**
 * دالة مساعدة لتنفيذ SampleDataInitializer مباشرة بدلالة الـ Context
 */
suspend fun SampleDataInitializer(context: Context): Int {
    return SampleDataInitializer.initialize(context)
}

/**
 * دالة مساعدة لتنفيذ SampleDataInitializer مباشرة بدلالة AppDatabase
 */
suspend fun SampleDataInitializer(database: AppDatabase): Int {
    return SampleDataInitializer.initialize(database)
}
