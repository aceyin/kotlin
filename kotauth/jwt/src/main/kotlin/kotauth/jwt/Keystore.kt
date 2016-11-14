package kotauth.jwt

import java.io.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Created by ace on 2016/11/13.
 */

sealed class Keystore {
    protected val dir: String

    init {
        val home = System.getProperty("user.home")
        this.dir = "$home/.keystore"
        val file = File(this.dir)
        if (!file.exists()) file.mkdirs()
    }

    /* load a serialized key from file system */
    protected fun load(path: String): SecretKey? {
        val file = File(path)
        if (!file.exists())
            return null
        if (!file.isFile || !file.canRead())
            throw RuntimeException("File '$path' is not exists or is not readable")

        val ois = ObjectInputStream(FileInputStream(file))
        val o = ois.readObject()
        if (o !is SecretKey)
            throw RuntimeException("The file '$path' is not a valid SecretKey object")
        ois.close()
        return o
    }

    protected fun write(file: String, key: SecretKey) {
        val keyFile = File(file)

        if (keyFile.exists() && keyFile.isDirectory)
            throw RuntimeException("File is a directory: $file")

        if (keyFile.exists() && !keyFile.canWrite())
            throw RuntimeException("File is not writable: $file")

        if (!keyFile.exists()) keyFile.createNewFile()

        val oos = ObjectOutputStream(FileOutputStream(keyFile, false))
        oos.writeObject(key)
        oos.flush()
        oos.close()
    }

    /* get a key from file */
    abstract fun get(name: String): SecretKey

    /* save a key into file */
    abstract fun refresh(name: String)

    /* create a secret key */
    abstract fun create(name: String, save: Boolean = false): SecretKey

    /* AES 256的key */
    class AES256 : Keystore() {
        companion object {
            private var cache = mutableMapOf<String, SecretKey>()
        }

        override fun get(name: String): SecretKey {
            synchronized(cache) {
                var key = cache[name]
                if (key != null) return key

                key = this.load("$dir/$name.key") ?: this.create(name, true)
                cache[name] = key
                return key
            }
        }

        override fun create(name: String, save: Boolean): SecretKey {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)

            val key = keyGen.generateKey()
            if (save) {
                this.write("$dir/$name.key", key)
            }
            return key
        }

        override fun refresh(name: String) {
            val key = this.create(name)
            cache[name] = key
        }

    }

}