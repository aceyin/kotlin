package kotweb.rest

import org.junit.Test
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext

/**
 * Created by ace on 2016/11/10.
 */

class SpringTest {

    @Test
    fun test_application_context() {
        val ctx = AnnotationConfigWebApplicationContext()

    }
}

@Configuration
open class Conf {

    @Bean("bean1")
    open fun bean1(): StringBuilder {
        return StringBuilder("Bean1")
    }

    @Bean("bean2")
    open fun bean2(): MutableMap<String, String> {
        return mutableMapOf("a" to "a")
    }
}