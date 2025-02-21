# Keep all model classes (used in Firebase, Gson, Retrofit, etc.)
-keep class com.dev3mk.awraqi.data.model.** { *; }

# Keep annotated classes (Gson, Firestore, Room, etc.)
-keepattributes *Annotation*

# Keep Firebase-related classes
-keep class com.google.firebase.** { *; }

# Keep Gson model classes
-keep class com.google.gson.** { *; }

# Keep Parcelable classes
-keep class * implements android.os.Parcelable { *; }