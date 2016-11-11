package kotweb.rest

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.EnableWebMvc


@Configuration
@EnableWebMvc
@ComponentScan(basePackages = arrayOf("kotun"))
open class SpringConfig //: WebMvcConfigurerAdapter()

/**
 * 让Application支持申明式事务支持。
 * 注： 因为 datasource，transactionManager，jdbcTemplate等 spring bean的初始化工作
 * 已经在 DatasourceInitializer 类实现了，所以这里不需要再实例化这些bean，只需要让spring支持
 * 申明式事务即可。所以保留一个空的类。
 */
@Configuration
@EnableTransactionManagement
open class DeclarativeTransactionConfig