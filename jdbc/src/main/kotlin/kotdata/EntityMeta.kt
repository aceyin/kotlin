package kotdata

import org.slf4j.LoggerFactory
import javax.persistence.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

/**
 * Created by ace on 2016/10/9.
 */
/**
 * Entity meta data
 */
class EntityMeta {
    /* Entity类的名称 */
    lateinit var entityName: String
    /* Entity类对应的table的名称 */
    lateinit var table: String
    /* Entity类的属性对应的数据库字段名称 */
    internal var attr2col = mutableMapOf<KCallable<*>, String>()
    /* 数据库字段名称和entity属性名称的对应关系 */
    private var col2attr = mutableMapOf<String, String>()
    /* 一对多映射 */
//    val one2many = mutableListOf()
    /* 多对一映射 */
//    val many2one = mutableListOf()
    /* 多对多映射 */
//    val many2many = mutableListOf()
    /* 一对一映射 */
//    val one2one = mutableListOf()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EntityMeta::class.java)

        private val metaCache = mutableMapOf<KClass<out Any>, EntityMeta>()

        /**
         * Get the meta data for given entity class
         */
        fun get(clazz: KClass<out Any>): EntityMeta {
            LOGGER.debug("Getting meta for class $clazz")
            val meta = metaCache[clazz] ?: read(clazz)
            return meta
        }

        /**
         * read meta data for given entity class
         */
        private fun read(clazz: KClass<out Any>): EntityMeta {
            LOGGER.debug("Reading metadata from class $clazz")
            val meta = EntityMeta()
            // read entity name
            meta.entityName = clazz.simpleName ?: throw IllegalStateException("Entity class's name should not be empty")
            // read table annotation
            val table = clazz.annotations.find { it.annotationClass == Table::class } as? Table
            meta.table = if (table == null || table.name.isNullOrEmpty()) meta.entityName else table.name

            // process member <-> column mapping
            clazz.members.forEach readColumn@{ p ->
                // only read entity's members, skip functions
                if (p is KProperty) {
                    val field = p.javaField
                    if (field == null) {
                        LOGGER.warn("No field found for property $p")
                        return@readColumn
                    }
                    // read annotations of field
                    val annotations = field.annotations

                    // skip the property with @Transit annotation
                    annotations.find { it.annotationClass == Transient::class }?.let {
                        LOGGER.debug("Skipping transient property '$p'")
                        return@readColumn
                    }
                    // TODO process one-to-one, one-to-many, many-to-one
                    annotations.find { it.annotationClass == OneToOne::class }?.let {
                        return@readColumn
                    }
                    annotations.find { it.annotationClass == OneToMany::class }?.let {
                        return@readColumn
                    }
                    annotations.find { it.annotationClass == ManyToOne::class }?.let {
                        return@readColumn
                    }
                    annotations.find { it.annotationClass == ManyToMany::class }?.let {
                        return@readColumn
                    }
                    // process normal column field
                    val column = annotations.find { it.annotationClass == Column::class } as? Column
                    val columnName = if (column?.name.isNullOrEmpty()) p.name else column?.name
                    if (columnName != null) {
                        meta.attr2col.put(p, columnName)
                        meta.col2attr.put(columnName, p.name)
                    } else
                        throw IllegalStateException("Entity filed '$p' name cannot be empty")
                }
            }

            metaCache.put(clazz, meta)
            return meta
        }
    }

    /**
     * 获取一个entity类所包含的数据库字段名的列表
     */
    fun columns(sort: Boolean = false): List<String> {
        val list = this.attr2col.values.toList()
        return if (sort) list.sorted() else list
    }

    /**
     * 根据一个字段的名字，获取对应的属性的名字
     */
    fun prop(columnName: String): String {
        val prop = this.col2attr[columnName]
        if (prop == null || prop.isNullOrEmpty()) {
            LOGGER.warn("No property found for column '$columnName' of entity '$entityName', use column name '$columnName' instead")
            return columnName
        }
        return prop
    }
}