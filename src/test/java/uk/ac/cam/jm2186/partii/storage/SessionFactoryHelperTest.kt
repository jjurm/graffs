package uk.ac.cam.jm2186.partii.storage

import org.hibernate.SessionFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.partii.storage.model.TestEntity
import uk.ac.cam.jm2186.partii.storage.model.TestEntity_
import java.util.*

internal class SessionFactoryHelperTest {

    companion object {
        private val overrideProperties = Properties().apply {
            this["hibernate.connection.driver_class"] = "org.h2.Driver"
            this["hibernate.connection.url"] = "jdbc:h2:mem:test"
            this["hibernate.connection.username"] = ""
            this["hibernate.connection.password"] = ""
        }

        private lateinit var sessionFactory: SessionFactory

        @BeforeAll
        @JvmStatic
        fun setUp() {
            sessionFactory = SessionFactoryHelper
                .getBaseConfiguration()
                .addProperties(overrideProperties)
                .addAnnotatedClass(TestEntity::class.java)
                .buildSessionFactory()
        }
    }

    @Test
    fun `Hibernate database setup`() {
        sessionFactory.openSession().use { session ->
            val string = "hello world"

            // insert TestEntity
            session.beginTransaction()
            session.save(TestEntity(string))
            session.transaction.commit()

            val builder = session.criteriaBuilder

            val criteria = builder.createQuery(String::class.java)
            val root = criteria.from(TestEntity::class.java)
            criteria.select(root.get(TestEntity_.value))

            val result = session.createQuery(criteria).singleResult
            assertEquals(string, result)
        }
    }

}
