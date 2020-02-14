package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.parameters.options.*

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.nullableMultiple(): OptionWithValues<List<EachT>?, EachT, ValueT> {
    return transformAll {
        if (it.isEmpty()) null
        else it
    }
}

fun <T : Any> RawOption.choicePairs(
    choices: Map<String, T>,
    metavar: String = choices.keys.joinToString("|", prefix = "[", postfix = "]")
): NullableOption<Pair<String, T>, Pair<String, T>> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert(
        metavar,
        completionCandidates = com.github.ajalt.clikt.completion.CompletionCandidates.Fixed(choices.keys)
    ) {
        val choice = choices[it]
        if (choice != null) (it to choice) else fail("invalid choice: $it. (choose from ${choices.keys.joinToString(", ")})")
    }
}
