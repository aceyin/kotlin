package kotun.archetype.web

import com.wordnik.swagger.annotations.ApiOperation
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.ws.rs.core.MediaType

/**
 * Created by ace on 2016/11/11.
 */

@Controller
class Hello {
    @ResponseBody
    @GetMapping(value = "/hello", produces = arrayOf(MediaType.APPLICATION_JSON))
    @ApiOperation(value = "根据用户名获取用户对象", httpMethod = "GET", response = String::class, notes = "根据用户名获取用户对象")
    fun hello(): String {
        return "aaaaa"
    }
}