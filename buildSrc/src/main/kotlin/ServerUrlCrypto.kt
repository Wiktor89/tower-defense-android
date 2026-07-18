import java.io.File
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Расшифровывает URL сервера из config/server.url.enc.
 * Ключ только из local.properties / env — в git не попадает.
 */
object ServerUrlCrypto {
    fun resolve(rootDir: File): String {
        val key = readKey(rootDir)
            ?: error(
                "Нет ключа расшифровки URL. Добавьте server.url.key=... в local.properties " +
                    "(см. scripts/seal-server-url.sh)",
            )
        val encFile = File(rootDir, "config/server.url.enc")
        require(encFile.isFile) { "Не найден ${encFile.path}" }
        val blob = encFile.readText().trim()
        return decrypt(blob, key)
    }

    private fun readKey(rootDir: File): ByteArray? {
        System.getenv("SERVER_URL_KEY")?.trim()?.takeIf { it.isNotEmpty() }?.let {
            return Base64.getDecoder().decode(it)
        }
        val lp = File(rootDir, "local.properties")
        if (!lp.isFile) return null
        val props = Properties()
        lp.inputStream().use { props.load(it) }
        val raw = props.getProperty("server.url.key")?.trim().orEmpty()
        if (raw.isEmpty()) return null
        return Base64.getDecoder().decode(raw)
    }

    fun decrypt(blobB64: String, key: ByteArray): String {
        val raw = Base64.getDecoder().decode(blobB64)
        require(raw.size > 16) { "Некорректный ciphertext" }
        val iv = raw.copyOfRange(0, 16)
        val ct = raw.copyOfRange(16, raw.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return String(cipher.doFinal(ct), Charsets.UTF_8)
    }

    fun encrypt(plain: String, key: ByteArray): String {
        val iv = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + ct)
    }
}
