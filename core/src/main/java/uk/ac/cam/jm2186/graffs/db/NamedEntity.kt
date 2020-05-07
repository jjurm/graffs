package uk.ac.cam.jm2186.graffs.db

import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class NamedEntity(
    @Id
    val name: String
) : Entity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NamedEntity) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
