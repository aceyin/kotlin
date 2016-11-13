package kotun.support

/**
 * Application's startup arguments
 */

enum class StartupArguments(val key: String, val description: String) {
    ConfDir("conf.dir", "系统配置文件所在的目录，系统在启动的时候会从该目录读取:application.yml和sql.yml等文件"),
    SwaggerEnabled("swagger.enabled", "配置系统是否开启Swagger功能"),
    Log4jConfigurationFile("log4j.configurationFile", "用来指定Log4j2的配置文件(log4j2.xml)路径")
}