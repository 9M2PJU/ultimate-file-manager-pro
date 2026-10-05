package za.kilowatch.ultimatefilemanager.support

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityThreadTransactionHookStallTest {

    @Test
    fun identifiesGoogleTvStreamerObfuscatedHookStall() {
        val topFrame = StackTraceElement("ew1", "d", null, 1)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("cw1", "handleMessage", null, 34),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 102),
            StackTraceElement("android.os.Looper", "loopOnce", "Looper.java", 205),
            StackTraceElement("android.os.Looper", "loop", "Looper.java", 294),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 8177),
            StackTraceElement("java.lang.reflect.Method", "invoke", null, -2),
            StackTraceElement("com.android.internal.os.RuntimeInit\$MethodAndArgsCaller", "run", "RuntimeInit.java", 552),
            StackTraceElement("com.android.internal.os.ZygoteInit", "main", "ZygoteInit.java", 971)
        )

        assertTrue(CrashReportManager.isActivityThreadTransactionHookStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesUnobfuscatedDebugHookStall() {
        val topFrame = StackTraceElement(
            "za.kilowatch.ultimatefilemanager.support.CrashReportManager",
            "ensureLaunchingActivityRecord",
            "CrashReportManager.kt",
            838
        )
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement(
                "za.kilowatch.ultimatefilemanager.support.CrashReportManager\$installActivityThreadCrashHook\$1",
                "handleMessage",
                "CrashReportManager.kt",
                813
            ),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 102),
            StackTraceElement("android.os.Looper", "loopOnce", "Looper.java", 205),
            StackTraceElement("android.os.Looper", "loop", "Looper.java", 294),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 8177)
        )

        assertTrue(CrashReportManager.isActivityThreadTransactionHookStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesTopFrameInMethodInvoke() {
        val topFrame = StackTraceElement("java.lang.reflect.Method", "invoke", null, -2)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("ew1", "d", null, 1),
            StackTraceElement("cw1", "handleMessage", null, 34),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 102),
            StackTraceElement("android.os.Looper", "loopOnce", "Looper.java", 205),
            StackTraceElement("android.os.Looper", "loop", "Looper.java", 294),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 8177)
        )

        assertTrue(CrashReportManager.isActivityThreadTransactionHookStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesTopFrameInHandleMessage() {
        val topFrame = StackTraceElement("cw1", "handleMessage", null, 34)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 102),
            StackTraceElement("android.os.Looper", "loopOnce", "Looper.java", 205),
            StackTraceElement("android.os.Looper", "loop", "Looper.java", 294),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 8177)
        )

        assertTrue(CrashReportManager.isActivityThreadTransactionHookStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsAppBusinessLogicExecution() {
        val topFrame = StackTraceElement("ew1", "d", null, 1)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("cw1", "handleMessage", null, 34),
            StackTraceElement("za.kilowatch.ultimatefilemanager.ui.browser.FileBrowserActivity", "onCreate", "FileBrowserActivity.kt", 120),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 102),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 8177)
        )

        assertFalse(CrashReportManager.isActivityThreadTransactionHookStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsWhenBlockingPrimitiveHeld() {
        val topFrame = StackTraceElement("java.lang.Object", "wait", "Object.java", 442)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("ew1", "d", null, 1),
            StackTraceElement("cw1", "handleMessage", null, 34),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 102),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 8177)
        )

        assertFalse(CrashReportManager.isActivityThreadTransactionHookStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsWithoutActivityThreadMain() {
        val topFrame = StackTraceElement("ew1", "d", null, 1)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("cw1", "handleMessage", null, 34),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 102),
            StackTraceElement("android.os.HandlerThread", "run", "HandlerThread.java", 67)
        )

        assertFalse(CrashReportManager.isActivityThreadTransactionHookStall(topFrame, stackTrace))
    }
}
