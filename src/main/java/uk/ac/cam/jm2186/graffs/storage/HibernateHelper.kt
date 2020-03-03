package uk.ac.cam.jm2186.graffs.storage

import com.github.ajalt.clikt.core.BadParameterValue
import org.hibernate.Session
import org.hibernate.cfg.Configuration
import uk.ac.cam.jm2186.graffs.storage.model.NamedEntity
import uk.ac.cam.jm2186.graffs.storage.model.entities
import java.io.Serializable
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

inline fun <reified T> Session.getNullableEntity(id: Serializable): T? = get(T::class.java, id)

inline fun <reified T : NamedEntity> Session.getNamedEntity(name: String): T = getNullableEntity<T>(name)
    ?: throw BadParameterValue("${T::class.simpleName} `$name` does not exist")

fun <T> Session.getAllEntities(type: Class<T>): List<T> {
    val builder = criteriaBuilder
    val criteria = builder.createQuery(type)
    criteria.from(type)
    return createQuery(criteria).list()
}

suspend fun <R> Session.inTransaction(block: suspend Session.() -> R) {
    try {
        beginTransaction()
        this.block()
        transaction.commit()
    } catch (e: Exception) {
        transaction.rollback()
        throw e
    }
}
