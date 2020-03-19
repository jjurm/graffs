package uk.ac.cam.jm2186.graffs.util

import org.apache.commons.lang3.time.StopWatch
import java.time.Duration

/**
 * Class for performing measurements, returned in the form (phase name, time in ms).
 */
class TimePerf {
    private val stopWatch = StopWatch()
    private var acc = 0L
    private val measurements = mutableListOf<Measurement>()

    fun phase(phase: String): String {
        if (measurements.isEmpty()) {
            stopWatch.start()
            measurements.add(Measurement(phase))
        } else {
            stopWatch.split()
            measurements.last().duration = Duration.ofMillis(stopWatch.splitTime - acc)
            acc = stopWatch.splitTime
            measurements.add(Measurement(phase))
        }
        return phase
    }

    fun finish(): List<Measurement> {
        stopWatch.stop()
        measurements.last().duration = Duration.ofMillis(stopWatch.time - acc)

        return measurements
    }

    data class Measurement(val phase: String) {
        var duration: Duration = Duration.ZERO
            internal set

        fun humanReadableDuration(): String {
            return duration
                .withNanos(0)
                .toString()
                .substring(2)
                .replace(Regex("(\\d[HMS])(?!$)"), "$1 ")
                .toLowerCase()
                .trim()
        }
    }

}
