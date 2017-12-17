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
-keep class com.codebutler.retrograde.common.jna.**

## JNA
-dontwarn java.awt.*
-keep class com.sun.jna.* { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }

## Kotlin
-dontwarn kotlin.**
-dontnote kotlin.**
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }
-keep class android.arch.lifecycle.**

## OkHttp
-dontwarn okhttp3.**
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontnote com.android.org.conscrypt.SSLParametersImpl
-dontnote dalvik.system.CloseGuard
-dontnote sun.security.ssl.SSLContextImpl

## Okio
-dontwarn okio.**

## Picasso
-dontwarn com.squareup.okhttp.**
-dontnote com.squareup.okhttp.**

## Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

## Moshi
-dontnote sun.misc.Unsafe

## Misc
-dontwarn com.uber.javaxextras.**
-dontwarn java.lang.management.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn junit.**
-dontwarn com.google.errorprone.**
-dontnote android.net.http.*

## Google API
-keep class com.google.** { *;}
-keep interface com.google.** { *;}
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}


## The Google API library pulled in a bunch of apache/guava crap.
# TODO: Get rid of this soon.
-dontwarn org.apache.http.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.avalon.**
-dontwarn javax.servlet.**
-dontnote org.apache.log4j.**
-dontnote com.google.appengine.api.ThreadManager
-dontnote com.google.apphosting.api.ApiProxy

## Retrograde
-keep class com.codebutler.retrograde.lib.retro.**
-keep class **.model.**
-keepclassmembers class **.model.** {
  <init>(...);
  <fields>;
}
