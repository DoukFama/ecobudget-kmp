package com.example.resources

import com.example.model.Category
import com.example.shared.resources.Res
import com.example.shared.resources.category_alimentation
import com.example.shared.resources.category_logement
import com.example.shared.resources.category_loisirs
import com.example.shared.resources.category_transport
import org.jetbrains.compose.resources.StringResource

/**
 * Correspondance entre le modèle de domaine (`Category`, pur Kotlin) et le catalogue
 * de ressources textuelles partagé (`Res.string.*`).
 *
 * Cette séparation garde `commonMain/model` totalement neutre : le modèle ne connaît
 * ni les ressources Android (`R`) ni celles de Compose Multiplatform.
 */
val Category.labelRes: StringResource
    get() = when (this) {
        Category.TRANSPORT -> Res.string.category_transport
        Category.ALIMENTATION -> Res.string.category_alimentation
        Category.LOISIRS -> Res.string.category_loisirs
        Category.LOGEMENT -> Res.string.category_logement
    }
