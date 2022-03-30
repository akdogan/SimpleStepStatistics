package com.akdogan.simplestepstatistics.ui.main.settings.selectionlist

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.akdogan.simplestepstatistics.R
import com.akdogan.simplestepstatistics.databinding.SettingsDaySelectionItemBinding
import com.akdogan.simplestepstatistics.ui.main.settings.DayOfWeek

class DayOfWeekSelectionListAdapter(
    val clickListener: (DayOfWeek) -> Unit
) : RecyclerView.Adapter<DayOfWeekSelectionListAdapter.DayOfWeekSelectionListViewHolder>() {

    private var dataSet: List<DayOfWeekSelectionItem> = listOf()

    fun submitList(list: List<DayOfWeekSelectionItem>) {
        dataSet = list
        notifyDataSetChanged()
    }

    class DayOfWeekSelectionListViewHolder(val binding: SettingsDaySelectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
            val viewItem = binding.settingsDaySelectionItemLabel
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DayOfWeekSelectionListViewHolder = DayOfWeekSelectionListViewHolder(
        SettingsDaySelectionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: DayOfWeekSelectionListViewHolder, position: Int) {
        val dataItem = dataSet[position]

        val label = holder.itemView.context.getString(dataItem.day.label)
        holder.viewItem.text = label
        holder.viewItem.setTextColor(holder.getThemeColor(R.attr.colorOnSurface, Color.LTGRAY))

        if (dataItem.selected){
            val backgroundColor = holder.itemView.context.getColor(R.color.light_blue_200)
            holder.viewItem.setBackgroundColor(backgroundColor)
        } else {
            holder.viewItem.setBackgroundColor(holder.getThemeColor(R.attr.colorSurface, Color.WHITE))
        }

        holder.viewItem.setOnClickListener {
            clickListener(dataItem.day)
        }
    }

    override fun getItemCount(): Int = dataSet.size

    @ColorInt
    private fun DayOfWeekSelectionListViewHolder.getThemeColor(
        @AttrRes attr: Int,
        @ColorInt default: Int
    ): Int {
        val typedVal = TypedValue()
        return if (this.itemView.context.theme.resolveAttribute(attr, typedVal, true)) {
            typedVal.data
        } else {
            default
        }
    }
}