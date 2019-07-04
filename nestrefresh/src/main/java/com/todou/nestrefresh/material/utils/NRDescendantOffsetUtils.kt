package com.todou.nestrefresh.material.utils

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

class NRDescendantOffsetUtils {
    companion object {
        private val matrixM = ThreadLocal()
        private val rectFM = ThreadLocal()

        fun offsetDescendantRect(parent: ViewGroup, descendant: View, rect: Rect) {
            var m = matrixM.get()
            if (m == null) {
                m = Matrix()
                matrixM.set(m)
            } else {
                m!!.reset()
            }

            offsetDescendantMatrix(parent, descendant, m)
            var rectF = rectFM.get()
            if (rectF == null) {
                rectF = RectF()
                rectF!!.set(rectF)
            }

            rectF!!.set(rect)
            m!!.mapRect(rectF)
            rect.set(
                (rectF!!.left + 0.5f).toInt(),
                (rectF!!.top + 0.5f).toInt(),
                (rectF!!.right + 0.5f).toInt(),
                (rectF!!.bottom + 0.5f).toInt()
            )
        }

        fun getDescendantRect(parent: ViewGroup, descendant: View, out: Rect) {
            out.set(0, 0, descendant.width, descendant.height)
            offsetDescendantRect(parent, descendant, out)
        }

        private fun offsetDescendantMatrix(target: ViewParent, view: View, m: Matrix) {
            val parent = view.parent
            if (parent is View && parent !== target) {
                val vp = parent as View
                offsetDescendantMatrix(target, vp, m)
                m.preTranslate((-vp.scrollX).toFloat(), (-vp.scrollY).toFloat())
            }

            m.preTranslate(view.left.toFloat(), view.top.toFloat())
            if (!view.matrix.isIdentity) {
                m.preConcat(view.matrix)
            }

        }
    }
}