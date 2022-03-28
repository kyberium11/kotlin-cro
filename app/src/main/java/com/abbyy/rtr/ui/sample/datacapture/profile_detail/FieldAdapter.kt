// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture.profile_detail

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.abbyy.rtr.ui.sample.datacapture.R
import com.abbyy.rtr.ui.sample.datacapture.data.ProfileField
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Item(
    val field: ProfileField,
    val isSingleLine: Boolean,
    @StringRes val titleStringRes: Int
)

class FieldAdapter(
    private val items: List<Item>,
    private val listener: Listener
) : RecyclerView.Adapter<FieldViewHolder>() {
    interface Listener {
        fun onFieldChanged(position: Int, field: ProfileField)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_field, parent, false)
        return FieldViewHolder(view, listener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

class FieldViewHolder(
    view: View,
    private val listener: FieldAdapter.Listener
) : RecyclerView.ViewHolder(view) {
    private val textLayout: TextInputLayout = view.findViewById(R.id.fieldTextLayout)
    private val editText: TextInputEditText = view.findViewById(R.id.fieldEditText)
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onFieldChanged(position, ProfileField(fieldId, editText.text.toString()))
            }
        }
    }

    init {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editText.setSelection(editText.text?.length ?: 0)
            }
        }
    }

    private lateinit var fieldId: String

    fun bind(item: Item) {
        fieldId = item.field.id
        textLayout.hint = textLayout.resources.getString(item.titleStringRes)
        editText.removeTextChangedListener(textWatcher)
        editText.setText(item.field.value)
        if (item.isSingleLine) {
            editText.maxLines = 1
            editText.isSingleLine = true
            editText.imeOptions = EditorInfo.IME_ACTION_NEXT
        } else {
            editText.isSingleLine = false
            editText.maxLines = Integer.MAX_VALUE
            editText.imeOptions = EditorInfo.IME_NULL
        }
        editText.addTextChangedListener(textWatcher)
    }
}
