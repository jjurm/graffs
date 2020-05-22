import org.hibernate.Session
import uk.ac.cam.jm2186.graffs.db.HibernateHelper
import uk.ac.cam.jm2186.graffs.db.model.Experiment
import uk.ac.cam.jm2186.graffs.db.model.Experiment_

val session: Session = HibernateHelper.getBaseConfiguration().buildSessionFactory().openSession()

//-----jpa_typed_query0-----
// A type-checked CriteriaQuery<String> (expected to result in String)
val query = session.criteriaBuilder.createQuery(String::class.java)

// The following will compile (Experiment::name is of type String)
query.select(query.from(Experiment::class.java).get(Experiment_.name))
// The following will not compile because Experiment::generator is not of type String
query.select(query.from(Experiment::class.java).get(Experiment_.generator))

// The result is checked to be String
val result: String = session.createQuery(query).singleResult
//-----jpa_typed_query1-----

//-----hibernate_load_entity0-----
// Load entity from database
val experiment: Experiment = session.get(Experiment::class.java, "experimentName")

// Modify and persist
experiment.metrics += "Degree"
session.update(experiment)
//-----hibernate_load_entity1-----
