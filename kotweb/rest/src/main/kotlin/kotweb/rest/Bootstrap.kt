package kotweb.rest

import kotun.support.Config
import kotun.support.ConfigLoader
import kotun.support.ModuleLifecycle
import kotweb.rest.initializer.DatasourceInitializer
import org.apache.logging.log4j.web.Log4jServletContextListener
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.web.WebApplicationInitializer
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.web.servlet.DispatcherServlet
import javax.servlet.ServletContext

/**
 * This is our web application entry point.
 * This gets discovered no matter in what package this is hidden.
 */
class Bootstrap : WebApplicationInitializer {
    private val KEY_CONF_DIR = "conf.dir"
    private val dispatcherServletName = "dispatcher"
    private val defaultUrlPattern = "/*"
    private val LOGGER = LoggerFactory.getLogger(Bootstrap::class.java)
    private var moduleInitializer = mutableSetOf<Class<*>>()

    override fun onStartup(context: ServletContext) {
        // load application.yml config
        this.loadApplicationConfig()
        // init log4j
        this.initLog4j(context)
        // create and init spring context
        val ctx = createSpringContext()
        // TODO: support customized listener
        // init dispatcher servlet
        this.initServletContainer(context, ctx)
        // enable filters
        this.addFilters(context, ctx)
        // enable swagger for the dev environment
        this.enableSwagger(ctx)
    }

    private fun enableSwagger(ctx: AnnotationConfigWebApplicationContext) {
        val enabled = System.getProperty("swagger.enabled", "false").toBoolean()
        if (enabled) {
            ctx.register(RestApiDocumentConfig::class.java)
        }
    }

    /**
     * 增加指定的filter
     */
    private fun addFilters(context: ServletContext, ctx: AnnotationConfigWebApplicationContext) {
        val encodingFilter = context.addFilter("encodingFilter", CharacterEncodingFilter("UTF-8", true, true))
        val urlPattern = Config.str("spring.dispatcherServletUrlPattern") ?: defaultUrlPattern
        encodingFilter.addMappingForUrlPatterns(null, true, urlPattern)
    }

    private fun initLog4j(context: ServletContext) {
        //Log4jConfigListener
        context.setInitParameter("log4jConfigLocation", "classpath:conf/log4j.properties")
        context.addListener(Log4jServletContextListener::class.java)
    }

    private fun loadApplicationConfig() {
        // read configurations
        val map = ConfigLoader.load(System.getProperty(KEY_CONF_DIR))
        Config.PROPS.putAll(map)
    }

    private fun initServletContainer(context: ServletContext, ctx: AnnotationConfigWebApplicationContext) {
        context.addListener(ContextLoaderListener(ctx))
        val dispatcher = context.addServlet(dispatcherServletName, DispatcherServlet(ctx))
        dispatcher.setLoadOnStartup(1)
        val url = Config.str("spring.dispatcherServletUrlPattern") ?: defaultUrlPattern
        dispatcher.addMapping(url)
    }

    private fun createSpringContext(): AnnotationConfigWebApplicationContext {
        val ctx = AnnotationConfigWebApplicationContext()

        // set scan
        val pkg = Config.strs("spring.packageScan")
        if (pkg.isNotEmpty()) ctx.scan(*pkg.toTypedArray())
        // register the java config into spring context
        this.registerConfig(ctx)
        // initial datasource
        this.autoInitDataSource(ctx)
        // init other modules
        this.initDependedModules(ctx)

        return ctx
    }

    private fun registerConfig(ctx: AnnotationConfigWebApplicationContext) {
        // register config class
        ctx.register(SpringConfig::class.java)
        // register the config defined in application.yml
        val confs = Config.strs("spring.configurationClasses")
        confs.forEach {
            try {
                val clazz = Class.forName(it)
                ctx.register(clazz)
            } catch (e: Exception) {
                LOGGER.warn("Error while register config class '$it' defined in application.yml", e)
            }
        }
    }

    private fun initDependedModules(ctx: AnnotationConfigWebApplicationContext) {
        Reflections().getSubTypesOf(ModuleLifecycle::class.java)?.forEach {
            this.moduleInitializer.add(it)
        }

        this.moduleInitializer.forEach { clazz ->
            try {
                val listener = clazz.newInstance()
                val method = clazz.getDeclaredMethod("onInit", ApplicationContext::class.java)
                method.invoke(listener, ctx)
                LOGGER.info("Calling initialize of '${clazz.name}'")
            } catch (e: Exception) {
                LOGGER.error("Error while calling module initializer: '${clazz.name}'.")
                throw e
            }
        }
        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread() {
            LOGGER.info("Preparing to shutdown module ...")
            this.moduleInitializer.forEach { clazz ->
                try {
                    val listener = clazz.newInstance()
                    val method = clazz.getDeclaredMethod("onDestroy", ApplicationContext::class.java)
                    method.invoke(listener, ctx)
                    LOGGER.info("Calling destroy of '${clazz.name}'")
                } catch (e: Exception) {
                    LOGGER.error("Error while destroy module: '${clazz.name}'.")
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