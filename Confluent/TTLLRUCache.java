package Confluent;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class TTLLRUCache<K, V> {

    private final int capacity;
    private final long defaultTTL;
    private final ConcurrentHashMap<K, CacheEntry<V>> map;
    private final LinkedHashMap<K, Long> lruOrder;
    private final ReentrantLock lock = new ReentrantLock();

    private static class CacheEntry<V> {
        final V value;
        final long expiresAt;
        CacheEntry(V value, long ttlMillis) {
            this.value = value;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public TTLLRUCache(int capacity, long defaultTTL) {
        this.capacity = capacity;
        this.defaultTTL = defaultTTL;
        this.map = new ConcurrentHashMap<>(capacity);
        this.lruOrder = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    public V get(K key) {
        CacheEntry<V> entry = map.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            remove(key);
            return null;
        }

        // update LRU order
        lock.lock();
        try {
            lruOrder.get(key); // triggers access-order update
        } finally {
            lock.unlock();
        }

        return entry.value;
    }

    public void put(K key, V value, long ttlMillis) {
        long ttl = ttlMillis > 0 ? ttlMillis : defaultTTL;
        CacheEntry<V> entry = new CacheEntry<>(value, ttl);
        map.put(key, entry);

        lock.lock();
        try {
            lruOrder.put(key, System.currentTimeMillis());
            evictIfNeeded();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        evictExpired();
        return map.size();
    }

    private void evictIfNeeded() {
        evictExpired();
        while (map.size() > capacity) {
            Iterator<K> it = lruOrder.keySet().iterator();
            if (it.hasNext()) {
                K oldestKey = it.next();
                it.remove();
                map.remove(oldestKey);
            } else break;
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<K, CacheEntry<V>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, CacheEntry<V>> e = it.next();
            if (e.getValue().expiresAt <= now) {
                it.remove();
                lock.lock();
                try {
                    lruOrder.remove(e.getKey());
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private void remove(K key) {
        map.remove(key);
        lock.lock();
        try {
            lruOrder.remove(key);
        } finally {
            lock.unlock();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        TTLLRUCache<Integer, String> cache = new TTLLRUCache<>(3, 2000); // 2s TTL

        cache.put(1, "A", 1000);
        cache.put(2, "B", 3000);
        cache.put(3, "C", 2000);

        System.out.println(cache.get(1)); // "A"
        Thread.sleep(1500);
        System.out.println(cache.get(1)); // null (expired)
        cache.put(4, "D", 2000);           // triggers LRU eviction
        System.out.println(cache.get(2)); // "B" (still valid)
        System.out.println(cache.get(3)); // "C"
        System.out.println(cache.get(4)); // "D"
    }
}
