package ru.kpfu.itis;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class InitConnection {
    private static AmazonS3 s3;

    private static AmazonS3 initS3() {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(
                                "storage.yandexcloud.net", "ru-central1"
                        )
                )
                .build();
        return s3;
    }

    public static AmazonS3 getInstance() {
        if (s3 == null) {
            s3 = initS3();
        }
        return s3;
    }
}
