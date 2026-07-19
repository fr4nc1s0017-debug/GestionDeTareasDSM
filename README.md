# 📱 Gestión de Tareas con Temporizador Pomodoro

Proyecto desarrollado para la asignatura **Desarrollo de Software para Móviles** de la **Universidad Don Bosco**.

## Descripción

Esta aplicación permite gestionar tareas utilizando la técnica Pomodoro, facilitando la organización del trabajo mediante sesiones de concentración y descansos.

Entre sus principales características se encuentran:

- Agregar nuevas tareas.
- Seleccionar una tarea activa.
- Marcar tareas como completadas.
- Eliminar tareas.
- Temporizador Pomodoro configurable.
- Control de iniciar, pausar, reanudar y reiniciar.
- Registro del historial de sesiones.
- Persistencia de datos.
- Conservación del estado durante cambios de orientación y ciclo de vida de la aplicación.

---

## Tecnologías utilizadas

- Android Studio
- Kotlin
- XML
- View Binding
- ViewModel
- Git & GitHub

---

## Integrantes

| Nombre | Carnet |
|---------|---------|
| Rodrigo Leando Hernandez Ordoñes | HO250329 |
| Erika Gabriela Guevara Chicas | GC250074 |
| Francisco Josue Santos Lopez | SL251022 |
| Rudy Mauricio González Pineda | GP250120 |
| Diego Alejandro Cruz Campos | CC251293 |

---

## Estructura del proyecto

```
app
│
├── ui
├── model
├── viewmodel
├── storage
├── timer
├── utils
└── adapter
```

---

## Funcionalidades

- [ ] Agregar tareas
- [ ] Eliminar tareas
- [ ] Completar tareas
- [ ] Seleccionar tarea activa
- [ ] Temporizador Pomodoro
- [ ] Barra de progreso
- [ ] Historial de sesiones
- [ ] Persistencia de datos
- [ ] Soporte para rotación de pantalla
- [ ] Recuperación del temporizador al volver a la aplicación

---

## Ejecución del proyecto

1. Clonar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle.
4. Ejecutar la aplicación en un emulador o dispositivo físico.

---

## Video de explicación

> Enlace del video: https://drive.google.com/drive/folders/1Gui86Tz424hzB1H5DrrBsIzI8BcGapri?usp=drive_link

## Configuración

Se habilitó View Binding en el archivo `build.gradle` del módulo de la aplicación.

```gradle
android {
    buildFeatures {
        viewBinding true
    }
}
```

Posteriormente se sincronizó el proyecto con Gradle para que Android Studio generara automáticamente las clases Binding correspondientes.
Esta clase permite acceder directamente a cada componente definido en el archivo XML.
# 2. Diseño de la pantalla principal (activity_main.xml)

Se diseñó la interfaz principal de la aplicación, organizando los elementos de forma clara para mejorar la experiencia del usuario.

La pantalla principal incluye:

- Temporizador Pomodoro
- Barra de progreso
- Botones de control
- Campo para ingresar tareas
- Botón para agregar tareas
- Resumen de productividad
- Lista de tareas
- Historial de sesiones
- Mensajes de estado cuando no existen datos

---

## Estructura general

```
ScrollView
│
└── LinearLayout
      │
      ├── Título
      ├── Tiempo restante
      ├── ProgressBar
      ├── Botones del temporizador
      ├── EditText
      ├── Botón Agregar tarea
      ├── Resumen
      ├── Mensaje sin tareas
      ├── Contenedor de tareas
      ├── Mensaje sin historial
      └── Contenedor del historial
```

---

# 3. Temporizador

Se diseñó la sección encargada de mostrar el tiempo restante del método Pomodoro.

Esta sección está compuesta por:

- TextView para mostrar el tiempo restante.
- ProgressBar horizontal.
- Botones de control.

Inicialmente el temporizador muestra:

```
25:00
```

La lógica del conteo regresivo será implementada posteriormente.

---

# 4. ProgressBar

Se incorporó una barra de progreso horizontal para representar visualmente el avance de la sesión Pomodoro.

Su función es mostrar el porcentaje del tiempo transcurrido durante cada sesión de trabajo.

Inicialmente permanece en estado de espera hasta que el temporizador sea iniciado.

---

# 5. Botones del temporizador

Se agregaron cuatro botones encargados del control del temporizador.

| Botón | Función |
|--------|---------|
| Iniciar | Comenzar la cuenta regresiva |
| Pausar | Detener temporalmente el conteo |
| Reanudar | Continuar desde el tiempo restante |
| Reiniciar | Volver al tiempo inicial |

En esta etapa únicamente se diseñó la interfaz gráfica; la funcionalidad será implementada posteriormente.

---

# 6. Sección de tareas

Se agregó un campo de texto (`EditText`) para ingresar nuevas tareas.

También se incorporó un botón encargado de agregarlas a la lista.

La lógica de validación y almacenamiento será desarrollada posteriormente.

---

# 7. Resumen de productividad

Se diseñó una sección destinada a mostrar información resumida sobre el avance del usuario.

Incluye:

- Número de tareas pendientes.
- Número de sesiones Pomodoro completadas.

Estos valores serán actualizados dinámicamente cuando se implemente la lógica de la aplicación.

---

# 8. Mensajes de estado vacío
Se agregaron mensajes informativos para mejorar la experiencia del usuario cuando aún no existen datos.

## Sin tareas

```
No hay tareas registradas.
```

## Sin historial

```
No hay sesiones completadas.
```

Estos mensajes desaparecerán automáticamente cuando existan elementos para mostrar.

---

# 9. XML reutilizable para tareas

Se creó un archivo independiente llamado:

```
item_task.xml
```

Este diseño representa una única tarea.

Contiene:

- CheckBox
- TextView
- Botón eliminar

La reutilización permite crear dinámicamente múltiples tareas sin duplicar código.

---

# 10. XML reutilizable para historial

Se creó un segundo archivo llamado:

```
item_history.xml
```

Representa una sesión completada del historial.
# Componentes utilizados

Durante el diseño de la interfaz se emplearon los siguientes componentes de Android:

- ScrollView
- LinearLayout
- TextView
- EditText
- Button
- ProgressBar
- CheckBox


---
