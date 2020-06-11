package com.gospell.stall

import com.gospell.stall.entity.User

class Constants {
    companion object {
        var user: User? = null;
        var token :String? = "";
        const val APP_ID = "wx1094c1641cc70b55"
        const val APP_SECRET = "eb814d999f9544e1daf78287bdb0e9e4"
        const val baseUrl = "http://10.0.2.2:8080/"
        const val registerUrl = baseUrl+"app/user/register"
        const val checkAccountUrl = baseUrl+"app/user/check"
        const val loginUrl = baseUrl+"app/user/login"
    }
}