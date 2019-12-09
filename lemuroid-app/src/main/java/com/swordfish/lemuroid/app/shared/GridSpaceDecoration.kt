package com.swordfish.lemuroid.app.shared

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpaceDecoration private constructor(private val halfSpace: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.top = halfSpace
        outRect.bottom = halfSpace
        outRect.left = halfSpace
        outRect.right = halfSpace
    }

    companion object {
        fun setSingleGridSpaceDecoration(recyclerView: RecyclerView, pixelSpacing: Int) {
            if (recyclerView.itemDecorationCount == 0) {
                val halfSpacing = pixelSpacing / 2
                recyclerView.addItemDecoration(GridSpaceDecoration(halfSpacing))
                recyclerView.setPadding(halfSpacing, halfSpacing, halfSpacing, halfSpacing)
                recyclerView.clipToPadding = false
            }
        }
    }
}
