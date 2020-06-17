package com.gospell.stall.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import java.net.NetworkInterface
import java.net.SocketException

class NetworkUtil {
    companion object {
        /**
         * 检查网络是否可用
         *
         * @param paramContext
         * @return
         */
        fun checkEnable(paramContext: Context): Boolean {
            //boolean i = false;
            val localNetworkInfo =
                (paramContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            return localNetworkInfo != null && localNetworkInfo.isAvailable
        }

        /**
         * 将ip的整数形式转换成ip形式
         *
         * @param ipInt
         * @return
         */
        fun int2ip(ipInt: Int): String? {
            val sb = StringBuilder()
            sb.append(ipInt and 0xFF).append(".")
            sb.append(ipInt shr 8 and 0xFF).append(".")
            sb.append(ipInt shr 16 and 0xFF).append(".")
            sb.append(ipInt shr 24 and 0xFF)
            return sb.toString()
        }

        /**
         * 获取当前ip地址
         *
         * @param context
         * @return
         */
        fun getLocalIpAddress(context: Context): String? {
            return try {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val i = wifiInfo.ipAddress
                int2ip(i)
            } catch (ex: Exception) {
                """ 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!
                    ${ex.message}"""
            }
            // return null;
        }

        //GPRS连接下的ip
        fun getLocalIpAddress(): String? {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr =
                        intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            return inetAddress.hostAddress
                        }
                    }
                }
            } catch (ex: SocketException) {
                Log.e("WifiPreference IpAddress", ex.toString())
            }
            return null
        }

        /**
         * 检查wifi是否处开连接状态
         *
         * @return
         */
        fun isWifiConnect(context: Context): Boolean {
            val connManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return mWifiInfo.isConnected
        }

        fun getWiFiRssiMsg(rssi: Int): String? {
            var msg = ""
            if (rssi >= -50 && rssi <= 0) { //最强
                msg = "最强"
            } else if (rssi >= -70 && rssi <= -50) { //较强
                msg = "较强"
            } else if (rssi >= -80 && rssi <= -70) { //较弱
                msg = "较弱"
            } else if (rssi >= -100 && rssi <= -80) { //微弱
                msg = "微弱"
            }
            return msg
        }

        fun getCurrentWiFiInfo(context: Context): WifiInfo? {
            if (isWifiConnect(context)) {
                val mWifiManager = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                return mWifiManager.connectionInfo
            }
            return null
        }
    }
}