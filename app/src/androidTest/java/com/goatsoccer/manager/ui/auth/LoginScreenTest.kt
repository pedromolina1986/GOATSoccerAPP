package com.goatsoccer.manager.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goatsoccer.manager.data.repository.AuthRepository
import com.goatsoccer.manager.ui.theme.GoatSoccerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

/**
 * Compose UI instrumentation tests for LoginScreen.
 *
 * A mock AuthRepository is passed directly to LoginViewModel so the screen
 * renders without network access.  All validation logic lives in the ViewModel
 * (pure JVM), so these tests focus on the UI layer: element visibility,
 * user interaction, and displayed error text.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Helper – renders LoginScreen with injectable callbacks and ViewModel
    private fun launchScreen(
        onLoginSuccess: () -> Unit = {},
        onNavigateToRegister: () -> Unit = {},
        viewModel: LoginViewModel = LoginViewModel(mock<AuthRepository>())
    ) {
        composeTestRule.setContent {
            GoatSoccerTheme {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onNavigateToRegister = onNavigateToRegister,
                    viewModel = viewModel
                )
            }
        }
    }

    // ── Static content ────────────────────────────────────────────────────────

    @Test
    fun loginScreen_showsEmailField() {
        launchScreen()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsPasswordField() {
        launchScreen()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsLoginButton() {
        launchScreen()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsRegisterLink() {
        launchScreen()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loginButtonIsEnabledByDefault() {
        launchScreen()
        composeTestRule.onNodeWithText("Login").assertIsEnabled()
    }

    // ── Interactions ──────────────────────────────────────────────────────────

    @Test
    fun loginScreen_clickingRegisterLink_triggersNavigationCallback() {
        var navigated = false
        launchScreen(onNavigateToRegister = { navigated = true })

        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()

        assertTrue(navigated)
    }

    @Test
    fun loginScreen_clickingLoginWithEmptyFields_showsErrorMessage() {
        launchScreen()

        // Leave both fields empty and tap Login
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule
            .onNodeWithText("Email and password are required")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_typingInEmailField_updatesDisplayedText() {
        launchScreen()

        composeTestRule
            .onNodeWithText("Email")
            .performTextInput("user@test.com")

        // After typing, clicking Login with no password should still show the error
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule
            .onNodeWithText("Email and password are required")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_errorMessage_notVisibleInitially() {
        launchScreen()

        // Error node should not exist before any interaction
        composeTestRule
            .onNodeWithText("Email and password are required")
            .assertDoesNotExist()
    }
}
