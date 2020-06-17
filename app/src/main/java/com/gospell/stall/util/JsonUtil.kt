package com.gospell.stall.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer
import com.alibaba.fastjson.serializer.SerializeConfig
import com.alibaba.fastjson.serializer.SerializerFeature
import java.sql.Date

class JsonUtil {
    companion object {
        private var config: SerializeConfig? = null
        init {
            config = SerializeConfig()
            config!!.put(java.util.Date::class.java, JSONLibDataFormatSerializer()) // 使用和json-lib兼容的日期输出格式
            config!!.put(Date::class.java, JSONLibDataFormatSerializer()) // 使用和json-lib兼容的日期输出格式
        }

        private val features = arrayOf(SerializerFeature.WriteMapNullValue,  // 输出空置字段
                SerializerFeature.WriteNullListAsEmpty,  // list字段如果为null，输出为[]，而不是null
                SerializerFeature.WriteNullNumberAsZero,  // 数值字段如果为null，输出为0，而不是null
                SerializerFeature.WriteNullBooleanAsFalse,  // Boolean字段如果为null，输出为false，而不是null
                SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null
        )

        /**
         * 将对象的所有字段转换为json
         *
         * @return
         * @Author peiyongdong
         * @Description (将对象的所有字段转换为json)
         * @Date 15:18 2020/5/19
         * @Param
         */
        fun toJsonFeatures(any: Any?): String {
            return JSON.toJSONString(any, config, *features)
        }

        /**
         * 将对象的非空字段转换为json
         *
         * @return
         * @Author peiyongdong
         * @Description (将对象的非空字段转换为json)
         * @Date 15:18 2020/5/19
         * @Param
         */
        fun toJson(any: Any?): String? {
            return JSON.toJSONString(any, config)
        }


        fun toBean(text: String?): Any? {
            return JSON.parse(text)
        }

        fun <T> toBean(text: String?, clazz: Class<T>?): T {
            return JSON.parseObject(text, clazz)
        }

        /**
         * 转换为数组
         *
         * @param text
         * @return
         */
        fun <T> toArray(text: String?): Array<Any?>? {
            return toArray<Any>(text, null)
        }

        /**
         * 转换为数组
         *
         * @param text
         * @param clazz
         * @return
         */
        fun <T> toArray(text: String?, clazz: Class<T>?): Array<Any?>? {
            return JSON.parseArray(text, clazz).toTypedArray()
        }

        /**
         * 转换为List
         *
         * @param text
         * @param clazz
         * @return
         */
        fun <T> toList(text: String?, clazz: Class<T>?): List<T>? {
            return JSON.parseArray(text, clazz)
        }

        /**
         * 将string转化为序列化的json字符串
         *
         * @return
         */
        fun textToJson(text: String?): Any? {
            return JSON.parse(text)
        }

        /**
         * json字符串转化为map
         *
         * @param s
         * @return
         */
        fun <K, V> stringToCollect(s: String?): Map<K, V>? {
            var jsonObject = JSONObject.parseObject(s)
            jsonObject.remove("saved")
            return jsonObject as Map<K, V>
        }

        /**
         * 转换JSON字符串为对象
         *
         * @param jsonData
         * @param clazz
         * @return
         */
        fun convertJsonToObject(jsonData: String?, clazz: Class<*>?): Any? {
            return JSONObject.parseObject(jsonData, clazz)
        }

        /**
         * 将map转化为string
         *
         * @param map
         * @return
         */
        fun <K, V> collectToString(map: Map<K, V>?): String? {
            return JSONObject.toJSONString(map)
        }
    }
}