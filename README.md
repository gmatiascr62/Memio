# Memio ✨

Notas adhesivas para Android que podés fijar como widget en la pantalla de inicio.

## Funciones

- Crear, editar y eliminar notas con 5 colores.
- Fijar cualquier nota como widget desde la app («📌 Agregar a inicio»).
- Widgets redimensionables desde 1x1; el texto se centra y se achica para entrar.
- Tocar un widget abre su nota para editarla, con la opción «Cambiar nota».

## Requisitos

- Android 8.0 (API 26) o superior.
- Para compilar: JDK 17 y el Android SDK (API 35).

## Compilar

```bash
./gradlew :app:assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Descargar el APK

Cada push a `main` compila la app con GitHub Actions. El APK se descarga desde la
pestaña **Actions** → última ejecución de «Compilar APK Memio» → artefacto **Memio-APK**.

## Estructura

| Archivo | Qué hace |
| --- | --- |
| `SplashActivity.kt` | Pantalla de bienvenida. |
| `MainActivity.kt` | Lista de notas, editor y selección de nota para un widget. |
| `MemoWidget.kt` | Proveedor del widget: dibuja la nota vinculada. |
| `PinWidgetReceiver.kt` | Vincula la nota con el widget recién fijado. |

Las notas se guardan como JSON en `SharedPreferences` (`notes` → `all_notes`), y
cada widget guarda la nota que muestra en `linked_<widgetId>`.
