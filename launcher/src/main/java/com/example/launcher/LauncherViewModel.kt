package com.gemileith.launcher

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.gemileith.core.LauncherItem
import com.gemileith.core.LauncherState

class LauncherViewModel {
    private val _uiState = mutableStateOf(
        LauncherState(
            apps = listOf(
                LauncherItem(1, "Mail", "Email", "com.example.mail", "M"),
                LauncherItem(2, "Calendar", "Schedule", "com.example.calendar", "C"),
                LauncherItem(3, "Settings", "System", "com.example.settings", "S"),
                LauncherItem(4, "Messages", "Chat", "com.example.messages", "T")
            )
        )
    )

    val uiState: State<LauncherState> = _uiState

    fun onQueryChanged(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.apps
        } else {
            _uiState.value.apps.filter { it.title.contains(query, ignoreCase = true) }
        }
        _uiState.value = _uiState.value.copy(query = query, apps = filtered)
    }

    fun launchApp(app: LauncherItem) {
        _uiState.value = _uiState.value.copy(selectedApp = app)
    }
}
