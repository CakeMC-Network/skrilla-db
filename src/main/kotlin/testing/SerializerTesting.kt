package testing

import net.cakemc.skrilla.database.serial.SerializationSystem
import java.util.*

fun main() {
    val serializer = SerializationSystem()

    val person = Person("Alice", 30, true,
        UUID.randomUUID(), Date(), arrayOf("tag1", "tag2"))

    var start = System.currentTimeMillis()
    val serializedData = serializer.serialize(person)
    println("serialization took ${System.currentTimeMillis()-start} ms")

    start = System.currentTimeMillis()
    val deserializedPerson = serializer.deserialize(serializedData, Person::class.java)
    println("deserialization took ${System.currentTimeMillis()-start} ms")

    println("Original person: $person")
    println("Deserialized person: $deserializedPerson")
}

data class Person(var name: String, var age: Int, var isActive: Boolean,
                  var uuid: UUID, var birthDate: Date, var tags: Array<String>)