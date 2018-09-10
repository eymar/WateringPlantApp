package app.autowatering.bluetooth

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import app.autowatering.databinding.BluetoothDeviceListItemBinding

class BtDevicesArrayAdapter : RecyclerView.Adapter<BtDevicesArrayAdapter.ViewHolder>() {

    private val items = ArrayList<BtDeviceViewPresentation>()

    var listener: BtDeviceItemActionsListener? = null

    fun update(newItems: ArrayList<BtDeviceViewPresentation>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val new = newItems[newItemPosition]
                val old = items[oldItemPosition]
                return new.mac == old.mac
            }

            override fun getOldListSize() = items.size

            override fun getNewListSize() = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val new = newItems[newItemPosition]
                val old = items[oldItemPosition]
                return new.mac == old.mac && new.name == old.name
            }
        })

        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BluetoothDeviceListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val binding: BluetoothDeviceListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener({
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                listener?.onBtDeviceItemSelected(items[adapterPosition])
            })
        }

        fun bind(item: BtDeviceViewPresentation) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    interface BtDeviceItemActionsListener {
        fun onBtDeviceItemSelected(item: BtDeviceViewPresentation)
    }
}