package com.gospell.stall.ui.home

import android.Manifest
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.LocationSource.OnLocationChangedListener
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.entity.User
import com.gospell.stall.helper.RequestHelper
import com.gospell.stall.helper.StallInfoWindowAdapter
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.LoadDialog
import com.gospell.stall.util.JsonUtil
import org.json.JSONObject
import ru.alexbykov.nopermission.PermissionHelper


class HomeFragment : BaseFragment(), LocationSource, AMapLocationListener,AMap.OnMarkerClickListener{

    private var myLocationStyle: MyLocationStyle? = null
    private var aMap: AMap? = null

    //定位需要的数据
    private var mListener: OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var permissionHelper: PermissionHelper? = null
    //自定义marker相关
    private var oldMarker: Marker? = null
    @InjectView(layout = R.layout.fragment_home)
    private lateinit var root: View

    @InjectView(id = R.id.map)
    private var mMapView: MapView? = null
    override fun onCreateView() {
        permissionHelper = PermissionHelper(this)
        permissionHelper!!.check(
            Manifest.permission.ACCESS_FINE_LOCATION
        ).onSuccess {
            initMap()
        }.onDenied {
            Toast.makeText(requireContext(), "权限被拒绝！将无法获取到WiFi信息!", Toast.LENGTH_SHORT).show()
        }.onNeverAskAgain {
            Toast.makeText(requireContext(), "自动同步功能需要授权后才能使用！", Toast.LENGTH_SHORT).show();
            permissionHelper!!.startApplicationSettingsActivity();
        }.run()
    }
    private fun initMap() {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView!!.onCreate(savedInstanceState)
        aMap = mMapView?.map
        //设置地图的放缩级别
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(12f))
        // 设置定位监听
        aMap?.setLocationSource(this)
        //设置默认定位按钮是否显示
        aMap!!.uiSettings.isMyLocationButtonEnabled = true
        //蓝点初始化
        //初始化定位蓝点样式类
        myLocationStyle = MyLocationStyle()
        //定位一次，且将视角移动到地图中心点。
        myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap?.isMyLocationEnabled = true

        myLocationStyle?.showMyLocation(true)
        aMap?.myLocationStyle = myLocationStyle
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap?.isMyLocationEnabled = true
        aMap?.setOnMyLocationChangeListener { location: Location? -> position(location) }
        aMap?.setInfoWindowAdapter(StallInfoWindowAdapter(requireContext()))
        //aMap?.setOnMapClickListener(this)
        aMap?.setOnMarkerClickListener(this)
    }

    //上传位置信息
    private fun position(location: Location?) {
        //从location对象中获取经纬度信息，地址描述信息，建议拿到位置之后调用逆地理编码接口获取
        if (Constants.user != null && location != null) {
            Constants.user?.latitude = location.latitude
            Constants.user?.longitude = location.longitude
            var map = mutableMapOf<String, Any>()
            map["id"] = Constants.user?.id.toString()
            map["latitude"] = Constants.user?.latitude.toString()
            map["longitude"] = Constants.user?.longitude.toString()
            RequestHelper.getInstance(requireContext())
                .post(Constants.updateUrl, map, "定位中...") { result ->
                    var json = JSONObject(result)
                    requireActivity().runOnUiThread {
                        if (json.getBoolean("success")) {
                            val visibleRegion = aMap?.projection!!.visibleRegion
                            val latLngBounds = visibleRegion.latLngBounds //由可视区域的四个顶点形成的经纬度范围
                            initNearUsers(latLngBounds.southwest,latLngBounds.northeast)
                        } else {
                            ToastUtil.makeText(requireContext(), "上传位置信息失败!")
                        }
                    }
                }
        }
    }
    //获取当前缩放等级可视区域范围坐标内的店铺信息
    private fun initNearUsers(southwest: LatLng,northeast: LatLng) {
        var map = mutableMapOf<String, Any>()
        map["southwestLat"] = southwest.latitude.toString()
        map["southwestLng"] = southwest.longitude.toString()
        map["northeastLat"] = northeast.latitude.toString()
        map["northeastLng"] = northeast.longitude.toString()
        RequestHelper.getInstance(requireContext())
            .post(Constants.getNearStallUrl, map, "正在获取周围摊位...") { result ->
                var json = JSONObject(result)
                if (json.getBoolean("success")) {
                    var users = JsonUtil.toList(json.getString("data"), User::class.java)
                    for (user in users!!) {
                        var stallInfo = user.stallInfo
                        if(stallInfo?.latitude!=null&&stallInfo?.longitude!=null){
                            var latLng = LatLng(stallInfo?.latitude!!, stallInfo?.longitude!!)
                            var marker = MarkerOptions().position(latLng).title("摊位名称:${stallInfo?.name}").snippet("摊位信息:${stallInfo?.content}")
                            marker.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources,R.drawable.marker_stall)))
                            aMap!!.addMarker(marker)
                        }
                    }
                }else{
                    requireActivity().runOnUiThread {
                        var msg = json.getString("msg")
                        LoadDialog(requireContext()).setResultMessage("获取周围摊位信息失败！$msg")
                    }
                }
            }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        if(oldMarker!=p0){
            oldMarker?.hideInfoWindow()
        }
        oldMarker = p0
        return false; //返回 “false”，除定义的操作之外，默认操作也将会被执行
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        permissionHelper!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView?.onResume()
        //initMap()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView?.onDestroy()
        mlocationClient?.onDestroy()
    }

    override fun deactivate() {
        mListener = null
        mlocationClient?.stopLocation()
        mlocationClient?.onDestroy()
        mlocationClient = null
    }

    override fun activate(listener: OnLocationChangedListener?) {
        mListener = listener
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = AMapLocationClient(requireActivity())
            //初始化定位参数
            mLocationOption = AMapLocationClientOption()
            //设置定位回调监听
            mlocationClient!!.setLocationListener(this)
            //设置为高精度定位模式
            mLocationOption!!.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //只定位一次
            mLocationOption!!.isOnceLocation = true;
            //设置定位参数
            mlocationClient!!.setLocationOption(mLocationOption)

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient!!.startLocation() //启动定位
        }
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                mListener!!.onLocationChanged(aMapLocation) // 显示系统小蓝点
            } else {
                val errText = "定位失败," + aMapLocation.errorCode + ": " + aMapLocation.errorInfo
                Log.e("定位AmapErr", errText)
            }
        }
    }
}