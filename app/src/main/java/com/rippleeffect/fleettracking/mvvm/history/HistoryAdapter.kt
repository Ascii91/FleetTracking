package com.rippleeffect.fleettracking.mvvm.history

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import com.rippleeffect.fleettracking.R
import com.rippleeffect.fleettracking.databinding.RowHistoryItemBinding
import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.model.RecordOrigin
import org.joda.time.DateTime


class HistoryAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), RecyclerViewFastScroller.OnPopupTextUpdate {


    private val locationItems: ArrayList<LocationRecord> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding =
            RowHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return HistoryListViewHolder(itemBinding)

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HistoryListViewHolder) {
            holder.bind(locationItems[position])
        }
    }

    override fun getItemCount(): Int = locationItems.size

    fun setItems(items: List<LocationRecord>) {
        this.locationItems.clear()
        this.locationItems.addAll(items)
        notifyDataSetChanged()
    }


    inner class HistoryListViewHolder(private val itemBinding: RowHistoryItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {


        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(locationRecord: LocationRecord) {
            itemBinding.tvId.text = locationRecord.id.toString()
            itemBinding.tvOrigin.text = locationRecord.getOriginText()

            when (locationRecord.origin) {
                RecordOrigin.TIMED.ordinal -> {
                    itemBinding.tvOrigin.setBackgroundColor(Color.GREEN)
                }
                RecordOrigin.ALARM.ordinal -> itemBinding.tvOrigin.setBackgroundColor(Color.CYAN)
                else -> {

                    itemBinding.tvOrigin.background = ContextCompat.getDrawable(
                        itemBinding.root.context,
                        R.drawable.border_background_row
                    )
                }
            }



            itemBinding.tvProvider.text = locationRecord.provider
            itemBinding.tvTime.text =
                DateTime(locationRecord.timeInMillis).toString("HH:mm:ss - dd.M")

            val position = bindingAdapterPosition
            if (position < 0 || position >= locationItems.size - 1) return

            val distanceTime =
                (locationRecord.timeInMillis - locationItems[position + 1].timeInMillis) / 1000
            itemBinding.tvTimeDistance.text =
                distanceTime.toString()


            if (distanceTime > 300) {
                itemBinding.tvTimeDistance.setTextColor(Color.RED)
            } else {
                itemBinding.tvTimeDistance.setTextColor(Color.GREEN)
            }

            val adapterPosition = bindingAdapterPosition
            if (adapterPosition > 1) {
                val previousLocation = locationItems[adapterPosition - 1]
                if (locationRecord.latitude == previousLocation.latitude &&
                    locationRecord.longitude == previousLocation.longitude
                    && locationRecord.accuracy == previousLocation.accuracy
                ) {
                    itemBinding.tvId.setBackgroundColor(Color.GREEN)
                } else {
                    itemBinding.tvId.setBackgroundColor(Color.WHITE)
                }

            }

        }
    }

    override fun onChange(position: Int): CharSequence {
        return DateTime(locationItems[position].timeInMillis).toString("HH:mm:EEE - dd.M")
    }


}