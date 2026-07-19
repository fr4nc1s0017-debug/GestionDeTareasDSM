package com.example.gestiondetareasdsm.timer

import android.os.CountDownTimer

class PomodoroTimer(

    private val onTick: (Long, Int) -> Unit,
    private val onFinish: () -> Unit

) {

    companion object {

        const val TOTAL_TIME = 25 * 60 * 1000L

    }

    private var remainingTime = TOTAL_TIME

    private var timer: CountDownTimer? = null

    var isRunning = false
        private set

    fun start() {

        timer?.cancel()

        timer = object : CountDownTimer(remainingTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                remainingTime = millisUntilFinished

                val progress =

                    (((TOTAL_TIME - remainingTime).toFloat() / TOTAL_TIME) * 100).toInt()

                onTick(remainingTime, progress)

            }

            override fun onFinish() {

                remainingTime = TOTAL_TIME

                isRunning = false

                onFinish()

            }

        }

        isRunning = true

        timer?.start()

    }

    fun pause() {

        timer?.cancel()

        isRunning = false

    }

    fun resume() {

        start()

    }

    fun reset() {

        timer?.cancel()

        remainingTime = TOTAL_TIME

        isRunning = false

        onTick(TOTAL_TIME, 0)

    }

}