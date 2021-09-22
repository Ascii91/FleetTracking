package com.rippleeffect.fleettracking.mvvm.map

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rippleeffect.fleettracking.databinding.RowDayItemBinding
import org.joda.time.DateTime


class DaysAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //In millis
    private val dateItems: ArrayList<Long> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding =
            RowDayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return DateListViewHolder(itemBinding)

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateListViewHolder) {
            holder.bind(dateItems[position])
        }
    }

    override fun getItemCount(): Int = dateItems.size

    fun setItems(items: List<Long>) {
        this.dateItems.clear()
        this.dateItems.addAll(items)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Long {
        return dateItems[position]
    }


    inner class DateListViewHolder(private val itemBinding: RowDayItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {


        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(dateInMillis: Long) {

            itemBinding.tvDate.text = DateTime(dateInMillis).toString("EEEE, dd MMM")


        }
    }


}