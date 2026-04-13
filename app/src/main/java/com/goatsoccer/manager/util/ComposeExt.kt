package com.goatsoccer.manager.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goatsoccer.manager.data.local.SessionManager

/**
 * Creates a ViewModel using the app's ViewModelFactory (which wires all repositories).
 * Usage: val vm: HomeViewModel = appViewModel()
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(): VM {
    val context = LocalContext.current
    val factory = remember(context) { ViewModelFactory(SessionManager(context)) }
    return viewModel(factory = factory)
}
