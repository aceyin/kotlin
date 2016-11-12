package kotweb.rest

import com.fasterxml.jackson.databind.ObjectMapper
import kotun.support.Config
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.i18n.CookieLocaleResolver
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*

/**
 * Spring MVC 的基本支持，包括 i18n, 自动转成JSON等
 * 如果要使用AspectJ，则把 @EnableAspectJAutoProxy 注解加到 class 上
 */
@Configuration
@EnableWebMvc
open class SpringConfig : WebMvcConfigurerAdapter() {

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(jsonConverter())
    }

    @Bean
    open fun jsonConverter(): MappingJackson2HttpMessageConverter {
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = ObjectMapper()
        return converter
    }

    private fun i18nEnabled(): Boolean {
        val configed = Config.has("i18n")
        val enabled = Config.bool("i18n.enabled") ?: false
        return configed && enabled
    }

    @Bean open fun messageSource(): MessageSource? {
        if (!i18nEnabled()) return null

        val basename = Config.str("i18n.basename") ?: "/conf/i18n/messages"
        val source = ReloadableResourceBundleMessageSource()
        source.setBasename(basename)
        source.setDefaultEncoding("UTF-8")
        source.setUseCodeAsDefaultMessage(true)
        return source
    }

    @Bean
    open fun localeResolver(): LocaleResolver? {
        if (!i18nEnabled()) return null
        val resolver = CookieLocaleResolver()
        val defLocale = Config.str("i18n.defaultLocale") ?: "zh"
        val cookieName = Config.str("application.id") ?: "Application"

        resolver.setDefaultLocale(Locale(defLocale))
        resolver.cookieName = "${cookieName}LocaleCookie"
        resolver.cookieMaxAge = 4800
        return resolver
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        if (i18nEnabled()) {
            val name = Config.strs("application.id")
            val interceptor = LocaleChangeInterceptor()
            interceptor.paramName = "${name}LocaleInterceptor"
            registry.addInterceptor(interceptor)
        }
    }
}

/**
 * 支持自动生成 Restful API 的文档
 */
@Configuration
@EnableSwagger2
open class RestApiDocumentConfig : WebMvcConfigurerAdapter() {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/")
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
    }
}

/**
 * 让Application支持申明式事务支持。
 * 注： 因为 datasource，transactionManager，jdbcTemplate等 spring bean的初始化工作
 * 已经在 DatasourceInitializer 类实现了，所以这里不需要再实例化这些bean，只需要让spring支持
 * 申明式事务即可。所以保留一个空的类。
 */
@Configuration
@EnableTransactionManagement
open class DeclarativeTransactionConfig