package com.anchorguard.client.enforcement

import android.content.Context
import com.anchorguard.client.auth.security.PinSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object EnforcementController {
    private val _state = MutableStateFlow(EnforcementState.IDLE)
    val state: StateFlow<EnforcementState> = _state.asStateFlow()

    private val _foregroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackage: StateFlow<String?> = _foregroundPackage.asStateFlow()

    fun start(context: Context) {
        AnchorGuardService.start(context)
    }

    fun stop(context: Context) {
        AnchorGuardService.stop(context)
    }

    fun enterParentMode(context: Context) {
        val appCtx = context.applicationContext
        PolicyEngine(appCtx).setParentMode(true)
        PinSecurityManager(appCtx).setParentSessionValid()
        _state.value = EnforcementState.PARENT_MODE
        BlockOverlayView.hide(appCtx)
        AnchorGuardService.requestRefresh(appCtx)
    }

    fun exitParentMode(context: Context) {
        val appCtx = context.applicationContext
        PolicyEngine(appCtx).setParentMode(false)
        PinSecurityManager(appCtx).clearParentSession()
        AnchorGuardService.requestRefresh(appCtx)
    }

    internal fun updateState(newState: EnforcementState) {
        _state.value = newState
    }

    internal fun updateForegroundPackage(pkg: String?) {
        _foregroundPackage.value = pkg
    }

    fun syncBlockedPackages(context: Context, blockedPackageNames: Set<String>) {
        val appCtx = context.applicationContext
        PolicyEngine(appCtx).syncFromAppPolicies(blockedPackageNames)
        AnchorGuardService.requestRefresh(appCtx)
    }

    fun syncSchedule(context: Context, weeklyScheduleJson: String?) {
        val appCtx = context.applicationContext
        PolicyEngine(appCtx).setWeeklySchedule(weeklyScheduleJson)
        AnchorGuardService.requestRefresh(appCtx)
    }

    fun syncAppDailyLimit(context: Context, packageName: String, minutes: Int?) {
        val appCtx = context.applicationContext
        PolicyEngine(appCtx).setAppDailyLimit(packageName, minutes)
        AnchorGuardService.requestRefresh(appCtx)
    }
}
