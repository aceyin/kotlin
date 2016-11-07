package kotdata

import java.util.*

/**
 * Created by ace on 2016/10/1.
 */
interface Entity {
    /**
     * Entity的唯一ID
     */
    var id: String

    /**
     * Id generator
     */
    object IdGen {
        fun next(): String {
            return UUID.randomUUID().toString().replace("-".toRegex(), "")
        }
    }
}