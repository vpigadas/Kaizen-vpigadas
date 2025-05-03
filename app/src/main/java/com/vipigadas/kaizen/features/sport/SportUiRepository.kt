package com.vipigadas.kaizen.features.sport

import android.content.Context
import com.vipigadas.kaizen.R
import com.vipigadas.kaizen.api.Event
import com.vipigadas.kaizen.api.Sport
import com.vipigadas.kaizen.ui.features.main.model.EventUiModel
import com.vipigadas.kaizen.ui.features.main.model.SportUiModel
import com.vipigadas.kaizen.ui.features.main.model.UiModelType
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class SportUiRepository(
    val context: Context,
    val isFavorite: (Event) -> Boolean,
    val isExpand: (Sport) -> Boolean
) {

    fun transformUIEvent(sport: Sport, event: Event): EventUiModel {
        return EventUiModel(
            id = event.id,
            title = event.getEventName(),
            imageUrl = findSportIcon(sport), // In a real app, this would be from the event
            startTime = calculateTimeRemaining(event.getTimestampInMillis()),
            isFavorite = isFavorite.invoke(event),
            type = UiModelType.EVENT
        )
    }

    fun transformUISport(sport: Sport): SportUiModel {
        return SportUiModel(
            id = sport.id,
            name = sport.getSportName(),
            isExpanded = isExpand.invoke(sport),
            eventsCount = sport.events.size
        )
    }

    fun updateEventTimer(event: Event): String =
        calculateTimeRemaining(event.getTimestampInMillis())

    /**
     * Calculates a human-readable string for time remaining until the event starts.
     * Uses string resources for localization.
     */
    private fun calculateTimeRemaining(startTimeMillis: Long): String {
        // Get current time
        val currentTimeMillis = System.currentTimeMillis()

        // Calculate difference in milliseconds
        val differenceMillis = startTimeMillis - currentTimeMillis

        // Event already started
        if (differenceMillis <= 0) {
            return context.getString(R.string.started)
        }

        // Convert to more readable units
        val totalSeconds = differenceMillis / 1000
        val days = totalSeconds / (24 * 60 * 60)
        val hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60)
        val minutes = (totalSeconds % (60 * 60)) / 60
        val seconds = totalSeconds % 60

        return when {
            // More than 3 hours - use original format with largest unit
            differenceMillis > 4.hours.inWholeMilliseconds -> {
                when {
                    days > 0 -> context.getString(R.string.starts_in_days, days)
                    hours > 0 -> context.getString(R.string.starts_in_hours, hours)
                    minutes > 0 -> context.getString(R.string.starts_in_minutes, minutes)
                    else -> context.getString(R.string.starts_in_seconds, seconds)
                }
            }

            // Less than 3 hours - detailed countdown with hours, minutes, seconds
            else -> {
                val hoursStr =
                    if (hours > 0) context.getString(R.string.hours_format, hours) else ""
                val minutesStr = context.getString(R.string.minutes_format, minutes)
                val secondsStr = context.getString(R.string.seconds_format, seconds)

                "${context.getString(R.string.starts)} $hoursStr $minutesStr $secondsStr".trim()
            }
        }
    }

    private fun findSportIcon(sport: Sport): Int = when (sport.id) {
        "FOOT" -> R.drawable.sports_soccer_24px
        "BASK" -> R.drawable.sports_basketball_24px
        "TENN" -> R.drawable.sports_tennis_24px
        "TABL" -> R.drawable.sports_tennis_24px
        "VOLL" -> R.drawable.sports_volleyball_24px
        "ESPS" -> R.drawable.sports_esports_24px
        "ICEH" -> R.drawable.sports_and_outdoors_24px
        "HAND" -> R.drawable.sports_soccer_24px
        "SNOO" -> R.drawable.sports_soccer_24px
        "FUTS" -> R.drawable.sports_soccer_24px
        "DART" -> R.drawable.sports_soccer_24px
        else -> R.drawable.sports_soccer_24px
    }
}