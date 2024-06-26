package pl.jutupe.ktor_rabbitmq

import com.rabbitmq.client.AMQP
import io.ktor.server.application.ApplicationCall
import kotlin.reflect.KClass

inline fun <reified T : Any> ApplicationCall.publish(exchange: String, routingKey: String, props: AMQP.BasicProperties?, body: T) =
    publish(exchange, routingKey, props, T::class, body)

fun <T : Any> ApplicationCall.publish(exchange: String, routingKey: String, props: AMQP.BasicProperties?, clazz: KClass<T>, body: T) =
    application.attributes[RabbitMQ.RabbitMQKey].publish(exchange, routingKey, props, clazz, body)

inline fun <reified T : Any> RabbitMQInstance.publish(exchange: String, routingKey: String, props: AMQP.BasicProperties?, body: T) =
    publish(exchange, routingKey, props, T::class, body)

fun <T : Any> RabbitMQInstance.publish(exchange: String, routingKey: String, props: AMQP.BasicProperties?, clazz: KClass<T>, body: T) {
    withChannel {
        val bytes = serialize(body, clazz)

        basicPublish(exchange, routingKey, props, bytes)
    }
}
