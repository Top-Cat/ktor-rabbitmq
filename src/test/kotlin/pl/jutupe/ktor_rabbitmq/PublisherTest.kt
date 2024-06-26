package pl.jutupe.ktor_rabbitmq

import io.ktor.client.request.get
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import pl.jutupe.ktor_rabbitmq.modules.SerializationTestType

private fun Application.testModule(testType: SerializationTestType, host: String, port: Int) {
    testType.helper.testModule(this, host, port)

    routing {
        get("/test") {
            val payload = IntegrationTest.TestObject(key = "value2")

            call.publish("exchange", "routingKey", null, payload)
        }
    }
}

class PublishTest : IntegrationTest() {

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should publish message when feature publish called`(testType: SerializationTestType) =
        testApplication {
            application {
                testModule(testType, rabbit.host, rabbit.amqpPort)

                // given
                val exchange = "exchange"
                val routingKey = "routingKey"
                val body = TestObject("value")

                // when
                attributes[RabbitMQ.RabbitMQKey].publish(exchange, routingKey, null, body)

                // then
                verifyMessages("queue", routingKey, listOf("{\"key\":\"value\"}"))
            }
        }

    @ParameterizedTest
    @EnumSource(value = SerializationTestType::class)
    fun `should publish message when call publish called`(testType: SerializationTestType) =
        testApplication {
            application {
                testModule(testType, rabbit.host, rabbit.amqpPort)
            }

            // given
            val queue = "queue"
            val routingKey = "routingKey"

            // when
            client.get("/test")

            // then
            verifyMessages(queue, routingKey, listOf("{\"key\":\"value2\"}"))
        }
}
