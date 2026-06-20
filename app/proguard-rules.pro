# osmdroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.tertiaryinfotech.freefood.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.tertiaryinfotech.freefood.**$$serializer { *; }

# Room (generated) is safe by default; nothing extra needed.
