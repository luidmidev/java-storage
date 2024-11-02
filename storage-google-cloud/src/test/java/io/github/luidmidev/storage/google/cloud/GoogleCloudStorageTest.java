package io.github.luidmidev.storage.google.cloud;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class GoogleCloudStorageTest {


    private static GoogleCloudStorage googleCloudStorage;

    @BeforeAll
    static void setUp() throws IOException {
        try (var resourceCredentials = getResource("firebase/serviceAccountKey.json")) {

            if (resourceCredentials == null) {
                throw new IllegalArgumentException("File not found");
            }

            var credentials = GoogleCredentials.fromStream(resourceCredentials);

            var options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            FirebaseApp.initializeApp(options);

            var bucket = StorageClient.getInstance().bucket("umwmec.appspot.com");
            googleCloudStorage = new GoogleCloudStorage(bucket);

        }
    }

    @Test
    void store() {
        log.info("Storing file");
        try (var resource = getResource("test-file.webp")) {
            if (resource == null) {
                throw new IllegalArgumentException("File not found");
            }
            googleCloudStorage.store(resource, "test.txt");

            assertTrue(true);
        } catch (IOException e) {
            log.error("Error storing file {}", e.getMessage());
            fail(e);
        }

    }

    @Test
    void storeWithPath() {

        var fileName = "test-file.webp";
        try (var resource = getResource(fileName)) {
            if (resource == null) {
                throw new IllegalArgumentException("File not found");
            }

            googleCloudStorage.store(resource, fileName, "test/");

            assertTrue(true);
        } catch (IOException e) {
            fail(e);
        }

    }

    @Test
    void download() {

        var fileName = "to-download.webp";
        try (var resource = getResource(fileName)) {
            if (resource == null) {
                throw new IllegalArgumentException("File not found");
            }

            googleCloudStorage.store(resource, fileName);

            var downloaded = googleCloudStorage.download(fileName);
            assertTrue(downloaded.isPresent());

            var fileInfo = downloaded.get().getInfo();
            log.info("Downloaded file with info {}", fileInfo);

            try (var fos = new FileOutputStream("downloaded-" + fileName)) {
                fos.write(downloaded.get().getContent());
            }

            assertNotNull(fileInfo);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void info() {

        var fileName = "test-file.webp";
        try (var resource = getResource(fileName)) {
            if (resource == null) {
                throw new IllegalArgumentException("File not found");
            }

            googleCloudStorage.store(resource, "to-info" + fileName);

            var info = googleCloudStorage.info("to-info" + fileName);
            assertTrue(info.isPresent());

            log.info("File info {}", info.get());

            assertNotNull(info.get());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void exists() {

        var fileName = "test-file.webp";
        try (var resource = getResource(fileName)) {
            if (resource == null) {
                throw new IllegalArgumentException("File not found");
            }

            googleCloudStorage.store(resource, "to-exists" + fileName);

            assertTrue(googleCloudStorage.exists("to-exists" + fileName));
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void remove() {

        var fileName = "test-file.webp";
        try (var resource = getResource(fileName)) {
            if (resource == null) {
                throw new IllegalArgumentException("File not found");
            }
            googleCloudStorage.store(resource, "to-remove" + fileName);

            googleCloudStorage.remove("to-remove" + fileName);

            assertFalse(googleCloudStorage.exists("to-remove" + fileName));
        } catch (IOException e) {
            fail(e);
        }
    }

    private static InputStream getResource(String resource) {
        return GoogleCloudStorageTest.class.getClassLoader().getResourceAsStream(resource);
    }
}