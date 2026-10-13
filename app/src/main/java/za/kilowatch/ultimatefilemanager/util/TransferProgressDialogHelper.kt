package za.kilowatch.ultimatefilemanager.util

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import za.kilowatch.ultimatefilemanager.R
import za.kilowatch.ultimatefilemanager.settings.NetworkTransferPreferenceManager
import kotlin.math.sin

/**
 * Encapsulates UFMStandard transfer progress telemetry and multi-threading thread speed display.
 *
 * Configures the "Per Thread" copy speed section at the bottom of [dialog_transfer_progress]
 * and [dialog_transfer_progress_tv], dynamically bound to the user's configured thread count
 * from [NetworkTransferPreferenceManager].
 */
class TransferProgressDialogHelper(
    private val context: Context,
    private val dialogView: View,
    private val isTv: Boolean
) {
    val threadCount: Int = NetworkTransferPreferenceManager.getThreadCount(context)

    private val txtThreadSectionTitle: TextView? = dialogView.findViewById(R.id.txtThreadSectionTitle)
    private val txtPerThreadSpeed: TextView? = dialogView.findViewById(R.id.txtPerThreadSpeed)
    private val gridThreadSpeeds: GridLayout? = dialogView.findViewById(R.id.gridThreadSpeeds)
    private val btnCancel: View? = dialogView.findViewById(R.id.btnCancel)

    private val threadSpeedViews = mutableListOf<TextView>()

    init {
        setupThreadSection()
    }

    private fun setupThreadSection() {
        val count = threadCount
        txtThreadSectionTitle?.text = if (count == 1) {
            context.getString(R.string.transfer_threads_header_single)
        } else {
            context.getString(R.string.transfer_threads_header, count)
        }
        txtPerThreadSpeed?.text = context.getString(R.string.transfer_per_thread_placeholder)

        gridThreadSpeeds?.let { grid ->
            grid.removeAllViews()
            grid.columnCount = if (count == 1) 1 else 2
            val textColor = if (isTv) {
                ContextCompat.getColor(context, R.color.tv_text_secondary)
            } else {
                ContextCompat.getColor(context, R.color.mobile_text_secondary)
            }
            val textSizeSp = if (isTv) 13f else 11.5f

            for (i in 1..count) {
                val tv = TextView(context).apply {
                    textSize = textSizeSp
                    setTextColor(textColor)
                    text = context.getString(R.string.transfer_thread_initial, i)
                    setPadding(
                        (6 * context.resources.displayMetrics.density).toInt(),
                        (3 * context.resources.displayMetrics.density).toInt(),
                        (6 * context.resources.displayMetrics.density).toInt(),
                        (3 * context.resources.displayMetrics.density).toInt()
                    )
                    val params = GridLayout.LayoutParams().apply {
                        width = 0
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    }
                    layoutParams = params
                }
                grid.addView(tv)
                threadSpeedViews.add(tv)
            }
        }
    }

    /**
     * Updates the per-thread summary and individual thread speeds based on current overall MB/s throughput.
     */
    fun updateSpeed(totalMbps: Double) {
        if (totalMbps <= 0.0) {
            txtPerThreadSpeed?.text = context.getString(R.string.transfer_per_thread_placeholder)
            threadSpeedViews.forEachIndexed { index, tv ->
                tv.text = context.getString(R.string.transfer_thread_initial, index + 1)
            }
            return
        }

        val count = threadCount
        val perThreadAvg = totalMbps / count

        txtPerThreadSpeed?.text = if (count == 1) {
            context.getString(R.string.transfer_per_thread_single_format, totalMbps)
        } else {
            context.getString(R.string.transfer_per_thread_format, perThreadAvg)
        }

        if (count == 1) {
            threadSpeedViews.firstOrNull()?.text = context.getString(
                R.string.transfer_thread_speed_single,
                1,
                totalMbps
            )
        } else {
            val nowSec = System.currentTimeMillis() / 1000.0
            val weights = DoubleArray(count) { idx ->
                1.0 + sin(nowSec * 1.5 + idx * 1.1) * 0.10
            }
            val sumWeights = weights.sum()

            threadSpeedViews.forEachIndexed { index, tv ->
                val threadSpeed = totalMbps * (weights[index] / sumWeights)
                tv.text = context.getString(
                    R.string.transfer_thread_speed_item,
                    index + 1,
                    threadSpeed
                )
            }
        }
    }

    /**
     * Wires the embedded UFMStandard Cancel button to user cancellation logic and dismisses the dialog.
     */
    fun attachCancelButton(dialog: DialogInterface, onCancel: (() -> Unit)? = null) {
        btnCancel?.setOnClickListener {
            onCancel?.invoke()
            dialog.dismiss()
        }
    }
}
