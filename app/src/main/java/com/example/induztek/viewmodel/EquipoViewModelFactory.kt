
package com.example.induztek.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.induztek.data.local.DatabaseProvider
import com.example.induztek.data.remote.terreno.ServiciosTerreno
import com.example.induztek.data.repository.EquipoRepository

class EquipoViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val applicationContext = context.applicationContext

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(EquipoViewModel::class.java)) {

            val database = DatabaseProvider.getDatabase(
                applicationContext
            )

            val repository = EquipoRepository(
                equipoDao = database.equipoDao(),
                remoteDataSource = ServiciosTerreno.equipos
            )

            @Suppress("UNCHECKED_CAST")
            return EquipoViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Clase de ViewModel desconocida: ${modelClass.name}"
        )
    }
}
