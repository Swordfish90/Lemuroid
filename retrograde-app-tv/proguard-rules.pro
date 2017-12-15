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
-keep class android.arch.lifecycle.**

## Retrograde
-keep class com.codebutler.retrograde.lib.retro.**

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
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8

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
-keepattributes SourceFile,LineNumberTable,Signature,JavascriptInterface,Exceptions
-verbose

# The Google API library pulled in a bunch of apache/guava crap.
# TODO: Get rid of this soon.
-dontwarn org.apache.http.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.avalon.**
-dontwarn javax.servlet.**
-dontnote org.apache.log4j.**
-dontnote com.google.appengine.api.ThreadManager
-dontnote com.google.apphosting.api.ApiProxy
