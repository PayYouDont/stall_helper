package com.gospell.stall

import com.gospell.stall.entity.User

class Constants {
    companion object {
        var user: User? = null;
        var token :String? = "";
        const val APP_ID = "wx1094c1641cc70b55"
        const val APP_SECRET = "eb814d999f9544e1daf78287bdb0e9e4"
        //云
        const val baseUrl = "http://106.13.212.169:8080/"
        //const val baseUrl = "http://192.168.1.161:8080/"
        //const val baseUrl = "http://10.0.2.2:8080/"
        //注册路径
        const val registerUrl = baseUrl+"app/user/register"
        //检查账号是否可以用路径
        const val checkAccountUrl = baseUrl+"app/user/check"
        //登录路径
        const val loginUrl = baseUrl+"app/user/login"
        //更新用户信息路径
        const val updateUrl = baseUrl+"app/user/update"
        //上传地摊信息路径
        const val updateStallUrl = baseUrl+"app/user/updateStall"
        //手机地图可视范围内数据请求路径
        const val getNearStallUrl = baseUrl+"app/user/near"
        //头像上传请求路径
        const val avatarUploadUrl = baseUrl+"app/user/upload"
        //获取最新版本信息
        const val lastVersionUrl = baseUrl+"app/version/last"
        //获取指定版本信息
        const val versionUrl = baseUrl+"app/version/get?code="
        //获取指定版本信息
        const val versionDownloadUrl = baseUrl+"app/version/download?code="
    }
}