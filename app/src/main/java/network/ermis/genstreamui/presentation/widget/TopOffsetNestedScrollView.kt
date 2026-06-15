package network.ermis.genstreamui.presentation.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import kotlin.math.max
import kotlin.math.min

class TopOffsetNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    var topOffset: Int = 0

    override fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
        if (childCount == 0) return 0

        val height = height
        var screenTop = scrollY
        var screenBottom = screenTop + height

        val fadingEdge = verticalFadingEdgeLength

        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0) {
            screenTop += fadingEdge
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.bottom < getChildAt(0).height) {
            screenBottom -= fadingEdge
        }

        var scrollYDelta = 0
        val adjustedScreenTop = screenTop + topOffset
        val effectiveHeight = height - topOffset

        if (rect.bottom > screenBottom && rect.top > adjustedScreenTop) {
            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).
            if (rect.height() > effectiveHeight) {
                // just enough to get screen size chunk on
                scrollYDelta += (rect.top - adjustedScreenTop)
            } else {
                // get entire rect at bottom of screen
                scrollYDelta += (rect.bottom - screenBottom)
            }

            // make sure we aren't scrolling beyond the end of our content
            val bottom = getChildAt(0).bottom
            val distanceToBottom = bottom - screenBottom
            scrollYDelta = min(scrollYDelta, distanceToBottom)

        } else if (rect.top < adjustedScreenTop && rect.bottom < screenBottom) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).
            if (rect.height() > effectiveHeight) {
                // screen size chunk
                scrollYDelta -= (screenBottom - rect.bottom)
            } else {
                // entire rect at top
                scrollYDelta -= (adjustedScreenTop - rect.top)
            }

            // make sure we aren't scrolling any further than the top our content
            scrollYDelta = max(scrollYDelta, -scrollY)
        }
        return scrollYDelta
    }
}
