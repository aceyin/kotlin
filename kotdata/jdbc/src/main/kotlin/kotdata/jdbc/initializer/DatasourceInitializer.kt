package kotdata.jdbc.initializer

import kotun.support.cfg.Config
import org.apache.commons.beanutils.BeanUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.RootBeanDefinition
import java.util.*
import javax.sql.DataSource

/**
 * Spring 的 BeanFactoryPostProcessor
 * 用来动态创建 在 application.yml 中配置的 datasource
 */
internal class DatasourceInitializer : BeanDefinitionRegistryPostProcessor {

    private val LOGGER = LoggerFactory.getLogger(DatasourceInitializer::class.java)
    private val datasourceBeans = mutableMapOf<String, Map<*, *>>()
    private val jdbcTemplateClassName = "org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate"
    private val transactionManagerClassName = "org.springframework.jdbc.datasource.DataSourceTransactionManager"
    private val dbClassName = "kotdata.jdbc.Db"
    private val jdbcTemplateBeanNameSuffix = "JdbcTemplate"
    private val transactionManagerBeanNameSuffix = "TransManager"
    private val dbBeanNameSuffix = "DB"

    /**
     * 从 application.yml 中读取 datasource 配置，将对应的datasource注册到spring
     * TODO enable the init and destroy method binding
     */
    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        LOGGER.info("Calling DatasourceInitializer.postProcessBeanDefinitionRegistry ")
        if (!Config.has("datasource")) return
        val ds = Config.list("datasource")

        ds.forEach { it ->
            if (it is LinkedHashMap<*, *>) {
                val name = it.remove("name")
                val clazz = it.remove("class")
                val datasourceBeanName = if (name is String) name else "defaultDataSource"

                // register bean
                if (clazz !is String) throw RuntimeException("Class parameter is invalid datasource $datasourceBeanName")

                // register datasource
                registerBeanDefinition(registry, datasourceBeanName, clazz)
                LOGGER.info("Register datasource bean $name -> $clazz")
                this.datasourceBeans.put(datasourceBeanName, it)

                // register jdbc template and set the dependency
                val jdbcTemplateBeanName = beanName(datasourceBeanName, jdbcTemplateBeanNameSuffix)
                registerBeanDefinition(registry, jdbcTemplateBeanName, jdbcTemplateClassName)
                LOGGER.info("Register JdbcTemplate bean $jdbcTemplateBeanName -> $jdbcTemplateClassName")

                // register transaction manager
                val transactionManagerBeanName = beanName(datasourceBeanName, transactionManagerBeanNameSuffix)
                registerBeanDefinition(registry, transactionManagerBeanName, transactionManagerClassName)
                LOGGER.info("Register TransactionManager bean $transactionManagerBeanName -> $transactionManagerClassName")

                // register Db with different datasource
                val dbBeanName = beanName(datasourceBeanName, dbBeanNameSuffix)
                registerBeanDefinition(registry, dbBeanName, dbClassName)
                LOGGER.info("Register DB bean $dbBeanName -> $dbClassName")
            }
        }
    }

    /**
     * 创建bean实例
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        datasourceBeans.entries.forEach { b ->
            val datasourceName = b.key
            val datasource = beanFactory.getBean(datasourceName) ?: LOGGER.warn("No datasource bean found with name '$datasourceName'")
            if (datasource is DataSource) {
                BeanUtils.populate(datasource, b.value)
                // 初始化JdbcTemplate实例，传入datasource构造函数
                val jdbcTemplateBeanName = beanName(datasourceName, jdbcTemplateBeanNameSuffix)
                beanFactory.getBean(jdbcTemplateBeanName, datasource)
                LOGGER.info("Initialize JdbcTemplate bean $jdbcTemplateBeanName with datasource $datasourceName")

                // 初始化transactionManager实例，传入datasource构造函数
                val transactionManagerBeanName = beanName(datasourceName, transactionManagerBeanNameSuffix)
                beanFactory.getBean(transactionManagerBeanName, datasource)
                LOGGER.info("Initialize TransactionManager bean $transactionManagerBeanName with datasource $datasourceName")

                // 初始化 Db 实例，传入 datasource 构造函数
                val dbBeanName = beanName(datasourceName, dbBeanNameSuffix)
                beanFactory.getBean(dbBeanName, datasource)
                LOGGER.info("Initialize Db bean $dbBeanName with datasource $datasourceName")
            }
        }
    }

    private fun beanName(datasourceName: String, suffix: String) = "${datasourceName}_$suffix"

    private fun registerBeanDefinition(registry: BeanDefinitionRegistry, beanName: String, className: String): RootBeanDefinition {
        val beanClass = Class.forName(className) ?: throw RuntimeException("Error while register bean $beanName for class $className")

        registry.registerBeanDefinition(beanName, RootBeanDefinition(beanClass).apply {
            targetType = beanClass
            role = BeanDefinition.ROLE_APPLICATION
        })
        return registry.getBeanDefinition(beanName) as RootBeanDefinition
    }

}