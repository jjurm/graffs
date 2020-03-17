package uk.ac.cam.jm2186.graffs.db

import java.io.Serializable
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class AbstractJpaPersistable<T : Serializable> : Serializable {

    companion object {
        private val serialVersionUID = -5554308939380869754L
    }

    @Id
    @GeneratedValue
    private var id: T? = null

    fun getId(): T? = id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractJpaPersistable<*>) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString() = "Entity of type ${this.javaClass.name} with id: $id"

}
