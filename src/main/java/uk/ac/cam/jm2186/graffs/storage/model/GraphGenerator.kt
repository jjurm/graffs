package uk.ac.cam.jm2186.graffs.storage.model

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
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
        val generatorFactory = GraphProducer.map.getValue(method).getDeclaredConstructor().newInstance()
        val seedSource = Random(seed)

        return (0 until n).map { _ ->
            val graphSeed = seedSource.nextLong()
            val graph = generatorFactory
                .createGraphProducer(sourceGraph, graphSeed, params)
                .produceComputed()
            DistortedGraph(graphSeed, graph)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GraphGenerator) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "GraphGenerator('$name')"
    }
}

fun CliktCommand.graphGenerator_name() =
    option("--name", help = "unique name of the graph generator", metavar = "NAME").required()

fun CliktCommand.graphGenerator_n() =
    option("-n", help = "Number of graphs to generate from each dataset").int().required()

fun CliktCommand.graphGenerator_method() =
    option(help = "Algorithm to generate graphs").choice(*GraphProducer.map.keys.toTypedArray())
        .default(RemovingEdgesGenerator.ID)

fun CliktCommand.graphGenerator_params() =
    option(help = "Parameters to pass to the generator, delimited by comma").double().split(delimiter = ",")
        .default(listOf())

fun CliktCommand.graphGenerator_seed() = option(help = "Seed for the generator").long()


