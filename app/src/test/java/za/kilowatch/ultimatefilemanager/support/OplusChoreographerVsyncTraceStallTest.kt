package za.kilowatch.ultimatefilemanager.support

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OplusChoreographerVsyncTraceStallTest {

    @Test
    fun identifiesOnePlusAc2001OplusChoreographerVsyncTraceStall() {
        val topFrame = StackTraceElement("java.lang.reflect.Method", "invoke", null, -2)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.oplus.reflect.RefStaticMethod", "call", "RefStaticMethod.java", 123),
            StackTraceElement("android.os.Trace", "traceBegin", "Trace.java", 223),
            StackTraceElement("android.view.Choreographer", "scheduleVsyncLocked", "Choreographer.java", 1066),
            StackTraceElement("android.view.Choreographer", "doScheduleVsync", "Choreographer.java", 1047),
            StackTraceElement("android.view.Choreographer\$FrameHandler", "handleMessage", "Choreographer.java", 1142),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 106),
            StackTraceElement("android.os.Looper", "loopOnce", "Looper.java", 233),
            StackTraceElement("android.os.Looper", "loop", "Looper.java", 344),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 8212),
            StackTraceElement("java.lang.reflect.Method", "invoke", null, -2),
            StackTraceElement("com.android.internal.os.RuntimeInit\$MethodAndArgsCaller", "run", "RuntimeInit.java", 584),
            StackTraceElement("com.android.internal.os.ZygoteInit", "main", "ZygoteInit.java", 1034)
        )

        assertTrue(CrashReportManager.isOplusChoreographerVsyncTraceStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesTopFrameInRefStaticMethod() {
        val topFrame = StackTraceElement("com.oplus.reflect.RefStaticMethod", "call", "RefStaticMethod.java", 123)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("android.os.Trace", "traceBegin", "Trace.java", 223),
            StackTraceElement("android.view.Choreographer", "scheduleVsyncLocked", "Choreographer.java", 1066),
            StackTraceElement("android.view.Choreographer", "doScheduleVsync", "Choreographer.java", 1047),
            StackTraceElement("android.view.Choreographer\$FrameHandler", "handleMessage", "Choreographer.java", 1142)
        )

        assertTrue(CrashReportManager.isOplusChoreographerVsyncTraceStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesTopFrameInTraceBegin() {
        val topFrame = StackTraceElement("android.os.Trace", "traceBegin", "Trace.java", 223)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.oplus.reflect.RefStaticMethod", "call", "RefStaticMethod.java", 123),
            StackTraceElement("android.view.Choreographer", "scheduleVsyncLocked", "Choreographer.java", 1066),
            StackTraceElement("android.view.Choreographer", "doScheduleVsync", "Choreographer.java", 1047)
        )

        assertTrue(CrashReportManager.isOplusChoreographerVsyncTraceStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesTopFrameInChoreographerScheduleVsync() {
        val topFrame = StackTraceElement("android.view.Choreographer", "scheduleVsyncLocked", "Choreographer.java", 1066)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.oplus.reflect.RefStaticMethod", "call", "RefStaticMethod.java", 123),
            StackTraceElement("android.view.Choreographer", "doScheduleVsync", "Choreographer.java", 1047),
            StackTraceElement("android.view.Choreographer\$FrameHandler", "handleMessage", "Choreographer.java", 1142)
        )

        assertTrue(CrashReportManager.isOplusChoreographerVsyncTraceStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsAppBusinessLogicExecution() {
        val topFrame = StackTraceElement("java.lang.reflect.Method", "invoke", null, -2)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.oplus.reflect.RefStaticMethod", "call", "RefStaticMethod.java", 123),
            StackTraceElement("android.view.Choreographer", "scheduleVsyncLocked", "Choreographer.java", 1066),
            StackTraceElement("za.kilowatch.ultimatefilemanager.storage.StorageAdapter", "bindViewHolder", "StorageAdapter.kt", 200)
        )

        assertFalse(CrashReportManager.isOplusChoreographerVsyncTraceStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsWhenBlockingPrimitiveHeld() {
        val topFrame = StackTraceElement("java.lang.reflect.Method", "invoke", null, -2)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.oplus.reflect.RefStaticMethod", "call", "RefStaticMethod.java", 123),
            StackTraceElement("android.view.Choreographer", "scheduleVsyncLocked", "Choreographer.java", 1066),
            StackTraceElement("java.lang.Object", "wait", "Object.java", 442)
        )

        assertFalse(CrashReportManager.isOplusChoreographerVsyncTraceStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsUnrelatedChoreographerCallbackWithoutOplus() {
        val topFrame = StackTraceElement("android.view.Choreographer", "scheduleVsyncLocked", "Choreographer.java", 1066)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("android.view.Choreographer", "doScheduleVsync", "Choreographer.java", 1047),
            StackTraceElement("android.view.Choreographer\$FrameHandler", "handleMessage", "Choreographer.java", 1142)
        )

        assertFalse(CrashReportManager.isOplusChoreographerVsyncTraceStall(topFrame, stackTrace))
    }
}
