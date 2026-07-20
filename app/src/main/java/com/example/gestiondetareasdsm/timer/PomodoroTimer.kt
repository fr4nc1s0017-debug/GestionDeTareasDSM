package com.example.gestiondetareasdsm.timer

import android.os.CountDownTimer

class PomodoroTimer(
    private val onTick: (Long, Int) -> Unit,
    private val onFinish: () -> Unit,
    private val onStateChanged: (TimerState) -> Unit
) {

    enum class TimerState {
        IDLE,
        RUNNING,
        PAUSED,
        FINISHED
    }

    companion object {
        const val DEFAULT_TIME = 25 * 60 * 1000L
    }

    var totalTime: Long = DEFAULT_TIME
        private set

    var remainingTime: Long = totalTime
        private set

    var finishTimeMillis: Long = 0L
        private set

    private var timer: CountDownTimer? = null

    var state = TimerState.IDLE
        private set

    fun setDurationMinutes(minutes: Int): Boolean {

        if (state == TimerState.RUNNING || state == TimerState.PAUSED)
            return false

        totalTime = minutes * 60 * 1000L
        remainingTime = totalTime

        onTick(remainingTime, 0)

        return true
    }

    fun start() {

        if (state == TimerState.RUNNING)
            return

        finishTimeMillis = System.currentTimeMillis() + remainingTime

        timer?.cancel()

        timer = createTimer()

        state = TimerState.RUNNING

        onStateChanged(state)

        timer?.start()
    }

    private fun createTimer(): CountDownTimer {

        return object : CountDownTimer(remainingTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                remainingTime = millisUntilFinished

                val progress =
                    (((totalTime - remainingTime).toFloat() / totalTime) * 100).toInt()

                onTick(remainingTime, progress)

            }

            override fun onFinish() {

                remainingTime = totalTime

                finishTimeMillis = 0L

                state = TimerState.FINISHED

                onStateChanged(state)

                this@PomodoroTimer.onFinish()

            }

        }

    }

    fun pause() {

        if (state != TimerState.RUNNING)
            return

        timer?.cancel()

        state = TimerState.PAUSED

        onStateChanged(state)

    }

    fun resume() {

        if (state != TimerState.PAUSED)
            return

        finishTimeMillis = System.currentTimeMillis() + remainingTime

        timer = createTimer()

        state = TimerState.RUNNING

        onStateChanged(state)

        timer?.start()

    }

    fun reset() {

        timer?.cancel()

        remainingTime = totalTime

        finishTimeMillis = 0L

        state = TimerState.IDLE

        onStateChanged(state)

        onTick(totalTime, 0)

    }

    /**
     * Se usa cuando el usuario vuelve a abrir la app.
     */
    fun restoreRemainingTime() {

        if (finishTimeMillis == 0L)
            return

        val current = System.currentTimeMillis()

        val diff = finishTimeMillis - current

        if (diff <= 0) {

            remainingTime = totalTime

            state = TimerState.FINISHED

            onFinish()

            return

        }

        remainingTime = diff

        if (state == TimerState.RUNNING) {

            timer?.cancel()

            timer = createTimer()

            timer?.start()

        }

    }

}