package com.gospell.stall.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*


class LocationUtils {
    companion object {
        private var mListener: OnLocationChangeListener? = null
        private var myLocationListener: MyLocationListener? = null
        private var mLocationManager: LocationManager? = null

        /**
         * 判断Gps是否可用
         *
         * @return `true`: 是<br></br>`false`: 否
         */
        fun isGpsEnabled(context: Context): Boolean {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        /**
         * 判断定位是否可用
         *
         * @return `true`: 是<br></br>`false`: 否
         */
        fun isLocationEnabled(context: Context): Boolean {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        }

        /**
         * 打开Gps设置界面
         */
        fun openGpsSettings(context: Context) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        /**
         * 注册
         *
         * 使用完记得调用[.unregister]
         *
         * 需添加权限 `<uses-permission android:name="android.permission.INTERNET"/>`
         *
         * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>`
         *
         * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>`
         *
         * 如果`minDistance`为0，则通过`minTime`来定时更新；
         *
         * `minDistance`不为0，则以`minDistance`为准；
         *
         * 两者都为0，则随时刷新。
         *
         * @param minTime     位置信息更新周期（单位：毫秒）
         * @param minDistance 位置变化最小距离：当位置距离变化超过此值时，将更新位置信息（单位：米）
         * @param listener    位置刷新的回调接口
         * @return `true`: 初始化成功<br></br>`false`: 初始化失败
         */
        fun register(
            context: Context,
            minTime: Long,
            minDistance: Long,
            listener: OnLocationChangeListener?
        ): Boolean {
            if (listener == null) return false
            mLocationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mListener = listener
            if (!isLocationEnabled(context)) {
                Toast.makeText(context, "无法定位，请打开定位服务", Toast.LENGTH_SHORT).show()
                return false
            }
            val provider = mLocationManager!!.getBestProvider(getCriteria(), true)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
            val location =
                mLocationManager!!.getLastKnownLocation(provider)
            if (location != null) listener.getLastKnownLocation(location)
            if (myLocationListener == null) myLocationListener = MyLocationListener()
            mLocationManager!!.requestLocationUpdates(
                provider,
                minTime,
                minDistance.toFloat(),
                myLocationListener
            )
            return true
        }


        /**
         * 注销
         *
         * 使用[.register]
         * 完记得调用此方法
         *
         * @param context 上下文
         */
        fun unregister(context: Context?) {
            if (mLocationManager != null) {
                if (myLocationListener != null) {
                    if (ActivityCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mLocationManager!!.removeUpdates(myLocationListener)
                    myLocationListener = null
                }
                mLocationManager = null
            }
        }

        /**
         * 设置定位参数
         *
         * @return [Criteria]
         */
        private fun getCriteria(): Criteria? {
            val criteria = Criteria()
            //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
            criteria.accuracy = Criteria.ACCURACY_FINE
            //设置是否要求速度
            criteria.isSpeedRequired = false
            // 设置是否允许运营商收费
            criteria.isCostAllowed = false
            //设置是否需要方位信息
            criteria.isBearingRequired = false
            //设置是否需要海拔信息
            criteria.isAltitudeRequired = false
            // 设置对电源的需求
            criteria.powerRequirement = Criteria.POWER_LOW
            return criteria
        }

        /**
         * 根据经纬度获取地理位置
         *
         * @param context   上下文
         * @param latitude  纬度
         * @param longitude 经度
         * @return [Address]
         */
        fun getAddress(
            context: Context?,
            latitude: Double,
            longitude: Double
        ): Address? {
            val geoCoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses: List<Address> = geoCoder.getFromLocation(latitude, longitude, 1)
                if (addresses.isNotEmpty()) return addresses[0]
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * 根据经纬度获取所在国家
         *
         * @param context   上下文
         * @param latitude  纬度
         * @param longitude 经度
         * @return 所在国家
         */
        fun getCountryName(
            context: Context?,
            latitude: Double,
            longitude: Double
        ): String? {
            val address: Address? = getAddress(context, latitude, longitude)
            return if (address == null) "unknown" else address.countryName
        }

        /**
         * 根据经纬度获取所在地
         *
         * @param context   上下文
         * @param latitude  纬度
         * @param longitude 经度
         * @return 所在地
         */
        fun getLocality(
            context: Context?,
            latitude: Double,
            longitude: Double
        ): String? {
            val address: Address? = getAddress(context, latitude, longitude)
            return if (address == null) "unknown" else address.getLocality()
        }

        /**
         * 根据经纬度获取所在街道
         *
         * @param context   上下文
         * @param latitude  纬度
         * @param longitude 经度
         * @return 所在街道
         */
        fun getStreet(
            context: Context?,
            latitude: Double,
            longitude: Double
        ): String? {
            val address: Address? = getAddress(context, latitude, longitude)
            return if (address == null) "unknown" else address.getAddressLine(0)
        }

        private class MyLocationListener : LocationListener {
            /**
             * 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
             *
             * @param location 坐标
             */
            override fun onLocationChanged(location: Location) {
                mListener?.onLocationChanged(location)
            }

            /**
             * provider的在可用、暂时不可用和无服务三个状态直接切换时触发此函数
             *
             * @param provider 提供者
             * @param status   状态
             * @param extras   provider可选包
             */
            override fun onStatusChanged(
                provider: String,
                status: Int,
                extras: Bundle
            ) {
                mListener?.onStatusChanged(provider, status, extras)
                when (status) {
                    LocationProvider.AVAILABLE -> Log.d("onStatusChanged", "当前GPS状态为可见状态")
                    LocationProvider.OUT_OF_SERVICE -> Log.d("onStatusChanged", "当前GPS状态为服务区外状态")
                    LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d(
                        "onStatusChanged",
                        "当前GPS状态为暂停服务状态"
                    )
                }
            }

            /**
             * provider被enable时触发此函数，比如GPS被打开
             */
            override fun onProviderEnabled(provider: String) {}

            /**
             * provider被disable时触发此函数，比如GPS被关闭
             */
            override fun onProviderDisabled(provider: String) {}
        }
    }

    open interface OnLocationChangeListener {
        /**
         * 获取最后一次保留的坐标
         *
         * @param location 坐标
         */
        fun getLastKnownLocation(location: Location?)

        /**
         * 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         *
         * @param location 坐标
         */
        fun onLocationChanged(location: Location?)

        /**
         * provider的在可用、暂时不可用和无服务三个状态直接切换时触发此函数
         *
         * @param provider 提供者
         * @param status   状态
         * @param extras   provider可选包
         */
        fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?
        ) //位置状态发生改变
    }
}