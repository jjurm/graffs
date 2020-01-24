package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkDGS
import uk.ac.cam.jm2186.graffs.metric.Metric.Companion.removeNodeAttributesExceptV
import java.io.ByteArrayOutputStream

class BetweennessCentralityMetric : Metric{

    class Factory : MetricFactory {
        override fun createMetric(params: List<Number>) = BetweennessCentralityMetric()
    }

    internal companion object {
        fun compute(graph: Graph) {
            val alg = BetweennessCentrality("v")
            alg.computeEdgeCentrality(false)
            alg.init(graph)
            alg.compute()
            removeNodeAttributesExceptV(graph)
            // graph nodes now contain 'v' attributes
        }
    }

    override fun evaluate(graph: Graph): MetricResult {
        compute(graph)

        val out = ByteArrayOutputStream()
        FileSinkDGS().writeAll(graph, out)
        val byteArray = out.toByteArray()
        return null to byteArray
    }
}

