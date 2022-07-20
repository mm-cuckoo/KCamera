package com.sgf.demo

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessageListView : RecyclerView, MessageItemHolder.ItemClickListener {

    private var viewListener : MessageListViewListener? = null

    private val adapter by lazy {
        MessageViewAdapter(this)
    }
    constructor(context: Context) : super(context) {
        initParams(context)
    }
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        initParams(context)
    }
    constructor(context: Context, attributes: AttributeSet, defStyleAttr : Int) : super(context, attributes, defStyleAttr) {
        initParams(context)
    }

    private fun initParams(context: Context) {
        val gridLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        layoutManager = gridLayoutManager
        setAdapter(adapter)
    }


    fun addMessageData(dataList: MutableList<MessageItem>) {
        adapter.addData(dataList)
    }

    fun setListener(listener: MessageListViewListener) {
        this.viewListener = listener
    }

    interface MessageListViewListener {
        fun onItemClick(item: MessageItem)
    }

    override fun onClick(item: MessageItem) {
        viewListener?.onItemClick(item)
    }
}

class MessageViewAdapter(private val listener: MessageItemHolder.ItemClickListener) : RecyclerView.Adapter<MessageItemHolder>() {

    private val itemData = mutableListOf<MessageItem>()

    fun addData(dataList: MutableList<MessageItem>) {
        itemData.clear()
        itemData.addAll(dataList)
        notifyItemRangeChanged(0, dataList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemHolder {
        return MessageDateViewHolder(MessageDateViewHolder.createView(parent), listener)
    }

    override fun onBindViewHolder(holder: MessageItemHolder, position: Int) {
        holder.onBindView(itemData[position])
    }

    override fun getItemCount(): Int {
        return itemData.size
    }
}

class MessageDateViewHolder(view: View, val listener : ItemClickListener) : MessageItemHolder(view) {

    companion object {
        fun createView(parent: ViewGroup) : View {
            return LayoutInflater.from(parent.context).inflate(R.layout.item_select,parent, false)
        }
    }

    private val textView : TextView = view.findViewById(R.id.camera_text)

    override fun onBindView(item: MessageItem) {
        if (item is MessageDate) {
            textView.text = "${item.size.width} x ${item.size.height}"
            textView.setOnClickListener {
                listener.onClick(item)
            }
        }
    }
}


abstract class MessageItemHolder(itemView:View) :RecyclerView.ViewHolder (itemView){
    interface ItemClickListener {
        fun onClick(item: MessageItem)
    }
    abstract fun onBindView(item: MessageItem)
}

abstract class MessageItem

data class MessageDate(val size:Size) : MessageItem()