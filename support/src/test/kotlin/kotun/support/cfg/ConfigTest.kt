package kotun.support.cfg

import org.junit.Test
import java.io.FileNotFoundException
import java.util.*
import kotlin.test.assertEquals

class ConfigTest {
    val file = "application-test3.yml"
    val conf = Config

    init {
        val map = ConfigLoader.load(ClassLoader.getSystemResource(file).path.replace(file, ""))
        conf.PROPS.putAll(map)
    }


    @Test
    fun should_get_property_by_flat_property_key_successfully() {
        assertEquals(conf.strs("spring.packageScan"), listOf<String>("komics"))
    }

    @Test
    fun should_get_properties_by_tree_type_key_successfully() {
        val datasource = conf.list("datasource")
        val ds = (datasource as ArrayList<*>)[0] as HashMap<*, *>

        assertEquals(ds["name"] as String, "default-datasource")
        assertEquals(ds["minIdle"] as String, "5")
    }

    @Test(expected = FileNotFoundException::class)
    fun should_throw_exception_when_config_file_not_found() {
        ConfigLoader.load("a-non-existance-file.yml")
    }

}