package testing

import net.cakemc.skrilla.datatrack.DataTracker
import net.cakemc.skrilla.datatrack.Trackable

@Trackable
data class TestPerson(
    @DataTracker
    var name: String,
    @DataTracker(documentName = "person_age")
    var age: Int,
    val hobby: String
)