import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class VersionedStore {
    private static class Version {
        long timestamp; String value; boolean deleted;
        Version(long timestamp, String value, boolean deleted) {
            this.timestamp = timestamp; this.value = value; this.deleted = deleted;
        }
    }

    private final Map<String, List<Version>> store = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void set(String key, String value, long timestamp) {
        lock.writeLock().lock();
        try {
            store.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(timestamp, value, false));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void delete(String key, long timestamp) {
        lock.writeLock().lock();
        try {
            store.computeIfAbsent(key, k -> new ArrayList<>()).add(new Version(timestamp, null, true));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String get(String key, long timestamp) {
        lock.readLock().lock();          // many readers may hold this at once
        try {
            List<Version> versions = store.get(key);
            if (versions == null) return null;
            int lo = 0, hi = versions.size() - 1;
            Version found = null;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (versions.get(mid).timestamp <= timestamp) {
                    found = versions.get(mid);
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            return (found == null || found.deleted) ? null : found.value;
        } finally {
            lock.readLock().unlock();
        }
    }
}
