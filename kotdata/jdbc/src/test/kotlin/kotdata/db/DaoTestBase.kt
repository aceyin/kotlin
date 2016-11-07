package kotdata.db

import org.h2.jdbcx.JdbcConnectionPool
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.StringReader
import java.sql.Connection

/**
 * 基于H2数据库测试用例的基类
 */


open class DaoTestBase() {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DaoTestBase::class.java)
        val connectionPool: JdbcConnectionPool
        val datasource: JdbcDataSource

        init {
            datasource = JdbcDataSource()
            with(datasource) {
                datasource.setURL("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;ALIAS_COLUMN_NAME=true;DB_CLOSE_DELAY=-1;MODE=MYSQL")
                datasource.user = "sa"
                datasource.password = "sa"
            }
            connectionPool = JdbcConnectionPool.create(datasource)
        }

        /**
         * 获取一个数据库连接,默认自动提交
         */
        fun getConn(autoCommit: Boolean = true): Connection {
            connectionPool.let {
                val conn = connectionPool.connection
                conn.autoCommit = autoCommit
                return conn
            }
        }

        /**
         * 从指定的目录读取sql文件，初始化数据表
         */
        fun createTables(dir: String) {
            val folder = File(dir)
            if (!folder.exists()) throw FileNotFoundException("File not found '$dir'")

            val conn = getConn()
            if (folder.isDirectory) {
                val sqls = folder.listFiles { file, s -> s.toLowerCase().endsWith(".sql") }
                if (sqls.isEmpty()) LOGGER.warn("No SQL file found under dir '$dir'")
                else {
                    sqls.forEach { s -> RunScript.execute(conn, s.bufferedReader()) }
                }
            } else {
                if (dir.toLowerCase().endsWith(".sql")) {
                    RunScript.execute(conn, File(dir).bufferedReader())
                }
            }
        }

        /**
         * 从给定的SQL创建表
         */
        fun createTable(sql: String) {
            val conn = getConn()
            RunScript.execute(conn, StringReader(sql))
        }

        /**
         * 执行一个SQL查询
         */
        fun query(sql: String, params: Map<String, Any> = emptyMap()): List<Map<String, Any>> {
            val pst = getConn().prepareStatement(sql)
            //TODO: 将params设置到查询里面
            val data = mutableListOf<Map<String, Any>>()
            val rs = pst.executeQuery()
            val meta = rs.metaData ?: throw RuntimeException("No metadata found for sql:${sql}")

            while (rs != null && rs.next()) {
                val m = mutableMapOf<String, Any>()
                for (i in 1..meta.columnCount) {
                    val name = meta.getColumnName(i)
                    m[name] = rs.getString(name) ?: ""
                }
                data.add(m)
            }
            rs.close()
            return data
        }
    }
}