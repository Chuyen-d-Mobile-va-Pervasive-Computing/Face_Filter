package com.example.arfacefilterdemo

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

class FilterSelectorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        orientation = HORIZONTAL
        val filters = arrayOf("none", "sunglasses", "cat_ears", "hat")
        for (filter in filters) {
            val button = TextView(context).apply {
                text = filter.replaceFirstChar { if (it.isLowerCase()) it.titlecase(context.resources.configuration.locales[0]) else it.toString() }
                setPadding(16, 16, 16, 16)
                setOnClickListener { onFilterSelected(filter) }
            }
            addView(button)
        }
    }

    private fun onFilterSelected(filter: String) {
        (context as? ARCameraActivity)?.let { activity ->
            activity.setFilter(filter) // Sử dụng phương thức công khai
        }
    }
}