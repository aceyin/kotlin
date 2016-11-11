package kotun.support

import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * 模块生命周期管理类。
 * 负责基于Spring的应用在初始化的时候进行自定义的初始化工作
 * 以及在application关闭的时候进行资源的释放工作。
 */
interface ModuleLifecycle {

    fun onInit(context: AnnotationConfigApplicationContext)

    fun onDestroy(context: AnnotationConfigApplicationContext)
}