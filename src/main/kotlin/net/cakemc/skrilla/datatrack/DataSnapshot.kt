package net.cakemc.skrilla.datatrack

class DataSnapshot(private val obj: Any) {
    private val snapshot: Map<String, Any?> = DataTrackUtil.toTrackedMap(obj)

    fun hasChanged(): Boolean {
        return snapshot != DataTrackUtil.toTrackedMap(obj)
    }

    fun getChanges(): Map<String, Pair<Any?, Any?>> {
        val current = DataTrackUtil.toTrackedMap(obj)
        val result = mutableMapOf<String, Pair<Any?, Any?>>()

        for ((key, oldVal) in snapshot) {
            val newVal = current[key]
            if (oldVal != newVal) {
                result[key] = oldVal to newVal
            }
        }

        return result
    }
}
