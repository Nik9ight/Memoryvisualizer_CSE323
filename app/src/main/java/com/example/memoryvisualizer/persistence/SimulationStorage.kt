package com.example.memoryvisualizer.persistence

import android.content.Context
import com.example.memoryvisualizer.memento.SimulationSnapshot
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Handles persistence of simulation states.
 */
class SimulationStorage(private val context: Context) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val baseDir: File = context.getDir("simulations", Context.MODE_PRIVATE)

    init {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    /**
     * Saves a simulation snapshot to a file
     */
    suspend fun saveSimulation(name: String, snapshot: SimulationSnapshot): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(baseDir, "$name.json")
            val json = gson.toJson(snapshot)
            file.writeText(json)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Loads a simulation snapshot from a file
     */
    suspend fun loadSimulation(name: String): Result<SimulationSnapshot> = withContext(Dispatchers.IO) {
        try {
            val file = File(baseDir, "$name.json")
            if (!file.exists()) {
                return@withContext Result.failure(IOException("Simulation '$name' not found"))
            }
            val json = file.readText()
            val snapshot = gson.fromJson(json, SimulationSnapshot::class.java)
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lists all saved simulations
     */
    suspend fun listSimulations(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val files = baseDir.listFiles()?.filter { it.extension == "json" }
                ?.map { it.nameWithoutExtension } ?: emptyList()
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a saved simulation
     */
    suspend fun deleteSimulation(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(baseDir, "$name.json")
            if (file.exists() && file.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to delete simulation '$name'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
