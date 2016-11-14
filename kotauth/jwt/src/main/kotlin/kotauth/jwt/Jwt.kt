package kotauth.jwt

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.DirectDecrypter
import com.nimbusds.jose.crypto.DirectEncrypter
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import java.util.*


sealed class Jwt {
    protected val issuer = "un32.com"

    /**
     * 创建一个JWT token
     */
    abstract fun create(expires: Long, data: Map<String, Any>): String

    /**
     * 解密 JWT Token
     */
    abstract fun decrypt(jwt: String): JwtResult

    /**
     * 用来处理用户相关JWT的类
     */
    class UserJwt : Jwt() {

        private val LOGGER = LoggerFactory.getLogger(UserJwt::class.java)

        companion object {
            private val keystore = Keystore.AES256()
            private val instance = UserJwt()

            fun create(expires: Long, data: Map<String, Any>): String {
                return instance.create(expires, data)
            }

            fun decrypt(jwt: String): JwtResult {
                return instance.decrypt(jwt)
            }
        }

        /* Create a JWT*/
        override fun create(expires: Long, data: Map<String, Any>): String {
            val secretKey = keystore.get(JwtSub.AUTHENTICATED_USER.name.toLowerCase())
            val signer = MACSigner(secretKey.encoded)

            val builder = JWTClaimsSet.Builder()
            with(builder) {
                expirationTime(Date(System.currentTimeMillis() + expires))
                subject(JwtSub.AUTHENTICATED_USER.name)
                issueTime(Date())
                issuer(issuer)
                data.forEach { claim(it.key, it.value) }
            }

            val claimsSet = builder.build()
            val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
            signedJWT.sign(signer)

            val jweHeader = JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                    .contentType("JWT") // required to signal nested JWT
                    .build()

            val jweObject = JWEObject(jweHeader, Payload(signedJWT))
            try {
                jweObject.encrypt(DirectEncrypter(secretKey.encoded))
            } catch (e: Exception) {
                val java_home = System.getProperty("java.home")
                println("**********************************************************************")
                println("The AES 256 Algorithm Not Supported")
                println("Please Follow The Suggestion Bellow to Solve This Issue")
                println("1. Download the files from:")
                println("  - (JAVA 6) http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html")
                println("  - (JAVA 7) http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html")
                println("  - (JAVA 8) http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html")
                println("2. Extract the jar files from the zip and save them in '$java_home/jre/lib/security/'")
                println("3. Restart The Application")
                println("**********************************************************************")
                throw e
            }

            return jweObject.serialize()
        }

        override fun decrypt(jwt: String): JwtResult {
            val jweObject = JWEObject.parse(jwt)
            val secretKey = keystore.get(JwtSub.AUTHENTICATED_USER.name)
            jweObject.decrypt(DirectDecrypter(secretKey.encoded))

            val signedJWT = jweObject.payload.toSignedJWT()
            val verified = signedJWT.verify(MACVerifier(secretKey.encoded))

            if (!verified) {
                LOGGER.error("Verified the given JWT string failed, JWT='$jwt'")
                return JwtResult(false)
            }

            val subject = signedJWT.jwtClaimsSet.subject ?: return JwtResult(false)
            if (subject != JwtSub.AUTHENTICATED_USER.name) {
                LOGGER.warn("The subject from JWT is not equals to '${JwtSub.AUTHENTICATED_USER.name}',JWT='$jwt'")
                return JwtResult(false)
            }

            val claims = signedJWT.jwtClaimsSet.claims
            val expTime = signedJWT.jwtClaimsSet.expirationTime
            val now = Date()

            if (expTime.before(now))
                return JwtResult(true, true, claims)
            else
                return JwtResult(true, false, claims)
        }
    }
}