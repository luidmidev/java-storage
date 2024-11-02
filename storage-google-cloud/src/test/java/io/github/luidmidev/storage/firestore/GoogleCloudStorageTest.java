package io.github.luidmidev.storage.firestore;

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
            var id = googleCloudStorage.store(resource, "test.txt");
            log.info("Stored file with id {}", id);

            assertNotNull(id);
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
            var id = googleCloudStorage.store(resource, fileName, "test/");
            log.info("Stored file with path with id {}", id);

            assertNotNull(id);
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
            var fullPath = googleCloudStorage.store(resource, fileName);
            log.info("Stored file to download with id {}", fullPath);

            var downloaded = googleCloudStorage.download(fullPath);
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
            var id = googleCloudStorage.store(resource, "to-info" + fileName);
            log.info("Stored file to get info with id {}", id);

            var info = googleCloudStorage.info(id);
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
            var id = googleCloudStorage.store(resource, "to-exists" + fileName);
            log.info("Stored file to check exists with id {}", id);

            assertTrue(googleCloudStorage.exists(id));
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
            var id = googleCloudStorage.store(resource, "to-remove" + fileName);
            log.info("Stored file to remove with id {}", id);

            googleCloudStorage.remove(id);

            assertFalse(googleCloudStorage.exists(id));
        } catch (IOException e) {
            fail(e);
        }
    }

    private static InputStream getResource(String resource) {
        return GoogleCloudStorageTest.class.getClassLoader().getResourceAsStream(resource);
    }
}