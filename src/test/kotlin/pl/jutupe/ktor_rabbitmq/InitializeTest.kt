package pl.jutupe.ktor_rabbitmq

import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import pl.jutupe.ktor_rabbitmq.modules.SerializationTestType
import java.io.IOException

class InitializeTest : IntegrationTest() {

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should create queue when declared in initialize block`(testType: SerializationTestType): Unit =
        testApplication {
            application {
                testType.helper.testModule(this, rabbit.host, rabbit.amqpPort)

                // when
                assertDoesNotThrow {
                    withChannel {
                        basicGet("queue", true)
                    }
                }
            }
        }

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should throw when queue not created in initialize block`(testType: SerializationTestType): Unit =
        testApplication {
            application {
                testType.helper.testModule(this, rabbit.host, rabbit.amqpPort)

                // when
                assertThrows<IOException> {
                    withChannel {
                        basicGet("queue1", true)
                    }
                }
            }
        }

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should support passing pre-initialized instance of RabbitMQ`(testType: SerializationTestType): Unit =
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

                // when
                assertDoesNotThrow {
                    withChannel {
                        basicGet("queue", true)
                    }
                }
            }
        }
}
