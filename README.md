# Volley Scoreboard

Aplicación Android para llevar el marcador de un partido de Volley

Muestra el nombre de los equipos (editable) y un color configurable para cada uno (cada mitad del marcador tiene el color de fondo de cada equipo) y se mostrará el nombre junto a la indicacion "local" y "visitante".

En el centro de la pantalla, en dos bloques de tamaño grande se controlarán los puntos del set en curso.

En tamaño menor el conteo de sets de cada equipo.

-----------------------------------
|     Local     |    Visitante    |
|    A TEAM     |     Equipo B    |
|               |                 |
|      14       |       21        |
|               |                 |
|            2  |  1              |
-----------------------------------

Cada bloque de conteos (puntos de set en curso y sets de equipo) tiene dos controles (+ y -) para incrementear o decrementar el valor.

También hay un botón para reiniciar a 0 los puntos del set en curso de los dos marcadores.

Cuando uno de los equipos llegue a un punto de set, el color de su marcador se pondrá en rojo.
Cuando uno de los equipos llegue a un punto de partido, el color del texto de su marcador se pondrá en rojo con un efecto como de latido de corazón.

Para evitar consumo de batería, y utilizar la aplicación con la pantalla apagada, también se pueden incrementar los puntos de cada equipo con los botones de subir y bajar volumen del teléfono (subir para local y bajar para visitante).

## UX y aspecto gráfico

- Marcador y sets: partidos al mejor de 1/3/5; sets normales a 25 con diferencia
  de 2; último set (3º o 5º) a 15 con diferencia de 2; sets se actualizan
  automáticamente al ganar y reinician puntos; controles manuales de sets
  limitados 0–3.
- Interacción y edición: tocar el nombre abre modal para editar nombre y elegir
  color (paleta RGB), por defecto naranja/morado; puntos no pueden bajar de 0.
- Resets: un reset de puntos del set en juego y otro general que devuelve
  nombres/colores por defecto y pone puntos y sets a 0.
- Persistencia/rotación: estado (nombres, colores, puntos, sets) se mantiene en
  rotación y al reabrir; app forzada a horizontal.
- Volumen: botones de volumen solo suman (+1 local con subir, +1 visitante con
  bajar), también con pantalla bloqueada; la app captura el volumen en primer
  plano anulando el sistema; habrá ajuste para activar/desactivar esta captura
  con aviso.
- Configuración: selector de número de sets (1, 3 o 5; si es 1 el set es a 25, si
  3/5 el último a 15); ajuste para captura de volumen.
- Finalización y avisos: set point → fondo rojo; match point → texto rojo con
  latido suave; fin de partido → confetti y aplauso suave tipo “golf clap”.
- UI de puntos: contadores estilo flip clock (dígitos divididos con animación de
  volteo, tipografía monoespaciada, fondo con color de equipo, buen contraste).