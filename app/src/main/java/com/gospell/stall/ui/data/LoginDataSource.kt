package com.gospell.stall.ui.data

import com.gospell.stall.Constants
import com.gospell.stall.entity.User
import com.gospell.stall.ui.data.model.LoggedInUser
import com.gospell.stall.util.HttpUtil
import com.gospell.stall.util.JsonUtil
import org.json.JSONObject
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private var resultMsg = ""
    @Throws(Exception::class)
    fun login(username: String, password: String): Result<LoggedInUser> {
        var map = mutableMapOf<String,Any>()
        map["account"] = username
        map["password"] = password
        HttpUtil.post(Constants.loginUrl,map) {
                response->
            try {
                var result = response.body!!.string()
                var json = JSONObject(result)
                if(json.getBoolean("success")){
                    Constants.user = JsonUtil.toBean(json.getString("user"),User::class.java)
                    Constants.token = json.getString("token")
                    resultMsg = "ok"
                }else{
                    resultMsg = json.getString("msg")
                }
            }catch (e:Exception){
                resultMsg = e.message!!
            }
        }
        return try {
            while (resultMsg==""){}
            if(resultMsg=="ok"){
                val fakeUser = LoggedInUser(Constants.user!!.id.toString(), Constants.user!!.nickname!!)
                Result.Success(fakeUser)
            }else{
                Result.Error(RuntimeException(resultMsg))
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}