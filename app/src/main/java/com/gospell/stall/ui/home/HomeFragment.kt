package com.gospell.stall.ui.home

import android.Manifest
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
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.entity.User
import com.gospell.stall.util.HttpUtil
import com.gospell.stall.util.JsonUtil
import com.gospell.stall.util.ToastUtil
import org.json.JSONObject
import ru.alexbykov.nopermission.PermissionHelper
import java.util.function.BiConsumer


class HomeFragment : BaseFragment(), LocationSource, AMapLocationListener {

    private var myLocationStyle: MyLocationStyle? = null
    private var aMap: AMap? = null

    //定位需要的数据
    private var mListener: OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var permissionHelper: PermissionHelper? = null

    //当前缩放等级可视区域范围坐标
    var southwest: LatLng? = null //西南角坐标
    var northeast: LatLng? = null //东北角坐标

    var markers = HashMap<Int,Marker>()

    @InjectView(layout = R.layout.fragment_home)
    private var root: View? = null;

    @InjectView(id = R.id.map)
    private var mMapView: MapView? = null
    override fun onCreateView() {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView!!.onCreate(savedInstanceState)
        permissionHelper = PermissionHelper(this)
        permissionHelper!!.check(
            Manifest.permission.ACCESS_FINE_LOCATION
        ).onSuccess {
           //initMap()
        }.onDenied {
            Toast.makeText(requireContext(), "权限被拒绝！将无法获取到WiFi信息!", Toast.LENGTH_SHORT).show()
        }.onNeverAskAgain {
            Toast.makeText(requireContext(), "自动同步功能需要授权后才能使用！", Toast.LENGTH_SHORT).show();
            permissionHelper!!.startApplicationSettingsActivity();
        }.run()
    }

    private fun initMap() {
        aMap = mMapView?.map
        //设置地图的放缩级别
        aMap!!.moveCamera(CameraUpdateFactory.zoomTo(12f))
        // 设置定位监听
        aMap!!.setLocationSource(this)
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap!!.isMyLocationEnabled = true
        //蓝点初始化
        //初始化定位蓝点样式类
        myLocationStyle = MyLocationStyle()
        //定位一次，且将视角移动到地图中心点。
        myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
        //连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        //myLocationStyle!!.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //myLocationStyle!!.interval(2000)
        myLocationStyle?.showMyLocation(true)
        aMap?.myLocationStyle = myLocationStyle
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap?.isMyLocationEnabled = true
        aMap?.setOnMyLocationChangeListener { location: Location? -> position(location) }

    }

    //上传位置信息
    private fun position(location: Location?) {
        //从location对象中获取经纬度信息，地址描述信息，建议拿到位置之后调用逆地理编码接口获取
        if (Constants.user != null && location != null) {
            var latitude = Constants.user?.latitude
            var longitude = Constants.user?.longitude
            var lat = String.format("%.4f", location?.latitude).toDouble()
            var log = String.format("%.4f", location?.longitude).toDouble()
            if (latitude != lat || longitude != log) {
                val visibleRegion = aMap?.projection!!.visibleRegion
                val latLngBounds = visibleRegion.latLngBounds //由可视区域的四个顶点形成的经纬度范围
                southwest = latLngBounds.southwest //西南角坐标
                northeast = latLngBounds.northeast //东北角坐标
                Constants.user?.latitude = lat
                Constants.user?.longitude = log
                var map = mutableMapOf<String,Any>()
                map["id"] = Constants.user?.id.toString()
                map["latitude"] = Constants.user?.latitude.toString()
                map["longitude"] = Constants.user?.longitude.toString()
                HttpUtil.post(Constants.updateUrl, map) { response ->
                    var result = response.body!!.string()
                    var json = JSONObject(result)
                    if (json.getBoolean("success")) {
                        initNearUsers()
                    } else {
                        ToastUtil.makeText(requireContext(), "上传位置信息失败!")
                    }
                }
            }
        }
    }
    private fun initNearUsers(){
        var map = mutableMapOf<String,Any>()
        map["southwestLat"] = southwest?.latitude.toString()
        map["southwestLng"] = southwest?.longitude.toString()
        map["northeastLat"] = northeast?.latitude.toString()
        map["northeastLng"] = northeast?.longitude.toString()
        HttpUtil.post(Constants.getNearStallUrl, map) { response ->
            var json = JSONObject(response.body!!.string())
            if (json.getBoolean("success")) {
                var users = JsonUtil.toList(json.getString("data"), User::class.java)
                for (i in users!!){
                    var lat = i.latitude
                    var lng = i.longitude
                    var stallInfo = i.stallInfo
                    var latLng = LatLng(lat!!,lng!!)
                    var marker:Marker
                    if(markers.containsKey(i.id)){
                        //marker = markers[i.id]!!
                        //marker.remove()
                        //aMap.invalidate();//刷新地图
                        continue
                    }
                    marker = aMap!!.addMarker( MarkerOptions().position(latLng).title(stallInfo?.name).snippet("摊位1"))
                    markers[i.id!!] = marker
                }
            }
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
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
        super.onSaveInstanceState(outState!!)
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
            mLocationOption!!.locationMode =
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
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
            if (aMapLocation != null && aMapLocation.errorCode == 0) {
                mListener!!.onLocationChanged(aMapLocation) // 显示系统小蓝点
            } else {
                val errText = "定位失败," + aMapLocation.errorCode + ": " + aMapLocation.errorInfo
                Log.e("定位AmapErr", errText)
            }
        }
    }
}