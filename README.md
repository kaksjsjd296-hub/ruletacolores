# Ruleta de Colores (Fabric 1.21.1)

Mod para **Fabric 1.21.1** (Java 21) que funciona en **servidor dedicado y multijugador**.
Instálalo en el servidor **y** en los clientes (necesita Fabric API).

## Qué hace
- **Item Ruleta de Colores** (`/ruleta dar`, también en la pestaña de herramientas del creativo). Clic derecho abre el menú.
- **`/ruleta menu`** (OP nivel 2): menú para elegir el color de la ruleta (rojo, naranja, amarillo, verde, cian, azul, morada, rosa o **aleatorio**), escribir **título** y **mensaje**, y enviarlo a todos.
- **Animación de la ruleta**: al enviar desde el menú, la ruleta gira en pantalla para **todos los jugadores**, frena suave y cae en el color elegido (o uno al azar); al terminar sale la notificación.
- **Configurar animación** (botón en `/ruleta menu`): tamaño, duración, vueltas, volumen, oscurecer pantalla y posición (centro/arriba/abajo), con botón **Probar animación**. Se guarda en `config/ruleta_colores_anim.json` del operador y se envía al servidor con cada giro, así que no necesitas archivos de configuración en el servidor.
- **Notificación** arriba a la izquierda (título en color + icono de la ruleta que tocó, mensaje en color y "Presiona L para ocultar este mensaje"). El icono del item también cambia al color que tocó.
- **Animación de muerte** para todos los jugadores + mensaje rojo en el chat con quién murió y por qué.
- El jugador que muere pasa a **espectador** (sin pantalla de reaparecer) y deja una **cabeza con su skin sobre una valla de roble**.
- Al entrar, mensaje **verde** "<nombre> se conectó" con el mini icono de la ruleta a la izquierda.

## Compilar y descargar el .jar desde GitHub
1. Crea un repositorio en GitHub y sube todo el contenido de esta carpeta (rama `main`).
2. Ve a la pestaña **Actions** → "Build mod" → entra a la última ejecución → baja el artefacto **ruleta-colores-jar**.
3. Para una descarga fija: crea un tag `v1.0.0` (`git tag v1.0.0 && git push --tags`) y el .jar aparece en **Releases**.

## Assets del texture pack (animación de muerte y sonidos)
Los archivos del texture pack llegaron vacíos (0 bytes), así que el mod trae una animación de respaldo.
Para usar los assets reales del pack, copia esto dentro de `src/main/resources/assets/ruleta_colores/` y vuelve a compilar:
- `textures/gui/death/frame_0.png`, `frame_1.png`, ... (los cuadros de la animación)
- `death_animation.json`: `{ "frames": 24, "fps": 12, "width": 256, "height": 256 }` (tamaño en píxeles de cada frame)
- `sounds/death_animation.ogg` (sonido de la muerte) y `sounds/spin.ogg` (sonido de la ruleta girando)
- `textures/gui/spin_wheel.png` (256x256, centrada, gira sobre su centro; el color de cada sector debe coincidir con `sectorAngle` en `RuletaColor.java`) y `textures/gui/spin_pointer.png` (la flecha fija)
- Reemplaza `textures/item/ruleta_*.png` y `textures/font/join_icon.png` si quieres tus propios iconos (128x128 / 64x64).

## Aternos
Es un mod de Fabric 1.21.1 normal: sube el .jar a la carpeta de mods del servidor Fabric 1.21.1 (junto con Fabric API) y también ponlo en el cliente de cada jugador.

## Notas
- La tecla **L** también abre los logros de Minecraft; puedes reasignar esos logros en Controles.
- Solo operadores (nivel 2) pueden abrir el menú y enviar notificaciones.
