package pl.jutupe.ktor_rabbitmq.modules

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.Application
import io.ktor.server.application.install
import pl.jutupe.ktor_rabbitmq.RabbitMQ
import pl.jutupe.ktor_rabbitmq.RabbitMQConfiguration

object JacksonTestHelper : ISerializationTestHelper {
    override fun testModule(application: Application, host: String, port: Int, queue: String) {
        with(application) {
            install(RabbitMQ) {
                enableLogging()

                configure(this, host, port, queue)
            }
        }
    }

    override fun configure(config: RabbitMQConfiguration, host: String, port: Int, queue: String) {
        with(config) {
            uri = "amqp://guest:guest@$host:$port"
            connectionName = "Connection name"

            serialize { it, _ -> jacksonObjectMapper().writeValueAsBytes(it) }
            deserialize { bytes, type -> jacksonObjectMapper().readValue(bytes, type.javaObjectType) }

            initialize {
                exchangeDeclare("exchange", "direct", true)
                queueDeclare(queue, true, false, false, emptyMap())
                queueBind(queue, "exchange", "routingKey")
            }
        }
    }
}
