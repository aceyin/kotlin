package kotdata.jdbc

import kotdata.jdbc.sql.Loader
import kotdata.jdbc.sql.Sql
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by ace on 2016/10/14.
 */
class SqlLoaderTest {

    @Test
    fun test_load_sql() {
        Loader.load("test_sqls.yml")
        val sql1 = Sql.get("abc.def.ghi#insert")
        assertEquals("insert into user (id,name) values (:id,:name)", sql1)

        val sql2 = Sql.get("abc.def.ghi#updateById")
        assertEquals("update user set name=:name where id=:id", sql2)

        val sql3 = Sql.get("some.sql.not.applied.on.entity")
        assertEquals("select x", sql3)

        val sql4 = Sql.get("multiple.line.sql")
        assertEquals("select x from y where z group by a order by a limit 0,1", sql4)

        val sql5 = Sql.get("report.sqls#sql.1")
        assertEquals("select count * from x", sql5)

        val sql6 = Sql.get("report.sqls#sql.2")
        assertEquals("select 1 from x", sql6)

        val sql7 = Sql.get("multile.level.sql#level1#sql1")
        assertEquals("select a from b", sql7)

        val sql8 = Sql.get("multile.level.sql#level1#sql2")
        assertEquals("select c from d", sql8)

        val sql9 = Sql.get("multile.level.sql#level2#level3#level4#sql4")
        assertEquals("select 4 from 5", sql9)
    }
}