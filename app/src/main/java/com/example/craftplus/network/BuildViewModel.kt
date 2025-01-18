package com.example.craftplus.network

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craftplus.FirestoreRepository
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface BuildUiState {
    data class Success(val builds: String, val randomBuild: BuildObject?) : BuildUiState
    object Error : BuildUiState
    object Loading : BuildUiState
}

class BuildViewModel(
    private val repository: FirestoreRepository
) : ViewModel() {

    /** The mutable State that stores the status of the most recent request */
    var buildUiState: BuildUiState by mutableStateOf(BuildUiState.Loading)
        private set

    private var listResult: List<BuildObject>? = null

    /** State to track if save was successful */
    var saveSuccess: Boolean by mutableStateOf(false)
        private set

    /**
     * Call getBuildObjects() on init so we can display status immediately.
     */
    init {
        //getBuildObjects()
    }

    /**
     * Gets BuildObject information from the repository and updates the state.
     */
    fun getBuildObjects(): List<BuildObject>? {
        viewModelScope.launch {
            try {
                listResult = repository.getBuildObjects() // Fetch BuildObjects from the repository
                buildUiState = BuildUiState.Success(
                    "Success: ${listResult!!.size} builds retrieved",
                    listResult!!.random()
                )
                listResult!!.map {
                    BuildObject(
                        id = it.id,
                        title = it.title,
                        starter = it.starter,
                        friend = it.friend,
                        builder = it.builder,
                        recorder = it.recorder,
                        blocks = it.blocks,
                        video = it.video,
                        steps = it.steps
                    )
                }

            } catch (e: IOException) {
                buildUiState = BuildUiState.Error
            }
        }
        return listResult
    }

    /**
     * Rotates the selected random build from the list without fetching data again.
     */
    fun rotateBuild() {
        listResult?.let {
            buildUiState = BuildUiState.Success(
                "Success: ${it.size} builds retrieved",
                it.random()
            )
        }
    }

    /**
     * Saves the current selected BuildObject to the repository.
     */
    fun saveCurrentBuild(build: BuildObject?) {
        viewModelScope.launch {
            try {
                if (build != null) {
                    repository.saveBuildObject(build)
                    saveSuccess = true
                }
            } catch (e: Exception) {
                // Handle error, update UI state
            }
        }
    }

    /**
     * Toggles a property (e.g., the "steps" or "blocks") in the current BuildObject.
     */
    fun toggleSteps() {
        if (buildUiState is BuildUiState.Success) {
            val currentState = buildUiState as BuildUiState.Success
            val currentBuild = currentState.randomBuild ?: return

            // Update the steps property (e.g., increment by 1)
            val updatedBuild = currentBuild.copy(steps = currentBuild.steps + 1)

            // Update the UI state with the modified build
            buildUiState = currentState.copy(randomBuild = updatedBuild)
        }
    }

    /**
     * Resets the saveSuccess state after displaying the message.
     */
    fun resetSaveSuccess() {
        saveSuccess = false
    }

    /**
     * Loads the last saved BuildObject from the repository.
     */
    fun loadLastBuild() {
        viewModelScope.launch {
            try {
                // Load the last build asynchronously
                val lastBuild: BuildObject? = repository.loadLastBuildObject()

                // Update the UI state depending on whether a build was successfully loaded or not
                buildUiState = if (lastBuild != null) {
                    BuildUiState.Success("Success: loaded last build!", lastBuild)
                } else {
                    BuildUiState.Error
                }
            } catch (e: Exception) {
                // If an error occurs, update the UI state to show an error message
                buildUiState = BuildUiState.Error
            }
        }
    }
}
