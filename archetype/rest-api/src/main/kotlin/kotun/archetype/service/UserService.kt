package kotun.archetype.service

import kotun.archetype.entity.User

/**
 * Created by ace on 2016/11/13.
 */
interface UserService {
    open fun register(user: User): Boolean
}