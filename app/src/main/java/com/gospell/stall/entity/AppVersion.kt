package com.gospell.stall.entity

import java.util.*

class AppVersion {
    var id: Int? = null
    //版本名称
    var name:String?=null
    //版本号
    var code:Int?=null
    //更新日志
    var log:String?=null
    //安装包请求路径
    var apkUrl:String?=null
    //安装包大小
    var apkSize:Long?=null
    //是否强制更新
    val forcedUpdate:Boolean?= false
    //是否可以忽略
    val ignorable:Boolean?= false
    //MD5用于验证安装包的完整性
    val md5: String? = null
    //发布日期
    var createTime: Date? = null
}