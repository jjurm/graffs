package uk.ac.cam.jm2186.graffs.storage

import org.hibernate.Session
import org.hibernate.cfg.Configuration
import uk.ac.cam.jm2186.graffs.storage.model.entities
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

fun <T> Session.getAllEntities(type:Class<T>): List<T> {
    val builder = criteriaBuilder
    val criteria = builder.createQuery(type)
    criteria.from(type)
    return createQuery(criteria).list()
}

fun <R> Session.inTransaction(block: Session.() -> R) {
    beginTransaction()
    this.block()
    transaction.commit()
}
