# Induztek Terreno — EQ073

Proyecto Android de la Evaluación 3 de **Desarrollo de Aplicaciones Móviles (DSY1105)**, en Kotlin + Jetpack Compose con Retrofit, Room y backend Spring Boot (Eureka + API Gateway + MySQL).

**[Ver guía completa de instalación, ejecución en la universidad, pruebas, cobertura y APK firmado](README_Induztek_EQ073.md)**

- Android (rama de integración): `feature/integracion-e3-final`
- Backend: https://github.com/dzzetafps/induztek-microservicios (rama `feature/sebastian-e3-integracion-backend`)
- Trello: https://trello.com/b/cikJWwM5/induztek-terreno-eq073

**Inicio rápido (mismo computador):** iniciar MySQL y los cinco procesos del backend, comprobar Eureka en `http://localhost:8761/`, abrir Android Studio, iniciar un emulador y ejecutar `app`. Retrofit utiliza `http://10.0.2.2:8081/` para acceder al Gateway del PC sin `adb reverse`. Es necesario preparar también el usuario de prueba en el MySQL local.

Para ejecutar pruebas y generar el reporte de cobertura:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:createDebugUnitTestCoverageReport
```

El reporte HTML se encuentra en `app/build/reports/coverage/test/debug/index.html`. Los porcentajes deben informarse según el reporte de la versión final y distinguiendo cobertura global de cobertura de lógica.
