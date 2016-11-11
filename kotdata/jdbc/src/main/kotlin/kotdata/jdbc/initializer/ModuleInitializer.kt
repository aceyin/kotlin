package kotdata.jdbc.initializer

import kotdata.jdbc.sql.Loader
import kotun.support.ModuleLifecycle
import org.springframework.context.ApplicationContext


class ModuleInitializer : ModuleLifecycle {
    override fun onDestroy(context: ApplicationContext) {

    }

    override fun onInit(context: ApplicationContext) {
        val path = System.getProperty("conf.dir")
        if (path != null && path.isNotEmpty()) {
            Loader.load("$path/sql.yml")
        }
    }

}