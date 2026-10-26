package com.example.induztek.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.induztek.data.local.DatabaseProvider
import com.example.induztek.data.repository.InstrumentoRepository
import com.example.induztek.ui.viewmodel.InstrumentoViewModel

class InstrumentoViewModelFactory(
    context: Context,
) : ViewModelProvider.Factory {

    private val applicationContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstrumentoViewModel::class.java)) {
            val database = DatabaseProvider.getDatabase(applicationContext)
            val repository = InstrumentoRepository(database.instrumentoDao(), database.calibracionDao())
            @Suppress("UNCHECKED_CAST")
            return InstrumentoViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase de ViewModel desconocida: ${modelClass.name}")
    }
}
