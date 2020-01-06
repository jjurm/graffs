package uk.ac.cam.jm2186.partii.storage

import org.hibernate.cfg.Configuration
import uk.ac.cam.jm2186.partii.storage.model.entities
import java.util.*

object HibernateHelper {

    fun delegate() = lazy { getBaseConfiguration().buildSessionFactory() }

    fun getBaseConfiguration(): Configuration {
        val configuration = Configuration()
            .configure()
            .addProperties(getDBProperties())
        entities.forEach { `class` -> configuration.addAnnotatedClass(`class`.java) }
        return configuration
    }

    private fun getDBProperties(): Properties {
        val properties = Properties()
        System.getenv("DB_URL")?.also { properties["hibernate.connection.url"] = it }
        System.getenv("DB_USER")?.also { properties["hibernate.connection.username"] = it }
        System.getenv("DB_PASSWORD")?.also { properties["hibernate.connection.password"] = it }
        return properties
    }

}
