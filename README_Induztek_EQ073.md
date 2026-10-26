# Induztek Terreno — EQ073

Aplicación Android desarrollada para apoyar el registro y la consulta de actividades técnicas en terreno de **Induztek**. El proyecto corresponde a la Evaluación Parcial 2 / Defensa Parcial 3 de Desarrollo de Aplicaciones Móviles.

## Equipo

- **Sebastián Montupil:** visitas técnicas, persistencia y consulta de visitas, navegación y animación del formulario.
- **Vicente Martínez:** equipos, fotografías, instrumentos, validaciones y reglas asociadas a instrumentos y calibraciones.
- **Equipo:** EQ073.

## Tecnologías y estructura

- **Lenguaje:** Kotlin.
- **Interfaz:** Jetpack Compose y Material 3.
- **Arquitectura:** MVVM sencillo con `UiState`, `ViewModel`, `StateFlow` y `collectAsStateWithLifecycle`.
- **Persistencia local sin conexión:** Room con DAOs, entidades, repositorios y migraciones hasta la versión 5.
- **Navegación:** Navigation Compose mediante `AppNavigation.kt`.
- **Operaciones asíncronas:** Kotlin Coroutines y Flow.
- **Control de versiones:** Git y GitHub, con ramas y Pull Requests individuales.

### Organización del código

- `app/src/main/java/com/example/induztek/model/`: modelos de interfaz, catálogos y estados.
- `app/src/main/java/com/example/induztek/ui/screens/`: pantallas Compose.
- `app/src/main/java/com/example/induztek/ui/navigation/`: rutas de navegación.
- `app/src/main/java/com/example/induztek/viewmodel/`: ViewModels y factories.
- `app/src/main/java/com/example/induztek/data/local/`: base de datos Room, DAOs, entidades y migraciones.
- `app/src/main/java/com/example/induztek/data/repository/`: acceso a datos y lógica de repositorios.
- `app/src/main/java/com/example/induztek/domain/validation/`: validaciones desacopladas.
- `app/src/test/`: pruebas unitarias.
- `app/src/androidTest/`: pruebas instrumentadas.

## Funcionalidades implementadas

### Inicio de sesión y roles

- Pantalla de ingreso, sesión y navegación principal.
- Catálogos de usuarios y roles: cliente, técnico, ingeniero y jefe técnico.
- Restricciones de acceso a consultas de visitas: los clientes solo obtienen visitas de su empresa desde la capa de datos; usuarios internos autorizados pueden consultar el conjunto correspondiente.

### Visitas técnicas

- Formulario para registrar visita con cliente, instalación vinculada al cliente, fecha, número de orden de trabajo (OT), tipo de visita, ingeniero a cargo, técnicos participantes y observaciones.
- Validaciones de campos y control de OT duplicada.
- Prevención de guardados repetidos, indicador de guardado y confirmación de éxito.
- Persistencia en Room de visitas y técnicos asociados.
- Listado reactivo desde Room, con estados diferenciados de carga, error y lista vacía.
- Animación `AnimatedVisibility` para mensajes de validación.

### Equipos

- Registro de equipos asociados a cliente e instalación.
- Formulario con iconos, validaciones y mensajes específicos.
- Listado reactivo consultado por cliente e instalación, con almacenamiento en Room.
- Navegación desde el listado al registro de equipos.
- Tarjetas expandibles animadas con `animateContentSize`.
- Captura mediante cámara y selección desde galería utilizando APIs nativas de Android (`ActivityResultContracts`).
- Almacenamiento local privado de fotografías vinculadas al identificador del equipo.

### Instrumentos y calibraciones

- Modelos, repositorios, validadores y lógica de instrumentos y calibraciones.
- Interfaz de instrumentos con MVVM y conexión a la sección de instrumentos de la aplicación (PR #20 y PR #22).
- Reglas RN-11/RN-12 contempladas en validaciones y repositorios, con alcance de integración pendiente de comprobación final.

**Alcance y limitaciones:** la interfaz de calibraciones y la aplicación completa de RN-11 al guardado de pruebas eléctricas no se consideran terminadas sin verificación adicional. La operación de galería, cancelaciones y los permisos en todos los recorridos deben comprobarse manualmente en el dispositivo o emulador. No se declara que estén verificadas pruebas no ejecutadas.

## Uso básico

1. Iniciar sesión con un usuario de los catálogos de prueba del proyecto.
2. Desde Inicio, ingresar a **Visitas técnicas** para registrar o consultar visitas.
3. En una nueva visita, seleccionar cliente e instalación, completar la OT y los demás campos obligatorios y guardar.
4. Regresar al listado y verificar el nuevo registro.
5. Desde Inicio, abrir **Equipos e instrumentos**; seleccionar cliente e instalación para consultar equipos o registrar uno nuevo.
6. Expandir una tarjeta de equipo para acceder a cámara o galería y consultar sus fotografías.
7. Entrar a **Instrumentos** desde la sección correspondiente.

## Instalación y ejecución

**Requisitos:** Android Studio con SDK de Android instalado, un emulador o dispositivo Android compatible y conexión a Internet para la primera descarga de dependencias Gradle.

1. Clonar o descargar el repositorio: `https://github.com/Sebastian-Montupil/Induztek`.
2. Abrir la carpeta del proyecto **Induztek** en Android Studio.
3. Esperar la sincronización de Gradle.
4. Iniciar un emulador Android (se utilizó **Medium Phone API 37.0**) o conectar un dispositivo.
5. Seleccionar la configuración **app** y pulsar **Run**.

Desde la terminal de Android Studio en Windows (PowerShell), ejecutar:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

La última tarea requiere un emulador iniciado o un dispositivo conectado.

## Verificación realizada

- Compilación de la aplicación Android (`assembleDebug`) informada con **BUILD SUCCESSFUL**.
- Pruebas unitarias y pruebas instrumentadas ejecutadas durante el desarrollo.
- Después de integrar PR #20 se observó **BUILD SUCCESSFUL** en `master` con las tareas de compilación y pruebas Gradle.
- Prueba manual de visitas: permanecen registradas al cerrar y abrir nuevamente la app.
- Prueba manual de equipos: aparecen tras reiniciar al volver a seleccionar cliente e instalación.
- Prueba manual de fotografía de cámara: la imagen se recupera después de reiniciar la aplicación.
- Integración visual de instrumentos del PR #22: compilación y **44 pruebas unitarias** comunicadas por el integrante responsable; queda pendiente registrar evidencia de prueba visual y RN-12 completa.

## Evidencias sugeridas para la evaluación

Capturas del ingreso, formulario y listado de visitas, persistencia después de reinicio, registro y consulta de equipos, fotografía almacenada, pantalla de instrumentos, terminal con `BUILD SUCCESSFUL`, historial de Pull Requests y tablero Trello. Las evidencias deben corresponder a funcionalidades probadas realmente.

## Desarrollo colaborativo

Se utilizaron ramas de trabajo separadas para implementar y revisar funcionalidades, con Pull Requests y revisión cruzada. Entre los PR integrados se encuentran:

- **#13:** registro y listado de equipos con Room y MVVM.
- **#14–#16:** persistencia y guardado de visitas en Room.
- **#17:** cámara, galería, almacenamiento de imágenes y animación de equipos.
- **#18:** navegación a registro de equipos.
- **#19:** listado reactivo y permisos de visitas.
- **#20:** interfaz y MVVM de instrumentos.
- **#21:** animación de validaciones en visitas.
- **#22:** conexión de la interfaz de instrumentos.

Repositorio: https://github.com/Sebastian-Montupil/Induztek

Planificación: tablero **Induztek Terreno — EQ073** en Trello (https://trello.com/b/cikJWwM5/induztek-terreno-eq073).