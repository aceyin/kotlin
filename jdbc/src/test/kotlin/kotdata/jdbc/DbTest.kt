package kotdata.jdbc

import kotdata.Entity
import kotdata.db.DaoTestBase
import org.junit.Before
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import java.util.*
import javax.persistence.Column
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Created by ace on 2016/10/7.
 */

class DbTest {

    companion object {
        init {
            val path = ClassPathResource("/src/test/resources/tables.sql", DbTest::class.java.classLoader).path
            DaoTestBase.createTables(path)
        }
    }

    val db = Db(DaoTestBase.datasource)

    @Before
    fun sleep() {
        // sleep to make H2 database free
        Thread.sleep(300)
    }

    @Test
    fun should_insert_single_data_success() {
        val user = createUser()

        val inserted = db.insert(user)
        assertEquals(inserted, true)
        val map = DaoTestBase.query("select * from User4DbTest where id = '${user.id}'")
        assertEquals(map[0]["username"], user.username)
    }

    @Test
    fun should_batch_insert_entities_success() {
        val users = Array<User4DbTest>(3) {
            createUser()
        }
        val res = db.batchInsert(users.toList())
        assertEquals(res, true)
        val map = DaoTestBase.query("select * from User4DbTest where id in ('${users[0].id}','${users[1].id}','${users[2].id}') ")
        assertEquals(map.size, 3)
    }

    @Test
    fun should_update_by_id_success() {
        val user = createUser()
        db.insert(user)
        val list1 = DaoTestBase.query("select * from User4DbTest where id = '${user.id}'")
        assertEquals(list1[0]["username"].toString(), user.username)

        val copy = user.copy(password = "new-password")
        db.updateById(user.id, copy)

        val list = DaoTestBase.query("select * from User4DbTest where id = '${user.id}'")
        assertEquals(list[0]["passwd"], copy.password)
    }

    @Test
    fun should_delete_by_id_success() {
        val user = createUser()
        db.insert(user)
        val list = DaoTestBase.query("select * from User4DbTest where id = '${user.id}'")
        assertEquals(list[0]["username"], user.username)

        db.deleteById(User4DbTest::class, user.id)
        val list2 = DaoTestBase.query("select * from User4DbTest where id = '${user.id}'")
        assertEquals(list2.size, 0)
    }

    @Test
    fun should_query_by_id_success() {
        val user = createUser()
        db.insert(user)

        val user2 = db.queryById(User4DbTest::class, user.id)
        assertEquals(user2?.username, user.username)
    }

    val update_sql = "update User4DbTest set username=:username, email=:email,passwd=:password where id=:id"
    @Test
    fun should_update_by_sql_for_an_entity_success() {
        val user = createUser()
        db.insert(user)
        // update

        val u = user.copy(username = "hahahaha", email = "hehe@111.com", password = "111111")
        val n = db.update(update_sql, u)

        assertEquals(true, n)

        val qu = db.queryById(User4DbTest::class, u.id)
        assertEquals("hehe@111.com", qu?.email)
        assertEquals("hahahaha", qu?.username)
        assertEquals("111111", qu?.password)
    }

    @Test
    fun should_update_by_param_success() {
        val user = createUser()
        db.insert(user)
        // update

        val n = db.update(update_sql, mapOf(
                "id" to user.id,
                "username" to "111",
                "email" to "aaa",
                "password" to "333"))

        assertEquals(true, n)

        val qu = db.queryById(User4DbTest::class, user.id)
        assertEquals("aaa", qu?.email)
        assertEquals("111", qu?.username)
        assertEquals("333", qu?.password)
    }

    @Test
    fun should_batch_update_entities_success() {
        val users = Array(3) { createUser() }

        db.batchInsert(*users)

        users.forEachIndexed { i, user -> user.password = "password:$i" }
        sleep()

        db.batchUpdate("update User4DbTest set passwd=:password where id=:id", *users)
        sleep()

        val params = mapOf("password" to listOf("password:0", "password:1", "password:2"))

        val list = db.query(User4DbTest::class, "select * from User4DbTest where passwd in (:password)", params)
        assertEquals(3, list.size)
    }

    @Test
    fun should_batch_update_by_sql_and_param_success() {
        val users = Array(3) { createUser() }

        db.batchInsert(*users)

        sleep()

        db.batchUpdate("update User4DbTest set username=:username where id=:id",
                mapOf("username" to "username0", "id" to users[0].id),
                mapOf("username" to "username1", "id" to users[1].id),
                mapOf("username" to "username2", "id" to users[2].id)
        )

        val list = DaoTestBase.query("select count(1) num from User4DbTest where username in ('username0','username1','username2')")
        assertEquals("3", list[0]["num"].toString())
    }

    @Test
    fun should_delete_by_ids_success() {
        val users = Array(3) { createUser() }
        val ids = Array(3) { users[it].id }.asList()
        val (id1, id2, id3) = ids

        db.batchInsert(*users)
        val list1 = DaoTestBase.query("select count(1) num from User4DbTest where id in ('$id1','$id2','$id3') ")
        assertEquals("3", list1[0]["num"])

        db.deleteByIds(User4DbTest::class, ids)

        val list = DaoTestBase.query("select count(1) num from User4DbTest where id in ('$id1','$id2','$id3') ")
        assertEquals("0", list[0]["num"])
    }


    @Test
    fun should_delete_entity_by_sql_success() {
        val user = createUser()
        db.insert(user)

        val u = db.queryById(User4DbTest::class, user.id)
        assertEquals(user.email, u?.email)

        db.delete("delete from User4DbTest where username=:username and passwd=:password", user)

        val u2 = db.queryById(User4DbTest::class, user.id)
        assertNull(u2)
    }

    @Test
    fun should_query_by_ids_success() {
        val users = Array(3) { createUser() }
        db.batchInsert(*users)
        val ids = Array(3) { users[it].id }.asList()
        val list = db.queryByIds(User4DbTest::class, ids)
        assertEquals(3, list.size)
    }

    @Test
    fun should_query_by_sql_and_param_success() {
        val user = createUser()
        db.insert(user)

        val list = db.query(User4DbTest::class,
                "select * from User4DbTest where email=:email and passwd=:password",
                mapOf("email" to user.email, "password" to user.password))
        assertEquals(1, list.size)
        assertEquals(user.mobile, list[0].mobile)
    }

    @Test
    fun should_query_by_sql_and_param_using_in_syntax_success() {
        val users = Array<User4DbTest>(3) { createUser() }
        db.batchInsert(*users)

        val names = Array<String>(3) { users[it].username }.asList()

        val list = db.query(User4DbTest::class,
                "select * from User4DbTest where username in (:username)",
                mapOf("username" to names))
        assertEquals(3, list.size)
    }

    @Test
    fun should_insert_success_use_sql_and_param() {
        val sql = "insert into User4DbTest (id,email,passwd,username,mobile,status) values (:id,:email,:passwd,:username,:mobile,:status)"

        val user = createUser()

        db.insert(sql, mutableMapOf(
                "id" to user.id,
                "email" to user.email,
                "passwd" to user.password,
                "username" to user.username,
                "mobile" to user.mobile,
                "status" to user.status
        ))

        val u = db.queryById(User4DbTest::class, user.id)
        assertEquals(user.username, u?.username)
    }

    @Test
    fun should_batch_insert_success_with_sql_and_param() {
        val sql = "insert into User4DbTest (id,email,passwd,username,mobile,status) values (:id,:email,:passwd,:username,:mobile,:status)"

        val users = Array(3) { createUser() }
        val maps = Array(3) {
            mutableMapOf(
                    "id" to users[it].id,
                    "email" to users[it].email,
                    "passwd" to users[it].password,
                    "username" to users[it].username,
                    "mobile" to users[it].mobile,
                    "status" to users[it].status
            )
        }

        db.batchInsert(sql, *maps)
        val ids = Array(3) { users[it].id }
        val list = db.queryByIds(User4DbTest::class, *ids)
        assertEquals(3, list.size)
    }

    @Test
    fun should_delete_by_sql_and_param_success() {
        val user = createUser()
        db.insert(user)

        val u = db.queryById(User4DbTest::class, user.id)
        assertEquals(u?.username, user.username)

        val sql = "delete from User4DbTest where username=:username"

        db.delete(sql, mapOf("username" to user.username))
        val u2 = db.queryById(User4DbTest::class, user.id)
        assertNull(u2)
    }

    @Test
    fun should_batch_delete_entities_success() {
        val users = Array<User4DbTest>(3) { createUser() }
        db.batchInsert(*users)

        val ids = Array<String>(3) { users[it].id }
        sleep()
        val list = db.queryByIds(User4DbTest::class, *ids)
        assertEquals(3, list.size)

        sleep()
        db.batchDelete("delete from User4DbTest where username=:username", *users)
        val list2 = db.queryByIds(User4DbTest::class, *ids)
        assertEquals(true, list2.isEmpty())
    }

    @Test
    fun should_batch_delete_by_sql_and_param_success() {
        val users = Array<User4DbTest>(3) { createUser() }
        db.batchInsert(*users)
        val ids = Array<String>(3) { users[it].id }
        val list = db.queryByIds(User4DbTest::class, *ids)
        assertEquals(3, list.size)

        val params = Array<Map<String, Any>>(3) {
            mapOf(
                    "email" to users[it].email,
                    "password" to users[it].password
            )
        }
        db.batchDelete("delete from User4DbTest where email=:email and passwd=:password", *params)
        val list2 = db.queryByIds(User4DbTest::class, *ids)
        assertEquals(true, list2.isEmpty())
    }

    @Test
    fun should_query_by_sql_and_entity_success() {
        val user = createUser()
        db.insert(user)
        val u = db.queryById(User4DbTest::class, user.id)
        assertEquals(user.username, u?.username)

        val u3 = db.query("select * from User4DbTest where username=:username and email=:email", user)
        assertEquals(user.id, u3[0]?.id)
    }

    @Test
    fun should_count_by_entity_class_success() {
        val n = db.count(User4DbTest::class)
        val user = createUser()
        db.insert(user)
        val m = db.count(User4DbTest::class)
        assertEquals(n + 1, m)
    }

    @Test
    fun should_count_by_sql_and_param_success() {
        val user = createUser()
        db.insert(user)

        val c = db.count("select count(1) num from User4DbTest where username=:username and passwd=:password", user)
        assertEquals(1, c)

        val d = db.count("select count(1) num from User4DbTest where username=:username and passwd=:password", mapOf("username" to user.username, "password" to user.password))
        assertEquals(1, d)
    }

    @Test
    fun should_query_data_by_page_success() {
        db.delete(User4DbTest::class)
        sleep()

        val users = Array(10) { createUser() }
        db.batchInsert(*users)

        sleep()
        val page = db.pageQuery(User4DbTest::class, 0, 0)
        assertEquals(0, page.rowNum)

        val page2 = db.pageQuery(User4DbTest::class, 1, 3)
        assertEquals(10, page2.rowNum)
        assertEquals(4, page2.pageNum)
        assertEquals(3, page2.pageData.size)

        val page3 = db.pageQuery(User4DbTest::class, "select * from User4DbTest where status=:status order by id", mapOf("status" to users[0].status), 1, 5)
        assertEquals(10, page3.rowNum)
        assertEquals(2, page3.pageNum)
        assertEquals(5, page3.pageData.size)
        assertEquals(1, page3.currentPage)

        val page5 = db.pageQuery(User4DbTest::class, "select * from User4DbTest where status=:status order by id", mapOf("status" to users[0].status), 2, 5)
        assertEquals(10, page3.rowNum)
        assertEquals(2, page3.pageNum)
        assertEquals(5, page3.pageData.size)
        assertEquals(2, page5.currentPage)
        assertNotEquals(page3.pageData[0].id, page5.pageData[0].id)

        val page4 = db.pageQuery(User4DbTest::class, "select * from User4DbTest where status=:status order by id limit 0,10", mapOf("status" to users[0].status), 1, 5)
        assertEquals(10, page4.rowNum)
        // 因为原始的SQL包含了limit部分，pageQuery将再增加一个limit部分
        assertEquals(2, page4.pageNum)
        assertEquals(5, page4.pageData.size)
    }

    fun createUser(): User4DbTest {
        val (email, passwd, name, mobile) = arrayOf(
                (Math.random() * 100000).toInt(),
                (Math.random() * 1000).toInt(),
                (Math.random() * 100000).toInt(),
                (Math.random() * 1000).toInt())
        return User4DbTest(
                id = UUID.randomUUID().toString().replace("-".toRegex(), ""),
                email = "u$email@163.com",
                password = "$passwd",
                username = "test-user-$name",
                mobile = "13800138$mobile",
                status = 100
        )
    }
}

data class User4DbTest(
        var email: String,
        @Column(name = "passwd")
        var password: String,
        var username: String,
        var mobile: String,
        var status: Int,
        override var id: String
) : Entity {
    constructor() : this(email = "", password = "", username = "", mobile = "", status = 0, id = "")
}

