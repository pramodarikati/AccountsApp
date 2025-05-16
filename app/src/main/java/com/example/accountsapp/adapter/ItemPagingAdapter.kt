package com.example.accountsapp.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.accountsapp.R
import com.example.accountsapp.data.model.ItemEntity
import org.json.JSONObject

class ItemPagingAdapter(
    private val onDeleteClick: (ItemEntity) -> Unit,
    private val onUpdateClick: (ItemEntity) -> Unit
) : PagingDataAdapter<ItemEntity, ItemPagingAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        private val layoutDynamic: LinearLayout = itemView.findViewById(R.id.layout_dynamic_details)

        fun bind(item: ItemEntity) {
            textName.text = item.name
            layoutDynamic.removeAllViews()

            val dataString = item.details
            if (!dataString.isNullOrBlank() && dataString != "null") {
                try {
                    val validJsonString = if (dataString.trim().startsWith("{") && !dataString.contains(":")) {
                        val corrected = dataString
                            .removePrefix("{").removeSuffix("}")
                            .split(",")
                            .joinToString(",", prefix = "{", postfix = "}") {
                                val parts = it.split("=")
                                val key = parts[0].trim()
                                val value = parts.getOrElse(1) { "" }.trim()
                                "\"$key\":\"$value\""
                            }
                        corrected
                    } else {
                        dataString
                            .replace("=", "\":\"")
                            .replace(",", "\",\"")
                            .replace("{", "{\"")
                            .replace("}", "\"}")
                    }

                    val jsonData = JSONObject(validJsonString)
                    val keys = jsonData.keys()

                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = jsonData.optString(key)

                        val row = LinearLayout(itemView.context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(0, 8, 0, 8)
                        }

                        val label = TextView(itemView.context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 2f)
                            text = "$key:"
                            setTypeface(null, Typeface.BOLD)
                            textSize = 14f
                        }

                        val valueView = TextView(itemView.context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 2f)
                            text = value.ifEmpty { "N/A" }
                            textSize = 14f
                        }

                        row.addView(label)
                        row.addView(valueView)
                        layoutDynamic.addView(row)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorText = TextView(itemView.context).apply {
                        text = "No details available"
                        textSize = 14f
                        setTypeface(null, Typeface.ITALIC)
                    }
                    layoutDynamic.addView(errorText)
                }
            } else {
                val emptyText = TextView(itemView.context).apply {
                    text = "No details available"
                    textSize = 14f
                    setTypeface(null, Typeface.ITALIC)
                }
                layoutDynamic.addView(emptyText)
            }

            itemView.findViewById<View>(R.id.layout_action1)?.setOnClickListener {
                onDeleteClick(item)
            }

            itemView.findViewById<View>(R.id.layout_action2)?.setOnClickListener {
                onUpdateClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemEntity>() {
        override fun areItemsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean =
            oldItem == newItem
    }
}
