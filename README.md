# Sistema de despacho de mercancías

Aplicación de consola en Java para administrar productos, destinos y solicitudes de despacho de mercancías. Cada solicitud tiene un código único, un destino, un producto, una cantidad, una prioridad y un estado.

**Actividad:** Estructuras de Datos — Unidad 3 — Protocolo Individual  
**Caso:** 19. Sistema de despacho de mercancías  
**Universidad de Cartagena — Ingeniería de Software**  
**Java:** 17

## Problema

El sistema debe registrar solicitudes de despacho sin repetir códigos ni destinos, permitir consultar una solicitud directamente por su código, agrupar y ordenar los despachos por destino y procesar primero las solicitudes de mayor prioridad, sin importar el orden en que fueron registradas.

La aplicación mantiene un registro general de solicitudes, un índice de búsqueda por código, un registro de destinos únicos, un catálogo de productos, un reporte ordenado por destino y una cola de despacho prioritaria. Todas las estructuras almacenan referencias a los mismos objetos y se mantienen sincronizadas cuando un registro se modifica o se elimina.

## Colecciones utilizadas

| Necesidad del sistema | Interfaz | Implementación | Justificación |
|---|---|---|---|
| Conservar todas las solicitudes en orden de registro | `List<SolicitudDespacho>` | `ArrayList<SolicitudDespacho>` | El registro general debe conservar todos los objetos, permitir recorridos completos y mantener el orden cronológico de registro. |
| Evitar destinos duplicados | `Set<Destino>` | `HashSet<Destino>` | Un destino se identifica por su nombre; `equals()` y `hashCode()` impiden registrar dos veces la misma ciudad sin depender de un orden de almacenamiento. |
| Buscar solicitudes por código | `Map<String, SolicitudDespacho>` | `HashMap<String, SolicitudDespacho>` | El código es una clave única y permite localizar la solicitud directamente, sin recorrer la lista completa. |
| Buscar productos por código | `Map<String, Producto>` | `HashMap<String, Producto>` | El catálogo necesita resolver un producto por su código cuando se registra o modifica una solicitud. |
| Mantener los despachos ordenados por destino | `NavigableMap<Destino, List<SolicitudDespacho>>` | `TreeMap<Destino, List<SolicitudDespacho>>` | Las claves se ordenan con `Destino.compareTo()` y producen un reporte alfabético por ciudad. |
| Procesar primero los despachos prioritarios | `Queue<SolicitudDespacho>` | `PriorityQueue<SolicitudDespacho>` | La salida depende de la prioridad y, en caso de empate, de la secuencia de registro, no del orden de llegada a la cola. |

## Comparación e igualdad

- `Destino` implementa `equals()` y `hashCode()` usando el nombre normalizado, por lo que `"Medellín"` y `"medellín"` son el mismo destino. Implementa `Comparable<Destino>` para el orden alfabético utilizado por el `TreeMap`.
- `Producto` y `SolicitudDespacho` implementan `equals()` y `hashCode()` a partir del código único.
- `Comparadores.PRIORIDAD_DESPACHO` ordena primero por peso de prioridad descendente (`CRÍTICA`, `ALTA`, `MEDIA`, `BAJA`) y luego por secuencia de registro ascendente.

## Consistencia entre colecciones

- Al registrar una solicitud se agrega a la lista, al índice por código y al árbol agrupado por destino.
- Al cambiar el código se reemplaza la clave anterior del `HashMap` por la nueva.
- Al cambiar el destino se retira la solicitud del grupo anterior del `TreeMap` y se agrega al nuevo.
- Al cambiar la prioridad de una solicitud que está en la cola, se retira y se vuelve a insertar para que el `PriorityQueue` la reubique.
- Al cambiar el estado a despachada o cancelada, la solicitud sale de la cola. Si vuelve a pendiente, debe programarse de nuevo con la opción 8.
- Al eliminar una solicitud se retira de la lista, del índice, del grupo de destino y de la cola.

## Menú

```text
 1. Registrar producto
 2. Registrar destino
 3. Registrar solicitud de despacho
 4. Consultar todas las solicitudes
 5. Buscar solicitud por código
 6. Consultar destinos registrados
 7. Mostrar despachos por destino
 8. Agregar solicitud pendiente a la cola
 9. Consultar próximo despacho prioritario
10. Procesar siguiente despacho prioritario
11. Consultar cola en orden de atención
12. Modificar solicitud
13. Eliminar solicitud
14. Mostrar estado de las colecciones
15. Cargar datos de demostración
16. Consultar solicitudes por estado
 0. Salir
```

## Datos de demostración

Al iniciar, la aplicación carga 18 solicitudes, 8 destinos y 8 productos. Los datos incluyen diferentes prioridades, destinos, estados y relaciones entre solicitudes, productos y destinos. La opción 15 reemplaza la información actual por este conjunto de demostración.

## Compilar y ejecutar

Requiere JDK 17 o superior.

### Windows PowerShell

```powershell
javac -encoding UTF-8 -d out/classes (Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName })
java "-Dfile.encoding=UTF-8" -cp out/classes edu.unicartagena.despacho.Main
```

### Linux o macOS

```bash
javac -encoding UTF-8 -d out/classes $(find src/main/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp out/classes edu.unicartagena.despacho.Main
```

## Pruebas

Las pruebas se ejecutan sin dependencias externas.

### Windows PowerShell

```powershell
javac -encoding UTF-8 -cp out/classes -d out/test-classes (Get-ChildItem -Recurse src/test/java -Filter *.java | ForEach-Object { $_.FullName })
java "-Dfile.encoding=UTF-8" -cp "out/classes;out/test-classes" edu.unicartagena.despacho.PruebasSistema
```

### Linux o macOS

```bash
javac -encoding UTF-8 -cp out/classes -d out/test-classes $(find src/test/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp "out/classes:out/test-classes" edu.unicartagena.despacho.PruebasSistema
```

Resultado esperado:

```text
OK - 150 verificaciones completadas.
```

## Estructura del proyecto

```text
src/
├── main/java/edu/unicartagena/despacho/
│   ├── Main.java
│   ├── comun/
│   │   ├── Validador.java
│   │   └── ValidacionException.java
│   ├── modelo/
│   │   ├── Destino.java
│   │   ├── EstadoSolicitud.java
│   │   ├── Prioridad.java
│   │   ├── Producto.java
│   │   └── SolicitudDespacho.java
│   ├── servicio/
│   │   ├── CatalogoProductos.java
│   │   ├── Comparadores.java
│   │   ├── DatosDemostracion.java
│   │   ├── GestorDespachos.java
│   │   ├── RegistroDestinos.java
│   │   ├── ResumenColecciones.java
│   │   └── SistemaDespacho.java
│   └── ui/
│       ├── Consola.java
│       └── MenuPrincipal.java
└── test/java/edu/unicartagena/despacho/
    └── PruebasSistema.java
```

## Documentación

- Java Collections Framework: <https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/doc-files/coll-overview.html>
- `PriorityQueue`: <https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/PriorityQueue.html>
- `TreeMap`: <https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/TreeMap.html>
- `Comparable` y `Comparator`: <https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Comparable.html>

## Repositorio

<https://github.com/JuanJoPV01/sistema-despacho-mercancias>
