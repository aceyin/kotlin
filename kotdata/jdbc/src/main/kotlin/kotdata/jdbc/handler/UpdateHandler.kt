package kotdata.jdbc.handler

import kotdata.Entity
import kotdata.jdbc.sql.Sql
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * Created by ace on 2016/10/17.
 */
internal class UpdateHandler(val template: NamedParameterJdbcTemplate) {

    /**
     * 根据给定的ID，将entity对象中所有不为空和默认值的数据更新到数据库
     * @param id 对象id
     * @param entity 需要被更新的数据。
     */
    fun <E : Entity> updateById(id: String, entity: E): Boolean {
        if (id.isNullOrBlank()) return false
        val sql = Sql.get(entity.javaClass.kotlin, Sql.Predefined.updateById)
        val n = template.update(sql, BeanPropertySqlParameterSource(entity))
        return n == 1
    }

    /**
     * 根据给定的SQL，更新entity中的数据到数据库。
     * @param sql 给定的SQL语句的id。sql语句在 sqls.yml中配置
     * @param entity 需要更新的数据。update语句中的 set x=y 和 where a=b 中需要的数据都从 entity 中获取
     */
    fun <E : Entity> update(sql: String, entity: E): Boolean {
        if (sql.isNullOrEmpty()) return false
        val n = template.update(sql, BeanPropertySqlParameterSource(entity))
        return n > 0
    }


    /**
     * 根据给定的SQL和参数，更新数据库的数据。
     * @param sql 数据更新语句
     * @param param sql语句的参数,包括要更新的数据和更新的查询条件中的参数。如果sql语句中不需要参数，则该参数可以不填
     * 注：如果查询条件和被更新的数据中含有相同的参数，则更新可能会失败。
     * 例如：update user set name=:name where name=:name
     *        如果要使用 in 语法(update t set x=:x, y=:y where z in (:z)
     *        那么需要保证param里面的参数"z"的类型为 List 类型
     */
    fun update(sql: String, param: Map<String, Any> = emptyMap()): Boolean {
        if (sql.isNullOrEmpty()) return false
        val n = template.update(sql, param)
        return n > 0
    }

    /**
     * 批量更新数据。
     * 注：entity只能为同一种类型。
     * @param sql 数据库更新语句Id
     * @param entities 需要被更新的数据
     */
    fun <E : Entity> batchUpdate(sql: String, entities: List<E>): Boolean {
        val size = entities.size
        if (sql.isNullOrEmpty() || size == 0) return false
        val params = Array(size) {
            BeanPropertySqlParameterSource(entities[it])
        }
        val n = template.batchUpdate(sql, params)
        return n.sum() == size
    }

    /**
     * 批量更新entity
     * @param sqlId 数据库更新预计的id
     * @param entity 需要被更新的entities
     */
    fun <E : Entity> batchUpdate(sqlId: String, vararg entity: E): Boolean {
        if (entity.isEmpty()) return false
        return batchUpdate(sqlId, entity.asList())
    }

    /**
     * 批量更新数据。
     * @param sqlId SQLID
     * @param param sql参数
     *        如果要使用 in 语法(update t set x=:x,y=:y where z in (:z)
     *        那么需要保证param里面的参数"z"的类型为 List 类型
     */
    fun batchUpdate(sql: String, param: Array<out Map<String, Any>>): Boolean {
        if (param.isEmpty()) return false
        if (sql.isNullOrEmpty()) return false

        val n = template.batchUpdate(sql, param)
        return n.sum() == param.size
    }

}