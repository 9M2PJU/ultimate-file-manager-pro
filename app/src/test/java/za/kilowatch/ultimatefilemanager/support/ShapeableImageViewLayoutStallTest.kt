package za.kilowatch.ultimatefilemanager.support

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShapeableImageViewLayoutStallTest {

    @Test
    fun identifiesScbcR3ObfuscatedShapeableImageViewLayoutStall() {
        val topFrame = StackTraceElement("sc8", "t", "r8", 12)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("qu8", "a", "r8", 102),
            StackTraceElement("com.google.android.material.imageview.ShapeableImageView", "d", "r8", 40),
            StackTraceElement("com.google.android.material.imageview.ShapeableImageView", "onSizeChanged", "r8", 4),
            StackTraceElement("android.view.View", "sizeChange", "View.java", 23032),
            StackTraceElement("android.view.View", "setFrame", "View.java", 22984),
            StackTraceElement("android.widget.ImageView", "setFrame", "ImageView.java", 1265),
            StackTraceElement("android.view.View", "layout", "View.java", 22841),
            StackTraceElement("android.widget.FrameLayout", "layoutChildren", "FrameLayout.java", 332),
            StackTraceElement("android.widget.FrameLayout", "onLayout", "FrameLayout.java", 270),
            StackTraceElement("android.view.View", "layout", "View.java", 22844),
            StackTraceElement("android.view.ViewGroup", "layout", "ViewGroup.java", 6389),
            StackTraceElement("android.widget.LinearLayout", "setChildFrame", "LinearLayout.java", 1829),
            StackTraceElement("android.widget.LinearLayout", "layoutHorizontal", "LinearLayout.java", 1818),
            StackTraceElement("android.widget.LinearLayout", "onLayout", "LinearLayout.java", 1584),
            StackTraceElement("android.view.View", "layout", "View.java", 22844),
            StackTraceElement("android.view.ViewGroup", "layout", "ViewGroup.java", 6389),
            StackTraceElement("android.widget.FrameLayout", "layoutChildren", "FrameLayout.java", 332),
            StackTraceElement("android.widget.FrameLayout", "onLayout", "FrameLayout.java", 270),
            StackTraceElement("android.view.View", "layout", "View.java", 22844),
            StackTraceElement("android.view.ViewGroup", "layout", "ViewGroup.java", 6389),
            StackTraceElement("android.widget.LinearLayout", "setChildFrame", "LinearLayout.java", 1829),
            StackTraceElement("android.widget.LinearLayout", "layoutVertical", "LinearLayout.java", 1673),
            StackTraceElement("android.widget.LinearLayout", "onLayout", "LinearLayout.java", 1582),
            StackTraceElement("android.view.View", "layout", "View.java", 22844),
            StackTraceElement("android.view.ViewGroup", "layout", "ViewGroup.java", 6389),
            StackTraceElement("android.widget.FrameLayout", "layoutChildren", "FrameLayout.java", 332),
            StackTraceElement("android.widget.FrameLayout", "onLayout", "FrameLayout.java", 270),
            StackTraceElement("androidx.core.widget.NestedScrollView", "onLayout", "r8", 1),
            StackTraceElement("android.view.View", "layout", "View.java", 22844),
            StackTraceElement("android.view.ViewGroup", "layout", "ViewGroup.java", 6389),
            StackTraceElement("androidx.constraintlayout.widget.ConstraintLayout", "onLayout", "r8", 62),
            StackTraceElement("android.view.View", "layout", "View.java", 22844),
            StackTraceElement("android.view.ViewGroup", "layout", "ViewGroup.java", 6389),
            StackTraceElement("android.widget.FrameLayout", "layoutChildren", "FrameLayout.java", 332),
            StackTraceElement("android.widget.FrameLayout", "onLayout", "FrameLayout.java", 270),
            StackTraceElement("com.android.internal.policy.DecorView", "onLayout", "DecorView.java", 784),
            StackTraceElement("android.view.View", "layout", "View.java", 22844),
            StackTraceElement("android.view.ViewGroup", "layout", "ViewGroup.java", 6389),
            StackTraceElement("android.view.ViewRootImpl", "performLayout", "ViewRootImpl.java", 3470),
            StackTraceElement("android.view.ViewRootImpl", "performTraversals", "ViewRootImpl.java", 2938),
            StackTraceElement("android.view.ViewRootImpl", "doTraversal", "ViewRootImpl.java", 1952),
            StackTraceElement("android.view.ViewRootImpl\$TraversalRunnable", "run", "ViewRootImpl.java", 8184),
            StackTraceElement("android.view.Choreographer\$CallbackRecord", "run", "Choreographer.java", 972),
            StackTraceElement("android.view.Choreographer", "doCallbacks", "Choreographer.java", 796),
            StackTraceElement("android.view.Choreographer", "doFrame", "Choreographer.java", 731),
            StackTraceElement("android.view.Choreographer\$FrameDisplayEventReceiver", "run", "Choreographer.java", 957),
            StackTraceElement("android.os.Handler", "handleCallback", "Handler.java", 938),
            StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 99),
            StackTraceElement("android.os.Looper", "loop", "Looper.java", 223),
            StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 7656)
        )

        assertTrue(CrashReportManager.isShapeableImageViewLayoutStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesUnobfuscatedShapeableImageViewLayoutStall() {
        val topFrame = StackTraceElement(
            "com.google.android.material.shape.ShapePath",
            "addArc",
            "ShapePath.java",
            249
        )
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.google.android.material.shape.RoundedCornerTreatment", "getCornerPath", "RoundedCornerTreatment.java", 46),
            StackTraceElement("com.google.android.material.shape.ShapeAppearancePathProvider", "calculatePath", "ShapeAppearancePathProvider.java", 180),
            StackTraceElement("com.google.android.material.imageview.ShapeableImageView", "updateShapeMask", "ShapeableImageView.java", 300),
            StackTraceElement("com.google.android.material.imageview.ShapeableImageView", "onSizeChanged", "ShapeableImageView.java", 250),
            StackTraceElement("android.view.View", "sizeChange", "View.java", 23032),
            StackTraceElement("android.view.View", "layout", "View.java", 22841),
            StackTraceElement("android.widget.FrameLayout", "onLayout", "FrameLayout.java", 270),
            StackTraceElement("android.view.ViewRootImpl", "performLayout", "ViewRootImpl.java", 3470),
            StackTraceElement("android.view.Choreographer", "doFrame", "Choreographer.java", 731)
        )

        assertTrue(CrashReportManager.isShapeableImageViewLayoutStall(topFrame, stackTrace))
    }

    @Test
    fun identifiesShapeableImageViewOnSizeChangedTopFrame() {
        val topFrame = StackTraceElement(
            "com.google.android.material.imageview.ShapeableImageView",
            "onSizeChanged",
            "ShapeableImageView.java",
            250
        )
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("android.view.View", "sizeChange", "View.java", 23032),
            StackTraceElement("android.view.View", "layout", "View.java", 22841),
            StackTraceElement("android.widget.FrameLayout", "onLayout", "FrameLayout.java", 270),
            StackTraceElement("android.view.ViewRootImpl", "performLayout", "ViewRootImpl.java", 3470),
            StackTraceElement("android.view.Choreographer", "doFrame", "Choreographer.java", 731)
        )

        assertTrue(CrashReportManager.isShapeableImageViewLayoutStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsAppBusinessLogicExecution() {
        val topFrame = StackTraceElement(
            "za.kilowatch.ultimatefilemanager.storage.StorageAdapter",
            "calculateStorageSizes",
            "StorageAdapter.kt",
            500
        )
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.google.android.material.imageview.ShapeableImageView", "onSizeChanged", "ShapeableImageView.java", 250),
            StackTraceElement("android.view.View", "layout", "View.java", 22841),
            StackTraceElement("android.view.ViewRootImpl", "performLayout", "ViewRootImpl.java", 3470)
        )

        assertFalse(CrashReportManager.isShapeableImageViewLayoutStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsWhenBlockingPrimitiveHeld() {
        val topFrame = StackTraceElement("sc8", "t", "r8", 12)
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("com.google.android.material.imageview.ShapeableImageView", "onSizeChanged", "ShapeableImageView.java", 250),
            StackTraceElement("android.view.View", "layout", "View.java", 22841),
            StackTraceElement("android.view.ViewRootImpl", "performLayout", "ViewRootImpl.java", 3470),
            StackTraceElement("java.lang.Object", "wait", "Object.java", 442)
        )

        assertFalse(CrashReportManager.isShapeableImageViewLayoutStall(topFrame, stackTrace))
    }

    @Test
    fun rejectsUnrelatedViewLayoutWithoutShapeableImageView() {
        val topFrame = StackTraceElement(
            "android.widget.TextView",
            "onLayout",
            "TextView.java",
            100
        )
        val stackTrace = arrayOf(
            topFrame,
            StackTraceElement("android.view.View", "layout", "View.java", 22841),
            StackTraceElement("android.widget.FrameLayout", "onLayout", "FrameLayout.java", 270),
            StackTraceElement("android.view.ViewRootImpl", "performLayout", "ViewRootImpl.java", 3470)
        )

        assertFalse(CrashReportManager.isShapeableImageViewLayoutStall(topFrame, stackTrace))
    }
}
