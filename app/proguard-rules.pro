# AnchorGuard ProGuard/R8 rules
# Keep generic attributes for reflection/serialization/DI while allowing shrinking/obfuscation.
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions,*Annotation*,SourceFile,LineNumberTable

# Kotlin/Compose/coroutines metadata used by reflection and tooling.
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { *; }
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# AnchorGuard runtime entry points referenced from manifest/services/receivers/providers.
-keep class com.anchorguard.client.** { *; }

# Hilt/Dagger generated components and adapters.
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Retrofit/Gson/OkHttp model and interface reflection.
-keep interface com.anchorguard.client.**.*Api { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
