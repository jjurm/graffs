package uk.ac.cam.jm2186.graffs.storage.model

import uk.ac.cam.jm2186.graffs.storage.AbstractJpaPersistable
import javax.persistence.Entity

@Entity
class TestEntity(

    val value: String

) : AbstractJpaPersistable<Long>()
