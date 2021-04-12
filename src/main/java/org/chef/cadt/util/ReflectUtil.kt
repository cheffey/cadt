package org.chef.cadt.util

/**
 * Created by Chef.Xie
 */
object ReflectUtil {
    fun setValue(target: Any, fieldName: String, value: Any?) {
        var clazz: Class<*>? = target.javaClass
        while (clazz != null) {
            val fields = clazz.declaredFields
            fields.firstOrNull { it.name == fieldName }?.let {
                it.isAccessible = true
                it.set(target, value)
                return
            }
            clazz = clazz.superclass
        }
        throw NoSuchFieldException("target: $target, with type: ${target.javaClass} does NOT have field named: $fieldName")
    }

    @JvmStatic
    fun getValue(target: Any, fieldName: String): Any? {
        var clazz: Class<*>? = target.javaClass
        while (clazz != null) {
            val fields = clazz.declaredFields
            fields.firstOrNull { it.name == fieldName }?.let {
                it.isAccessible = true
                return it.get(target)
            }
            clazz = clazz.superclass
        }
        throw NoSuchFieldException("target: $target, with type: ${target.javaClass} does NOT have field named: $fieldName")
    }
}