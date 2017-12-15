## Arch Components
-keep class * implements android.arch.lifecycle.GeneratedAdapter {<init>(...);}

## Fabric
-dontnote com.google.android.gms.**
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
-dontnote com.android.org.conscrypt.SSLParametersImpl
-dontnote org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
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

## Misc
-dontwarn com.uber.javaxextras.**
-dontwarn java.lang.management.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn junit.**
-dontnote android.net.http.*
-dontnote org.apache.http.**
-keepattributes SourceFile,LineNumberTable,Signature,JavascriptInterface,Exceptions
-verbose
