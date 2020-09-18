package io.suprgames.sqs

import software.amazon.awssdk.services.sqs.model.*
import java.util.concurrent.CompletableFuture

interface SqsPublisher {

    /**
     * Publisher method for a SqsEvent list as batch
     *
     * @param event The events to publish
     * @param source Source of the publication
     * @return A completable future with the SendMessageResponse resulted on the event publishing
     */
    fun <T : SqsEvent> publish(event: T, source: String): CompletableFuture<SendMessageResponse>

    /**
     * Publisher method for a SqsEvent
     *
     * @param events The event to publish
     * @param source Source of the publication
     * @return A completable future with the SendMessageResponse resulted on the event publishing
     */
    fun <T : SqsEvent> publishBatch(events: List<T>, source: String): CompletableFuture<SendMessageBatchResponse>

}

