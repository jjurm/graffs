package uk.ac.cam.jm2186.partii.storage

import org.hibernate.cfg.Configuration
import uk.ac.cam.jm2186.partii.storage.model.entities
import java.util.*

object SessionFactoryHelper {

    fun getBaseConfiguration(): Configuration {
        val configuration = Configuration()
            .configure()
            .addProperties(getDBProperties())
        entities.forEach { `class` -> configuration.addAnnotatedClass(`class`.java) }
        return configuration
    }

    private fun getDBProperties(): Properties {
        val properties = Properties()
        System.getenv("DB_URL")?.also { properties["javax.persistence.jdbc.url"] = it }
        System.getenv("DB_USER")?.also { properties["javax.persistence.jdbc.user"] = it }
        System.getenv("DB_PASSWORD")?.also { properties["javax.persistence.jdbc.password"] = it }
        return properties
    }

}
