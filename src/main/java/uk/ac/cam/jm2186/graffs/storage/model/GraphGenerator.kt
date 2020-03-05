package uk.ac.cam.jm2186.graffs.storage.model

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.GraphProducer
import uk.ac.cam.jm2186.graffs.graph.GraphProducerId
import uk.ac.cam.jm2186.graffs.graph.RemovingEdgesGenerator
import java.io.Serializable
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import kotlin.random.Random

@Entity
class GraphGenerator(
    name: String,

    val n: Int,
    val method: GraphProducerId,
    @ElementCollection(fetch = FetchType.EAGER)
    val params: List<Double>,
    val seed: Long
) : NamedEntity(name) {

    fun produceFromGraph(sourceGraph: Graph): List<DistortedGraph> {
        val generatorFactory = GraphProducer.map.getValue(method)
        return generatorFactory(seed, params)
            .produce(sourceGraph, n)
    }

    override fun toString(): String {
        return "$name(n=$n, method='$method', params=$params, seed=$seed)"
    }
}

fun CliktCommand.graphGenerator_name() =
    option("--name", help = "unique name of the graph generator", metavar = "NAME").required()

fun CliktCommand.graphGenerator_n() =
    option("-n", help = "Number of graphs to generate from each dataset").int().required().validate {
        require(it > 0) { "Value must be >=1" }
    }

fun CliktCommand.graphGenerator_method() =
    option(help = "Algorithm to generate graphs").choice(*GraphProducer.map.keys.toTypedArray())
        .default(RemovingEdgesGenerator.id)

fun CliktCommand.graphGenerator_params() =
    option(
        help = "Parameters to pass to the generator, delimited by comma",
        metavar = "FLOAT,..."
    ).double().split(delimiter = ",")
        .default(listOf())

fun CliktCommand.graphGenerator_seed() = option(help = "Seed for the generator").long()


