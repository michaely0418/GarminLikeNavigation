package app.garminlike.nav.core.model

enum class AiProviderType { OPENAI, GEMINI, DISABLED }

enum class NavigationTool { SEARCH_DESTINATION, CALCULATE_ROUTES, SELECT_ROUTE, ADD_WAYPOINT, REMOVE_WAYPOINT, CHANGE_VEHICLE, REROUTE, GET_STATUS, GET_SAFETY_ALERTS }

data class AiSessionConfig(val provider: AiProviderType, val locale: String = "zh-TW", val permittedTools: Set<NavigationTool> = emptySet())

data class AiToolRequest(val requestId: String, val tool: NavigationTool, val argumentsJson: String)

/** Navigation-changing tools require verification and, where appropriate, user approval. */
data class AiToolDecision(val permitted: Boolean, val requiresUserConfirmation: Boolean, val reason: String)

sealed interface AiEvent {
    data class Transcript(val text: String) : AiEvent
    data class ToolRequested(val request: AiToolRequest) : AiEvent
    data class Failed(val reason: String) : AiEvent
}
