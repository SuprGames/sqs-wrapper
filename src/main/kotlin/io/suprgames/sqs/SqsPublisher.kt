package io.suprgames.sqs

import io.suprgames.kjson.kJsonMapper
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import java.util.concurrent.CompletableFuture


/**
 * The SqsPublisher is a utility class that help us while publishing events the Event Driven Queue
 *
 * @property queueName The name of the Sqs queue
 * @property source Identifier for WHO is publishing the event in the queue
 */
open class SqsPublisher(private val queueName: String, private val source: String) {

    companion object {
        const val AGGREGATE_ID = "aggregateId"
        const val DETAIL_TYPE = "detailType"
        const val SOURCE = "source"

        private val logger = LogManager.getLogger(SqsPublisher::class.java)

        //Single instance of the Sqs Async client
        private val sqsAsyncClient = SqsAsyncClient.create()

        /**
         * Extension function for Strings to create a Sqs MessageAttirbuteValue
         */
        fun String.sqsAttVal(): MessageAttributeValue =
                MessageAttributeValue
                        .builder()
                        .dataType("String")
                        .stringValue(this)
                        .build()
    }

    /**
     * Function that provides a sqs async client
     */
    open fun sqsClient(): SqsAsyncClient = sqsAsyncClient

    /**
     * Publisher method for a SqsEvent
     *
     * @param event The event to publish
     * @return A completable future with the SendMessageResponse resulted on the event publishing
     */
    fun <T : SqsEvent> publish(event: T): CompletableFuture<SendMessageResponse> =
            sqsClient().sendMessage(
                    SendMessageRequest
                            .builder()
                            .messageGroupId(event.traceId)
                            .eventProcess(event)
                            .queueUrl(System.getenv("urlFor$queueName"))
                            .build())

    /**
     * Helper method to process the event within the SendMessageRequest builder context
     *
     * @param event The event to add to the send message request
     * @return A SendMessageRequest.Builder that contains the event processed
     */
    private fun <T : SqsEvent> SendMessageRequest.Builder.eventProcess(event: T): SendMessageRequest.Builder {
        val attributes = mapOf(
                AGGREGATE_ID to event.aggregateId.sqsAttVal(),
                SOURCE to source.sqsAttVal(),
                DETAIL_TYPE to event::class.qualifiedName!!.sqsAttVal())
        logger.info("Attributes: $attributes")
        logger.info("Event: ${kJsonMapper.writeValueAsString(event)}")
        return this.messageAttributes(attributes).messageBody(kJsonMapper.writeValueAsString(event))
    }
}
