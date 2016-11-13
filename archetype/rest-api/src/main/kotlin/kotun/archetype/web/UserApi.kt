package kotun.archetype.web

import io.swagger.annotations.ApiOperation
import kotun.archetype.entity.User
import kotun.archetype.service.UserService
import kotweb.rest.Resp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import javax.ws.rs.core.MediaType

/**
 * Created by ace on 2016/11/11.
 */

@Controller
class UserApi {

    @Autowired
    private lateinit var userSvc: UserService
    private val log = LoggerFactory.getLogger(UserApi::class.java)

    @ResponseBody
    @PostMapping(value = "/register",
            consumes = arrayOf(MediaType.APPLICATION_JSON),
            produces = arrayOf(MediaType.APPLICATION_JSON))
    @ApiOperation(value = "用户注册", httpMethod = "POST", response = String::class, notes = "用户注册")
    fun register(@RequestBody user: User): ResponseEntity<Resp> {
        log.info("Register user: $user")
        val resp = userSvc.register(user)

        return ResponseEntity.ok().header("User-Defined-Header", user.id).body(resp)
    }
}