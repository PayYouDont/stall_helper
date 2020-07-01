package com.gospell.stall

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.gospell.stall.entity.User
import com.gospell.stall.helper.ActivityTack
import com.gospell.stall.helper.RequestHelper
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.LoadDialog
import com.gospell.stall.util.JsonUtil
import org.json.JSONObject
import ru.alexbykov.nopermission.PermissionHelper

class MapActivity : AppCompatActivity(),LocationSource, AMapLocationListener {
    private var myLocationStyle: MyLocationStyle? = null
    private var aMap: AMap? = null

    //定位需要的数据
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var permissionHelper: PermissionHelper? = null

    //当前缩放等级可视区域范围坐标
    var southwest: LatLng? = null //西南角坐标
    var northeast: LatLng? = null //东北角坐标

    var markers = HashMap<Int, Marker>()

    private var mMapView: MapView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //去掉标题栏
        supportActionBar?.hide()
        setContentView(R.layout.activity_map)
        mMapView = findViewById(R.id.map)
        mMapView!!.onCreate(savedInstanceState)
        permissionHelper = PermissionHelper(this)
        permissionHelper!!.check(
            Manifest.permission.ACCESS_FINE_LOCATION
        ).onSuccess {
            initMap()
            setPosition()
        }.onDenied {
            Toast.makeText(this, "权限被拒绝！将无法获取到WiFi信息!", Toast.LENGTH_SHORT).show()
        }.onNeverAskAgain {
            Toast.makeText(this, "自动同步功能需要授权后才能使用！", Toast.LENGTH_SHORT).show();
            permissionHelper!!.startApplicationSettingsActivity();
        }.run()
        ActivityTack.getInstanse()?.addActivity(this)
    }
    private fun initMap() {
        aMap = mMapView?.map
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
            RequestHelper.getInstance(this)
                .post(Constants.updateUrl, map, "定位中...") { result ->
                    var json = JSONObject(result)
                    runOnUiThread {
                        if (json.getBoolean("success")) {
                            val visibleRegion = aMap?.projection!!.visibleRegion
                            val latLngBounds = visibleRegion.latLngBounds //由可视区域的四个顶点形成的经纬度范围
                            initNearUsers(latLngBounds.southwest,latLngBounds.northeast)
                        } else {
                            ToastUtil.makeText(this, "上传位置信息失败!")
                        }
                    }
                }
        }
    }

    private fun initNearUsers(southwest: LatLng,northeast: LatLng) {
        var map = mutableMapOf<String, Any>()
        map["southwestLat"] = southwest.latitude.toString()
        map["southwestLng"] = southwest.longitude.toString()
        map["northeastLat"] = northeast.latitude.toString()
        map["northeastLng"] = northeast.longitude.toString()
        RequestHelper.getInstance(this)
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
                    this.runOnUiThread {
                        var msg = json.getString("msg")
                        LoadDialog(this).setResultMessage("获取周围摊位信息失败！$msg")
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
        initMap()
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

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = AMapLocationClient(this)
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
    private fun setPosition(){
        var code = intent.getIntExtra("code",-1)
        if (code>0){
            var markerOptions = MarkerOptions()
            var lat = Constants.user?.latitude
            var lng = Constants.user?.longitude
            while(lat==null||lng==null){
                Thread.sleep(1000)
            }
            var latLng = LatLng(lat,lng)
            markerOptions.position(latLng)
            markerOptions.title("拖动选择位置")
            markerOptions.draggable(true)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources,R.drawable.icon_location_marker)))
            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
            markerOptions.isFlat = true;//设置marker平贴地图效果
            aMap?.setOnMarkerDragListener(object :AMap.OnMarkerDragListener{
                override fun onMarkerDragEnd(p0: Marker?) {
                    var latLng = p0?.position
                    LoadDialog(this@MapActivity)
                        .setResultMessage("是否定位在这个位置？")
                        .setConfirm("确定")
                        .setConfirmListener{ _, dialog ->
                            dialog.dismiss()
                            var intent = Intent().putExtra("position","${latLng?.latitude},${latLng?.longitude}")
                            setResult(code,intent)
                            finish()
                        }
                        .setCancel("取消")
                        .setCancelListener{ _, dialog ->
                            dialog.dismiss()
                        }
                        .show()
                }

                override fun onMarkerDragStart(p0: Marker?) {
                }

                override fun onMarkerDrag(p0: Marker?) {
                }
            })
            aMap?.addMarker(markerOptions)
        }
    }
}