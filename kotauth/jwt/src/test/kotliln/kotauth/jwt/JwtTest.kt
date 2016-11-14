package kotauth.jwt

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Created by ace on 2016/11/13.
 */

class JwtTest {

    @Test
    fun test_user_jwt() {
        val jwt = Jwt.UserJwt.create(Millis.FIVE_MIN.mills, mapOf("name" to "aceyin", "id" to "1"))
        assertNotNull(jwt)

        val map = Jwt.UserJwt.decrypt(jwt)
        assertEquals(true, map.valid)
        assertEquals(false, map.expired)
        assertEquals("aceyin", map.payload["name"])
    }

    @Test
    fun test_expires_jwt() {
        val jwt = Jwt.UserJwt.create(2000, mapOf("name" to "aceyin", "id" to "1"))
        assertNotNull(jwt)
        Thread.sleep(3000)

        val map = Jwt.UserJwt.decrypt(jwt)
        assertEquals(true, map.valid)
        assertEquals(true, map.expired)
        assertEquals("aceyin", map.payload["name"])
    }
}