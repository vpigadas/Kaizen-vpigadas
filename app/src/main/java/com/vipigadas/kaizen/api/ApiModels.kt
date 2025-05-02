package com.vipigadas.kaizen.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

/**
 * Represents the root structure of the sports data
 * Contains a list of different sport categories
 */
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class SportsData(
    val sports: List<Sport> = emptyList()
)

/**
 * Represents a sport category like SOCCER, BASKETBALL, etc.
 * @property i The unique identifier for the sport
 * @property d The display name of the sport
 * @property e The list of events for this sport
 */
@Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
data class Sport(
    @SerialName("i") val id: String,
    @SerialName("d") val name: String,
    @SerialName("e") val events: List<Event> = emptyList()
) {

    fun getSportName(): String = name.split("-").joinToString(System.lineSeparator())
}

/**
 * Represents a single sports event
 * @property i The unique identifier for the event
 * @property d The display name of the event (typically shows opponents)
 * @property sh The short name/display of the event
 * @property si The sport identifier this event belongs to
 * @property tt The timestamp of the event
 */
@Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
data class Event(
    @SerialName("i") val id: String,
    @SerialName("d") val name: String,
    @SerialName("sh") val short_name: String,
    @SerialName("si") val identifier: String,
    @SerialName("tt") val timestamp: Long
){
    fun getEventName(): String = name.split("-").joinToString(System.lineSeparator())

    fun getTimestampInMillis(): Long = timestamp * 1000
}

/**
 * Extension functions to help with common operations
 */

/**
 * Parses the raw JSON string into the SportsData object
 */
fun parseSportsData(jsonString: Array<Sport>): SportsData {
    // This assumes you're using kotlinx.serialization
    return SportsData(jsonString.toList())
}

/**
 * Returns all events sorted by timestamp
 */
fun SportsData.getAllEventsSortedByTime(): List<Event> {
    return sports.flatMap { it.events }.sortedBy { it.timestamp }
}

/**
 * Returns all events for a specific sport
 */
fun SportsData.getEventsForSport(sportId: String): List<Event> {
    return sports.find { it.id == sportId }?.events ?: emptyList()
}

/**
 * Returns the event with the given ID or null if not found
 */
fun SportsData.findEventById(eventId: String): Event? {
    return sports.flatMap { it.events }.find { it.id == eventId }
}

/**
 * Returns true if the event is happening today
 */
fun Event.isToday(): Boolean {
    val today = System.currentTimeMillis() / 1000
    val oneDayInSeconds = 24 * 60 * 60
    return (timestamp - today).absoluteValue < oneDayInSeconds
}