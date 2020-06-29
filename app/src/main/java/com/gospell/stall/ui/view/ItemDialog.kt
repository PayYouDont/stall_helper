package com.gospell.stall.ui.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.view.setPadding
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.ui.util.DhUtil
import com.gospell.stall.ui.util.DrawableUtil
import com.gospell.stall.util.ReflectUtil

open class ItemDialog(context: Context) : Dialog(context, R.style.CustomDialog) {
    //整体layout
    @InjectView(id = R.id.main_layout)
    var mainLayout: LinearLayout? = null

    //显示的标题
    @InjectView(id = R.id.item_title)
    var titleText: TextView? = null

    //单选按钮组
    @InjectView(id = R.id.radio_group)
    var radioGroup: RadioGroup? = null

    //多选框和纯文字layout
    @InjectView(id = R.id.item_list_layout)
    var itemListLayout: LinearLayout? = null

    //按钮组
    @InjectView(id = R.id.load_result_btn_layout)
    var btnLayout: LinearLayout? = null

    //取消按钮
    @InjectView(id = R.id.cancel_btn)
    var cancelBtn: Button? = null

    //确定按钮
    @InjectView(id = R.id.confirm_btn)
    var confirmBtn: Button? = null

    @InjectView(id = R.id.column_space)
    var columnSpace: Space? = null

    var width: Int = WindowManager.LayoutParams.MATCH_PARENT
    var height: Int = WindowManager.LayoutParams.WRAP_CONTENT
    var radius: Float = 5f
    var canCanceled = false
    var backgroundColor: Int = Color.WHITE
    var title: String? = null
    var cancel: String? = null
    var confirm: String? = null

    //取消按钮事件
    var cancelListener: ((Button, ItemDialog) -> Unit)? = null
    //确定按钮事件
    var confirmListener: ((Button, ItemDialog) -> Unit)? = null
    //item点击时监听事件
    var itemListener: ((ItemDialog, Int) -> Unit)? = null
    //窗口内控件自定义事件
    var viewListener:((View) -> Unit)? = null
    //内容数据
    private var radioList: ArrayList<String>? = null
    private var checkBoxList: ArrayList<String>? = null
    private var labelList: ArrayList<String>? = null
    private var content: String? = null
    private var gravity: Int = Gravity.CENTER
    private var viewList:ArrayList<View>? = null
    private var viewResourceId:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_item_layout)
        setCanceledOnTouchOutside(canCanceled)
        //初始化界面控件
        initView()
    }

    /**
     * 初始化界面控件的显示数据
     */
    private fun refreshView() {
        if (title != null) {
            titleText?.text = title
        } else {
            titleText?.visibility = View.GONE
        }
        if (radioList != null) {
            radioGroup?.visibility = View.VISIBLE
            createRadioDialog()
        } else {
            radioGroup?.visibility = View.GONE
        }
        if (checkBoxList != null) {
            itemListLayout?.visibility = View.VISIBLE
            createCheckBoxDialog()
        }
        if (labelList != null) {
            itemListLayout?.visibility = View.VISIBLE
            createLabelDialog()
        }
        if (viewList!=null){
            itemListLayout?.visibility = View.VISIBLE
            createViewDialog()
        }
        if(viewResourceId!=null){
            itemListLayout?.visibility = View.VISIBLE
            createDialog()
        }
        setBackground()
        setSize()
        setButtons()
    }

    fun setTitle(title: String): ItemDialog {
        this.title = title
        return this
    }

    fun setGravity(gravity:Int): ItemDialog {
        this.gravity = gravity
        return this
    }
    fun setCanCanceled(canCanceled:Boolean): ItemDialog{
        this.canCanceled = canCanceled
        return this
    }
    //确定按钮文字
    fun setConfirm(confirm: String): ItemDialog {
        this.confirm = confirm
        return this
    }

    //取消按钮文字
    fun setCancel(cancel: String): ItemDialog {
        this.cancel = cancel
        return this
    }

    fun setConfirmListener(listener: (confirmBtn: Button, dialog: ItemDialog) -> Unit): ItemDialog {
        this.confirmListener = listener
        return this
    }

    fun setCancelListener(listener: (confirmBtn: Button, dialog: ItemDialog) -> Unit): ItemDialog {
        this.cancelListener = listener
        return this
    }
    //点击某条数据时监听
    fun setItemListener(listener: (dialog: ItemDialog, position: Int) -> Unit): ItemDialog {
        this.itemListener = listener
        return this
    }
    fun setViewListener(listener:((View) -> Unit)?): ItemDialog {
        this.viewListener = listener
        return this
    }
    //设置背景
    private fun setBackground() {
        mainLayout?.background = DrawableUtil.getGradientDrawable(
            context,
            DhUtil.dip2px(context, radius),
            backgroundColor,
            DhUtil.dip2px(context, 0.2f),
            backgroundColor
        )
    }
    //设置大小
    fun setSize(width: Int, height: Int): ItemDialog {
        this.width = width
        this.height = height
        return this
    }
    //设置按钮
    private fun setButtons() {
        if (cancel != null || confirm != null) {
            btnLayout?.visibility = View.VISIBLE
            var columnShow = true
            if (cancel != null) {
                cancelBtn?.visibility = View.VISIBLE
                if (cancelListener == null) {
                    cancelBtn?.setOnClickListener { dismiss() }
                } else {
                    cancelBtn?.setOnClickListener { cancelListener?.invoke(cancelBtn!!, this) }
                }
                cancelBtn?.text = cancel
            } else {
                cancelBtn?.visibility = View.GONE
                columnShow = false
            }
            if (confirm != null) {
                confirmBtn?.visibility = View.VISIBLE
                if (confirmListener == null) {
                    confirmBtn?.setOnClickListener { dismiss() }
                } else {
                    confirmBtn?.setOnClickListener { confirmListener?.invoke(confirmBtn!!, this) }
                }
                confirmBtn?.text = confirm
            } else {
                confirmBtn?.visibility = View.GONE
                columnShow = false
            }
            if (columnShow) {
                columnSpace?.visibility = View.VISIBLE
            } else {
                columnSpace?.visibility = View.GONE
            }
        }
    }

    fun createRadioDialog(radioList: ArrayList<String>): ItemDialog {
        this.radioList = radioList
        return this
    }

    private fun createRadioDialog(): ItemDialog {
        for (text in radioList!!) {
            var radioButton = RadioButton(context)
            radioButton.text = text
            radioButton.setTextColor(Color.BLACK)
            radioButton.textSize = 16f
            radioButton.setOnClickListener {
                itemListener?.invoke(this, radioList!!.indexOf(text))
            }
            radioGroup?.addView(radioButton)
        }
        return this
    }

    fun createCheckBoxDialog(checkBoxList: ArrayList<String>): ItemDialog {
        this.checkBoxList = checkBoxList
        return this
    }

    private fun createCheckBoxDialog(): ItemDialog {
        for (text in checkBoxList!!) {
            var checkBox = CheckBox(context)
            checkBox.text = text
            checkBox.setTextColor(Color.BLACK)
            checkBox.textSize = 16f
            checkBox.setOnClickListener {
                itemListener?.invoke(this, checkBoxList!!.indexOf(text))
            }
            itemListLayout?.addView(checkBox)
        }

        return this
    }

    fun createLabelDialog(content: String?, labelList: ArrayList<String>): ItemDialog {
        this.labelList = labelList
        this.content = content
        return this
    }

    private fun createLabelDialog(): ItemDialog {
        if (title != null) {
            titleText?.gravity = Gravity.CENTER
        }
        if (!content.isNullOrBlank()) {
            var contentText = TextView(context)
            itemListLayout?.addView(contentText)
            contentText.text = content
            contentText.setTextColor(Color.BLACK)
            contentText.textSize = 16f
            var param = contentText.layoutParams as LinearLayout.LayoutParams
            var margin = DhUtil.dip2px(context, 10f)
            param.setMargins(margin, margin, margin, margin)
            contentText.layoutParams = param
            var imageView = ImageView(context)
            itemListLayout?.addView(imageView)
            imageView.setBackgroundColor(Color.GRAY)
            var imageViewParam = imageView.layoutParams
            imageViewParam.width = ViewGroup.LayoutParams.MATCH_PARENT
            imageViewParam.height = DhUtil.dip2px(context, 0.2f)
            imageView.layoutParams = imageViewParam
        }
        for (text in labelList!!) {
            var textView = TextView(context)
            itemListLayout?.addView(textView)
            textView.text = text
            textView.setTextColor(Color.parseColor("#38ADFF"))
            textView.textSize = 16f
            textView.gravity = Gravity.CENTER
            var textViewParam = textView.layoutParams as LinearLayout.LayoutParams
            textViewParam.width = ViewGroup.LayoutParams.MATCH_PARENT
            textViewParam.height = DhUtil.dip2px(context, 30f)
            textViewParam.topMargin = DhUtil.dip2px(context, 10f)
            textViewParam.bottomMargin = DhUtil.dip2px(context, 10f)
            textView.layoutParams = textViewParam
            textView.setOnClickListener {
                itemListener?.invoke(this, labelList!!.indexOf(text))
            }
            var imageView = ImageView(context)
            itemListLayout?.addView(imageView)
            imageView.setBackgroundColor(Color.GRAY)
            var imageViewParam = imageView.layoutParams
            imageViewParam.width = ViewGroup.LayoutParams.MATCH_PARENT
            imageViewParam.height = DhUtil.dip2px(context, 0.2f)
            imageView.layoutParams = imageViewParam
        }
        btnLayout?.visibility = View.GONE
        gravity = Gravity.BOTTOM
        return this
    }
    fun createViewDialog(viewList: ArrayList<View>): ItemDialog {
        this.viewList = viewList
        return this
    }
    private fun createViewDialog(){
        for (view in viewList!!) {
            itemListLayout?.addView(view)
            var viewParam = view.layoutParams as LinearLayout.LayoutParams
            viewParam.width = ViewGroup.LayoutParams.MATCH_PARENT
            viewParam.height = ViewGroup.LayoutParams.WRAP_CONTENT
            viewParam.topMargin = DhUtil.dip2px(context, 20f)
            view.layoutParams = viewParam
            view.setOnClickListener {
                itemListener?.invoke(this, viewList!!.indexOf(view))
            }
        }
    }
    fun createDialog(viewResourceId: Int): ItemDialog {
        this.viewResourceId = viewResourceId
        return this
    }
    private fun createDialog(){
        var view = View.inflate(context,viewResourceId!!,null)
        itemListLayout?.addView(view)
        btnLayout?.visibility = View.GONE
        viewListener?.invoke(view)
    }
    private fun setSize() {
        var param = window.attributes
        param.gravity = gravity
        param.width = width
        param.height = height
        window.attributes = param
    }

    private fun initView() {
        ReflectUtil.initFieldByAnnotation(javaClass, InjectView::class.java, { annotation, field ->
            val injectView: InjectView = annotation as InjectView
            try {
                if (injectView.id != -1) {
                    field!!.set(this, findViewById(injectView.id))
                }
            } catch (e: IllegalAccessException) {
                Log.e(javaClass.name, e.message, e)
            }
        }, false)
    }

    override fun show() {
        super.show()
        refreshView()
    }
}