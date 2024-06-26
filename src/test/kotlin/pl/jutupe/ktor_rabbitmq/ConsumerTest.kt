package pl.jutupe.ktor_rabbitmq

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.slf4j.Logger
import pl.jutupe.ktor_rabbitmq.modules.SerializationTestType

class ConsumerTest : IntegrationTest() {

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should consume message when published`(testType: SerializationTestType) {
        val consumer = mockk<ConsumerScope.(TestObject) -> Unit>()

        testApplication {
            application {
                testType.helper.testModule(this, rabbit.host, rabbit.amqpPort)

                rabbitConsumer {
                    consume("queue", true, rabbitDeliverCallback = consumer)
                }

                // given
                val body = TestObject("value")
                val convertedBody = jacksonObjectMapper().writeValueAsBytes(body)

                // when
                withChannel {
                    basicPublish("exchange", "routingKey", null, convertedBody)
                }

                // then
                verify(timeout = TIMEOUT) { consumer.invoke(any(), eq(body)) }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should consume message when published using precreated RabbitMQ`(testType: SerializationTestType) {
        val consumer = mockk<ConsumerScope.(TestObject) -> Unit>()

        testApplication {
            application {
                install(RabbitMQ) {
                    rabbitMQInstance = RabbitMQInstance(
                        RabbitMQConfiguration.create()
                            .apply {
                                testType.helper.configure(this, rabbit.host, rabbit.amqpPort)
                            }
                    )
                }

                rabbitConsumer {
                    consume("queue", true, rabbitDeliverCallback = consumer)
                }

                // given
                val body = TestObject("value")
                val convertedBody = jacksonObjectMapper().writeValueAsBytes(body)

                // when
                withChannel {
                    basicPublish("exchange", "routingKey", null, convertedBody)
                }

                // then
                verify(timeout = TIMEOUT) { consumer.invoke(any(), eq(body)) }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should log error when invalid body published`(testType: SerializationTestType) {
        val consumer = mockk<ConsumerScope.(TestObject) -> Unit>()
        val logger = mockk<Logger>(relaxUnitFun = true)

        testApplication {
            // given
            environment {
                this.log = logger
            }
            application {
                testType.helper.testModule(this, rabbit.host, rabbit.amqpPort)

                rabbitConsumer {
                    consume("queue", true, rabbitDeliverCallback = consumer)
                }

                val body = AnotherTestObject(string = "string", int = 1234)
                val convertedBody = jacksonObjectMapper().writeValueAsBytes(body)

                // when
                withChannel {
                    basicPublish("exchange", "routingKey", null, convertedBody)
                }

                // then
                verify(timeout = TIMEOUT) { logger.error(any(), any<Throwable>()) }
                verify(exactly = 0) { consumer.invoke(any(), any()) }
            }
        }
    }

    companion object {
        const val TIMEOUT = 1000L
    }
}
