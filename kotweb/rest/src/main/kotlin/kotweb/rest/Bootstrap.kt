package kotweb.rest

import kotun.support.Config
import kotun.support.ConfigLoader
import kotun.support.ModuleLifecycle
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import org.springframework.web.WebApplicationInitializer
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet
import javax.servlet.ServletContext

/**
 * This is our web application entry point.
 * This gets discovered no matter in what package this is hidden.
 */
class Bootstrap : WebApplicationInitializer {
    private val KEY_CONF_DIR = "conf.dir"
    private val LOGGER = LoggerFactory.getLogger(Bootstrap::class.java)
    private var moduleInitializer = mutableSetOf<Class<*>>()

    override fun onStartup(context: ServletContext) {
        // read configurations
        val map = ConfigLoader.load(System.getProperty(KEY_CONF_DIR))
        Config.PROPS.putAll(map)

        // create and init spring context
        val ctx = createSpringContext()
        context.addListener(ContextLoaderListener(ctx))

        // init dispatcher servlet
        this.initDispatcherServlet(context, ctx)
    }

    private fun initDispatcherServlet(context: ServletContext, ctx: AnnotationConfigWebApplicationContext) {
        val dispatcher = context.addServlet("dispatcher", DispatcherServlet(ctx))
        dispatcher.setLoadOnStartup(1)
        val url = Config.str("spring.dispatcherServletMapping") ?: "/"
        dispatcher.addMapping(url)
    }

    private fun createSpringContext(): AnnotationConfigWebApplicationContext {
        val ctx = AnnotationConfigWebApplicationContext()

        // set scan
        val pkg = Config.strs("spring.packageScan")
        if (pkg.isNotEmpty()) ctx.scan(*pkg.toTypedArray())
        // register config class
        ctx.register(SpringConfig::class.java)
        // initial datasource
        this.autoInitDataSource(ctx)
        // init other modules
        this.initDependedModules()

        return ctx
    }

    private fun initDependedModules() {
        Reflections().getSubTypesOf(ModuleLifecycle::class.java)?.forEach {
            this.moduleInitializer.add(it)
        }

        this.moduleInitializer.forEach { clazz ->
            try {
                val listener = clazz.newInstance()
                val method = clazz.getDeclaredMethod("initialize")
                method.invoke(listener)
                LOGGER.info("Calling initialize of '${clazz.name}'")
            } catch (e: Exception) {
                LOGGER.info("Error while calling module initializer: '${clazz.name}'.")
                throw e
            }
        }
        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread() {
            LOGGER.info("Preparing to shutdown module ...")
            this.moduleInitializer.forEach { clazz ->
                try {
                    val listener = clazz.newInstance()
                    val method = clazz.getDeclaredMethod("destroy")
                    method.invoke(listener)
                    LOGGER.info("Calling destroy of '${clazz.name}'")
                } catch (e: Exception) {
                    LOGGER.info("Error while destroy module: '${clazz.name}'.")
                    throw e
                }
            }
        })
    }

    private fun autoInitDataSource(context: AnnotationConfigWebApplicationContext) {
        val datasourceConf = Config.list("datasource")
        if (datasourceConf.isNotEmpty()) {
            // 如果发现配置文件中有datasource相关的配置，则初始化datasource和jdbctemplate
            context.addBeanFactoryPostProcessor(DatasourceInitializer())
            // 添加申明式事务配置类
            context.register(DeclarativeTransactionConfig::class.java)
        }
    }
}