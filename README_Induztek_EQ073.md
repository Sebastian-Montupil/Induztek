# Induztek Terreno — EQ073 | Evaluación 3

Aplicación Android de gestión de visitas técnicas, equipos, instrumentos y calibraciones de Induztek. Proyecto académico de **Desarrollo de Aplicaciones Móviles (DSY1105)**, desarrollado en Kotlin con Jetpack Compose y conectado a un backend Spring Boot mediante Retrofit.

## Equipo y repositorios

- **Sebastián Montupil y Vicente Martínez** — equipo EQ073; desarrollo e integración colaborativos.
- Android: https://github.com/Sebastian-Montupil/Induztek
- Backend: https://github.com/dzzetafps/induztek-microservicios
- Tablero Trello: https://trello.com/b/cikJWwM5/induztek-terreno-eq073
- **Rama integrada de Android:** `feature/integracion-e3-final`.
- **Rama integrada del backend:** `feature/sebastian-e3-integracion-backend`.

> Las ramas de integración son las referencias utilizadas para la evaluación 3. No se debe suponer que `master` o `main` ya contienen estos cambios.

## Arquitectura y tecnologías

| Componente | Implementación |
| --- | --- |
| Interfaz Android | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Organización | MVVM, ViewModels, StateFlow, coroutines y repositorios |
| Persistencia local | Room, DAOs, entidades y migraciones |
| Integración remota | Retrofit, OkHttp y Gson |
| Autenticación | Usuario y contraseña; peticiones protegidas con autenticación HTTP Basic a través del Gateway |
| Backend | Spring Boot, Java 21, Eureka, API Gateway, tres microservicios |
| Datos del servidor | Tres bases MySQL: `induztek_usuarios`, `induztek_visitas`, `induztek_equipos` |
| API externa | Open-Meteo, información meteorológica de referencia |

Arquitectura de conexión:

```text
Android (Retrofit)
       |
       | http://10.0.2.2:8081/  (emulador estándar)
       v
API Gateway :8081 ---- Eureka :8761
       |                 (descubrimiento)
       +-- ms-usuarios :8082 --> MySQL induztek_usuarios
       +-- ms-visitas  :8083 --> MySQL induztek_visitas
       +-- ms-equipos  :8084 --> MySQL induztek_equipos
```

La aplicación solicita el clima a **Open-Meteo** directamente desde Android; no pasa por uno de estos microservicios.

## Funcionalidades

- **Inicio de sesión y perfil:** autenticación, consulta de perfil, actualización de información y cierre de sesión.
- **Visitas técnicas:** registro de orden de trabajo, cliente, instalación, fecha, tipo, ingeniero, técnicos y observaciones; consulta de visitas persistidas mediante el servicio remoto.
- **Equipos:** registro y consulta por cliente e instalación; número de serie único en el backend; mensajes de validación y manejo de errores.
- **Fotografías:** captura por cámara y selección desde galería, con almacenamiento privado en el dispositivo y recuperación tras reabrir la aplicación. **Las imágenes son locales, no se sincronizan con MySQL.**
- **Instrumentos:** registro y consulta mediante Retrofit y el microservicio de equipos; validaciones y reglas de acceso.
- **Calibraciones:** interfaz y lógica de gestión; la implementación de `CalibracionRepository` que existe en esta rama trabaja con el DAO local de Room.
- **Clima:** temperatura, humedad y precipitación de referencia obtenidas de Open-Meteo.

### Alcances técnicos conocidos

- En esta versión `RetrofitClient.kt` utiliza `http://10.0.2.2:8081/` como URL fija para el **emulador Android estándar ejecutado en el mismo computador** que el backend. No es una dirección válida para un teléfono físico ni para conectarse a un Gateway alojado en otro equipo.
- La consulta de técnicos asociados a una visita desde el repositorio remoto todavía no está disponible: el método correspondiente lanza `UnsupportedOperationException`.
- En `InstrumentoRepository`, la actualización remota de instrumentos no está implementada; la validación contra calibraciones aún usa DAOs locales.
- La disponibilidad de cada funcionalidad requiere que estén levantados los servicios y bases de datos de los que depende. Los datos MySQL del computador personal **no viajan automáticamente** al computador de la universidad.

## Ejecutar en el computador de la universidad

### Requisitos

- Android Studio con Android SDK y un emulador compatible. El entorno comprobado por el equipo fue **Medium Phone API 37**.
- Java **JDK 21** para el backend y acceso a MySQL en `localhost:3306`.
- Conectividad para descargar inicialmente dependencias de Gradle y Maven.
- Puertos libres **8761, 8081, 8082, 8083 y 8084**.

### 1. Preparar el backend

Clonar la rama integrada del backend:

```powershell
git clone --branch feature/sebastian-e3-integracion-backend https://github.com/dzzetafps/induztek-microservicios.git
cd .\induztek-microservicios
```

Crear en MySQL las tres bases:

```sql
CREATE DATABASE IF NOT EXISTS induztek_usuarios;
CREATE DATABASE IF NOT EXISTS induztek_visitas;
CREATE DATABASE IF NOT EXISTS induztek_equipos;
```

**Importante:** en `ms-visitas` y `ms-equipos`, las credenciales de MySQL se leen de `DB_USERNAME` y `DB_PASSWORD`; se establecen en **cada terminal** desde la que se ejecuta cada módulo. En `ms-usuarios`, revisar `application.properties`: usa `induztek_app` con contraseña vacía por defecto e importa opcionalmente `application-local.properties`. Configurar un usuario MySQL y la contraseña local de manera segura antes de ejecutar; **no subir secretos a GitHub**. También se necesita una cuenta de demostración existente en la base `induztek_usuarios`; la cuenta creada en el PC de desarrollo no se copia automáticamente.

Abrir **cinco terminales separadas**, cada una situada en la carpeta del módulo correspondiente, y mantenerlas activas:

```powershell
# Terminal 1 — desde la raíz del backend
cd .\eureka-server
.\mvnw.cmd spring-boot:run
```

```powershell
# Terminal 2 — desde la raíz del backend
cd .\api-gateway
.\mvnw.cmd spring-boot:run
```

```powershell
# Terminal 3 — desde la raíz del backend
cd .\ms-usuarios
.\mvnw.cmd spring-boot:run
```

```powershell
# Terminal 4 — desde la raíz del backend
cd .\ms-visitas
$env:DB_USERNAME = "root"  # Adaptar al usuario configurado
$segura = Read-Host "Clave de MySQL" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $segura).Password
Remove-Variable segura
.\mvnw.cmd spring-boot:run
```

```powershell
# Terminal 5 — desde la raíz del backend
cd .\ms-equipos
$env:DB_USERNAME = "root"  # Adaptar al usuario configurado
$segura = Read-Host "Clave de MySQL" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $segura).Password
Remove-Variable segura
.\mvnw.cmd spring-boot:run
```

Las variables de entorno se conservan en el proceso de PowerShell mientras la terminal permanezca abierta; la clave no debe mostrarse ni publicarse. Las instrucciones anteriores suponen que cada terminal nueva comienza en la raíz del backend.

Verificar Eureka en `http://localhost:8761/` y comprobar que aparecen `MS-USUARIOS`, `MS-VISITAS` y `MS-EQUIPOS`. Comprobar que el Gateway escucha en `localhost:8081`; una respuesta `401` de un recurso protegido puede significar autenticación faltante y no una caída del servidor.

### 2. Ejecutar Android

```powershell
git clone --branch feature/integracion-e3-final https://github.com/Sebastian-Montupil/Induztek.git
cd .\Induztek
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

Abrir el directorio **Induztek** en Android Studio, iniciar el emulador y ejecutar **app** con **Run**.

**Con esta configuración no es necesario utilizar `adb reverse`**: `10.0.2.2` comunica el emulador estándar de Android con el `localhost` del computador anfitrión. El backend y el emulador deben ejecutarse en el **mismo computador** para esta guía. En Android API 37, si se solicita permiso de acceso a la red local, concederlo para que la aplicación pueda contactar al Gateway.

La aplicación inicia con la pantalla de ingreso. Utilizar una cuenta válida configurada en el **MySQL de ese computador**; no utilizar las credenciales de MySQL para iniciar sesión en Android.

### 3. Verificación funcional breve

1. Iniciar sesión y consultar **Mi perfil**.
2. Registrar una visita con OT no repetida y verificar que aparece en **Visitas técnicas**.
3. Registrar un equipo con número de serie único y consultar su ficha.
4. Capturar una foto con cámara y seleccionar otra desde galería. Salir y entrar a la aplicación para comprobar almacenamiento **local**.
5. Consultar instrumentos y, según el flujo disponible, la sección de calibraciones.
6. Verificar los datos de la visita/equipo/instrumento desde el Gateway o MySQL.
7. Consultar el panel de clima; esta operación requiere conexión hacia Open-Meteo.

## Pruebas y cobertura

Ejecutar desde la raíz del proyecto Android:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:createDebugUnitTestCoverageReport
```

Reporte HTML:

```text
app/build/reports/coverage/test/debug/index.html
```

También existe una tarea JaCoCo personalizada `jacocoTestReport` configurada en `app/build.gradle.kts`. Para comprobar pruebas instrumentadas, utilizar un emulador iniciado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

**Estado de verificación al 23 de noviembre de 2026:** se informó `BUILD SUCCESSFUL` para compilación Android, pruebas unitarias completas y generación del informe de cobertura. La última ampliación incluyó ocho pruebas de API simulada de `VisitaRepositoryRemotoTest.kt` (commit `df987eb`).

La última captura de cobertura global anterior a esa ampliación indicó **19 % de instrucciones**, mientras varios paquetes de lógica presentaron cobertura notablemente mayor (por ejemplo, `com.example.induztek.viewmodel`, 83 %). **No corresponde afirmar que Android completo alcanzó 80 % de cobertura global.** El porcentaje actualizado deberá obtenerse del informe generado sobre la versión final.

Las pruebas del backend se ejecutan **por separado**, dentro de cada microservicio Maven; las cifras de cobertura de Android y backend no son intercambiables.

## APK de entrega

Se generó un **APK release firmado**, con clave almacenada por el equipo fuera del repositorio. El APK fue instalado y probado en emulador tras corregir un problema inicial de optimización de la variante release.

Para volver a generar el APK firmado: en Android Studio, seleccionar **Build > Generate Signed App Bundle or APK**, elegir **APK**, seleccionar el keystore propio y la variante **release**. No incluir `.jks`, claves privadas ni contraseñas en GitHub. Guardar el APK resultante en el directorio de entrega.

> El directorio `app/release/` no es código fuente y se mantiene fuera del control de versiones. El APK firmado depende del backend accesible para funcionar con datos remotos.

## Estructura relevante

- `app/src/main/java/com/example/induztek/ui/screens/`: pantallas Compose.
- `app/src/main/java/com/example/induztek/ui/navigation/`: navegación.
- `app/src/main/java/com/example/induztek/viewmodel/` y `ui/viewmodel/`: ViewModels.
- `app/src/main/java/com/example/induztek/data/local/`: Room, DAOs y entidades.
- `app/src/main/java/com/example/induztek/data/repository/`: repositorios.
- `app/src/main/java/com/example/induztek/data/remote/`: fuentes de datos de APIs.
- `app/src/main/java/com/example/induztek/network/`: Retrofit, autenticación y sesión HTTP.
- `app/src/main/java/com/example/induztek/domain/validation/`: reglas de validación.
- `app/src/test/`: pruebas unitarias locales y remotas simuladas.

## Seguridad y límites de la demostración

El Gateway local utiliza **HTTP** (`10.0.2.2:8081`) y la aplicación permite tráfico sin cifrar para ese escenario académico. Esto **no equivale a una configuración de producción segura**. Para exponer el servicio en Internet o distribuirlo a teléfonos reales se necesita un servidor accesible, HTTPS, control de secretos y revisión de seguridad de autenticación/autorización. Los registros de pruebas y fotografías deben utilizar datos ficticios y respetar la privacidad.

## Coordinación de entrega

Conservar el código fuente de **ambos repositorios**, informes de pruebas, documentación de configuración, APK firmado y evidencias verificadas. No fusionar las ramas integradas a `master`/`main` salvo decisión del equipo. Los archivos con credenciales deben permanecer fuera de los repositorios públicos.
