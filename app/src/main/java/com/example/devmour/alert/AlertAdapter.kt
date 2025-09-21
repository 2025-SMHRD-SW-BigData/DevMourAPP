package com.example.devmour.alert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.devmour.R
import com.example.devmour.data.db.repository.RoadControlEntity
import java.text.SimpleDateFormat
import java.util.*

class AlertAdapter(
    private var items: List<RoadControlEntity> = emptyList(),
    private val onDeleteClick: (RoadControlEntity) -> Unit = {}
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addrText: TextView = itemView.findViewById(R.id.tvAddr)
        val descText: TextView = itemView.findViewById(R.id.tvDesc)
        val timeText: TextView = itemView.findViewById(R.id.tvTime)
        val icon: ImageView = itemView.findViewById(R.id.ivIcon)
        val deleteButton: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val item = items[position]
        holder.addrText.text = item.control_addr
        holder.descText.text = item.control_desc
        holder.timeText.text = formatTimestamp(item.control_st_tm)

        val iconRes = when {
            item.control_desc.contains("공사") -> R.drawable.construction
            item.control_desc.contains("통제") -> R.drawable.barrier
            item.control_desc.contains("침수") -> R.drawable.flood
            else -> R.drawable.ic_notification
        }
        holder.icon.setImageResource(iconRes)
        
        // 삭제 버튼 클릭 리스너
        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<RoadControlEntity>) {
        android.util.Log.d("AlertAdapter", "updateData 호출됨. 데이터 개수: ${newItems.size}")
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatTimestamp(ts: Long): String {
        val millis = if (ts < 1_000_000_000_000L) ts * 1000L else ts
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
        return try {
            sdf.format(Date(millis))
        } catch (e: Exception) {
            millis.toString()
        }
    }
}
