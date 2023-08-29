package com.phongnn.imagepicker.data.model

data class MyImage(val matrix: ByteArray, val uri: String, val type: String, val frameNumber: Int, var downloadId: Long): java.io.Serializable {

    // Auto gen code when add a ByteArray parameter
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyImage

        if (!matrix.contentEquals(other.matrix)) return false
        if (uri != other.uri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = matrix.contentHashCode()
        result = 31 * result + uri.hashCode()
        return result
    }
}