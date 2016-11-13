package kotun.archetype.service

import kotun.archetype.entity.User
import kotweb.rest.Resp

/**
 * Created by ace on 2016/11/13.
 */
interface UserService {
    open fun register(user: User): Resp
}