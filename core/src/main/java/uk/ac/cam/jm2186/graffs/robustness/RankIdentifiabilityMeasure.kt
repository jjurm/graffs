package uk.ac.cam.jm2186.graffs.robustness

import org.apache.log4j.Logger
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.db.model.GraphCollection

class RankIdentifiabilityMeasure : RobustnessMeasure {

    companion object {

        val factory: RobustnessMeasureFactory = ::RankIdentifiabilityMeasure

        /**
         * Default value of k for the alpha-relaxed k-similarity.
         * The value is fixed so that this generalises to graphs of any size, but is based on the k values used in the
         * paper by Bozhilova et al.
         *
         * ~= 0.0234
         */
        val DEFAULT_K = listOf(100.0 / 3255, 100.0 / 4144, 100.0 / 6418).average()

        /**
         * Default value of alpha for the alpha-relaxed k-similarity, from the paper by Bozhilova et al.
         */
        val DEFAULT_ALPHA = 1.5
    }

    private val logger = Logger.getLogger(RankInstabilityMeasure::class.java)

    override suspend fun evaluate(
        metric: MetricInfo,
        graphCollection: GraphCollection,
        metadata: GraphCollectionMetadata
    ): Double {
        val overallRanking = metadata.getOverallRanking()
        val N = overallRanking.size
        val k = DEFAULT_K
        if (N * k < 50) logger.warn("k*N (=~${(k * N).toInt()}) is too small. The Paper uses k*N=100. The generalised RankIdentifiability measure may be imprecise on small graphs.")
        val alpha = DEFAULT_ALPHA

        val rankIdentifiability = overallRanking.rankings.map { ranking ->
            alphaRelaxedKSimilarity(k, alpha, overallRanking, ranking, overallRanking)
        }.min()!!

        return rankIdentifiability
    }
}
