package uk.ac.cam.jm2186.partii.graph

import org.graphstream.graph.Graph

interface GraphProducer {

    fun produce(): Graph

    fun compute()

}
