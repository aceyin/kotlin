package kotun.archetype.entity

import kotdata.Entity
import javax.persistence.Table

@Table(name = "user")
data class User(override var id: String,
                var username: String,
                var password: String,
                var status: String,
                var mobile: String,
                var email: String
) : Entity {
    constructor() : this(id = "", username = "", password = "", status = "", mobile = "", email = "")
}