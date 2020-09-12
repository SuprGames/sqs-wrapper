package io.suprgames.sqs

import io.suprgames.kjson.kJsonMapper
import io.suprgames.sqs.SqsPublisher.Companion.AGGREGATE_ID
import io.suprgames.sqs.SqsPublisher.Companion.sqsAttVal
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.*
import java.util.concurrent.CompletableFuture


/**
 * The SqsPublisher is a utility class that help us while publishing events the Event Driven Queue
 *
 * @property queueUrl
 *              The url of the Sqs queue
 *              As a recommendation, you could define it like this in the environment variables in serverless-base.yml
 *                  https://sqs.${self:provider.region}.amazonaws.com/#{AWS::AccountId}/${self:custom.eventQueueName}
 *              Important:
 *                  * We have used the "serverless-pseudo-parameters" plugin to retrieve #{AWS::AccountId}
 *                  * We have a variable for the eventQueueName
 *
 * @property source
 *              Identifier for WHO is publishing the event in the queue
 */
open class SqsPublisher(private val queueUrl: String, private val source: String) {

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
                            .queueUrl(queueUrl)
                            .build())

    /**
     * Publisher method for a SqsEvent list as batch
     *
     * @param event The event to publish
     * @return A completable future with the SendMessageResponse resulted on the event publishing
     */
    fun <T : SqsEvent> publishBatch(events: List<T>): CompletableFuture<SendMessageBatchResponse> =
            sqsClient().sendMessageBatch(
                    SendMessageBatchRequest
                            .builder()
                            .entries(events.map {
                                SendMessageBatchRequestEntry
                                        .builder()
                                        .messageGroupId(it.traceId)
                                        .eventProcess(it)
                                        .build()
                            })
                            .queueUrl(queueUrl)
                            .build()
            )

    /**
     * Helper method to process the event within the SendMessageRequest builder context
     *
     * @param event The event to add to the send message request
     * @return A SendMessageRequest.Builder that contains the event processed
     */
    private fun <T : SqsEvent> SendMessageRequest.Builder.eventProcess(event: T): SendMessageRequest.Builder =
            event.toAttributesMap().let {
                logger.info("Attributes: $it")
                logger.info("Event: ${kJsonMapper.writeValueAsString(this)}")
                this.messageAttributes(it).messageBody(kJsonMapper.writeValueAsString(event))
            }

    /**
     * Helper method to process the event within the SendMessageBatchRequestEntry builder context
     *
     * @param event The event to add to the send message request
     * @return A SendMessageRequest.Builder that contains the event processed
     */
    private fun <T : SqsEvent> SendMessageBatchRequestEntry.Builder.eventProcess(event: T): SendMessageBatchRequestEntry.Builder =
            event.toAttributesMap().let {
                logger.info("Attributes: $it")
                logger.info("Event: ${kJsonMapper.writeValueAsString(this)}")
                this.messageAttributes(it).messageBody(kJsonMapper.writeValueAsString(event))
            }

    /**
     * Helper method to generate the main attributes map from this SQS event
     *
     * @return Map containing the MessageAttributeValue
     */
    private fun <T : SqsEvent> T.toAttributesMap(): Map<String, MessageAttributeValue> =
            mapOf(AGGREGATE_ID to this.aggregateId.sqsAttVal(),
                    SOURCE to source.sqsAttVal(),
                    DETAIL_TYPE to this::class.qualifiedName!!.sqsAttVal())
}
