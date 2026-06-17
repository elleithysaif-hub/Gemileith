package com.gemileith.launcher

import com.gemileith.core.LauncherItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherViewModelTest {
    @Test
    fun `filtering by query returns matching apps`() {
        val viewModel = LauncherViewModel()

        viewModel.onQueryChanged("mail")

        assertTrue(viewModel.uiState.value.apps.isNotEmpty())
        assertTrue(viewModel.uiState.value.apps.all { it.title.contains("mail", ignoreCase = true) })
    }

    @Test
    fun `launching an app marks it as selected`() {
        val viewModel = LauncherViewModel()
        val app = LauncherItem(
            id = 1,
            title = "Mail",
            packageName = "com.example.mail",
            summary = "Email",
            iconLabel = "M"
        )

        viewModel.launchApp(app)

        assertEquals(app.packageName, viewModel.uiState.value.selectedApp?.packageName)
    }
}
