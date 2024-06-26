package pl.jutupe.ktor_rabbitmq.modules

import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import pl.jutupe.ktor_rabbitmq.RabbitMQ
import pl.jutupe.ktor_rabbitmq.RabbitMQConfiguration
import kotlin.reflect.full.starProjectedType

object KotlinxTestHelper : ISerializationTestHelper {
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

            serialize { it, type -> Json.Default.encodeToString(serializer(type.starProjectedType), it).encodeToByteArray() }
            deserialize { bytes, type -> Json.Default.decodeFromString(serializer(type.starProjectedType), bytes.decodeToString())!! }

            initialize {
                exchangeDeclare("exchange", "direct", true)
                queueDeclare(queue, true, false, false, emptyMap())
                queueBind(queue, "exchange", "routingKey")
            }
        }
    }
}
