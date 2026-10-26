package com.example.induztek.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EquipoFotoStorage(
    context: Context,
) {

    private val applicationContext = context.applicationContext

    private fun validarEquipoId(equipoId: Long) {
        if (equipoId <= 0L) {
            throw IllegalArgumentException("El ID del equipo debe ser mayor que cero.")
        }
    }

    private fun obtenerArchivoCamara(equipoId: Long): File {
        return File(applicationContext.filesDir, "equipo_${equipoId}_camara.jpg")
    }

    private fun obtenerArchivoGaleria(equipoId: Long): File {
        return File(applicationContext.filesDir, "equipo_${equipoId}_galeria.jpg")
    }

    private fun calcularInSampleSize(options: BitmapFactory.Options): Int {
        val reqWidth = 1024
        val reqHeight = 1024
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if ((height > reqHeight) || (width > reqWidth)) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (((halfHeight / inSampleSize) >= reqHeight) && ((halfWidth / inSampleSize) >= reqWidth)) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun decodificarBitmapOptimizado(archivo: File): Bitmap? {
        if (!archivo.exists() || !archivo.isFile) {
            return null
        }
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(archivo.absolutePath, options)
        options.inSampleSize = calcularInSampleSize(options)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(archivo.absolutePath, options)
    }

    suspend fun guardarFotoCamara(equipoId: Long, bitmap: Bitmap) {
        validarEquipoId(equipoId)
        withContext(Dispatchers.IO) {
            val archivo = obtenerArchivoCamara(equipoId)
            FileOutputStream(archivo).use { fos ->
                val exito = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                if (!exito) {
                    throw IllegalStateException("No se pudo comprimir y guardar la fotografía de cámara.")
                }
            }
        }
    }

    suspend fun guardarFotoGaleria(equipoId: Long, uri: Uri) {
        validarEquipoId(equipoId)
        withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: throw IllegalArgumentException("No se pudo abrir el InputStream de la URI proporcionada.")

            options.inSampleSize = calcularInSampleSize(options)
            options.inJustDecodeBounds = false

            val bitmap = applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: throw IllegalStateException("No se pudo decodificar el bitmap desde la URI.")

            val archivo = obtenerArchivoGaleria(equipoId)
            FileOutputStream(archivo).use { fos ->
                val exito = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                if (!exito) {
                    throw IllegalStateException("No se pudo comprimir y guardar la fotografía de galería.")
                }
            }
        }
    }

    suspend fun cargarFotoCamara(equipoId: Long): Bitmap? {
        validarEquipoId(equipoId)
        return withContext(Dispatchers.IO) {
            decodificarBitmapOptimizado(obtenerArchivoCamara(equipoId))
        }
    }

    suspend fun cargarFotoGaleria(equipoId: Long): Bitmap? {
        validarEquipoId(equipoId)
        return withContext(Dispatchers.IO) {
            decodificarBitmapOptimizado(obtenerArchivoGaleria(equipoId))
        }
    }
}
