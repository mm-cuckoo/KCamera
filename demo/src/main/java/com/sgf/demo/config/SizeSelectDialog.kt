package com.sgf.demo.config

import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sgf.demo.MessageDate
import com.sgf.demo.MessageItem
import com.sgf.demo.MessageListView
import com.sgf.demo.R
import com.sgf.kcamera.camera.info.CameraInfoHelper

class SizeSelectDialog(private val cameraId: String, val selectCall : (Size) -> Unit) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_size_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val messageList = mutableListOf<MessageItem>()
        val listView = view.findViewById<MessageListView>(R.id.camera_size_list)
        val sizes = CameraInfoHelper.getInstance().getCameraInfo(cameraId).previewSize
        sizes.forEach {

            messageList.add(MessageDate(it))
        }

        listView.addMessageData(messageList)

        listView.setListener(object : MessageListView.MessageListViewListener {
            override fun onItemClick(item: MessageItem) {
                if (item is MessageDate) {
                    selectCall(item.size)
                    dismiss()
                }
            }
        })

    }
}