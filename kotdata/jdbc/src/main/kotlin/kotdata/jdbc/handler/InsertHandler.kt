package kotdata.jdbc.handler

import kotdata.Entity
import kotdata.jdbc.sql.Sql
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * Created by ace on 2016/10/17.
 */
internal class InsertHandler(val template: NamedParameterJdbcTemplate) {

    private val LOGGER = LoggerFactory.getLogger(InsertHandler::class.java)
    /**
     * 向数据库中插入一个对象。
     * @param entity 被插入的对象
     */
    fun <E : Entity> insert(entity: E): Boolean {
        val sql = Sql.get(entity.javaClass.kotlin, Sql.Predefined.insert)

        val param = BeanPropertySqlParameterSource(entity)
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Execting sql: $sql with parameter $param")
        }
        val e = template.update(sql, param)
        return e > 0
    }

    /**
     * 按照给定的SQL语句和参数插入数据。
     * @param sql 需要执行的SQL预计
     * @param param sql参数
     */
    fun insert(sql: String, param: MutableMap<String, Any>): Boolean {
        if (sql.isNullOrEmpty()) return false
        val n = template.update(sql, param)
        return n > 0
    }

    /**
     * 在同一个事务中，批量插入数据到数据库。
     * 如果entities中的数据为不同的entity实例，则会插入到不同的数据库表。
     * @param entities 需要被批量插入的数据
     */
    fun <E : Entity> batchInsert(entities: List<E>): Boolean {
        if (entities.isEmpty()) return true
        val example = entities[0]
        val clazz = example.javaClass.kotlin

        val sql = Sql.get(clazz, Sql.Predefined.insert)
        if (sql.isNullOrEmpty()) {
            LOGGER.warn("No SQL found for entity class '$clazz' and predefined sql '${Sql.Predefined.insert}'")
            return false
        }

        val params = Array(entities.size) {
            val entity = entities[it]
            BeanPropertySqlParameterSource(entity)
        }

        val n = template.batchUpdate(sql, params)
        return n.sum() == entities.size
    }

    /**
     * 根据给定的sql和参数，批量插入数据.
     * @param sql 数据库insert语句
     * @param param 要插入的数据
     */
    fun batchInsert(sql: String, param: Array<out MutableMap<String, Any>>): Boolean {
        if (sql.isNullOrEmpty()) return false
        val n = template.batchUpdate(sql, param)
        return n.sum() == param.size
    }

}