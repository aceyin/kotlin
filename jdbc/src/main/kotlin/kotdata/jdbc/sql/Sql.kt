package kotdata.jdbc.sql

import kotdata.Entity
import kotdata.EntityMeta
import kotlin.reflect.KClass

/**
 * Created by ace on 2016/10/11.
 */
object Sql {

    /**
     * 预定义的一些SQL - 即可以根据 Entity 的注解生成的一些SQL。
     */
    enum class Predefined {
        insert, updateById, deleteById, queryById, queryByIds, count, queryAll, deleteAll, truncate
    }

    /* SQL cache */
    internal val CACHE = mutableMapOf<String, String>()

    /**
     * 从配置文件加载一个SQL
     * @param key 配置文件中的sql语句的ID
     */
    fun get(key: String): String? {
        return CACHE[key]
    }

    /**
     * 根据 entity 的类型获取一个predefine的 sql
     * @param clazz  entity 的类型
     * @param type 预定义的一些sql
     * @return 预定义的sql语句
     */
    fun get(clazz: KClass<out Any>, type: Predefined): String? {
        val id = keyOf(clazz, type)
        return get(id) ?: load(clazz, type)
    }

    /**
     * 往 cache 中手动添加一个sql
     */
    internal fun add(key: String, sql: String) {
        this.CACHE[key] = sql
    }

    /**
     * 根据Entity类和查询类型，获取一个sql id
     */
    private fun keyOf(clazz: KClass<out Any>, type: Predefined): String = "${clazz.qualifiedName}#${type.name}"

    /**
     * 从一个实体类中获取SQL
     */
    private fun load(clazz: KClass<out Any>, type: Predefined): String? {
        var sql: String? = null
        when (type) {
            Predefined.insert -> sql = insertSql(clazz)
            Predefined.updateById -> sql = updateByIdSql(clazz)
            Predefined.deleteById -> sql = deleteById(clazz)
            Predefined.queryById -> sql = queryById(clazz)
            Predefined.queryByIds -> sql = queryByIds(clazz)
            Predefined.count -> sql = count(clazz)
            Predefined.queryAll -> sql = queryAll(clazz)
            Predefined.deleteAll -> sql = deleteAll(clazz)
            Predefined.truncate -> sql = truncate(clazz)
        }
        if (sql.isNotBlank()) CACHE.put(keyOf(clazz, type), sql)
        return sql
    }

    /**
     * 生成 truncate 语句
     */
    private fun truncate(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        return "TRUNCATE TABLE ${meta.table}"
    }

    /**
     * 生成delete all sql 语句
     */
    private fun deleteAll(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        return "DELETE FROM ${meta.table}"
    }

    /**
     * 生成 select all SQL
     */
    private fun queryAll(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        val (table, cols) = tableCols(meta, emptyList(), "")
        val columns = Array<String>(cols.size) { "`" + cols[it] + "` " }
        return "SELECT ${columns.joinToString(",")} FROM $table"
    }

    /**
     * 生成Count sql
     */
    private fun count(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        return "SELECT COUNT(1) as num FROM ${meta.table}"
    }

    /**
     * 生成 query by ids 的sql
     */
    private fun queryByIds(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        val (table, cols) = tableCols(meta, emptyList(), "")
        val columns = Array<String>(cols.size) { "`" + cols[it] + "` " }
        val id = Entity::id.name
        return "SELECT ${columns.joinToString(",")} FROM $table WHERE $id in (:$id)"
    }

    /**
     * 生成 queryById 的 sql
     */
    private fun queryById(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        val (table, cols) = tableCols(meta, emptyList(), "")
        val columns = Array<String>(cols.size) { "`" + cols[it] + "` " }
        val id = Entity::id.name
        return "SELECT ${columns.joinToString(",")} FROM $table WHERE $id=:$id"
    }

    /**
     * 生成 deleteById 的 sql
     */
    private fun deleteById(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        val id = Entity::id.name
        return "DELETE FROM ${meta.table} WHERE $id=:$id"
    }

    /**
     * 生成 update by id 的sql
     */
    private fun updateByIdSql(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)
        val id = Entity::id.name
        val exclusion = arrayOf(id)
        val (table, cols, params) = tableColsProps(meta, exclusion.toList())
        val columns = Array<String>(cols.size) {
            cols[it] + "=" + params[it]
        }.joinToString(",")
        return "UPDATE $table SET $columns WHERE $id=:$id"
    }

    /**
     * 生成 insert sql语句
     */
    private fun insertSql(clazz: KClass<out Any>): String {
        val meta = EntityMeta.get(clazz)

        val (table, cols, params) = tableColsProps(meta)
        val v = params.joinToString(",")
        val c = cols.joinToString(",")
        return "INSERT INTO $table($c) VALUES ($v)"
    }

    /**
     * 从 meta 中获取数据库的：表名，字段名 以及 字段对应的entity的属性名
     * @param meta entity 的 metadata
     * @param exclusion 需要被过滤掉的属性
     * @param propPrefix 属性名前缀。默认属性名前面都带了冒号(:)以方便NamedPreparedStatement设置数据
     * @return Triple(表名,字段名数组,属性名数组)。
     */
    private fun tableColsProps(meta: EntityMeta, exclusion: List<String> = emptyList(), propPrefix: String = ":"): Triple<String, Array<String>, Array<String>> {
        val table = meta.table
        val cols = mutableListOf<String>()
        val params = mutableListOf<String>()

        meta.attr2col.entries.forEachIndexed { i, entry ->
            val prop = entry.key.name
            if (!exclusion.contains(prop)) {
                cols.add(entry.value)
                params.add("$propPrefix$prop")
            }
        }
        return Triple(table, cols.toTypedArray(), params.toTypedArray())
    }

    private fun tableCols(meta: EntityMeta, exclusion: List<String> = emptyList(), propPrefix: String = ":"): Pair<String, Array<String>> {
        val table = meta.table
        val cols = mutableListOf<String>()

        meta.attr2col.entries.forEachIndexed { i, entry ->
            val prop = entry.key.name
            if (!exclusion.contains(prop)) {
                cols.add(entry.value)
            }
        }
        return Pair(table, cols.toTypedArray())
    }

}