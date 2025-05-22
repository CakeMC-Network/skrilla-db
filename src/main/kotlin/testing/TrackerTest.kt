package testing

import net.cakemc.database.api.Document
import net.cakemc.skrilla.datatrack.DataSnapshot
import net.cakemc.skrilla.datatrack.DataTrackSerializer
import net.cakemc.skrilla.datatrack.DataTrackUtil
import net.cakemc.skrilla.datatrack.model.DocumentAdapter
import net.cakemc.skrilla.datatrack.model.MemoryAdapter

fun main() {
    readWriteTest()
}

fun readWriteTest() {
    val adapter = DocumentAdapter(Document(0, 0, 0, mutableMapOf()))

    val person = TestPerson("Alice", 25, "test")
    DataTrackSerializer.serialize(person, adapter)

    println(adapter.keys())

    val newPerson = DataTrackSerializer.deserialize(TestPerson::class.java, adapter)

    println("Original: $person")
    println("Deserialized: $newPerson")
}

fun trackerTest() {
    val testPerson = TestPerson("Alice", 25, "test")
    val snapshot = DataSnapshot(testPerson)

    println("Original: ${DataTrackUtil.toTrackedMap(testPerson)}")

    testPerson.age = 26
    testPerson.name = "Alicia"

    if (snapshot.hasChanged()) {
        println("Changes: ${snapshot.getChanges()}")
    }

    val map = DataTrackUtil.toTrackedMap(testPerson)
    val restored = DataTrackUtil.fromTrackedMap(TestPerson::class.java, map)

    println("Restored: $restored")
}