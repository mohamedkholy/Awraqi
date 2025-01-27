package com.example.waraq.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import java.io.ByteArrayInputStream
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
            val buffer = ByteArray(8192)
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

    fun decryptFile(inputStream: InputStream,outStream: OutputStream) {
        inputStream.use { inStream ->
            val ivSize = inStream.read()
            val iv = ByteArray(ivSize)
            inStream.read(iv)
            val cipher = decryptCipher(iv)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            CipherInputStream(inStream, cipher).use {cInStream->
                while (cInStream.read(buffer).apply { bytesRead = this }!= -1)
                {
                    outStream.write(buffer,0,bytesRead)
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