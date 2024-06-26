package pl.jutupe.ktor_rabbitmq.modules

enum class SerializationTestType(val helper: ISerializationTestHelper) {
    JACKSON(JacksonTestHelper),
    KTOTLINX(KotlinxTestHelper)
}
