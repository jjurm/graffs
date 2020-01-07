package uk.ac.cam.jm2186.partii

import com.github.ajalt.clikt.core.CliktCommand
import org.apache.spark.sql.SparkSession
import uk.ac.cam.jm2186.BuildConfig
import kotlin.reflect.KProperty

object SparkHelper {

    fun delegate() = Delegate()

    class Delegate internal constructor() {
        private var value: SparkSession? = null

        operator fun getValue(command: CliktCommand, property: KProperty<*>): SparkSession {
            val v = value
            if (v != null) {
                return v
            }
            val v2 = SparkSession.builder()
                .appName("${BuildConfig.NAME} ${command.commandName}")
                .master("local")
                .getOrCreate()
            value = v2
            return v2
        }
    }
}
