# EcoBudget 🌿 — Migration Kotlin Multiplatform (KMP)

Projet de l'UE **Développement mobile avancé** (2025-2026), réalisé d'après le
*Chapitre 03 – Export Local & Amorçage de l'Intégration KMP*.

Objectif : faire évoluer le prototype **Android natif** EcoBudget vers une architecture
multiplateforme reposant sur **Kotlin Multiplatform (KMP)** et **Compose Multiplatform (CMP)**,
en extrayant la logique métier, la gestion des données, l'état d'interface et les ressources
textuelles vers un module partagé `shared`, consommé par l'application Android native.

---

## 1. Architecture cible

```
EcoBudget/
├── settings.gradle.kts          # inclut :app et :shared
├── build.gradle.kts             # plugins KMP/Compose déclarés (apply false)
├── gradle/libs.versions.toml    # catalogue de versions centralisé
├── shared/                      # MODULE PARTAGÉ MULTIPLATEFORME
│   ├── build.gradle.kts         # kotlin.multiplatform + android.library + compose + kotlin.compose
│   └── src/
│       ├── commonMain/
│       │   ├── composeResources/values/strings.xml   # catalogue de textes centralisé
│       │   └── kotlin/com/example/
│       │       ├── utils/            # expect generateUUID() / getCurrentTimeMillis()
│       │       ├── model/            # Transaction, Category, YearMonth
│       │       ├── data/repository/  # TransactionRepository + FakeTransactionRepository
│       │       ├── viewmodel/        # EcoBudgetUiState + EcoBudgetViewModel
│       │       └── resources/        # mapping Category -> Res.string.*
│       ├── androidMain/kotlin/com/example/utils/   # actual (java.util.UUID / System)
│       ├── iosMain/kotlin/com/example/utils/       # actual (NSUUID / NSDate)
│       └── commonTest/kotlin/                       # tests unitaires partagés
└── app/                         # APPLICATION ANDROID NATIVE (interface Jetpack Compose)
    ├── build.gradle.kts         # implementation(project(":shared"))
    └── src/main/java/com/example/
        ├── MainActivity.kt
        └── ui/                  # screens, components, theme (UI native conservée)
```

Conformément au cours, **l'interface graphique reste native Android** dans `app` : elle
consomme le socle métier et les ressources du module `shared`.

---

## 2. Compilation & exécution (Android)

```bash
# SDK Android requis (local.properties -> sdk.dir)
./gradlew :app:assembleDebug      # génère app/build/outputs/apk/debug/app-debug.apk
./gradlew :shared:testDebugUnitTest   # tests unitaires du code commun (cible JVM/Android)
```

Résultat vérifié en local :

- `:app:assembleDebug` → **BUILD SUCCESSFUL**, APK généré.
- `:shared:testDebugUnitTest` → **7 tests, 0 échec**.

### Exécution sur émulateur

```bash
export ANDROID_SDK_ROOT=/home/ibdou/Android/Sdk
$ANDROID_SDK_ROOT/emulator/emulator -avd ecobudget -no-snapshot -no-audio -no-window -gpu swiftshader_indirect &
$ANDROID_SDK_ROOT/platform-tools/adb wait-for-device
$ANDROID_SDK_ROOT/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$ANDROID_SDK_ROOT/platform-tools/adb shell am start -n com.example.ecobudget/com.example.MainActivity
```

> L'AVD utilisé (API 35, `google_apis/x86_64`) est créé avec les *command-line tools* :
> `avdmanager create avd -n ecobudget -k "system-images;android-35;google_apis;x86_64" -d pixel_6`.

---

## 3. Document Technique de Synthèse

Pour chaque fichier migré vers `commonMain`, on documente :
**le problème rencontré**, **le choix technique appliqué** et **la justification multiplateforme**.

### 3.1 `shared/build.gradle.kts` — configuration du module partagé

- **Problème** : un module Android classique ne peut pas produire de binaires iOS, et le code
  commun doit pouvoir générer le catalogue de ressources Compose Multiplatform.
- **Choix technique** : plugins `kotlin("multiplatform")`, `com.android.library`,
  `org.jetbrains.compose` (Compose Multiplatform) et `org.jetbrains.kotlin.plugin.compose`
  (compilateur Compose obligatoire depuis Kotlin 2.0+). Cibles `androidTarget()` +
  `iosX64() / iosArm64() / iosSimulatorArm64()` avec génération d'un `framework` statique
  `shared`. Dépendances `commonMain` : `kotlinx-coroutines-core`, `lifecycle-viewmodel` KMP,
  `kotlinx-datetime`, `api(compose.components.resources)` et `implementation(compose.runtime)`.
  Bloc `android { }` avec namespace `com.example.shared`. Bloc `compose.resources { publicResClass = true }`.
- **Justification** : une seule base de code est compilée en **bytecode JVM** pour Android et en
  **binaire natif** pour iOS. `api(compose.components.resources)` et `publicResClass = true`
  exposent le type `StringResource` et l'objet `Res` au module `app`, condition indispensable
  pour que l'UI native consomme les textes partagés.

### 3.2 `shared/src/commonMain/.../utils/UUID.kt` (+ androidMain / iosMain)

- **Problème** : `Transaction` utilisait `java.util.UUID`, interdit dans `commonMain` et
  inexistant sur iOS.
- **Choix technique** : déclaration `expect fun generateUUID(): String`, puis `actual` :
  - `androidMain` : `UUID.randomUUID().toString()` (machine virtuelle Java) ;
  - `iosMain` : `NSUUID().UUIDString()` (framework Foundation d'Apple).
- **Justification** : `expect`/`actual` est résolu **statiquement à la compilation** ; chaque
  cible fournit son implémentation native, sans coût à l'exécution. Le modèle commun ne
  dépend plus d'aucune API Java.

### 3.3 `shared/src/commonMain/.../utils/Time.kt` (+ androidMain / iosMain)

- **Problème** : `System.currentTimeMillis()` est une API de la JVM, interdite dans le code commun.
- **Choix technique** : `expect fun getCurrentTimeMillis(): Long` ; `actual` Android via
  `System.currentTimeMillis()`, `actual` iOS via `NSDate().timeIntervalSince1970 * 1000`.
- **Justification** : abstraction minimale et ciblée (bonne pratique du cours : ne pas abuser
  d'`expect`/`actual` et préférer les bibliothèques KMP lorsque c'est possible).

### 3.4 `shared/src/commonMain/.../model/Transaction.kt`

- **Problème** : dépendance directe à `java.util.UUID` pour la génération de l'identifiant.
- **Choix technique** : `id: String = generateUUID()` s'appuie sur l'abstraction commune.
- **Justification** : le modèle de données devient 100 % agnostique de la plateforme ;
  `data class` immuable compilable pour Android et iOS.

### 3.5 `shared/src/commonMain/.../model/Category.kt`

- **Problème** : l'énumération portait `labelResId: Int`, identifiant de la classe `R`
  **exclusive à Android** (`com.example.R.string.*`), inutilisable dans `commonMain`.
- **Choix technique** : suppression de `labelResId` ; seule la donnée universelle `emoji` est
  conservée. La correspondance vers le catalogue partagé est déportée dans
  `shared/.../resources/CategoryResources.kt` via `val Category.labelRes: StringResource`.
- **Justification** : on garde le modèle de domaine **totalement neutre** (aucune référence aux
  ressources), tandis que la localisation reste centralisée et multiplateforme.

### 3.6 `shared/src/commonMain/.../model/YearMonth.kt`

- **Problème** : recours à `java.util.Calendar`, `java.text.SimpleDateFormat`, `java.util.Date`
  et `java.util.Locale` — tous interdits dans `commonMain`.
- **Choix technique** : réécriture complète avec **kotlinx-datetime** : `Instant.fromEpochMilliseconds(...)`,
  `toLocalDateTime(TimeZone.currentSystemDefault())`, `dateTime.month.number` ; les libellés
  français sont fournis par un tableau de noms de mois (plus de `SimpleDateFormat`).
- **Justification** : kotlinx-datetime est la bibliothèque officielle multiplateforme de gestion
  des dates/heures ; l'algorithme de navigation mensuelle (pur calcul) devient identique pour
  Android et iOS.

### 3.7 `shared/src/commonMain/.../data/repository/TransactionRepository.kt`

- **Problème** : aucun — c'est un contrat pur.
- **Choix technique** : migration telle quelle vers `commonMain` ; seules dépendances :
  `kotlinx.coroutines.flow.Flow` et le modèle `Transaction`.
- **Justification** : une interface et des flux `Flow`/`StateFlow` fonctionnent à l'identique sur
  toutes les cibles (le cours rappelle que les modèles et interfaces sont la première vague migrée).

### 3.8 `shared/src/commonMain/.../data/repository/FakeTransactionRepository.kt`

- **Problème** : utilisation de `java.util.Calendar` (construction du jeu de données multi-mois)
  et de `java.util.UUID`.
- **Choix technique** : remplacement par `LocalDate` / `DatePeriod` / `LocalDate.plus` /
  `LocalDateTime(...).toInstant(TimeZone.currentSystemDefault())` de kotlinx-datetime, et par
  `generateUUID()`. L'horloge provient de `getCurrentTimeMillis()`.
- **Justification** : la construction des dates cesse d'être dépendante du fuseau/`Calendar` Java
  et devient portable ; `StateFlow` conserve son comportement réactif sur iOS.

### 3.9 `shared/src/commonMain/.../viewmodel/EcoBudgetUiState.kt`

- **Problème** : l'état d'interface vivait dans le module Android.
- **Choix technique** : extraction dans `commonMain` et annotation `@Immutable` (Compose).
  Les propriétés dérivées (`budgetUsageRatio`, `budgetUsagePercentage`, `isAllCategoriesSelected`)
  restent du pur calcul Kotlin.
- **Justification** : `@Immutable` aide le moteur de recomposition Compose à optimiser les
  redessins sur toutes les plateformes ; la structure est observable via `StateFlow` sans
  dépendance au SDK Android.

### 3.10 `shared/src/commonMain/.../viewmodel/EcoBudgetViewModel.kt`

- **Problème** : la classe héritait de `androidx.lifecycle.ViewModel` **exclusif Android**, et
  utilisait `java.util.Calendar`, `java.util.UUID`, `System.currentTimeMillis()`.
- **Choix technique** :
  - dépendance **Jetpack Lifecycle KMP** (`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel`) ;
  - `viewModelScope` importé depuis le code commun ;
  - dates via kotlinx-datetime, identifiants via `generateUUID()`, horloge via `getCurrentTimeMillis()` ;
  - correction d'un décalage de base : `monthNumber = currentYearMonth.month + 1` (le mois du
    modèle est indexé de 0 à 11, `LocalDateTime` attend 1 à 12).
- **Justification** : Google a porté `ViewModel`/`viewModelScope` vers KMP ; l'opérateur
  `combine` et la chaîne réactive `StateFlow` restent **strictement identiques**. Le composant de
  présentation est entièrement neutralisé des spécificités Android.

### 3.11 Ressources textuelles — `shared/src/commonMain/composeResources/values/strings.xml`

- **Problème** : les libellés étaient dans `app/src/main/res/values/strings.xml` et référencés via
  la classe `R` d'Android (`Int`), impossible à partager avec iOS.
- **Choix technique** : déplacement de toutes les chaînes dans
  `commonMain/composeResources/values/strings.xml` ; le plugin Compose Multiplatform génère
  l'objet `Res` (`com.example.shared.resources.Res`). L'UI native utilise désormais
  `stringResource(Res.string.xxx)` importé de `org.jetbrains.compose.resources`.
- **Justification** : un **unique catalogue XML central** pour Android et iOS, accès **typé et
  sécurisé** (les clés manquantes sont détectées à la compilation). On conserve seulement
  `app_name` dans les ressources Android, car `AndroidManifest.xml` ne sait pas lire
  `composeResources`.

### 3.12 Module `app` — consommation du socle commun

- **Problème** : le module applicatif possédait sa propre copie des modèles, du dépôt et du ViewModel.
- **Choix technique** : `implementation(project(":shared"))` ; suppression des fichiers migrés et
  des dépendances redondantes (`kotlinx-coroutines-core/android`, base locale Room non utilisée) ;
  imports `R.string.*` remplacés par `Res.string.*` et
  `androidx.compose.ui.res.stringResource` par `org.jetbrains.compose.resources.stringResource`.
- **Justification** : l'application native devient un **simple point d'entrée** ; la logique est
  centralisée, ce qui supprime toute duplication et sécurise la cohérence Android/iOS.

---

## 4. Erreurs de compilation rencontrées et corrections

| Erreur | Cause | Correction |
|---|---|---|
| `Unresolved reference 'number'` sur `month.number` | `Month.number` est une **propriété d'extension** (`kotlinx.datetime.number`), pas un membre. | Ajout de `import kotlinx.datetime.number` dans `YearMonth.kt` et `FakeTransactionRepository.kt`. |
| `Unresolved reference 'Calendar' / 'UUID' / 'System'` | Utilisation d'API JVM dans `commonMain`. | Remplacement par `kotlinx-datetime` et par les abstractions `generateUUID()` / `getCurrentTimeMillis()`. |
| `labelResId` introuvable dans le code commun | `R` est propre à Android. | Suppression de la propriété et mapping `Category.labelRes` vers `Res.string.*`. |
| Avertissement *Kotlin Multiplatform ↔ AGP compatibility* (AGP 8.10 > max testé 8.5) | Version d'AGP plus récente que la matrice officielle KGP. | Build validé ; avertissement neutralisé via `kotlin.mpp.androidGradlePluginCompatibility.nowarn=true`. |
| Cibles `iosArm64/iosX64/iosSimulatorArm64` désactivées | Compilation Apple impossible hors macOS. | Configuration conservée (le framework est généré par Xcode sur Mac) ; `kotlin.native.ignoreDisabledTargets=true` sur cette machine Linux. |
| `%s` / `%d` **non substitués à l'exécution** (`Total dépensé : %s FCFA`) | Le moteur de ressources Compose Multiplatform 1.6.11 n'interpole **que les arguments positionnels** (regex interne `%(\d)\$[ds]`), et ne traite pas `%%`. | Tous les placeholders du catalogue passés en **positionnel** : `%1$s`, `%1$d`, et `%%` remplacé par `%` (`%1$d% consommé`). |

---

## 5. Validation fonctionnelle

### 5.1 Compilation et tests

- Compilation de l'application Android : **OK** (`:app:assembleDebug` → APK généré).
- Tests unitaires du code commun : **7/7 OK** (navigation mensuelle `YearMonth`, calculs dérivés
  de `EcoBudgetUiState`).

### 5.2 Exécution réelle sur émulateur Android

Application **installée et exécutée** sur un émulateur Android (AVD `ecobudget`, API 35,
`x86_64`) via `adb install` puis `adb shell am start`. Vérifications réalisées (captures dans
[`docs/screenshots/`](docs/screenshots/)) :

| Vérification demandée | Résultat observé sur l'émulateur | Capture |
|---|---|---|
| **Tableau de bord** | Écran d'accueil affiché : mois courant « Septembre 2026 », budget restant **134 200 FCFA**, total dépensé **365 800 FCFA**, jauge **73 %** | `01_dashboard.png` |
| **Calcul des dépenses** | Somme des dépenses du mois correcte (**365 800 FCFA**, 7 dépenses) et budget restant = budget − dépenses (**500 000 − 365 800 = 134 200**) | `01_dashboard.png` |
| **Navigation mensuelle** | Clic sur « mois suivant » → **Octobre 2026**, 2 dépenses, total **270 000 FCFA**, reste **230 000 FCFA** | `02_navigation_mois_suivant.png` |
| **Filtres par catégorie** | Clic sur le filtre **Transport** → libellé « 🚌 Transport », **2 dépenses**, total catégorie **6 000 FCFA** (2 500 + 3 500) | `03_filtre_transport.png` |
| Retour à « Tous » | Réaffichage des 7 dépenses du mois | `04_toutes_categories.png` |

Aucune exception fatale (`FATAL EXCEPTION`) relevée dans `logcat` pendant la session.

**Bug détecté et corrigé lors de cette validation** : les libellés contenant des placeholders
non positionnels (`%s`, `%d`, `%%`) s'affichaient littéralement. Le catalogue
`composeResources/values/strings.xml` a été corrigé pour n'utiliser que des arguments
positionnels (`%1$s`, `%1$d`), conformément au fonctionnement réel de Compose Multiplatform
(voir la ligne correspondante du tableau §4).

---

## 6. Auteur

Projet personnel — compte GitHub [@DoukFama](https://github.com/DoukFama).
