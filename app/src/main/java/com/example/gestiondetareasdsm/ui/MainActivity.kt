package com.example.gestiondetareasdsm.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.gestiondetareasdsm.R
import com.example.gestiondetareasdsm.databinding.ActivityMainBinding
import com.example.gestiondetareasdsm.model.Session
import com.example.gestiondetareasdsm.model.Task
import com.example.gestiondetareasdsm.storage.DataStoreManager
import com.example.gestiondetareasdsm.timer.PomodoroTimer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var storage: DataStoreManager
    private lateinit var pomodoro: PomodoroTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = DataStoreManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        lifecycleScope.launch {
            viewModel.tasks.clear()
            viewModel.tasks.addAll(storage.getTasks().first())
            viewModel.sessions.clear()
            viewModel.sessions.addAll(storage.getSessions().first())

            viewModel.nextTaskId = (viewModel.tasks.maxOfOrNull { it.id } ?: 0) + 1
            viewModel.nextSessionId = (viewModel.sessions.maxOfOrNull { it.id } ?: 0) + 1

            viewModel.remainingTime = storage.getRemainingTime().first().takeIf { it > 0 } ?: PomodoroTimer.DEFAULT_TIME
            viewModel.finishTimeMillis = storage.getFinishTime().first()

            setupTimer()
            drawTasks()
            drawHistory()
            updateSummary()
            updateEmptyState()
            updateEmptyHistory()
        }

        binding.tvTimer.setOnClickListener { showSetDurationDialog() }
        binding.btnAdd.setOnClickListener { addTask() }
        binding.btnStart.setOnClickListener {
            if (viewModel.getActiveTask() == null) {
                Toast.makeText(this, "Seleccione una tarea antes de iniciar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pomodoro.start()
        }
        binding.btnPause.setOnClickListener { pomodoro.pause() }
        binding.btnResume.setOnClickListener { pomodoro.resume() }
        binding.btnReset.setOnClickListener {
            pomodoro.reset()
            lifecycleScope.launch {
                storage.saveRemainingTime(pomodoro.remainingTime)
                storage.saveFinishTime(0L)
            }
        }
    }

    private fun setupTimer() {
        pomodoro = PomodoroTimer(
            onTick = { time, progress ->
                viewModel.remainingTime = time
                binding.tvTimer.text = String.format("%02d:%02d", (time / 1000) / 60, (time / 1000) % 60)
                binding.pbPomodoro.progress = progress

                lifecycleScope.launch {
                    storage.saveRemainingTime(time)
                    storage.saveFinishTime(pomodoro.finishTimeMillis)
                }
            },
            onFinish = {
                binding.pbPomodoro.progress = 100
                vibrate()
                viewModel.getActiveTask()?.let { registerSession(it.title) }
                lifecycleScope.launch {
                    storage.saveRemainingTime(0L)
                    storage.saveFinishTime(0L)
                }
                Toast.makeText(this, "¡Pomodoro terminado!", Toast.LENGTH_LONG).show()
            },
            onStateChanged = { updateButtonStates(it) }
        )

        if (viewModel.remainingTime != PomodoroTimer.DEFAULT_TIME) {
            val field = pomodoro::class.java.getDeclaredField("remainingTime")
            field.isAccessible = true
            field.setLong(pomodoro, viewModel.remainingTime)
        }

        if (viewModel.finishTimeMillis > 0) {
            val finishField = pomodoro::class.java.getDeclaredField("finishTimeMillis")
            finishField.isAccessible = true
            finishField.setLong(pomodoro, viewModel.finishTimeMillis)
            pomodoro.restoreRemainingTime()
        }

        binding.tvTimer.text = String.format("%02d:%02d", (viewModel.remainingTime / 1000) / 60, (viewModel.remainingTime / 1000) % 60)
        updateButtonStates(pomodoro.state)
    }

    private fun addTask() {
        val title = binding.etTask.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Ingrese una tarea.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addTask(title)
        binding.etTask.text.clear()
        saveTasks()
        drawTasks()
    }

    private fun saveTasks() {
        lifecycleScope.launch { storage.saveTasks(viewModel.tasks) }
    }

    private fun saveSessions() {
        lifecycleScope.launch { storage.saveSessions(viewModel.sessions) }
    }

    private fun showSetDurationDialog() {
        if (pomodoro.state == PomodoroTimer.TimerState.RUNNING || pomodoro.state == PomodoroTimer.TimerState.PAUSED) {
            Toast.makeText(this, "Reinicia el temporizador antes de cambiar la duración.", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText((pomodoro.totalTime / 1000 / 60).toInt().toString())

        AlertDialog.Builder(this)
            .setTitle("Configurar duración")
            .setView(input)
            .setPositiveButton("Aceptar") { _, _ ->
                input.text.toString().toIntOrNull()?.let {
                    if (it > 0) pomodoro.setDurationMinutes(it)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun drawTasks() {
        binding.llTasksContainer.removeAllViews()

        for (task in viewModel.tasks) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_task, binding.llTasksContainer, false)

            val check = view.findViewById<CheckBox>(R.id.cbCompleted)
            val text = view.findViewById<TextView>(R.id.tvTaskName)
            val delete = view.findViewById<Button>(R.id.btnDelete)

            text.text = task.title
            check.isChecked = task.completed
            view.setBackgroundColor(if (task.active) 0xFFE3F2FD.toInt() else 0x00000000)

            check.setOnCheckedChangeListener { _, isChecked ->
                task.completed = isChecked
                saveTasks()
                updateSummary()
            }

            delete.setOnClickListener {
                viewModel.removeTask(task)
                saveTasks()
                drawTasks()
            }

            view.setOnClickListener {
                viewModel.selectTask(task)
                saveTasks()
                drawTasks()
            }

            binding.llTasksContainer.addView(view)
        }

        updateSummary()
        updateEmptyState()
    }

    private fun registerSession(taskTitle: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
        viewModel.addSession(Session(viewModel.nextSessionId++, taskTitle, time))
        saveSessions()
        drawHistory()
        updateSummary()
    }

    private fun drawHistory() {
        binding.llHistoryContainer.removeAllViews()

        for (session in viewModel.sessions) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_session, binding.llHistoryContainer, false)
            view.findViewById<TextView>(R.id.tvSessionTask).text = session.taskTitle
            view.findViewById<TextView>(R.id.tvSessionTime).text = session.time
            binding.llHistoryContainer.addView(view)
        }

        updateEmptyHistory()
    }

    private fun updateEmptyHistory() {
        binding.tvEmptyHistory.visibility = if (viewModel.sessions.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateSummary() {
        binding.tvSummary.text = "Tareas pendientes: ${viewModel.pendingTasks()}\nSesiones completadas: ${viewModel.sessions.size}"
    }

    private fun updateEmptyState() {
        binding.tvEmptyTasks.visibility = if (viewModel.tasks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateButtonStates(state: PomodoroTimer.TimerState) {
        when (state) {
            PomodoroTimer.TimerState.IDLE, PomodoroTimer.TimerState.FINISHED -> {
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

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
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
