package com.wanted.backend.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3ConfigTest {

    @Test
    void presignCredentialsProvider_returnsStaticCredentials_whenKeysConfigured() {
        S3Config s3Config = new S3Config();
        ReflectionTestUtils.setField(s3Config, "presignAccessKey", "AKIATESTACCESSKEY");
        ReflectionTestUtils.setField(s3Config, "presignSecretKey", "test-secret-key");

        AwsCredentialsProvider provider = s3Config.presignCredentialsProvider();

        assertThat(provider).isInstanceOf(StaticCredentialsProvider.class);
        assertThat(provider.resolveCredentials().accessKeyId()).isEqualTo("AKIATESTACCESSKEY");
        assertThat(provider.resolveCredentials().secretAccessKey()).isEqualTo("test-secret-key");
    }

    @Test
    void presignCredentialsProvider_fallsBackToDefaultCredentials_whenKeysBlank() {
        S3Config s3Config = new S3Config();
        ReflectionTestUtils.setField(s3Config, "presignAccessKey", "");
        ReflectionTestUtils.setField(s3Config, "presignSecretKey", "");

        AwsCredentialsProvider provider = s3Config.presignCredentialsProvider();

        assertThat(provider).isInstanceOf(DefaultCredentialsProvider.class);
    }

    @Test
    void presignCredentialsProvider_throwsOnStartup_whenOnlyAccessKeySet() {
        S3Config s3Config = new S3Config();
        ReflectionTestUtils.setField(s3Config, "presignAccessKey", "AKIATESTACCESSKEY");
        ReflectionTestUtils.setField(s3Config, "presignSecretKey", "");

        assertThatThrownBy(s3Config::presignCredentialsProvider)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be configured together");
    }

    @Test
    void presignCredentialsProvider_throwsOnStartup_whenOnlySecretKeySet() {
        S3Config s3Config = new S3Config();
        ReflectionTestUtils.setField(s3Config, "presignAccessKey", "");
        ReflectionTestUtils.setField(s3Config, "presignSecretKey", "test-secret-key");

        assertThatThrownBy(s3Config::presignCredentialsProvider)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be configured together");
    }
}
