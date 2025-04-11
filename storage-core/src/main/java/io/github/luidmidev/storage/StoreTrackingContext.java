package io.github.luidmidev.storage;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class StoreTrackingContext {

    private StoreTrackingContext() {
        throw new IllegalAccessError("Cannot instantiate this class");
    }

    private static final ThreadLocal<List<String>> STORED_THREAD_LOCAL = new ThreadLocal<>();

    public static void start() {
        STORED_THREAD_LOCAL.set(new ArrayList<>());
        log.debug("Started store tracking for current thread.");
    }

    static void track(String paths) {
        var stored = STORED_THREAD_LOCAL.get();
        if (stored != null) {
            stored.add(paths);
            log.debug("Tracked stored key: {}", paths);
        } else {
            log.debug("Tracking not started, skipping path: {}", paths);
        }
    }

    static void track(List<String> paths) {
        var stored = STORED_THREAD_LOCAL.get();
        if (stored != null) {
            stored.addAll(paths);
            log.debug("Tracked stored paths: {}", paths);
        } else {
            log.debug("Tracking not started, skipping paths: {}", paths);
        }
    }

    public static List<String> getTracked() {
        var stored = STORED_THREAD_LOCAL.get();
        return stored != null ? stored : List.of();
    }

    public static boolean isTracking() {
        return STORED_THREAD_LOCAL.get() != null;
    }

    public static void clear() {
        STORED_THREAD_LOCAL.remove();
        log.debug("Cleared store tracking context.");
    }
}
