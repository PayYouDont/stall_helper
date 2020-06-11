package com.gospell.stall.common.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.util.ReflectUtil
import java.lang.reflect.Method

abstract class BaseFragment: Fragment() {
    private var root: View? = null
    private var inflater: LayoutInflater? = null
    private var container: ViewGroup? = null
    protected var savedInstanceState: Bundle? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.inflater = inflater
        this.container = container
        this.savedInstanceState = savedInstanceState
        initRootView()
        initViewByAnnotation()
        onCreateView()
        return root
    }

    protected abstract fun onCreateView()
    /**
     * @Author peiyongdong
     * @Description ( 初始化RootView注解 )
     * @Date 11:22 2020/3/20
     * @Param []
     * @return void
     */
    private fun initRootView() {
        ReflectUtil.initFieldByAnnotation(javaClass,InjectView::class.java,{
                annotation, field ->
            val injectView: InjectView = annotation as InjectView
            try {
                if(root!=null){
                    return@initFieldByAnnotation
                }
                if(injectView.layout!=-1){
                    field!!.set(this, inflater!!.inflate(injectView.layout, container, false))
                    root = field.get(this) as View?
                }
            } catch (e: IllegalAccessException) {
                Log.e(javaClass.name, e.message, e)
            }
        },false)
    }
    /**
     * @Author peiyongdong
     * @Description ( 初始化ViewById注解 )
     * @Date 11:22 2020/3/20
     * @Param []
     * @return void
     */
    private fun initViewByAnnotation() {
        ReflectUtil.initFieldByAnnotation(javaClass, InjectView::class.java,{ annotation, field ->
            val injectView: InjectView = annotation as InjectView
            try {
                if(injectView.id!=-1){
                    field!!.set(this, root!!.findViewById(injectView.id))
                }
                if(injectView.listeners.isNotEmpty()){
                    var listeners = injectView.listeners
                    for(i in listeners){
                        var methodName = "set"+i.simpleName
                        var method: Method?=null
                        try {
                            method = field?.get(this)?.javaClass?.getMethod(methodName,i.java)
                        }catch (e:Exception){}
                        if(method==null){
                            methodName = "add"+i.simpleName
                            method = field?.get(this)?.javaClass?.getMethod(methodName,i.java)
                        }
                        method?.invoke(field?.get(this),this)
                    }
                }
            } catch (e: IllegalAccessException) {
                Log.e(javaClass.name, e.message, e)
            }
        },false)
    }
}