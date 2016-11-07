package kotdata

import kotdata.EntityMeta
import kotdata.Entity
import org.junit.Test
import javax.persistence.Column
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import kotlin.test.assertEquals

/**
 * Created by ace on 2016/10/9.
 */

class EntityMetaTest {
    @Test
    fun should_get_meta_success() {
        val meta = EntityMeta.get(Class4TestMeta::class)
        val columns = meta.columns()

        assertEquals(columns.sorted(), listOf("id", "user_name").sorted())
    }

    @Test
    fun should_get_annotation_on_entity() {
        Class4TestMeta::class.members.forEach {
            if (it is KProperty) {
                val field = it.javaField
                println("_____" + field?.annotations?.size)
            }
        }
    }
}

data class Class4TestMeta(@Column(name = "user_name") val name: String,
                          override var id: String
) : Entity
