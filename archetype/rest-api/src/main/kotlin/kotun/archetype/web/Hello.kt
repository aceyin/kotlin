package kotun.archetype.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import javax.ws.rs.core.MediaType

/**
 * Created by ace on 2016/11/11.
 */

@Controller
class Hello {
    @RequestMapping(value = "/hello",
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.APPLICATION_JSON))
    @ResponseBody
    fun hello(): String {
        return "aaaaa"
    }
}