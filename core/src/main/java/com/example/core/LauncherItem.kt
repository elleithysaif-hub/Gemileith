package com.example.core

import androidx.compose.runtime.Immutable

@Immutable
data class LauncherItem(
    val id: Int,
    val title: String,
    val summary: String,
    val packageName: String,
    val iconLabel: String
)
