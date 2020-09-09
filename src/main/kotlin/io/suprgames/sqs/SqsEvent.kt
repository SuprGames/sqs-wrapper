package io.suprgames.sqs

import io.suprgames.kjson.kJsonMapper

/**
 * Interface that defines what needs to be in an event that is going to be enqueued in the EventManager Sqs queue
 */
interface SqsEvent {

    /**
     * Unique identifier used to trace the movement of the event
     */
    val traceId: String

    /**
     * Identifier of the aggregate that is related with this event, if any.
     * Empty if not present
     */
    val aggregateId: String

    /**
     * Connection identifier related with this event, if any
     * Empty if not present
     *
     * An example of use could be a WebSocket connection identifier, or a deferred HTTP require
     */
    val connectionId: String

    /**
     * The detail type for the event, by default will be the Qualified class name
     *
     * @return The class qualified name of the interface implementer
     */
    fun detailType(): String = this::class.qualifiedName!!

    /**
     * The event string representation, by default will be defined as a Json String
     *
     * @return JSON String representing the object
     */
    fun generateDetails(): String = kJsonMapper.writeValueAsString(this)

}
