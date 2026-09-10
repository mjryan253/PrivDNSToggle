# Default ProGuard rules for PrivDNSToggle
-keepattributes *Annotation*

# Shizuku instantiates the user service by class name via reflection in a separate process;
# keep the class name, constructor and methods.
-keep class com.privdnstoggle.app.ShizukuUserService { *; }
