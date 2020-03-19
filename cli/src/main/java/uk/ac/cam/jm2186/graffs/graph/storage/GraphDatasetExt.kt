package uk.ac.cam.jm2186.graffs.graph.storage

import com.github.ajalt.clikt.core.CliktError


fun GraphDataset.Companion.getAvailableDatasetsChecked() = getAvailableDatasets()
    .let {
        when {
            it == null -> throw CliktError("No `$DATASET_DIRECTORY_NAME` directory exists in the current path!")
            it.isEmpty() -> throw CliktError("The `$DATASET_DIRECTORY_NAME` directory has no subdirectories.")
            else -> it
        }
    }
