## Options
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable,Signature,JavascriptInterface,Exceptions
-verbose

## Arch Components
-keep class * implements android.arch.lifecycle.GeneratedAdapter {<init>(...);}

## Fabric
-dontnote com.google.android.gms.**
-dontnote com.google.firebase.crash.FirebaseCrash

## Kotlin
-dontwarn kotlin.**
-dontnote kotlin.**
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }
-keep class android.arch.lifecycle.**
-dontwarn kotlinx.coroutines.flow.**
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

## Okio
-dontwarn okio.**

## OkHttp
-dontwarn okhttp3.**
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontnote com.android.org.conscrypt.SSLParametersImpl
-dontnote dalvik.system.CloseGuard
-dontnote sun.security.ssl.SSLContextImpl
-dontnote org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontnote org.conscrypt.ConscryptEngineSocket

## Retrofit
-dontwarn retrofit2.Platform$Java8
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

## Moshi
-dontnote sun.misc.Unsafe

## Google API
-dontwarn com.google.api.client.json.jackson2.JacksonFactory
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

## Guava
-dontnote com.google.appengine.api.ThreadManager
-dontnote com.google.apphosting.api.ApiProxy
-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn org.apache.commons.codec.binary.Base64
-dontwarn org.apache.commons.codec.binary.StringUtils

## Retrograde
-keep class **.model.**
-keepclassmembers class **.model.** {
  <init>(...);
  <fields>;
}

## Misc
-dontwarn com.uber.javaxextras.**
-dontwarn java.lang.management.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn junit.**
-dontwarn com.google.errorprone.**
-dontnote android.net.http.*

## Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.swordfish.lemuroid.**$$serializer { *; }
-keepclassmembers class com.swordfish.lemuroid.** {
    *** Companion;
}
-keepclasseswithmembers class com.swordfish.lemuroid.** {
    kotlinx.serialization.KSerializer serializer(...);
}

## LibretroDroid
-keep class com.swordfish.libretrodroid.** { *; }
