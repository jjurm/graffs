package uk.ac.cam.jm2186.graffs.metric

sealed class MetricResult(val time: Long) {
    class Unit(time: Long) : MetricResult(time) {
        override fun toString() = "[true]"
    }

    class Double(val value: kotlin.Double, time: Long) : MetricResult(time) {
        override fun toString() = "%.2f".format(value)
    }
}
