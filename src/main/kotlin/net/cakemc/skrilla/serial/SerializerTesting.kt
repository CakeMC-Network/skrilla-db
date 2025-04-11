package net.cakemc.skrilla.serial

import java.util.*

fun main() {
    val serializer = SerializationSystem()

    val person = Person("Alice", 30, true,
        UUID.randomUUID(), Date(), arrayOf("tag1", "tag2"))

    val serializedData = serializer.serialize(person)

    val deserializedPerson = serializer.deserialize(serializedData, Person::class.java)

    println("Original person: $person")
    println("Deserialized person: $deserializedPerson")
}

data class Person(var name: String, var age: Int, var isActive: Boolean,
                  var uuid: UUID, var birthDate: Date, var tags: Array<String>)