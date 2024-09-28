package com.altibbi.telehealth.model

enum class RelationType(val type: String) {
    PERSONAL("personal"),
    FATHER("father"),
    MOTHER("mother"),
    SISTER("sister"),
    BROTHER("brother"),
    CHILD("child"),
    HUSBAND("husband"),
    WIFE("wife"),
    OTHER("other");
}