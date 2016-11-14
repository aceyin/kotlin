package kotauth.jwt

/**
 * Created by ace on 2016/11/14.
 */

data class JwtResult(val valid: Boolean = false, val expired: Boolean = false, val payload: Map<String, Any> = emptyMap())