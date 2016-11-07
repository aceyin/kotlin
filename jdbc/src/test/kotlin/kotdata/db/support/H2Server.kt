package kotdata.db.support

import org.h2.tools.Server


object H2Server {
    private val port = "9092"

    init {
        val server: Server = Server.createTcpServer("-tcpPort", port, "-tcpAllowOthers")
        server.start()
    }

    fun stop() {
        Server.shutdownTcpServer("tcp://localhost:${port}", "", true, true)
    }
}
