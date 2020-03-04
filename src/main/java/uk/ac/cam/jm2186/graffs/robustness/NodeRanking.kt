package uk.ac.cam.jm2186.graffs.robustness

interface NodeRanking : List<String> {

    fun getRank(node: String): Rank

    fun getNode(rank: Rank) = this[rank.index]

}

inline fun NodeRanking.forEachRanked(action: (rank: Rank, nodeId: String) -> Unit) {
    forEachIndexed { index, nodeId -> action(Rank(index), nodeId) }
}
