package app.garminlike.nav.core.interfaces

import app.garminlike.nav.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface INavigationCore {
    val state: StateFlow<NavState>
    val voicePrompts: Flow<NavigationVoicePrompt>
    /** Reject any route with known forbidden segments. */
    suspend fun start(route: NavRoute): Boolean
    suspend fun acceptLocation(sample: LocationSample)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
}
