package com.example.induztek.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EquipoFotoStorage(
    context: Context
) {

    private val applicationContext = context.applicationContext

    private fun validarEquipoId(equipoId: Long) {
        require(equipoId > 0L) {
            "El ID del equipo debe ser mayor que cero."
        }
    }

    private fun obtenerArchivoCamara(equipoId: Long): File {
        return File(
            applicationContext.filesDir,
            "equipo_${equipoId}_camara.jpg"
        )
    }

    private fun obtenerArchivoGaleria(equipoId: Long): File {
        return File(
            applicationContext.filesDir,
            "equipo_${equipoId}_galeria.jpg"
        )
    }

    private fun calcularInSampleSize(
        options: BitmapFactory.Options
    ): Int {
        val reqWidth = 1024
        val reqHeight = 1024

        val height = options.outHeight
        val width = options.outWidth

        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (
                halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun decodificarBitmapOptimizado(
        archivo: File
    ): Bitmap? {
        if (!archivo.exists() || !archivo.isFile) {
            return null
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(
            archivo.absolutePath,
            options
        )

        options.inSampleSize = calcularInSampleSize(options)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(
            archivo.absolutePath,
            options
        )
    }

    private fun abrirImagen(uri: Uri): InputStream {
        val resolver = applicationContext.contentResolver
        var ultimoError: Exception? = null

        try {
            resolver.openInputStream(uri)?.let {
                return it
            }
        } catch (e: Exception) {
            ultimoError = e
        }

        try {
            resolver.openFileDescriptor(uri, "r")?.let {
                return ParcelFileDescriptor.AutoCloseInputStream(it)
            }
        } catch (e: Exception) {
            ultimoError = e
        }

        throw IllegalStateException(
            "No se pudo leer la imagen del proveedor " +
                    "${uri.authority ?: "desconocido"}. " +
                    (ultimoError?.localizedMessage
                        ?: "El proveedor no entregó el archivo."),
            ultimoError
        )
    }

    suspend fun guardarFotoCamara(
        equipoId: Long,
        bitmap: Bitmap
    ) {
        validarEquipoId(equipoId)

        withContext(Dispatchers.IO) {
            val archivo = obtenerArchivoCamara(equipoId)

            FileOutputStream(archivo).use { fos ->
                val exito = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    fos
                )

                if (!exito) {
                    throw IllegalStateException(
                        "No se pudo guardar la fotografía de cámara."
                    )
                }
            }
        }
    }

    suspend fun guardarFotoGaleria(
        equipoId: Long,
        uri: Uri
    ) {
        validarEquipoId(equipoId)

        withContext(Dispatchers.IO) {
            val temporal = File.createTempFile(
                "imagen_equipo_",
                ".tmp",
                applicationContext.cacheDir
            )

            try {
                abrirImagen(uri).use { entrada ->
                    FileOutputStream(temporal).use { salida ->
                        entrada.copyTo(salida)
                    }
                }

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                BitmapFactory.decodeFile(
                    temporal.absolutePath,
                    options
                )

                if (options.outWidth <= 0 || options.outHeight <= 0) {
                    throw IllegalStateException(
                        "El archivo seleccionado no contiene una imagen válida."
                    )
                }

                options.inSampleSize =
                    calcularInSampleSize(options)

                options.inJustDecodeBounds = false

                val bitmap = BitmapFactory.decodeFile(
                    temporal.absolutePath,
                    options
                ) ?: throw IllegalStateException(
                    "No se pudo decodificar la imagen seleccionada."
                )

                try {
                    val archivo = obtenerArchivoGaleria(equipoId)

                    FileOutputStream(archivo).use { fos ->
                        val exito = bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            90,
                            fos
                        )

                        if (!exito) {
                            throw IllegalStateException(
                                "No se pudo guardar la imagen de galería."
                            )
                        }
                    }
                } finally {
                    bitmap.recycle()
                }
            } finally {
                temporal.delete()
            }
        }
    }

    suspend fun cargarFotoCamara(
        equipoId: Long
    ): Bitmap? {
        validarEquipoId(equipoId)

        return withContext(Dispatchers.IO) {
            decodificarBitmapOptimizado(
                obtenerArchivoCamara(equipoId)
            )
        }
    }

    suspend fun cargarFotoGaleria(
        equipoId: Long
    ): Bitmap? {
        validarEquipoId(equipoId)

        return withContext(Dispatchers.IO) {
            decodificarBitmapOptimizado(
                obtenerArchivoGaleria(equipoId)
            )
        }
    }
}
