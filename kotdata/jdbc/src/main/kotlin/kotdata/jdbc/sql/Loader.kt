package kotdata.jdbc.sql

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

/**
 * SQL Config loader
 */
internal object Loader {
    private val LOGGER = LoggerFactory.getLogger(Loader::class.java.name)

    /**
     * load from config file
     */
    internal fun load(path: String) {
        val file = File(path.trim())

        val stream =
                if (file.exists() && file.isFile && file.canRead()) file.inputStream()
                else ClassPathResource(path, Loader::class.java.classLoader).file?.inputStream()

        if (stream == null) {
            LOGGER.warn("No SQL file found in specified path '$path'.")
            return
        }
        LOGGER.info("Loading SQLs from '${file.absolutePath}'")
        val iterator = Yaml().loadAll(stream)
        iterator.forEach { item ->
            if (item is HashMap<*, *>) {
                item.forEach { key, value ->
                    if (key is String) {
                        if (value is String) Sql.CACHE.put(key, value.trim().replace("\r\n", ""))
                        else if (value is Map<*, *>) read(key, value)
                        else if (value is ArrayList<*>) LOGGER.warn("Skipping list item: '$item'")
                    } else {
                        LOGGER.warn("SQL key should be a string, skipping non string key item: $item")
                    }
                }
            } else LOGGER.warn("Skipping non-map configuration item: $item")
        }
        stream.close()
    }

    private fun read(prefix: String, map: Map<*, *>) {
        map.forEach { entry ->
            val key = entry.key
            if (key is String) {
                val value = entry.value
                if (value is String) {
                    Sql.CACHE.put("$prefix#$key", value.trim().replace("\r\n", ""))
                } else if (value is Map<*, *>) {
                    read("$prefix#$key", value)
                } else {
                    LOGGER.warn("Skipping illegal SQL item: $entry")
                }
            } else {
                LOGGER.warn("Skipping non-string key : $key")
            }
        }
    }
}