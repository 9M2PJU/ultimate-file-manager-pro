package za.kilowatch.ultimatefilemanager.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import za.kilowatch.ultimatefilemanager.util.GoRoLog

/**
 * Defensive ImageView that guards against drawing recycled bitmaps on canvas.
 *
 * Catches and suppresses "java.lang.RuntimeException: Canvas: trying to use a recycled bitmap"
 * crashes during rapid RecyclerView scrolling, view recycling, or asynchronous thumbnail
 * cache updates, especially on memory-constrained Android TV STBs and low-end devices.
 */
class SafeImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        if (bm != null && bm.isRecycled) {
            super.setImageDrawable(null)
            return
        }
        super.setImageBitmap(bm)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (isRecycledDrawable(drawable)) {
            super.setImageDrawable(null)
            return
        }
        super.setImageDrawable(drawable)
    }

    override fun onDraw(canvas: Canvas) {
        if (isRecycledDrawable(drawable)) {
            setImageDrawable(null)
            return
        }
        try {
            super.onDraw(canvas)
        } catch (e: RuntimeException) {
            val msg = e.message
            if (msg != null && msg.contains("recycled bitmap", ignoreCase = true)) {
                GoRoLog.w("SafeImageView", "Suppressed recycled bitmap draw: $msg")
                setImageDrawable(null)
            } else {
                throw e
            }
        }
    }

    private fun isRecycledDrawable(d: Drawable?): Boolean {
        if (d == null) return false
        if (d is BitmapDrawable) {
            val bmp = d.bitmap
            return bmp == null || bmp.isRecycled
        }
        return false
    }
}
