package kotdata.jdbc.initializer

import kotdata.jdbc.sql.Loader
import kotun.support.ModuleLifecycle
import kotun.support.cons.StartupArguments
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext


class ModuleInitializer : ModuleLifecycle {
    private val LOGGER = LoggerFactory.getLogger(ModuleInitializer::class.java)
    override fun onDestroy(context: ApplicationContext) {

    }

    override fun onInit(context: ApplicationContext) {
        LOGGER.info("Initialize module '${ModuleInitializer::class.java.name}'")
        val path = System.getProperty(StartupArguments.ConfDir.key)
        if (path != null && path.isNotEmpty()) {
            Loader.load("$path/sql.yml")
        }
    }

}