package com.gospell.stall.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class ReflectUtil {
    companion object {
        fun getFields(clazz: Class<*>?): List<Field> {
            //属性集合
            var fieldList: MutableList<Field> = ArrayList()
            //获取实体类属性
            val fields = clazz!!.declaredFields
            fieldList = addFildToList(fieldList, fields)
            //获取父类属性
            if (clazz.superclass is Class<*>) {
                val superFields = clazz.superclass!!.declaredFields
                fieldList = addFildToList(fieldList, superFields)
            }
            return fieldList
        }

        private fun addFildToList(fieldList: MutableList<Field>, fields: Array<Field>): MutableList<Field> {
            for (i in fields.indices) {
                val field = fields[i]
                field.isAccessible = true
                fieldList.add(field)
            }
            return fieldList
        }

        fun getMethods(clazz: Class<*>): List<Method>? {
            //方法集合
            val methodList: MutableList<Method> = ArrayList()
            //获取实体类方法
            val methods = clazz.declaredMethods
            methodList.addAll(listOf(*methods))
            //获取父类方法
            if (clazz.superclass is Class<*>) {
                val superMethods = clazz.superclass!!.methods
                methodList.addAll(listOf(*superMethods))
            }
            return methodList
        }

        fun getGeneric(collection: Collection<*>): Class<*>? {
            return if (collection.isNotEmpty()) {
                collection.iterator().next()!!.javaClass
            } else null
        }

        fun getFields(collection: Collection<*>): List<Field>? {
            return getFields(getGeneric(collection))
        }

        /**
         * @Author peiyongdong
         * @Description ( 初始化指定注解属性并回调 )
         * @Date 16:08 2019/11/11
         * @Param [clazz, annotationClass, onAnnotationCallback]
         * @return void
         */
        fun initFieldByAnnotation(clazz: Class<*>?, annotationClass: Class<*>?, onAnnotationCallback: (annotation: Annotation?, field: Field?) -> Unit) {
            initFieldByAnnotation(clazz, annotationClass, onAnnotationCallback, true)
        }

        /**
         * @Author peiyongdong
         * @Description ( 初始化指定注解属性并回调 )
         * @Date 16:08 2019/11/11
         * @Param [clazz, annotationClass, onAnnotationCallback, isSingle=是否只操作单个属性]
         * @return void
         */
        fun initFieldByAnnotation(clazz: Class<*>?, annotationClass: Class<*>?, onAnnotationCallback: (annotation: Annotation?, field: Field?) -> Unit, isSingle: Boolean) {
            val fieldList = getFields(clazz)
            for (field in fieldList) {
                val annotation = field.annotations.find {it.annotationClass.simpleName == annotationClass?.simpleName}
                if (annotation != null) {
                    onAnnotationCallback.invoke(annotation, field)
                    if (isSingle) {
                        return
                    }
                }
            }
        }

        /**
         * @Author peiyongdong
         * @Description ( 获取泛型class )
         * @Date 16:08 2019/11/11
         * @Param [clazz]
         * @return java.lang.Class
         */
        fun getGenericClass(clazz: Class<*>): Class<*> {
            val genericSuperclass = clazz.genericSuperclass
            return if (genericSuperclass is ParameterizedType) {
                //参数化类型
                //返回表示此类型实际类型参数的 Type 对象的数组
                val actualTypeArguments = genericSuperclass.actualTypeArguments
                actualTypeArguments[0] as Class<*>
            } else {
                genericSuperclass as Class<*>
            }
        }

        /**
         * @Author peiyongdong
         * @Description ( 根据泛型类型创建实体类 )
         * @Date 16:08 2019/11/11
         * @Param [clazz]
         * @return java.lang.Object
         */
        fun createEntityByGeneric(clazz: Class<*>): Any? {
            val tClass = getGenericClass(clazz)
            try {
                return tClass.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}