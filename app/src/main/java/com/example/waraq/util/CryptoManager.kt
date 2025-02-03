package com.example.waraq.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }

    private fun encryptCipher() =
        Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, getKey()) }

    private fun decryptCipher(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION)
            .apply {
                init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
            }
    }


    private fun getKey(): SecretKey {
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        return existingKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .build()
            )
        }.generateKey()
    }

    fun encryptFile(file: File, outStream: OutputStream) {
        val cipher = encryptCipher()
        outStream.use { out ->
            out.write(cipher.iv.size)
            out.write(cipher.iv)
            val buffer = ByteArray(8192 * 8)
            val fileInputStream = FileInputStream(file)
            var bytesRead: Int
            CipherOutputStream(out, cipher).use { cipherOutputStream ->
                fileInputStream.use { inputStream ->
                    while (inputStream.read(buffer).apply { bytesRead = this } != -1) {
                        cipherOutputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
        file.delete()
    }

    fun decryptFile(inputStream: InputStream, outputStream: OutputStream) {
        outputStream.use { outStream ->
            inputStream.use { inStream ->
                val ivSize = inStream.read()
                val iv = ByteArray(ivSize)
                DataInputStream(inStream).readFully(iv)
                val cipher = decryptCipher(iv)

                val buffer = ByteArray(8192 * 8)
                var bytesRead: Int

                while (inStream.read(buffer).also { bytesRead = it } != -1) {
                    val decrypted = cipher.update(buffer, 0, bytesRead)
                    if (decrypted != null) {
                        outStream.write(decrypted)
                    }
                }

                val decryptedFinal = cipher.doFinal()
                if (decryptedFinal != null) {
                    outStream.write(decryptedFinal)
                }
            }
        }
    }


    companion object {
        private const val ALGORITHM = KEY_ALGORITHM_AES
        private const val PADDING = ENCRYPTION_PADDING_PKCS7
        private const val BLOCK_MODE = BLOCK_MODE_CBC
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val KEY_ALIAS = "SECRET_KEY"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"

    }

}