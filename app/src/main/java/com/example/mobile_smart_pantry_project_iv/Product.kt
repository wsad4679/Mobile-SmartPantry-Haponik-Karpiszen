package com.example.mobile_smart_pantry_project_iv

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val UUID: String,
    val Name: String,
    val Quantity: Int,
    val Category: String,
    val ImageRef: String
)