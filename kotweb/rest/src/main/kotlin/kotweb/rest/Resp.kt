package kotweb.rest

/**
 * 返回给客户端的Response数据的封装.
 */
class Resp private constructor(val head: Head) {

    constructor(head: Head, data: Any?) : this(head) {
        this.body = data ?: emptyMap<String, String>()
    }

    var body: Any? = emptyMap<String, String>()

    companion object Builder {
        /**
         * 创建一个代表成功的 response。
         */
        fun succ(): Resp = Resp(Head.success)

        /**
         * 用指定的data创建response对象。
         */
        fun succ(data: Any?): Resp = Resp(Head.success, data)

        /**
         * 创建一个代表失败的 response。
         */
        fun fail(): Resp = Resp(Head.fail)

        /**
         * 用指定的data创建一个response对象
         */
        fun fail(data: Any?): Resp = Resp(Head.fail, data)
    }
}

/**
 * HTTP响应消息的消息头。
 */
data class Head(val code: Int, val desc: String = "") {
    companion object Builder {
        /* 成功的状态码 */
        val SUCC_CODE = 0
        /* 失败的状态码 */
        val FAIL_CODE = 1

        /* 默认的成功 head */
        val success = Head(SUCC_CODE)
        /* 默认的失败的 head */
        val fail = Head(FAIL_CODE)

        fun succ(message: String): Head = Head(SUCC_CODE, message)

        fun fail(message: String): Head = Head(FAIL_CODE, message)
    }
}
