package com.anchorguard.client.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anchorguard.client.ui.screens.AccountScreen
import com.anchorguard.client.ui.screens.AuthLoginScreen
import com.anchorguard.client.ui.screens.AuthRegisterScreen
import com.anchorguard.client.ui.screens.AuthWelcomeScreen
import com.anchorguard.client.ui.screens.ChildHomeScreen
import com.anchorguard.client.ui.screens.DeviceSettingsScreen
import com.anchorguard.client.ui.screens.PairingScreen
import com.anchorguard.client.ui.screens.ParentDashboardScreen
import com.anchorguard.client.ui.screens.ParentGateScreen
import com.anchorguard.client.ui.screens.PinChangeScreen
import com.anchorguard.client.ui.screens.PinCreateScreen
import com.anchorguard.client.ui.screens.PinVerifyScreen
import com.anchorguard.client.ui.screens.RoleSelectionScreen
import com.anchorguard.client.ui.screens.SecurityScreen
import com.anchorguard.client.ui.screens.SplashScreen
import com.anchorguard.client.ui.screens.TimerScreen
import com.anchorguard.client.ui.screens.UsersScreen
import com.anchorguard.client.ui.screens.WelcomeScreen

@Composable
fun AnchorGuardNavHost(
    navController: NavHostController = rememberNavController(),
    openParentGateTick: Int = 0
) {
    androidx.compose.runtime.LaunchedEffect(openParentGateTick) {
        if (openParentGateTick > 0) {
            navController.navigate(Routes.ParentGate) {
                launchSingleTop = true
            }
        }
    }
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.Welcome) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            })
        }
        composable(Routes.Welcome) {
            WelcomeScreen(onContinue = { navController.navigate(Routes.RoleSelection) })
        }
        composable(Routes.RoleSelection) {
            RoleSelectionScreen(
                onParentDevice = { navController.navigate(Routes.AuthWelcome) },
                onChildDevice = { navController.navigate(Routes.Pairing) }
            )
        }
        composable(Routes.AuthWelcome) {
            AuthWelcomeScreen(
                onLogin = { navController.navigate(Routes.AuthLogin) },
                onRegister = { navController.navigate(Routes.AuthRegister) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.AuthLogin) {
            AuthLoginScreen(
                onSignedIn = { navController.navigate(Routes.PinCreate) },
                onCreateAccount = { navController.navigate(Routes.AuthRegister) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.AuthRegister) {
            AuthRegisterScreen(
                onRegistered = { navController.navigate(Routes.PinCreate) },
                onHaveAccount = { navController.navigate(Routes.AuthLogin) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PinCreate) {
            PinCreateScreen(
                onContinue = {
                    navController.navigate(Routes.ParentDashboard) {
                        popUpTo(Routes.RoleSelection) { inclusive = false }
                    }
                },
                onSkipIfSet = {
                    navController.navigate(Routes.ParentDashboard) {
                        popUpTo(Routes.RoleSelection) { inclusive = false }
                    }
                }
            )
        }
        composable(Routes.PinVerify) {
            PinVerifyScreen(
                onVerified = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.PinChange) {
            PinChangeScreen(
                onChanged = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.Pairing) {
            PairingScreen(
                onPaired = {
                    navController.navigate(Routes.ChildHome) {
                        popUpTo(Routes.RoleSelection) { inclusive = false }
                    }
                },
                onNeedSignIn = { navController.navigate(Routes.AuthWelcome) }
            )
        }
        composable(Routes.ChildHome) {
            ChildHomeScreen(onOpenParentGate = { navController.navigate(Routes.ParentGate) })
        }
        composable(Routes.ParentGate) {
            ParentGateScreen(
                onUnlocked = {
                    navController.navigate(Routes.ParentDashboard) {
                        popUpTo(Routes.ChildHome) { inclusive = false }
                    }
                },
                onForgotPin = { navController.navigate(Routes.Account) },
                onChangePin = { navController.navigate(Routes.PinChange) }
            )
        }
        composable(Routes.ParentDashboard) {
            ParentDashboardScreen(
                onDeviceSettings = { navController.navigate(Routes.DeviceSettings) },
                onSecurity = { navController.navigate(Routes.Security) },
                onUsers = { navController.navigate(Routes.Users) },
                onTimer = { navController.navigate(Routes.Timer) },
                onAccount = { navController.navigate(Routes.Account) }
            )
        }
        composable(Routes.DeviceSettings) { DeviceSettingsScreen() }
        composable(Routes.Security) { SecurityScreen() }
        composable(Routes.Users) { UsersScreen() }
        composable(Routes.Timer) { TimerScreen() }
        composable(Routes.Account) { AccountScreen(
            onChangePin = { navController.navigate(Routes.PinChange) },
            onSignOut = {
                navController.navigate(Routes.Welcome) {
                    popUpTo(0) { inclusive = true }
                }
            }
        ) }
    }
}
