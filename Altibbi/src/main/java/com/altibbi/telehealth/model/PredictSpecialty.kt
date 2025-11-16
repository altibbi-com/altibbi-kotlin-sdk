package com.altibbi.telehealth.model

data class SubCategory(
    val subCategoryId: Int,
    val nameEn: String,
    val nameAr: String
) {
    companion object {
        fun fromJson(json: Map<String, Any>): SubCategory {
            return SubCategory(
                subCategoryId = json["sub_category_id"] as Int,
                nameEn = json["name_en"] as String,
                nameAr = json["name_ar"] as String
            )
        }
    }
}

data class PredictSpecialty(
    val specialtyId: Int,
    val subCategories: List<SubCategory>
) {
    companion object {
        fun fromJson(json: Map<String, Any>): PredictSpecialty {
            val subCategoriesJson = json["subCategories"] as List<Map<String, Any>>
            val subCategoryList = subCategoriesJson.map { SubCategory.fromJson(it) }

            return PredictSpecialty(
                specialtyId = json["specialty_id"] as Int,
                subCategories = subCategoryList
            )
        }
    }
}