package uk.ac.cam.jm2186.graffs.robustness


fun kSimilarity(k: Double, nodes: List<String>, ATheta: NodeRanking, AMu: NodeRanking): Double {
    val N = nodes.size
    val threshold = N * (1 - k)

    return nodes.sumBy { node ->
        when (ATheta.getRank(node).rankValue > threshold && AMu.getRank(node).rankValue > threshold) {
            true -> 1
            false -> 0
        }
    } / (N * k)
}

fun alphaRelaxedKSimilarity(k: Double, alpha: Double, nodes: List<String>, A: NodeRanking, B: NodeRanking): Double {
    val N = nodes.size
    val thresholdA = N * (1 - k * alpha)
    val thresholdB = N * (1 - k)

    return nodes.sumBy { node ->
        when (A.getRank(node).rankValue > thresholdA && B.getRank(node).rankValue > thresholdB) {
            true -> 1
            false -> 0
        }
    } / (N * k)
}
