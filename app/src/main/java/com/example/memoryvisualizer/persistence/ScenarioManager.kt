package com.example.memoryvisualizer.persistence

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ScenarioManager(private val context: Context) {
    private val gson = Gson()
    private val scenariosDir: File = context.getDir("scenarios", Context.MODE_PRIVATE)
    private val builtInScenarios = Scenario.createSampleScenarios()
    
    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())
    val scenarios: StateFlow<List<Scenario>> = _scenarios.asStateFlow()
    
    init {
        scenariosDir.mkdirs()
        loadScenarios()
    }
    
    private fun loadScenarios() {
        val customScenarios = scenariosDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { file ->
                try {
                    gson.fromJson<Scenario>(
                        file.readText(),
                        object : TypeToken<Scenario>() {}.type
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            
        _scenarios.value = builtInScenarios + customScenarios
    }
    
    suspend fun saveScenario(scenario: Scenario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(scenariosDir, "${scenario.name}.json")
            file.writeText(gson.toJson(scenario))
            loadScenarios()
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteScenario(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(scenariosDir, "$name.json")
            if (!file.exists() || file.delete()) {
                loadScenarios()
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to delete scenario"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    
    fun getScenario(name: String): Scenario? {
        return scenarios.value.find { it.name == name }
    }
}
