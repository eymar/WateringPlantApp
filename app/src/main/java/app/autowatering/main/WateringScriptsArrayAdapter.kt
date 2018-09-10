package app.autowatering.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import app.autowatering.databinding.WateringScriptsListItemBinding
import kotlinx.android.synthetic.main.watering_scripts_list_item.view.*

class WateringScriptsArrayAdapter : RecyclerView.Adapter<WateringScriptsArrayAdapter.ViewHolder>() {

    private val items = ArrayList<WateringScriptViewPresentation>()

    var listener: OnWateringScriptActionsListener? = null

    fun update(newItems: ArrayList<WateringScriptViewPresentation>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = items[oldItemPosition]
                val new = newItems[newItemPosition]
                return old.scriptIdText == new.scriptIdText
            }

            override fun getOldListSize() = items.size

            override fun getNewListSize() = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = items[oldItemPosition]
                val new = newItems[newItemPosition]
                return old.startInText == new.startInText
                        && old.intervalText == new.intervalText
                        && old.durationText == new.durationText
            }
        })

        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WateringScriptsListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val binding: WateringScriptsListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.actionButton.setOnClickListener({
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                listener?.onScriptActionClick(items[adapterPosition])
            })
        }

        fun bind(item: WateringScriptViewPresentation) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    interface OnWateringScriptActionsListener {
        fun onScriptActionClick(item: WateringScriptViewPresentation)
    }
}