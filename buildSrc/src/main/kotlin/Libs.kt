object Versions {

    // Kotlin
    const val kotlin = "1.8.0"
    const val kotlinx = "1.7.0"
    const val coreKtx = "1.8.0"
    const val serializationConverter = "0.8.0"
    const val serializationJson = "1.5.0"
    const val workRuntimeKtx = "2.7.1"

    // Room
    const val room = "2.5.0"

    // Retrofit
    const val retrofit = "2.9.0"

    const val material = "1.9.0"

    // Firebase Messaging
    const val firebase = "23.1.1"

    // Glide
    const val glide = "4.14.2"

    // Test
    const val jUnit = "4.13.2"
    const val extJUnit = "1.1.4"

    // Default Android Dependencies
    const val appCompat = "1.6.1"
    const val lifecycleProcess = "2.5.1"
    const val constraintLayout = "2.1.4"

    // Google play
    const val google = "4.3.14"

    //Card View
    const val cardView = "1.0.0"

    //Recycle View
    const val recycleView = "1.3.1"

    //MVVM
    const val mvvmLifecycleExtensions = "2.2.0"
    const val mvvmLifecycleCommon = "2.5.1"
    const val mvvmLifecycleViewmodel = "2.5.1"
    const val mvvmActivity = "1.6.1"
    const val swiperefreshlayout = "1.1.0"
    const val fragment = "1.3.5"

}

object Libs {

    // Kotlin
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val kotlinSerialization = "org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val kotlinxCoroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinx}"
    const val workRuntimeKtx = "androidx.work:work-runtime-ktx:${Versions.workRuntimeKtx}"
    const val kotlinxSerializationConverter =
        "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:${Versions.serializationConverter}"
    const val kotlinxSerializationJson =
        "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serializationJson}"

    // Firebase Dependencies
    const val firebaseMessaging = "com.google.firebase:firebase-messaging:${Versions.firebase}"

    // Room
    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"

    // Retrofit
    const val converterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val material = "com.google.android.material:material:${Versions.material}"

    // Glide
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    // Test Dependencies
    const val jUnit = "junit:junit:${Versions.jUnit}"
    const val extJUnit = "androidx.test.ext:junit:${Versions.extJUnit}"

    // Default Android Dependencies
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val lifecycleProcess = "androidx.lifecycle:lifecycle-process:${Versions.lifecycleProcess}"
    const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"

    // Google
    const val googleServices = "com.google.gms:google-services:${Versions.google}"

    // cardView
    const val cardView = "androidx.cardview:cardview:${Versions.cardView}"

    //recycleView
    const val recycleView = "androidx.recyclerview:recyclerview:${Versions.recycleView}"

    //MVVM
    const val mvvmLifecycleExtensions =
        "androidx.lifecycle:lifecycle-extensions:${Versions.mvvmLifecycleExtensions}"
    const val mvvmLifecycleCommon =
        "androidx.lifecycle:lifecycle-common-java8:${Versions.mvvmLifecycleCommon}"
    const val mvvmLifecycleViewmodel =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.mvvmLifecycleViewmodel}"
    const val mvvmActivity = "androidx.activity:activity-ktx:${Versions.mvvmActivity}"
    const val swiperefreshlayout =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swiperefreshlayout}"
    const val fragment = "androidx.fragment:fragment-ktx::${Versions.fragment}"
}
