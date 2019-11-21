package uk.ac.cam.jm2186.partii.graph

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.partii.GraphAssert

internal class RemovingEdgesGraphProducerTest {

    @Test
    fun generateGraphs() {
        val graph = Utils.generateSmallGraph()
        val producer = RemovingEdgesGraphProducer(graph, deletionRate = 0.3, seed = 42)
        val generated = producer.produce()
        producer.compute()

        GraphAssert.assertGraphEquals("""
            DGS004
            null 0 0
            an "0"
            an "1"
            an "2"
            an "3"
            an "4"
            an "5"
            an "6"
            an "7"
            an "8"
            an "9"
            ae "2_6" "2"  "6"
            ae "5_7" "5"  "7"
            ae "1_3" "1"  "3"
            ae "2_4" "2"  "4"
            ae "0_8" "0"  "8"
            ae "2_7" "2"  "7"
            ae "3_7" "3"  "7"
            ae "4_7" "4"  "7"
            ae "0_9" "0"  "9"
            ae "3_9" "3"  "9"
            ae "5_9" "5"  "9"
            ae "6_9" "6"  "9"
        """, generated)
    }
}
