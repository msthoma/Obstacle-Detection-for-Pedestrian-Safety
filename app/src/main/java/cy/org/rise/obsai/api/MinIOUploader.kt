package cy.org.rise.obsai.api

import io.minio.MinioClient
import io.minio.PutObjectOptions
import java.io.File

/*
* Class to handle Min.io photo uploads
* see https://github.com/minio/minio-java
* for Java client API see https://docs.min.io/docs/java-client-api-reference.html
* may throw NoSuchAlgorithmException, IOException, InvalidKeyException, XmlPullParserException
* */

class MinIOUploader {

    private var minioClient: MinioClient

    init {
        minioClient = MinioClient(endpoint, accessKey, secretKey)
    }

    fun uploadPhoto(serverPhotoName: String, photoPath: String, bucket: String) {
        if (!minioClient.bucketExists(bucket)) {
            minioClient.makeBucket(bucket)
        }

        minioClient.putObject(
            bucket,
            serverPhotoName,
            photoPath,
            PutObjectOptions(
                File(photoPath).length(), // photoSize
                -1
            ) // partSize, -1 means auto-determine
        )
    }

    companion object {
        private const val endpoint = "http://192.168.10.10:9001/"
        private const val accessKey = "minio"
        private const val secretKey = "minio123"

        // endpoint for tests, works without accessKey and secretKey
        // private const val endpoint = "https://play.min.io"

        val instance = MinIOUploader()
    }
}