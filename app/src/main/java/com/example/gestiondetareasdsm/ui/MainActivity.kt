package com.example.gestiondetareasdsm.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import com.example.gestiondetareasdsm.model.Session
import com.example.gestiondetareasdsm.model.Task
import com.example.gestiondetareasdsm.timer.PomodoroTimer
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.AlertDialog
import android.text.InputType
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    private val sessions = mutableListOf<Session>()
    private var nextSessionId = 1

    private lateinit var pomodoro: PomodoroTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        //==========================
        // TEMPORIZADOR
        //==========================

        pomodoro = PomodoroTimer(

            onTick = { time, progress ->
                val minutes = (time / 1000) / 60
                val seconds = (time / 1000) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                binding.pbPomodoro.progress = progress
            },

            onFinish = {
                val minutes = (pomodoro.totalTime / 1000) / 60
                val seconds = (pomodoro.totalTime / 1000) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                binding.pbPomodoro.progress = 100

                vibrate()

                val activeTask = tasks.find { it.active }
                if (activeTask != null) {
                    registerSession(activeTask.title)
                }

                Toast.makeText(this, "¡Pomodoro terminado!", Toast.LENGTH_LONG).show()
            },

            onStateChanged = { state ->
                updateButtonStates(state)
            }
        )

        //==========================
        // TOCAR EL TIMER PARA CONFIGURAR MINUTOS
        //==========================

        binding.tvTimer.setOnClickListener {
            showSetDurationDialog()
        }



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
                Toast.makeText(this, "Seleccione una tarea antes de iniciar.", Toast.LENGTH_SHORT).show()
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

        updateButtonStates(pomodoro.state)
        updateSummary()
        updateEmptyState()
        updateEmptyHistory()
    }

    //==========================================================
    // AGREGAR TAREA
    //==========================================================

    private fun addTask() {
        val title = binding.etTask.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Ingrese una tarea.", Toast.LENGTH_SHORT).show()
            return
        }

        tasks.add(Task(id = nextId++, title = title))
        binding.etTask.text.clear()
        drawTasks()
    }


    //==========================================================
    // CONFIGURAR DURACIÓN DEL TEMPORIZADOR
    //==========================================================

    private fun showSetDurationDialog() {

        if (pomodoro.state == PomodoroTimer.TimerState.RUNNING ||
            pomodoro.state == PomodoroTimer.TimerState.PAUSED) {

            Toast.makeText(
                this,
                "Reinicia el temporizador antes de cambiar la duración.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        val currentMinutes = (pomodoro.totalTime / 1000 / 60).toInt()
        input.setText(currentMinutes.toString())
        input.hint = "Minutos"
        input.setSelection(input.text.length)

        AlertDialog.Builder(this)
            .setTitle("Configurar duración")
            .setMessage("Ingresa la cantidad de minutos para la cuenta regresiva")
            .setView(input)
            .setPositiveButton("Aceptar") { _, _ ->

                val minutes = input.text.toString().toIntOrNull()

                if (minutes == null || minutes <= 0) {

                    Toast.makeText(
                        this,
                        "Ingresa un número de minutos válido.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    pomodoro.setDurationMinutes(minutes)

                }

            }
            .setNegativeButton("Cancelar", null)
            .show()

    }

    //==========================================================
    // DIBUJAR TAREAS
    //==========================================================

    private fun drawTasks() {
        binding.llTasksContainer.removeAllViews()

        for (task in tasks) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_task, binding.llTasksContainer, false)

            val check = view.findViewById<CheckBox>(R.id.cbCompleted)
            val text = view.findViewById<TextView>(R.id.tvTaskName)
            val delete = view.findViewById<Button>(R.id.btnDelete)

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
        tasks.forEach { it.active = false }
        selected.active = true
        drawTasks()
    }

    //==========================================================
    // HISTORIAL DE SESIONES
    //==========================================================

    private fun registerSession(taskTitle: String) {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val time = format.format(System.currentTimeMillis())

        sessions.add(
            Session(
                id = nextSessionId++,
                taskTitle = taskTitle,
                time = time
            )
        )

        drawHistory()
        updateSummary()
    }

    private fun drawHistory() {
        binding.llHistoryContainer.removeAllViews()

        for (session in sessions) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_session, binding.llHistoryContainer, false)
            val taskName = view.findViewById<TextView>(R.id.tvSessionTask)
            val time = view.findViewById<TextView>(R.id.tvSessionTime)
            taskName.text = session.taskTitle
            time.text = session.time
            binding.llHistoryContainer.addView(view)
        }
        updateEmptyHistory()
    }

    private fun updateEmptyHistory() {
        binding.tvEmptyHistory.visibility =
            if (sessions.isEmpty()) View.VISIBLE else View.GONE
    }

    //==========================================================
    // RESUMEN
    //==========================================================

    private fun updateSummary() {
        val completed = tasks.count { it.completed }
        val pending = tasks.size - completed

        binding.tvSummary.text =
            "Tareas pendientes: $pending\nSesiones completadas: ${sessions.size}"
    }

    //==========================================================
    // MENSAJE CUANDO NO HAY TAREAS
    //==========================================================

    private fun updateEmptyState() {
        binding.tvEmptyTasks.visibility =
            if (tasks.isEmpty()) View.VISIBLE else View.GONE
    }

    //==========================================================
    // ESTADO DE BOTONES SEGÚN EL TEMPORIZADOR
    //==========================================================

    private fun updateButtonStates(state: PomodoroTimer.TimerState) {
        when (state) {
            PomodoroTimer.TimerState.IDLE,
            PomodoroTimer.TimerState.FINISHED -> {
                binding.btnStart.isEnabled = true
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = false
                binding.btnReset.isEnabled = false
            }
            PomodoroTimer.TimerState.RUNNING -> {
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = true
                binding.btnResume.isEnabled = false
                binding.btnReset.isEnabled = true
            }
            PomodoroTimer.TimerState.PAUSED -> {
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = true
                binding.btnReset.isEnabled = true
            }
        }
    }

    //==========================================================
    // VIBRACIÓN AL TERMINAR
    //==========================================================

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}