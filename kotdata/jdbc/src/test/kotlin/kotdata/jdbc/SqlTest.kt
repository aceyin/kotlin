package kotdata.jdbc

import kotdata.Entity
import kotdata.jdbc.sql.Sql
import org.junit.Test
import javax.persistence.Column
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Created by ace on 2016/10/11.
 */
class SqlTest {

    @Test
    fun should_generate_insert_sql_success() {
        val sql = Sql.get(User4Sqls::class, Sql.Predefined.insert)
        assertEquals(true, sql?.startsWith("INSERT INTO User4Sqls(id,user_name) VALUES (:id,:name"))
    }

    @Test
    fun should_generate_updateById_sql_success() {
        val sql = Sql.get(User4Sqls::class, Sql.Predefined.updateById)
        assertNotNull(sql)
        assertEquals(true, sql!!.startsWith("UPDATE User4Sqls SET user_name=:name") && sql.endsWith(" WHERE id=:id"))
    }

    @Test
    fun should_generate_deleteById_sql_success() {
        val sql = Sql.get(User4Sqls::class, Sql.Predefined.deleteById)
        assertNotNull(sql)
        assertEquals(sql, "DELETE FROM User4Sqls WHERE id=:id")
    }

    @Test
    fun should_generate_queryById_sql_success() {
        val sql = Sql.get(User4Sqls::class, Sql.Predefined.queryById)
        assertEquals(sql, "SELECT `id` ,`user_name`  FROM User4Sqls WHERE id=:id")
    }
}

data class User4Sqls(@Column(name = "user_name") val name: String, override var id: String) : Entity
