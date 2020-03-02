package uk.ac.cam.jm2186.graffs.storage.model

import java.io.Serializable
import javax.persistence.*

@Entity
class Tag(
    @Id
    val name: String?
) : Serializable {

    @OneToMany(mappedBy = "tag", cascade = [CascadeType.REMOVE], orphanRemoval = true, fetch = FetchType.LAZY)
    val distortedGraphs: List<DistortedGraphOld> = listOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false
        if (name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name.toString()
    }

}
