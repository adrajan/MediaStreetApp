package ads.mediastreet.ai.adapter

import ads.mediastreet.ai.model.RetailerMonthlyStats
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ads.mediastreet.ai.R

class MonthlyStatsAdapter : RecyclerView.Adapter<MonthlyStatsAdapter.ViewHolder>() {
    private var stats: List<RetailerMonthlyStats> = emptyList()

    fun updateStats(newStats: List<RetailerMonthlyStats>) {
        stats = newStats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_stats, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = stats[position]
        holder.bind(stat)
    }

    override fun getItemCount(): Int = stats.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMonth: TextView = view.findViewById(R.id.tvMonth)
        private val tvStats: TextView = view.findViewById(R.id.tvStats)

        fun bind(stat: RetailerMonthlyStats) {
            tvMonth.text = stat.month
            tvStats.text = "Ads: ${stat.ads} | Orders: ${stat.orders} | Paid: $${stat.paid}"
        }
    }
}
