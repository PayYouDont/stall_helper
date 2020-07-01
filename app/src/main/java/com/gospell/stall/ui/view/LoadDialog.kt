package com.gospell.stall.ui.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.sprite.Sprite
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.ui.util.DhUtil
import com.gospell.stall.ui.util.DrawableUtil
import com.gospell.stall.util.ReflectUtil

class LoadDialog(context: Context) : Dialog(context, R.style.CustomDialog) {
    //整体layout
    @InjectView(id = R.id.main_layout)
    lateinit var mainLayout: LinearLayout

    //显示的标题
    @InjectView(id = R.id.load_title)
    var titleText: TextView? = null

    //加载消息内容
    @InjectView(id = R.id.load_message)
    var loadMessageText: TextView? = null

    @InjectView(id=R.id.load_result_layout)
    lateinit var loadResultLayout:LinearLayout

    //加载图片
    @InjectView(id = R.id.load_view)
    var spinKitView: SpinKitView? = null

    //加载结果描述
    @InjectView(id = R.id.load_result_scroll)
    var resultMessageScrollView: ScrollView? = null

    //加载结果描述
    @InjectView(id = R.id.load_result_message)
    var resultMessageText: TextView? = null

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

    //内容数据
    var title: String? = null
    var loadMessage: String? = null
    var resultMessage: String? = null
    //sprite类型参考：https://github.com/ybq/Android-SpinKit
    private var sprite: Sprite? = null
    var width: Int = DhUtil.dip2px(context,190f)
    var height: Int = DhUtil.dip2px(context,195f)
    var maxHeight:Int = DhUtil.dip2px(context,400f)
    var radius: Float = 5f
    var backgroundColor: Int = Color.parseColor("#99000000")
    var canCanceled = false
    var cancel: String? = null
    var confirm: String? = null
    //取消按钮事件
    private var cancelListener: ((Button, LoadDialog) -> Unit)? = null
    //确定按钮事件
    private var confirmListener: ((Button, LoadDialog) -> Unit)? = null

    /**
     * 自定义加载View
     */
    private var loadViewResourceId:Int?=null
    //自定义加载View回调
    private var loadViewListener: ((View, LoadDialog) -> Unit)? = null
    //自定义进度条，需要当前窗口中包含该进度条时才能显示
    private var progressBar:ProgressBar?=null
    //当自定义进度条存在时，更新进度条时其他需要更新的view列表
    private var progressViewList:ArrayList<View>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_load_layout)
        setCanceledOnTouchOutside(canCanceled)
        //初始化界面控件
        initView()
    }

    override fun show() {
        super.show()
        refreshView()
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

    /**
     * 初始化界面控件的显示数据
     */
    private fun refreshView() {
        if (!title.isNullOrBlank()) {
            titleText?.text = title
            titleText?.visibility = View.VISIBLE
        } else {
            titleText?.visibility = View.GONE
        }
        if (!loadMessage.isNullOrBlank()) {
            loadMessageText?.text = loadMessage
            loadMessageText?.visibility = View.VISIBLE
        } else {
            loadMessageText?.visibility = View.GONE
        }
        if (sprite != null) {
            spinKitView?.setIndeterminateDrawable(sprite)
        }
        if (resultMessage != null) {
            resultMessageText?.text = resultMessage
            resultMessageText?.visibility = View.VISIBLE
            spinKitView?.visibility = View.GONE
        } else {
            resultMessageText?.visibility = View.GONE
            spinKitView?.visibility = View.VISIBLE
        }
        if(loadViewResourceId!=null){
            spinKitView?.visibility = View.GONE
            createLoadDialog()
        }
        setButtons()
        setSize()
        setCanceledOnTouchOutside(canCanceled)
        setBackground()
    }

    fun setTitle(title: String): LoadDialog {
        this.title = title
        return this
    }

    fun setSprite(sprite: Sprite): LoadDialog {
        this.sprite = sprite
        return this
    }

    fun setSize(width: Int, height: Int): LoadDialog {
        this.width = width
        this.height = height
        return this
    }

    private fun setSize() {
        var param = window.attributes
        param.gravity = Gravity.CENTER
        param.width = width
        param.height = height
        window.attributes = param
    }

    fun setLoadMessage(message: String?): LoadDialog {
        this.loadMessage = message
        return this
    }

    fun setResultMessage(message: String): LoadDialog {
        this.resultMessage = message
        return this
    }

    //取消按钮文字
    fun setCancel(cancel: String): LoadDialog {
        this.cancel = cancel
        return this
    }

    //确定按钮文字
    fun setConfirm(confirm: String): LoadDialog {
        this.confirm = confirm
        return this
    }

    //点击其他地方是否允许关闭弹出
    fun setCanCanceled(canCanceled: Boolean): LoadDialog {
        this.canCanceled = canCanceled
        return this
    }

    fun setConfirmListener(listener: (confirmBtn: Button, dialog: LoadDialog) -> Unit): LoadDialog {
        this.confirmListener = listener
        return this
    }

    fun setCancelListener(listener: (confirmBtn: Button, dialog: LoadDialog) -> Unit): LoadDialog {
        this.cancelListener = listener
        return this
    }

    fun setBackground(backgroundColor: Int): LoadDialog {
        this.backgroundColor = backgroundColor
        return this
    }

    private fun setButtons() {
        if (cancel != null || confirm != null) {
            btnLayout?.visibility = View.VISIBLE
            loadMessageText?.visibility = View.GONE
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
                    confirmBtn?.setOnClickListener {confirmListener?.invoke(confirmBtn!!, this) }
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

    private fun setBackground() {
        mainLayout?.background = DrawableUtil.getGradientDrawable(
            context,
            DhUtil.dip2px(context, radius),
            backgroundColor,
            DhUtil.dip2px(context, 0.2f),
            backgroundColor
        )
    }

    /**
     * 一下为自定义加载窗口方法
     */
    fun createLoadDialog(loadViewResourceId:Int):LoadDialog{
        this.loadViewResourceId = loadViewResourceId
        return this
    }
    fun setLoadViewListener(loadViewListener:((View, LoadDialog) -> Unit)?):LoadDialog{
        this.loadViewListener = loadViewListener
        return this
    }
    fun setProgressBar(progressBar:ProgressBar,progressViewList:ArrayList<View>?):LoadDialog{
        this.progressBar = progressBar
        this.progressViewList = progressViewList
        return this
    }
    fun setProgress(progress:Int,progressListener: ((ArrayList<View>) -> Unit)?):LoadDialog{
        if(this.progressBar!=null){
            this.progressBar!!.progress = progress
        }
        if (progressViewList!=null&&progressListener!=null){
            progressListener.invoke(progressViewList!!)
        }
        return this
    }
    private fun createLoadDialog(){
        mainLayout.setPadding(0,0,0,0)
        var view = View.inflate(context,loadViewResourceId!!,null)
        loadResultLayout.addView(view)
        if(loadViewListener!=null){
            loadViewListener!!.invoke(view,this)
        }
    }

    /**
     * 创建默认带进度条窗口
     */
    fun createProgressDialog(title:String,fileName:String,fileSize:String,playOrPauseListener:View.OnClickListener):LoadDialog{
        this.loadViewResourceId = R.layout.dialog_progress_layout
        setSize(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        loadViewListener = { view, _ ->
            var layoutParams = view.layoutParams
            layoutParams.height = DhUtil.dip2px(context,80f)
            view.layoutParams = layoutParams
            view.findViewById<ImageView>(R.id.app_img).visibility = View.GONE
            var updateViewList = ArrayList<View>()
            updateViewList.add(view.findViewById(R.id.load_progress))
            updateViewList.add(view.findViewById(R.id.load_rate))
            setProgressBar(view.findViewById(R.id.notify_progress),updateViewList)
            view.findViewById<TextView>(R.id.notify_progress_title).text = title
            view.findViewById<TextView>(R.id.notify_filename).text = fileName
            view.findViewById<TextView>(R.id.load_size).text = fileSize
            view.findViewById<ImageView>(R.id.notify_play_btn).setOnClickListener(playOrPauseListener)
        }
        return this
    }
}