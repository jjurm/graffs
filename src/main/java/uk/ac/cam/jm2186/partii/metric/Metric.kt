package uk.ac.cam.jm2186.partii.metric

import org.graphstream.graph.Graph
import java.io.Serializable

interface Metric<Result : Serializable> : Serializable {

    fun evaluate(graph: Graph): Result

}
