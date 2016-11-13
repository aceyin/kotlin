package kotun.support.cfg

import java.util.*


/**
 * The config of application
 */
object Config {

    val PROPS = mutableMapOf<String, Any>()
    /**
     * 获取一个字符串列表
     *
     * @param key
     * @return
     */
    fun strs(key: String): List<String> {
        val o = PROPS[key] ?: return emptyList()
        val res = mutableListOf<String>()
        when (o) {
            is ArrayList<*> -> o.forEach {
                if (it is String) res.add(it)
                else throw RuntimeException("Value $it for key $key is not a string ")
            }
            is String -> res.add(o)
            else -> throw RuntimeException("Value $o for key $key is not a string ")
        }
        return res
    }

    /**
     * 获取单个字符串配置
     */
    fun str(key: String): String? {
        val v = PROPS[key] ?: return null
        if (v is String) return v
        else throw RuntimeException("Value $v for key $key is not a string ")
    }

    /**
     * 获取单个整形配置
     */
    fun int(key: String): Int? {
        val v = PROPS[key] ?: return null
        if (v is Int) return v
        else if (v is String && v.matches(Regex("\\d+"))) return v.toInt()
        else throw RuntimeException("Value $v for key $key is not a Int ")
    }

    /**
     * 获取整形配置列表
     */
    fun ints(key: String): List<Int>? {
        val v = PROPS[key] ?: return null
        if (v is Int) return listOf(v)
        else if (v is ArrayList<*>) {
            val list = mutableListOf<Int>()
            for (i in v) {
                if (i is Int) list.add(i)
                else if (i is String && i.matches(Regex("\\d+"))) list.add(i.toInt())
                else throw RuntimeException("Value $v for key $key is not a Int ")
            }
            return list
        } else throw RuntimeException("Value $v for key $key is not a int list")
    }

    /**
     * 获取boolean值
     * @param key
     * @return
     */
    fun bool(key: String): Boolean? {
        val o = PROPS.get(key) ?: return null
        if (o is Boolean) return o
        else if (o is String) return o.toBoolean()
        else throw RuntimeException("Value of '$key' is not a boolean type: $o")
    }

    /**
     * 获取boolean列表
     * @param key
     * @return
     */
    fun bools(key: String): List<Boolean> {
        val o = PROPS.get(key) ?: return emptyList<Boolean>()
        if (o is ArrayList<*>) {
            val res = mutableListOf<Boolean>()
            o.forEach { i ->
                if (i is Boolean) res.add(i)
                else if (i is String) res.add(i.toBoolean())
                else throw RuntimeException("Value of '$key' is not a valid boolean: $i")
            }
            return res
        } else if (o is String) return listOf(o.toBoolean())
        else
            throw RuntimeException("Value of '$key' is not a valid boolean: $o")
    }

    /**
     * 获取float值
     * @param key
     * @return
     */
    fun float(key: String): Float? {
        val o = PROPS.get(key) ?: return null
        if (o is Float) return o
        else if (o is String) return o.toFloat()
        else throw RuntimeException("Value of '$key' is not a valid number format: $o")
    }

    /**
     * get float list
     * @param key
     * @return
     */
    fun floats(key: String): List<Float> {
        val o = PROPS.get(key) ?: return emptyList()
        if (o is ArrayList<*>) {
            val res = mutableListOf<Float>()
            o.forEach { i ->
                if (i is Float) res.add(i)
                else if (i is String) res.add(i.toFloat())
                else throw RuntimeException("Value of '$key' is not a valid number format: $i")
            }
            return res
        } else if (o is String) return listOf(o.toFloat())
        throw RuntimeException("Value of '$key' is not a valid number format: $o")
    }

    /**
     * Check if the specified key is in the config
     */
    fun has(key: String): Boolean {
        return PROPS.containsKey(key)
    }

    fun map(key: String): Map<*, *> {
        val v = PROPS[key] ?: return emptyMap<String, Any>()
        if (v is Map<*, *>) return v
        else return mapOf(key to v)
    }

    fun list(key: String): List<*> {
        val v = PROPS[key] ?: return emptyList<Any>()
        if (v is List<*>) return v
        else return listOf(v)
    }
}