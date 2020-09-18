package io.suprgames.sqs

import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResultEntry
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import java.util.concurrent.CompletableFuture

/**
 * Mock Sqs publisher that simulates an Async Sqs Publisher and keep track of all the events that have been published
 */
class AsyncMockSqsPublisher : SqsPublisher {

    var publishedEvents = mutableListOf<SqsEvent>()

    fun clear() {
        publishedEvents = mutableListOf()
    }

    override fun <T : SqsEvent> publish(event: T, source: String): CompletableFuture<SendMessageResponse> =
            publishedEvents.add(event).let {
                CompletableFuture.completedFuture(SendMessageResponse.builder().build())
            }


    override fun <T : SqsEvent> publishBatch(events: List<T>, source: String): CompletableFuture<SendMessageBatchResponse> =
            publishedEvents.addAll(events).let {
                CompletableFuture.completedFuture(
                        SendMessageBatchResponse
                                .builder()
                                .successful(
                                        events
                                                .map { SendMessageBatchResultEntry.builder().build() }
                                )
                                .build())
            }
}