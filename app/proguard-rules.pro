# Custom ProGuard rules for the app
-keepattributes Signature,InnerClasses,EnclosingMethod

# Keep Firebase models
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep our data model classes from being obfuscated or removed in release builds
-keep class com.Serviseyem.models.** { *; }
-keep class com.Serviseyem.services.** { *; }
