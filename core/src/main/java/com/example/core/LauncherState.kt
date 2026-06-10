package com.example.core

import androidx.compose.runtime.Immutable

@Immutable
data class LauncherState(
    val title: String = "Gemileith Launcher",
    val subtitle: String = "System Control",
    val isReady: Boolean = true,
    val query: String = "",
    val apps: List<LauncherItem> = emptyList(),
    val selectedApp: LauncherItem? = null
)
