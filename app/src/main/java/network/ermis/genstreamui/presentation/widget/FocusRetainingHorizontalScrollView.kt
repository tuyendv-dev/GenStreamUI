package network.ermis.genstreamui.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import java.util.ArrayList

class FocusRetainingHorizontalScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    var selectedChildView: View? = null

    override fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: android.graphics.Rect?): Boolean {
        if (selectedChildView != null && selectedChildView!!.requestFocus(direction, previouslyFocusedRect)) {
            return true
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect)
    }

    override fun addFocusables(views: ArrayList<View>?, direction: Int, focusableMode: Int) {
        if (hasFocus()) {
            super.addFocusables(views, direction, focusableMode)
        } else {
            if (selectedChildView != null && selectedChildView!!.isFocusable && selectedChildView!!.visibility == View.VISIBLE) {
                views?.add(selectedChildView!!)
            } else {
                super.addFocusables(views, direction, focusableMode)
            }
        }
    }
}
