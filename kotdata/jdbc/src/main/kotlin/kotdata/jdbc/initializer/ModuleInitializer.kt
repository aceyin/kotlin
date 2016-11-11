package kotdata.jdbc.initializer

import kotdata.jdbc.sql.Loader
import kotun.support.ModuleLifecycle
import org.springframework.context.annotation.AnnotationConfigApplicationContext


class ModuleInitializer : ModuleLifecycle {
    override fun onDestroy(context: AnnotationConfigApplicationContext) {

    }

    override fun onInit(context: AnnotationConfigApplicationContext) {
        val path = System.getProperty("conf.dir")
        if (path != null && path.isNotEmpty()) {
            Loader.load("$path/sql.yml")
        }
    }

}