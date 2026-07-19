package com.example.gestiondetareasdsm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gestiondetareasdsm.R
import com.example.gestiondetareasdsm.databinding.ActivityMainBinding
import com.example.gestiondetareasdsm.model.Task
import com.example.gestiondetareasdsm.timer.PomodoroTimer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    private lateinit var pomodoro: PomodoroTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->

            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        //==========================
        // TEMPORIZADOR
        //==========================

        pomodoro = PomodoroTimer(

            onTick = { time, progress ->

                val minutes = (time / 1000) / 60
                val seconds = (time / 1000) % 60

                binding.tvTimer.text =
                    String.format("%02d:%02d", minutes, seconds)

                binding.pbPomodoro.progress = progress
            },

            onFinish = {

                binding.tvTimer.text = "25:00"

                binding.pbPomodoro.progress = 100

                Toast.makeText(
                    this,
                    "¡Pomodoro terminado!",
                    Toast.LENGTH_LONG
                ).show()

            }

        )

        //==========================
        // BOTÓN AGREGAR
        //==========================

        binding.btnAdd.setOnClickListener {
            addTask()
        }

        //==========================
        // BOTÓN INICIAR
        //==========================

        binding.btnStart.setOnClickListener {

            val activeTask = tasks.find { it.active }

            if (activeTask == null) {

                Toast.makeText(
                    this,
                    "Seleccione una tarea antes de iniciar.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            pomodoro.start()

        }

        //==========================
        // BOTÓN PAUSAR
        //==========================

        binding.btnPause.setOnClickListener {

            pomodoro.pause()

        }

        //==========================
        // BOTÓN REANUDAR
        //==========================

        binding.btnResume.setOnClickListener {

            pomodoro.resume()

        }

        //==========================
        // BOTÓN REINICIAR
        //==========================

        binding.btnReset.setOnClickListener {

            pomodoro.reset()

        }

        updateSummary()
        updateEmptyState()
    }

    //==========================================================
    // AGREGAR TAREA
    //==========================================================

    private fun addTask() {

        val title = binding.etTask.text.toString().trim()

        if (title.isEmpty()) {

            Toast.makeText(
                this,
                "Ingrese una tarea.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        tasks.add(
            Task(
                id = nextId++,
                title = title
            )
        )

        binding.etTask.text.clear()

        drawTasks()

    }

    //==========================================================
    // DIBUJAR TAREAS
    //==========================================================

    private fun drawTasks() {

        binding.llTasksContainer.removeAllViews()

        for (task in tasks) {

            val view = LayoutInflater.from(this)
                .inflate(
                    R.layout.item_task,
                    binding.llTasksContainer,
                    false
                )

            val check =
                view.findViewById<CheckBox>(R.id.cbCompleted)

            val text =
                view.findViewById<TextView>(R.id.tvTaskName)

            val delete =
                view.findViewById<Button>(R.id.btnDelete)

            text.text = task.title

            check.isChecked = task.completed

            if (task.active) {
                view.setBackgroundColor(0xFFE3F2FD.toInt())
            } else {
                view.setBackgroundColor(0x00000000)
            }

            check.setOnCheckedChangeListener { _, isChecked ->

                task.completed = isChecked

                updateSummary()

            }

            delete.setOnClickListener {

                tasks.remove(task)

                drawTasks()

            }

            view.setOnClickListener {

                selectTask(task)

            }

            binding.llTasksContainer.addView(view)

        }

        updateSummary()

        updateEmptyState()

    }

    //==========================================================
    // SELECCIONAR TAREA ACTIVA
    //==========================================================

    private fun selectTask(selected: Task) {

        tasks.forEach {

            it.active = false

        }

        selected.active = true

        drawTasks()

    }

    //==========================================================
    // RESUMEN
    //==========================================================

    private fun updateSummary() {

        val completed = tasks.count {

            it.completed

        }

        val pending = tasks.size - completed

        binding.tvSummary.text =

            "Tareas pendientes: $pending\nSesiones completadas: 0"

    }

    //==========================================================
    // MENSAJE CUANDO NO HAY TAREAS
    //==========================================================

    private fun updateEmptyState() {

        binding.tvEmptyTasks.visibility =

            if (tasks.isEmpty())
                View.VISIBLE
            else
                View.GONE

    }

}