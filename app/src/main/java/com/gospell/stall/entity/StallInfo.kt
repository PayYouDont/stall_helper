package com.gospell.stall.entity

import java.util.*

class StallInfo {
    var id:Int?=null
    //店铺名称
    var name: String? = null
    //店铺简介
    var content: String? = null
    //经度
    var latitude:Double?=null
    //纬度
    var longitude:Double?=null
    //店铺logo
    var logoImgUrl: String? = null
    //店铺状态(0在线，1离线，-1不可用)
    var status:Int? = null
    //创建时间
    var createDate:Date?=null
    //修改时间
    var updateDate:Date?=null
}