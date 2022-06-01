# RosettaX

A fork of [rosetta](https://github.com/ahmedaljazzar/rosetta) language selector with AndroidX and Material support

Fork originally created by [@DerGoogler](https://github.com/DerGoogler) for [FoxMMM](https://github.com/Fox2Code/FoxMagiskModuleManager)

## Improvement

Apply language on startup with `androidx.startup:startup-runtime`

Smart Material support (only use `com.google.android.material:material` if imported)

## Add to Gradle

Add jitpack, example to add to `settings.gradle`:
```groovy
// Only add if `dependencyResolutionManagement` already exists
dependencyResolutionManagement {
    repositories {
        maven {
            url 'https://jitpack.io'
        }
    }
}
```


```groovy
// Only add "repositories" if "dependencyResolutionManagement" didn't exists in "settings.gradle"
repositories {
    maven {
        url 'https://jitpack.io'
    }
}


dependencies {
    implementation 'com.github.Fox2Code:RosettaX:1.0.0'
}
```
