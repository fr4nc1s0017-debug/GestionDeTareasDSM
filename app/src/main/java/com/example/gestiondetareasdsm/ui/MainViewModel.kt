package com.example.gestiondetareasdsm.ui

import androidx.lifecycle.ViewModel
import com.example.gestiondetareasdsm.model.Session
import com.example.gestiondetareasdsm.model.Task
import com.example.gestiondetareasdsm.timer.PomodoroTimer

class MainViewModel : ViewModel() {


    // listas principales


    val tasks = mutableListOf<Task>()

    val sessions = mutableListOf<Session>()


    // contadores


    var nextTaskId = 1

    var nextSessionId = 1


    // temporizador


    var timerState = PomodoroTimer.TimerState.IDLE

    var totalTime = PomodoroTimer.DEFAULT_TIME

    var remainingTime = PomodoroTimer.DEFAULT_TIME

    // Hora exacta en la que terminará el Pomodoro
    var finishTimeMillis: Long = 0L

    // Saber si realmente está corriendo
    var isRunning = false

    // tarea activa


    fun getActiveTask(): Task? {
        return tasks.find { it.active }
    }

    fun selectTask(task: Task) {

        tasks.forEach {
            it.active = false
        }

        task.active = true
    }


    // tareas

    fun addTask(title: String) {

        tasks.add(

            Task(
                id = nextTaskId++,
                title = title
            )

        )

    }

    fun removeTask(task: Task) {

        tasks.remove(task)

    }

    fun completedTasks(): Int {

        return tasks.count { it.completed }

    }

    fun pendingTasks(): Int {

        return tasks.size - completedTasks()

    }


    // sesiones


    fun addSession(session: Session) {

        sessions.add(session)

    }

}