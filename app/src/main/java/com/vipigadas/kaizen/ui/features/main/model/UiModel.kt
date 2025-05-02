package com.vipigadas.kaizen.ui.features.main.model

import androidx.compose.ui.graphics.painter.Painter

/**
 * UI model for sports and events representation.
 * This separates UI representation from data models.
 */

/**
 * Base interface for UI models to help with type safety when working with lists
 * containing different types of UI models.
 */
sealed interface UiModel {
    val id: String
    val type: UiModelType
}

/**
 * Enum to distinguish between different types of UI models
 */
enum class UiModelType {
    SPORT,
    EVENT
}

/**
 * UI model representing a sport section
 */
data class SportUiModel(
    override val id: String,
    val name: String,
    val isExpanded: Boolean = false,
    val eventsCount: Int = 0,
    // To easily identify this as a section in a mixed list
    override val type: UiModelType = UiModelType.SPORT
) : UiModel

/**
 * UI model representing an event
 */
data class EventUiModel(
    override val id: String,
    val title: String,
    val imageUrl: Int,
    val startTime: String,
    val isFavorite: Boolean = false,
    // To easily identify this as an event in a mixed list
    override val type: UiModelType = UiModelType.EVENT
) : UiModel