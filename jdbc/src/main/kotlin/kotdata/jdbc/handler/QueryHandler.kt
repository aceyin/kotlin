package kotdata.jdbc.handler

import jodd.bean.BeanUtil
import kotdata.Entity
import kotdata.EntityMeta
import kotdata.jdbc.Page
import kotdata.jdbc.sql.Sql
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import kotlin.reflect.KClass

/**
 * Created by ace on 2016/10/17.
 */

internal class QueryHandler(val template: NamedParameterJdbcTemplate) {

    private val LOGGER = LoggerFactory.getLogger(QueryHandler::class.java)

    /**
     * 根据id查询一个对象。
     * @param id 对象id
     * @param clazz 需要被查询的对象类型
     */
    fun <E : Entity> queryById(clazz: KClass<E>, id: String): E? {
        val sql = Sql.get(clazz, Sql.Predefined.queryById)
        val meta = EntityMeta.get(clazz)

        try {
            val map = template.queryForMap(sql, mapOf(Entity::id.name to id))
            if (map == null || map.isEmpty()) return null

            return toBean(clazz, map, meta)
        } catch (e: EmptyResultDataAccessException) {
            LOGGER.warn("No entity '$clazz' with id=$id found")
            return null
        } catch (e: IncorrectResultSizeDataAccessException) {
            LOGGER.warn("Too many entity '$clazz' with id=$id found, excepted=${e.expectedSize}, actual=${e.actualSize}")
            return null
        }
    }

    /**
     * 根据给定的id列表，查询entities
     * @param clazz 需要被查询的实体类
     * @param ids 给定的id列表
     */
    fun <E : Entity> queryByIds(clazz: KClass<E>, ids: List<String>): List<E> {
        if (ids.isEmpty()) return emptyList()
        val sql = Sql.get(clazz, Sql.Predefined.queryByIds)
        if (sql.isNullOrEmpty()) return emptyList()

        val list = template.queryForList(sql, mapOf(Entity::id.name to ids))
        if (list == null || list.isEmpty()) return emptyList()

        val meta = EntityMeta.get(clazz)
        val result = mutableListOf<E>()

        list.forEach { map ->
            result.add(toBean(clazz, map, meta))
        }
        return result
    }

    /**
     * 根据给定的id列表，查询entities
     * @param clazz 需要被查询的实体类
     * @param ids 给定的id列表
     */
    fun <E : Entity> queryByIds(clazz: KClass<E>, vararg ids: String): List<E> {
        if (ids.isEmpty()) return emptyList()
        return queryByIds(clazz, ids.toList())
    }

    /**
     * 根据给定条件查询对象。
     * @param sql 查询SQL语句的ID
     * @param clazz 被查询的对象类型
     * @param params 查询参数。
     *        如果要使用 in 语法(select x from y where z in (:z)
     *        那么需要保证param里面的参数"z"的类型为 List 类型
     */
    fun <E : Entity> query(clazz: KClass<E>, sql: String, params: Map<String, Any>): List<E> {
        if (sql.isNullOrEmpty()) {
            LOGGER.warn("No sql with id '$sql' found")
            return emptyList()
        }
        val list = template.queryForList(sql, params)
        if (list == null || list.isEmpty()) return emptyList()

        val result = mutableListOf<E>()
        val meta = EntityMeta.get(clazz)
        list.forEach { map ->
            result.add(toBean(clazz, map, meta))
        }

        return result
    }

    /**
     * 根据给定条件查询对象。
     * @param sql 查询SQL语句的ID
     * @param entity 查询参数
     */
    fun <E : Entity> query(sql: String, entity: E): List<E> {
        if (sql.isNullOrEmpty()) return emptyList()
        val list = template.queryForList(sql, BeanPropertySqlParameterSource(entity))
        if (list == null || list.isEmpty()) return emptyList()

        val result = mutableListOf<E>()
        val clazz = entity.javaClass.kotlin
        val meta = EntityMeta.get(clazz)

        list.forEach {
            result.add(toBean(clazz, it, meta))
        }

        return result
    }

    /**
     * 根据条件进行翻页查询。
     * @param clazz 要查询的entity的类型
     * @param sqlId 查询语句
     * @param param 查询参数
     * @param page 查询第几页的数据
     * @param pageSize 每页数据条数
     * @return 翻页数据
     */
    fun <E : Entity> pageQuery(clazz: KClass<E>, page: Int, pageSize: Int): Page<E> {
        if (page < 1 || pageSize <= 0) return Page.empty()

        val count = Sql.get(clazz, Sql.Predefined.count)
        val select = Sql.get(clazz, Sql.Predefined.queryAll)

        val num = template.queryForObject(count, emptyMap<String, Any>(), Int::class.java)
        if (num == 0) return Page.empty()

        val limit = limitClause(page, pageSize)
        val list = template.queryForList("$select $limit", emptyMap<String, Any>())

        val meta = EntityMeta.get(clazz)
        val result = mutableListOf<E>()
        list.forEach {
            result.add(toBean(clazz, it, meta))
        }

        val pages = pageCount(num, pageSize)
        return Page(num, pages, page, result)
    }

    /**
     * 根据条件进行翻页查询。
     * @param clazz 要查询的entity的类型
     * @param sql 查询语句
     * @param param 查询参数
     * @param page 查询第几页的数据
     * @param pageSize 每页数据条数
     * @return 翻页数据
     */
    fun <E : Entity> pageQuery(clazz: KClass<E>, sql: String, param: Map<String, Any>, page: Int, pageSize: Int): Page<E> {
        if (page < 1 || pageSize <= 0) return Page.empty()

        if (sql.isNullOrEmpty()) throw IllegalArgumentException("NO SQL with ID:'$sql' found")

        val table = "_PQ_TMP_${(Math.random() * 1000000).toInt()}"

        val countSql = "SELECT count(1) as dataCount from ($sql) $table"

        val num = template.queryForObject(countSql, param, Int::class.java)
        if (num == 0) return Page.empty()

        // query data
        val limit = limitClause(page, pageSize)
        val newSql = "SELECT * FROM ($sql) $table $limit"

        val list = template.queryForList(newSql, param)

        val result = mutableListOf<E>()
        val meta = EntityMeta.get(clazz)
        list.forEach {
            result.add(toBean(clazz, it, meta))
        }
        val pages = pageCount(num, pageSize)
        return Page(num, pages, page, result)
    }

    fun pageCount(num: Int, pageSize: Int): Int {
        return if (num % pageSize > 0) num / pageSize + 1 else num / pageSize
    }

    fun limitClause(page: Int, pageSize: Int) = "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"

    /**
     * 将一个JdbcTemplate返回的Map转换为JavaBan
     */
    fun <E : Entity> toBean(clazz: KClass<E>, map: MutableMap<String, Any>, meta: EntityMeta): E {
        val bean = clazz.java.newInstance()
        map.forEach {
            val prop = meta.prop(it.key)
            BeanUtil.pojo.setProperty(bean, prop, it.value)
        }
        return bean
    }

}