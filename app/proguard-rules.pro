# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/matthiasgranberry/Play/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepnames public class * extends io.realm.RealmObject
-keep class io.realm.** { *; }
-dontwarn javax.**
-dontwarn io.realm.**

-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-dontobfuscate
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-dontwarn sun.misc.Unsafe
-dontwarn com.google.appengine.api.urlfetch.*
-dontwarn android.renderscript.*
-dontwarn java.nio.file.*
-dontwarn org.w3c.dom.events.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn android.webkit.WebView
-dontwarn com.squareup.okhttp.internal.huc.HttpsURLConnectionImpl
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
-dontwarn com.squareup.okhttp.**
-keep class com.kludgenics.cgmlogger.app.** { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep class com.github.mikephil.charting.** { *; }
-keep class com.kludgenics.cgmlogger.app.adapter.AgpAdapter$onCreateViewHolder$card$1$1
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers class * extends android.support.design.widget.CoordinatorLayout$Behavior {
   public <init>();
}

-keepclassmembers class android.support.design.widget.FloatingActionButton$Behavior2 {
   public <init>();
}

-keepclassmembers class * extends android.view.View { 
   public <init>();
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
