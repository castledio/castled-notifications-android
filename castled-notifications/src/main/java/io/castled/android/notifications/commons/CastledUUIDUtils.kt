package io.castled.android.notifications.commons

//import java.util.Base64
import android.os.Build
import android.util.Base64
import java.nio.ByteBuffer
import java.util.UUID

internal object CastledUUIDUtils {

    private fun convertUUIDToBase64(uuid: UUID): String {
        // Convert UUID to byte array
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(uuid.mostSignificantBits)
        byteBuffer.putLong(uuid.leastSignificantBits)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use java.util.Base64 for Android O and above
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array())
        } else {
            // Use android.util.Base64 for Android versions below O
            Base64.encodeToString(
                byteBuffer.array(),
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
        }
    }

    fun getIdBase64() = convertUUIDToBase64(UUID.randomUUID())
}