package com.gospell.stall.helper

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.ui.util.DhUtil
import com.gospell.stall.ui.util.DrawableUtil
import com.gospell.stall.ui.util.ToastUtil


class StallInfoWindowAdapter(context: Context):AMap.InfoWindowAdapter,View.OnClickListener {
    private val mContext: Context = context
    private var callLayout: LinearLayout? = null
    private var navigationLayout: LinearLayout? = null
    private var infoTitleText: TextView? = null
    private var infoContentText: TextView? = null
    private lateinit var marker: Marker
    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    override fun getInfoWindow(p0: Marker): View {
        this.marker = p0
        return initView()
    }

    private fun initView(): View {
        val view = LayoutInflater.from(mContext).inflate(R.layout.window_info_stall, null)
        view?.background = DrawableUtil.getGradientDrawable(mContext,DhUtil.dip2px(mContext, 2f), Color.WHITE,DhUtil.dip2px(mContext, 0.2f),Color.WHITE)
        navigationLayout = view.findViewById(R.id.navigation_layout)
        callLayout = view.findViewById(R.id.call_layout)
        infoTitleText = view.findViewById(R.id.info_title)
        infoContentText = view.findViewById(R.id.info_content)
        infoTitleText?.text = marker.title
        infoContentText?.text = marker.snippet
        navigationLayout?.setOnClickListener(this)
        callLayout?.setOnClickListener(this)
        return view
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.navigation_layout -> {
                var latLng = marker.position
                ToastUtil.makeText(mContext,"latlng=$latLng")
            }
            R.id.call_layout ->{
                ToastUtil.makeText(mContext,"phoneNumber=${Constants.user?.phoneNumber}")
            }
        }
    }
}