package kotun.support.cfg

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * Created by ace on 2016/11/11.
 */

object ConfigLoader {
    private val conf_file = "application.yml"
    private val LOGGER = LoggerFactory.getLogger(Config::class.java)
    /**
     * 从指定的目录读取配置文件，如果 dir 为空，则从默认的 "conf" 目录读取配置文件
     * @param dir 配置文件所在的目录
     */
    fun load(dir: String?): Map<String, Any> {
        val path = "${dir?.trim()}/${conf_file}"
        val file = File(path)

        val stream = if (file.exists() && file.isFile) file.inputStream()
        else ClassPathResource(path, Config::class.java.classLoader).file?.inputStream()

        if (stream == null) {
            LOGGER.warn("No file found in path '$dir', application will starting without configuration file")
            return emptyMap()
        }
        val map = mutableMapOf<String, Any>()
        LOGGER.info("Loading configuration from file '$dir' ")
        readYaml(stream, map)
        stream.close()
        return map
    }

    fun readYaml(stream: InputStream, conf: MutableMap<String, Any>) {
        val yml = Yaml()
        val iterator = yml.loadAll(stream)
        iterator.forEach { item ->
            when (item) {
                is LinkedHashMap<*, *> -> readMap("", item, conf)
                else -> LOGGER.warn("Skipping non-map item: $item")
            }
        }
    }

    fun readMap(prevKey: String, item: LinkedHashMap<*, *>, conf: MutableMap<String, Any>) {
        item.forEach {
            val v = it.value
            val k = it.key as String
            val key = if (prevKey.isNullOrEmpty()) k else "$prevKey.$k"
            when (v) {
                is LinkedHashMap<*, *> -> readMap(key, v, conf)
                else -> conf[key] = v
            }
        }
    }
}