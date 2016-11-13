package kotun.archetype.service.impl

import kotdata.Entity
import kotdata.jdbc.Db
import kotdata.jdbc.sql.Sql
import kotun.archetype.entity.User
import kotun.archetype.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by ace on 2016/11/13.
 */

@Service
open class UserServiceImpl : UserService {
    @Autowired
    private lateinit var db: Db

    @Transactional(readOnly = false)
    override fun register(user: User): Boolean {
        val sql = Sql.get("check.if.user.exists") ?: throw RuntimeException("Cannot found SQL with key: 'check.if.user.exists'.")
        val num = db.count(sql, user)
        if (num > 0) {
            return false
        }
        user.id = Entity.IdGen.next()
        user.status = "NEW"
        val succ = db.insert(user)
        return succ
    }
}