import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  // Module multiplateforme (Kotlin Multiplatform)
  alias(libs.plugins.kotlin.multiplatform)
  // Génération de la bibliothèque binaire Android (AAR)
  alias(libs.plugins.android.library)
  // Compose Multiplatform : catalogue de ressources partagé (objet Res)
  alias(libs.plugins.compose.multiplatform)
  // Compilateur Compose obligatoire depuis Kotlin 2.0+
  alias(libs.plugins.kotlin.compose)
}

kotlin {
  // Cible Android
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  // Cibles iOS avec génération du framework binaire consommé par Xcode
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "shared"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      // Asynchronisme et flux réactifs (StateFlow / Flow)
      implementation(libs.kotlinx.coroutines.core)

      // Gestion de l'état UI et du cycle de vie multiplateforme (ViewModel KMP)
      implementation(libs.androidx.lifecycle.viewmodel)

      // Manipulation multiplateforme des dates et heures
      implementation(libs.kotlinx.datetime)

      // Ressources CMP exposées au module app (StringResource / objet Res)
      api(compose.components.resources)
      implementation(compose.runtime)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android {
  namespace = "com.example.shared"
  compileSdk = 35
  defaultConfig {
    minSdk = 24
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

// Autorise le module Android 'app' à consommer l'objet Res généré (visibilité public)
compose.resources {
  publicResClass = true
  packageOfResClass = "com.example.shared.resources"
}
