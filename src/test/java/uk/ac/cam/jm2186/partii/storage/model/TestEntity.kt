package uk.ac.cam.jm2186.partii.storage.model

import uk.ac.cam.jm2186.partii.storage.AbstractJpaPersistable
import javax.persistence.Entity

@Entity
class TestEntity(

    val value: String

) : AbstractJpaPersistable<Long>()
