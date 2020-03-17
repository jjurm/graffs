package uk.ac.cam.jm2186.graffs.db.model

import uk.ac.cam.jm2186.graffs.db.AbstractJpaPersistable
import javax.persistence.Entity

@Entity
class TestEntity(

    val value: String

) : AbstractJpaPersistable<Long>()
