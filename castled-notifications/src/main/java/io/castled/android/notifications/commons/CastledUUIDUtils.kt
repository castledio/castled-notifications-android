package io.castled.android.notifications.commons

import java.nio.ByteBuffer
import java.util.Base64
import java.util.UUID


internal object CastledUUIDUtils {

    private fun convertUUIDToBase64(uuid: UUID): String {
        // Convert UUID to byte array
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(uuid.mostSignificantBits)
        byteBuffer.putLong(uuid.leastSignificantBits)

        // Encode byte array to Base64 string
        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array())
    }

    fun getIdBase64() = convertUUIDToBase64(UUID.randomUUID())
}