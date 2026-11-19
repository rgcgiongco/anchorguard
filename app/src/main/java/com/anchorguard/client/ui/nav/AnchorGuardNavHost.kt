package com.anchorguard.client.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.screens.AppListScreen
import com.anchorguard.client.enforcement.EnforcementPermissionChecker
import com.anchorguard.client.ui.screens.AccountScreen
import com.anchorguard.client.ui.screens.AuthLoginScreen
import com.anchorguard.client.ui.screens.AuthRegisterScreen
import com.anchorguard.client.ui.screens.AuthWelcomeScreen
import com.anchorguard.client.ui.screens.ChildHomeScreen
import com.anchorguard.client.ui.screens.DeviceSettingsScreen
import com.anchorguard.client.ui.screens.PerAppLimitsScreen
import com.anchorguard.client.ui.screens.PermissionGuideScreen
import com.anchorguard.client.ui.screens.SOSScreen
import com.anchorguard.client.ui.screens.ScheduleScreen
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
import com.anchorguard.client.ui.screens.wizard.WizardChildRegisterScreen
import com.anchorguard.client.ui.screens.wizard.WizardCompleteScreen
import com.anchorguard.client.ui.screens.wizard.WizardPairingScreen
import com.anchorguard.client.ui.screens.wizard.WizardParentAuthScreen
import com.anchorguard.client.ui.screens.wizard.WizardPermissionGuideScreen
import com.anchorguard.client.ui.screens.wizard.WizardRoleSelectionScreen
import com.anchorguard.client.ui.screens.wizard.WizardSafetyOptionsScreen
import com.anchorguard.client.ui.screens.wizard.WizardWelcomeScreen

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
            val context = LocalContext.current
            val store = remember { SecureSessionStore(context) }
            val wizardRole = store.wizardRole()
            SplashScreen(
                context = context,
                onDeviceReady = {
                    navController.navigate(Routes.ChildHome) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNeedsPairing = {
                    navController.navigate(Routes.Pairing) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNeedsRegistration = {
                    navController.navigate(Routes.Pairing) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onPermissionsNeeded = {
                    navController.navigate(Routes.PermissionGuide) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onWizardNeeded = {
                    navController.navigate(Routes.WizardWelcome) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onWizardComplete = {
                    val target = when {
                        wizardRole == "parent" -> Routes.ParentDashboard
                        else -> Routes.ChildHome
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.WizardWelcome) {
            WizardWelcomeScreen(
                onGetStarted = { navController.navigate(Routes.WizardRoleSelection) }
            )
        }
        composable(Routes.WizardRoleSelection) {
            WizardRoleSelectionScreen(
                onParentDevice = { navController.navigate(Routes.WizardParentAuth) },
                onChildDevice = { navController.navigate(Routes.WizardChildRegister) }
            )
        }
        composable(Routes.WizardParentAuth) {
            WizardParentAuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.WizardPermissions) {
                        popUpTo(Routes.WizardParentAuth) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.WizardChildRegister) {
            WizardChildRegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.WizardPairing) {
                        popUpTo(Routes.WizardChildRegister) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.WizardPairing) {
            WizardPairingScreen(
                onPaired = {
                    navController.navigate(Routes.WizardSafety) {
                        popUpTo(Routes.WizardPairing) { inclusive = true }
                    }
                },
                onSkipped = {
                    navController.navigate(Routes.WizardSafety) {
                        popUpTo(Routes.WizardPairing) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.WizardSafety) {
            WizardSafetyOptionsScreen(
                onComplete = {
                    navController.navigate(Routes.WizardComplete) {
                        popUpTo(Routes.WizardSafety) { inclusive = true }
                    }
                },
                onSkipped = {
                    navController.navigate(Routes.WizardComplete) {
                        popUpTo(Routes.WizardSafety) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.WizardComplete) {
            val context = LocalContext.current
            val store = remember { SecureSessionStore(context) }
            val role = store.wizardRole()
            WizardCompleteScreen(
                onFinish = {
                    val target = when {
                        role == "parent" -> Routes.ParentDashboard
                        role == "child" -> Routes.ChildHome
                        else -> Routes.ChildHome
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onReview = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.WizardPermissions) {
            WizardPermissionGuideScreen(
                onAllGranted = {
                    navController.navigate(Routes.WizardPairing) {
                        popUpTo(Routes.WizardPermissions) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
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
            ChildHomeScreen(
                onOpenParentGate = { navController.navigate(Routes.ParentGate) },
                onOpenSos = { navController.navigate(Routes.Sos) }
            )
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
            val context = LocalContext.current
            val checker = remember { EnforcementPermissionChecker(context) }
            val enforcementActive by remember { mutableStateOf(checker.allPermissionsGranted()) }
            ParentDashboardScreen(
                onDeviceSettings = { navController.navigate(Routes.DeviceSettings) },
                onSecurity = { navController.navigate(Routes.Security) },
                onUsers = { navController.navigate(Routes.Users) },
                onTimer = { navController.navigate(Routes.Timer) },
                onAccount = { navController.navigate(Routes.Account) },
                enforcementActive = enforcementActive
            )
        }
        composable(Routes.PermissionGuide) {
            PermissionGuideScreen(onAllGranted = {
                navController.navigate(Routes.ChildHome) {
                    popUpTo(Routes.PermissionGuide) { inclusive = true }
                }
            })
        }
        composable(Routes.Sos) {
            SOSScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DeviceSettings) {
            DeviceSettingsScreen(
                onOpenAppList = { navController.navigate(Routes.AppList) },
                onOpenSchedules = { navController.navigate(Routes.Schedule) },
                onOpenPerAppLimits = { navController.navigate(Routes.PerAppLimits) }
            )
        }
        composable(Routes.AppList) {
            AppListScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Schedule) {
            ScheduleScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PerAppLimits) {
            PerAppLimitsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Security) { SecurityScreen() }
        composable(Routes.Users) {
            val context = LocalContext.current
            val authRepo = remember { AuthRepository(context) }
            val scope = rememberCoroutineScope()
            UsersScreen(onDeviceSelected = { deviceId ->
                scope.launch {
                    authRepo.secureSession().raw().edit().putString("selected_device_id", deviceId).apply()
                }
                navController.navigate(Routes.DeviceSettings)
            })
        }
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
