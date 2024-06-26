package pl.jutupe.ktor_rabbitmq.modules

import io.ktor.server.application.Application
import pl.jutupe.ktor_rabbitmq.RabbitMQConfiguration

interface ISerializationTestHelper {
    fun testModule(application: Application, host: String, port: Int, queue: String = "queue")
    fun configure(config: RabbitMQConfiguration, host: String, port: Int, queue: String = "queue")
}
