# Arquitectura y manejo de excepciones

## Patrón MVC aplicado

- **Modelo**: `main.model` encapsula estado persistente y lógica compartida.  
  - `GameRegistry` administra el catálogo de juegos y la carga dinámica desde JAR.  
  - `StatsManager` persiste estadísticas en `stats.json`.
- **Vista**: `main.view` contiene los `JInternalFrame` de interacción.  
  - `MainView` actúa como contenedor (`JDesktopPane`).  
  - `MenuView` lista juegos y permite cargar JAR externos.  
  - `StatsView` muestra los récords.
- **Controlador**: `GameController` coordina UI y modelo, delega al `GameRegistry`, actualiza `StatsManager` y abre los internal frames. Las vistas no manipulan directamente el modelo y el modelo no conoce a las vistas.

## Excepciones documentadas

1. **Carga de juegos** – `GameController.loadGame` captura excepciones al pedir juegos al `GameRegistry` y notifica mediante `JOptionPane` para evitar que la app se caiga.  
2. **Persistencia de estadísticas** – `StatsManager.saveStats` y `loadStats` capturan `IOException` al leer/escribir `stats.json`, registrando el problema sin detener la plataforma.

## Nota sobre la firma de `iniciar()`

Por indicación docente (mensaje 19/11), la interfaz `GameFunction.iniciar()` retorna `JInternalFrame` en lugar de `void`. Todos los juegos siguen esta firma y el controlador consume el frame devuelto. Si se compara con el enunciado inicial, este ajuste responde a la circular del profesor y se deja documentado aquí para el evaluador.
