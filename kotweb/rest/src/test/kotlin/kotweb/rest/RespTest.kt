package kotweb.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by ace on 2016/11/13.
 */

class RespTest {

    val MAPPER = jacksonObjectMapper()

    @Test
    fun test_create_def_resp() {
        var res = Resp.succ()

        var actual = MAPPER.writeValueAsString(res)
        var expected = """{"head":{"code":0,"desc":""},"body":{}}""".trimMargin()
        assertEquals(expected, actual)

        res = Resp.fail()
        actual = MAPPER.writeValueAsString(res)
        expected = """{"head":{"code":1,"desc":""},"body":{}}""".trimMargin()
        assertEquals(expected, actual)
    }

    @Test
    fun test_create_resp_with_data() {
        val bean = Bean4Test(1, "abc")
        var res = Resp.succ(bean)
        var actual = MAPPER.writeValueAsString(res)
        var expected = """{"head":{"code":0,"desc":""},"body":{"id":1,"name":"abc"}}""".trimMargin()
        assertEquals(expected, actual)

        res = Resp.fail(bean)
        actual = MAPPER.writeValueAsString(res)
        expected = """{"head":{"code":1,"desc":""},"body":{"id":1,"name":"abc"}}""".trimMargin()
        assertEquals(expected, actual)
    }

    @Test
    fun test_create_with_list_data() {
        val list = listOf(Bean4Test(1, "abc"), Bean4Test(2, "def"))
        val res = Resp.succ(list)
        val actual = MAPPER.writeValueAsString(res)
        val expected = """{"head":{"code":0,"desc":""},"body":[{"id":1,"name":"abc"},{"id":2,"name":"def"}]}""".trimMargin()
        assertEquals(expected, actual)
    }

    @Test
    fun test_create_with_map_data() {
        val list = listOf(Bean4Test(1, "abc"), Bean4Test(2, "def"))
        val res = Resp.succ(mapOf("users" to list, "list2" to listOf(1, 2, 3)))
        val actual = MAPPER.writeValueAsString(res)
        val expected = """{"head":{"code":0,"desc":""},"body":{"users":[{"id":1,"name":"abc"},{"id":2,"name":"def"}],"list2":[1,2,3]}}""".trimMargin()
        assertEquals(expected, actual)
    }

    @Test
    fun test_create_with_complex_object() {
        val page = Page<Bean4Test>(292, 10, 10, listOf(Bean4Test(1, "abc"), Bean4Test(2, "def")))
        val res = Resp.succ(page)
        val actual = MAPPER.writeValueAsString(res)
        val expected = """{"head":{"code":0,"desc":""},"body":{"rowNum":292,"pageNum":10,"currentPage":10,"pageData":[{"id":1,"name":"abc"},{"id":2,"name":"def"}]}}""".trimMargin()
        assertEquals(expected, actual)
    }
}

private data class Bean4Test(val id: Int, val name: String)

private data class Page<out E : Any>(
        val rowNum: Int = 0,
        val pageNum: Int = 0,
        val currentPage: Int = 0,
        val pageData: List<E>) {
    companion object {
        fun <E : Any> empty() = Page<E>(0, 0, 0, emptyList())
    }
}