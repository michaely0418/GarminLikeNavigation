package app.garminlike.nav.core.interfaces

import app.garminlike.nav.core.model.*
import kotlinx.coroutines.flow.Flow

interface IAiVoiceAssistant {
    val events: Flow<AiEvent>
    suspend fun connect(config: AiSessionConfig)
    suspend fun submitText(text: String)
    suspend fun submitPcmAudio(pcm16: ByteArray, sampleRateHz: Int)
    suspend fun disconnect()
    /** Must obtain Navigation Core approval and user confirmation for mutating tools. */
    fun authorizeTool(request: AiToolRequest): AiToolDecision
}
