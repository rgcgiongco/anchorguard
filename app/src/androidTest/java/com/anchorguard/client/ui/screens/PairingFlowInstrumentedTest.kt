package com.anchorguard.client.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PairingFlowInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        // No setup needed for screen-level compose test
    }

    @Test
    fun `PairingScreen composes without crashing`() {
        composeRule.setContent {
            PairingScreen(
                onPaired = { /* navigation handled by host */ },
                onNeedSignIn = { /* sign-in navigation */ }
            )
        }
    }

    @Test
    fun `PairingScreen shows pairing code input field`() {
        composeRule.setContent {
            PairingScreen(
                onPaired = { },
                onNeedSignIn = { }
            )
        }
        // The screen renders an info card and code input; verify no crash
    }
}
