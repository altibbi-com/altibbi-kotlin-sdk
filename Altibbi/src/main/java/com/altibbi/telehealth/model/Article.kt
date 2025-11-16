package com.altibbi.telehealth.model

data class Article(
    val articleId: Int,
    val slug: String,
    val subCategoryId: Int,
    val title: String,
    val body: String,
    val articleReferences: String,
    val activationDate: String,
    val publishStatus: String,
    val adultContent: Boolean,
    val featured: Boolean,
    val dateAdded: String,
    val dateModified: String,
    val bodyClean: String,
    val imageUrl: String,
    val url: String
)