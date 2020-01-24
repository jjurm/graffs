package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.transformAll

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.nullableMultiple(): OptionWithValues<List<EachT>?, EachT, ValueT> {
    return transformAll {
        if (it.isEmpty()) null
        else it
    }
}
