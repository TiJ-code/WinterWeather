# ============================================================
# WinterWeather ProGuard configuration
# ============================================================


# ------------------------------------------------------------
# General Obfuscation & Build Settings
# ------------------------------------------------------------

# Use a single, compact package for obfuscated classes.
# Example:
#   dk.tij.winterweather.server.FreezeServerController
# becomes something similar to:
#   a.b
-repackageclasses 'a'

# Avoid mixed-case generated class names.
-dontusemixedcaseclassnames

# Do not emit informational notes.
-dontnote

# For now, only obfuscate/rename. Do not shrink or optimize.
# This is considerably safer for Fabric/Mixin/resource-driven code.
-dontshrink
-dontoptimize


# ------------------------------------------------------------
# Resource Files
# ------------------------------------------------------------

# Update class names contained in JSON resource files.
#
# This is important for:
#   fabric.mod.json
#   *.mixins.json
#
# For example:
#   dk.tij.winterweather.WinterWeather
#
# can be changed to the obfuscated class name in the processed JAR.
-adaptresourcefilecontents **.json


# ------------------------------------------------------------
# Dependency Warnings
# ------------------------------------------------------------

# Minecraft / Fabric / Mixin classes are library classes and may
# contain references ProGuard cannot fully resolve.
-dontwarn org.spongepowered.asm.**
-dontwarn net.fabricmc.**
-dontwarn com.mojang.**
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**


# ------------------------------------------------------------
# Runtime Metadata
# ------------------------------------------------------------

# Keep metadata required by Java reflection, Fabric, Mixin,
# generic types and other runtime mechanisms.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions


# ------------------------------------------------------------
# Fabric Main Entrypoint
# ------------------------------------------------------------

# Fabric must still be able to instantiate the entrypoint.
#
# allowobfuscation is intentional:
# the class name is allowed to change from
#   dk.tij.winterweather.WinterWeather
# to its ProGuard-generated name.
#
# fabric.mod.json is updated through:
#   -adaptresourcefilecontents **.json
#
-keep,allowoptimization,allowobfuscation class dk.tij.winterweather.WinterWeather {
    public <init>();
    public void onInitialize();
}


# ------------------------------------------------------------
# Fabric Client Entrypoint
# ------------------------------------------------------------

# Same principle as the main entrypoint.
-keep,allowoptimization,allowobfuscation class dk.tij.winterweather.client.WinterWeatherClient {
    public <init>();
    public void onInitializeClient();
}


# ------------------------------------------------------------
# Mixins
# ------------------------------------------------------------

# Keep Mixin classes themselves and all members required by
# the runtime transformation system.
#
# allowobfuscation is intentional so the actual class names can
# be changed. The corresponding Mixin JSON resource is updated
# by -adaptresourcefilecontents.
-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.Mixin class * {
    *;
}


# ------------------------------------------------------------
# Mixin Injection / Runtime Annotations
# ------------------------------------------------------------

# Preserve classes which use common Mixin runtime annotations.
# These are allowed to be renamed.
-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.Pseudo class * {
    *;
}

-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.Overwrite class * {
    *;
}

-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.Inject class * {
    *;
}

-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.Redirect class * {
    *;
}

-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.ModifyArg class * {
    *;
}

-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.ModifyArgs class * {
    *;
}

-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.ModifyVariable class * {
    *;
}

-keep,allowoptimization,allowobfuscation @org.spongepowered.asm.mixin.Constant class * {
    *;
}


# ------------------------------------------------------------
# Enums
# ------------------------------------------------------------

# Preserve enum methods used by Java/Minecraft serialization
# and registry-style lookups.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


# ------------------------------------------------------------
# Gson / Serialized Configuration Data
# ------------------------------------------------------------

# Your configuration classes may be accessed through Gson.
# Keep their members so JSON deserialization does not break.
#
# This is deliberately restricted to your own config package.
-keepclassmembers class dk.tij.winterweather.config.** {
    *;
}


# ------------------------------------------------------------
# Fabric Custom Payloads / Networking
# ------------------------------------------------------------

# Payload classes can be referenced through Fabric's networking
# registration system and serialization code.
#
# Keep members while still allowing their class names to change.
-keepclassmembers class dk.tij.winterweather.network.** {
    *;
}


# ------------------------------------------------------------
# Records
# ------------------------------------------------------------

# Preserve record component accessors / generated methods.
# Records are commonly used for serialized/network configuration
# objects in WinterWeather.
-keepclassmembers class * extends java.lang.Record {
    <fields>;
    <methods>;
}


# ------------------------------------------------------------
# Annotations
# ------------------------------------------------------------

# Keep annotation-related metadata on your own classes.
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault


# ------------------------------------------------------------
# Source File / Line Information
# ------------------------------------------------------------

# Intentionally omitted for release builds.
#
# Add these during debugging if stack traces need source information:
#
# -keepattributes SourceFile,LineNumberTable
# -renamesourcefileattribute SourceFile

# ------------------------------------------------------------
# Mixin packages
# ------------------------------------------------------------

-keep class dk.tij.winterweather.mixin.** {
    *;
}

-keep class dk.tij.winterweather.mixin.client.** {
    *;
}