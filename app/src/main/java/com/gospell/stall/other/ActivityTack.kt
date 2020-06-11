package com.gospell.stall.other

import android.app.Activity
import java.util.*
import kotlin.system.exitProcess

class ActivityTack {

    private var activityList: MutableList<Activity>? = null
    private constructor(){
        activityList =  ArrayList()
    }
    companion object{
        var tack:ActivityTack? = null
        fun getInstanse(): ActivityTack? {
            if (tack==null){
                synchronized(ActivityTack::class.java){
                    if(tack==null){
                        tack = ActivityTack()
                    }
                }
            }
            return tack
        }
    }

    fun addActivity(activity: Activity) {
        activityList!!.add(activity)
    }

    fun removeActivity(activity: Activity?) {
        activityList!!.remove(activity)
    }

    /**
     * 完全退出
     */
    fun exit() {
        while (activityList!!.size > 0) {
            var activity = activityList!![activityList!!.size - 1]
            activity.finish()
            removeActivity(activity)
        }
        exitProcess(0)
    }

    /**
     * 根据class name获取activity
     * @param name
     * @return
     */
    fun getActivityByClassName(name: String?): Activity? {
        for (ac in activityList!!) {
            if (ac.javaClass.name.indexOf(name!!) >= 0) {
                return ac
            }
        }
        return null
    }

    fun getActivityByClass(cs: Class<*>): Activity? {
        for (ac in activityList!!) {
            if (ac.javaClass == cs) {
                return ac
            }
        }
        return null
    }

    /**
     * 弹出activity
     * @param activity
     */
    fun popActivity(activity: Activity) {
        removeActivity(activity)
        activity.finish()
    }


    /**
     * 弹出activity到
     * @param cs
     */
    fun popUntilActivity(vararg cs: Class<*>) {
        val list: MutableList<Activity> = ArrayList()
        for (i in activityList!!.indices.reversed()) {
            val ac = activityList!![i]
            var isTop = false
            for (element in cs) {
                if (ac.javaClass == element) {
                    isTop = true
                    break
                }
            }
            if (!isTop) {
                list.add(ac)
            } else break
        }
        val iterator: Iterator<Activity> = list.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            popActivity(activity)
        }
    }
}