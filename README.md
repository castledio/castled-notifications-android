<p align="center">
  <a href="https://castled.io/#gh-light-mode-only">
    <img src="https://cdn.castled.io/logo/castled_logo_light_mode.png" width="318px" alt="Castled logo" />
  </a>
  <a href="https://castled.io/#gh-dark-mode-only">
    <img src="https://cdn.castled.io/logo/castled_logo_dark_mode.png" width="318px" alt="Castled logo" />
    <p align="center">Customer Engagement Platform for the Modern Data Stack</p>
  </a>
</p>

---

![min Android SDK version is 24](https://img.shields.io/badge/min%20Android%20SDK-24-green)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.castled.android/castled-notifications/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.castled.android/castled-notifications)
[![publish artifacts](https://github.com/castledio/castled-notifications-android/actions/workflows/publish.yaml/badge.svg)](https://github.com/castledio/castled-notifications-android/actions/workflows/publish.yaml)

# Castled Android SDK

## :star: Introduction

Castled Android SDK enables mobile applications running on Android devices to receive push and in-app notifications originating from the Castled Customer Engagement Platform. The steps outlined below will assist your Android app developers in integrating the SDK with your mobile application

## :gift: Contents

This repo is split into two libraries. Include the appropriate ones based on your use case.

| Group Id           | Artifact Id           | Latest Version                                                                                                        | Description                                                                                                                                     |
| ------------------ | --------------------- | --------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| io.castled.android | castled-notifications | ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.castled.android/castled-notifications/badge.svg) | Core sdk library to include for displaying push notifications and in-app messages                                                               |
| io.castled.android | castled-mipush        | ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.castled.android/castled-mipush/badge.svg)        | Supports sending push messages using Xiaomi MiPush framework. Recommended to include this library if you app has users on Xiaomi mobile devices |

## :roller_coaster: Getting Started

### Installing SDK

Add `castled-notifications` as a dependency in the `build.gradle` file of your Android application module. SDK libraries are available in `maven` repository

```groovy
    ...
    dependencies {
        ...
        implementation 'io.castled.android:castled-notifications:<sdk-latest-version>'
    }
```

Replace `latest-version` with the latest version number of the SDK found here. Sync the gradle file to pull the dependencies just added.

### Initializing SDK

The next step is to initialize the SDK. Initialization is typically done in the `onCreate` method of your
`Application` class as follows.

```kotlin MyApplicationClass.kt
class MyApplicationClass : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // SDK initialization
        CastledNotifications.initialize(
            this, CastledConfigs.Builder()
                .apiKey("<api-key>")
                .location(<location>)
                .build()
        )
        ...
    }
    ...
}
```

`api-key` is a unique key associated with your Castled account. It can be found in the Castled dashboard at **Settings > Api Keys**. `location` is the region where you have your Castled account. It can be one of the following locations.

```kotlin CastledConfigs.kt
    enum class CastledLocation {
        US,   // United States
        EU,   // Europe
        IN,   // India
        AP    // Asia Pacific
    }
```

## :books: Documentation

More details about the SDK integration can be found at https://docs.castled.io/developer-resources/sdk-integration/overview
