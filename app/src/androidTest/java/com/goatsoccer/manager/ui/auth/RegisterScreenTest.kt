package com.goatsoccer.manager.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goatsoccer.manager.data.repository.AuthRepository
import com.goatsoccer.manager.ui.theme.GoatSoccerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

/**
 * Compose UI instrumentation tests for RegisterScreen.
 *
 * A mock AuthRepository is injected so no network calls are made.
 * Tests verify: static content, role radio buttons, validation error display,
 * and navigation callbacks.
 */
@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchScreen(
        onRegisterSuccess: () -> Unit = {},
        onNavigateBack: () -> Unit = {},
        viewModel: RegisterViewModel = RegisterViewModel(mock<AuthRepository>())
    ) {
        composeTestRule.setContent {
            GoatSoccerTheme {
                RegisterScreen(
                    onRegisterSuccess = onRegisterSuccess,
                    onNavigateBack = onNavigateBack,
                    viewModel = viewModel
                )
            }
        }
    }

    // ── Static content ────────────────────────────────────────────────────────

    @Test
    fun registerScreen_showsPageTitle() {
        launchScreen()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsEmailField() {
        launchScreen()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsFullNameField() {
        launchScreen()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsPasswordField() {
        launchScreen()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsConfirmPasswordField() {
        launchScreen()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsRegisterButton() {
        launchScreen()
        composeTestRule.onNodeWithText("Register").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsLoginLink() {
        launchScreen()
        composeTestRule.onNodeWithText("Already have an account? Login").assertIsDisplayed()
    }

    @Test
    fun registerScreen_registerButtonIsEnabledByDefault() {
        launchScreen()
        composeTestRule.onNodeWithText("Register").assertIsEnabled()
    }

    // ── Role radio buttons ────────────────────────────────────────────────────

    @Test
    fun registerScreen_showsFanRoleOption() {
        launchScreen()
        composeTestRule.onNodeWithText("Fan").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsPlayerRoleOption() {
        launchScreen()
        composeTestRule.onNodeWithText("Player").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsCoachRoleOption() {
        launchScreen()
        composeTestRule.onNodeWithText("Coach").assertIsDisplayed()
    }

    // ── Validation error display ──────────────────────────────────────────────

    @Test
    fun registerScreen_clickingRegisterWithEmptyFields_showsNameRequiredError() {
        launchScreen()

        // All fields empty → first validation failure is "Name is required"
        composeTestRule.onNodeWithText("Register").performClick()

        composeTestRule.onNodeWithText("Name is required").assertIsDisplayed()
    }

    @Test
    fun registerScreen_errorMessage_notVisibleInitially() {
        launchScreen()

        composeTestRule.onNodeWithText("Name is required").assertDoesNotExist()
        composeTestRule.onNodeWithText("Email is required").assertDoesNotExist()
        composeTestRule.onNodeWithText("Password is required").assertDoesNotExist()
    }

    // ── Navigation callbacks ──────────────────────────────────────────────────

    @Test
    fun registerScreen_clickingLoginLink_triggersNavigationCallback() {
        var navigatedBack = false
        launchScreen(onNavigateBack = { navigatedBack = true })

        composeTestRule.onNodeWithText("Already have an account? Login").performClick()

        assertTrue(navigatedBack)
    }

    // ── Role label (displayed in the Role section) ────────────────────────────

    @Test
    fun registerScreen_showsRoleLabel() {
        launchScreen()
        composeTestRule.onNodeWithText("Role").assertIsDisplayed()
    }
}
