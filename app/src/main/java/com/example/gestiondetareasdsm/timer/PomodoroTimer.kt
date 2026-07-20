package com.example.gestiondetareasdsm.timer

import android.os.CountDownTimer

class PomodoroTimer(
    private val onTick: (Long, Int) -> Unit,
    private val onFinish: () -> Unit,
    private val onStateChanged: (TimerState) -> Unit
) {

    enum class TimerState {
        IDLE, RUNNING, PAUSED, FINISHED
    }

    companion object {
        const val DEFAULT_TIME = 25 * 60 * 1000L
    }

    // Duración configurable (por defecto 25 minutos)
    var totalTime: Long = DEFAULT_TIME
        private set

    private var remainingTime = totalTime
    private var timer: CountDownTimer? = null

    var state: TimerState = TimerState.IDLE
        private set

    /**
     * Cambia la duración del temporizador (en minutos).
     * Solo permitido si el timer está en IDLE o FINISHED.
     */
    fun setDurationMinutes(minutes: Int): Boolean {

        if (state == TimerState.RUNNING || state == TimerState.PAUSED) {
            return false
        }

        totalTime = minutes * 60 * 1000L
        remainingTime = totalTime

        onTick(remainingTime, 0)

        return true
    }

    fun start() {

        if (state == TimerState.RUNNING) return

        timer?.cancel()

        timer = object : CountDownTimer(remainingTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val progress =
                    (((totalTime - remainingTime).toFloat() / totalTime) * 100).toInt()
                onTick(remainingTime, progress)
            }

            override fun onFinish() {
                remainingTime = totalTime
                state = TimerState.FINISHED
                onStateChanged(state)
                this@PomodoroTimer.onFinish()
            }
        }

        state = TimerState.RUNNING
        onStateChanged(state)

        timer?.start()
    }

    fun pause() {
        if (state != TimerState.RUNNING) return

        timer?.cancel()
        state = TimerState.PAUSED
        onStateChanged(state)
    }

    fun resume() {
        if (state != TimerState.PAUSED) return
        start()
    }

    fun reset() {
        timer?.cancel()
        remainingTime = totalTime
        state = TimerState.IDLE
        onStateChanged(state)
        onTick(totalTime, 0)
    }
}