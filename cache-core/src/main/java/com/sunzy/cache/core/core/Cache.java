package com.sunzy.cache.core.core;

import com.sunzy.cache.annotation.CacheInterceptor;
import com.sunzy.cache.api.ICache;
import com.sunzy.cache.api.ICacheEvict;
import com.sunzy.cache.core.evict.CacheEvictContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Cache<K,V> implements ICache<K,V> {

    /**
     * map信息
     */
    private Map<K,V> map;

    /**
     * 缓存大小限制
     */
    private int sizeLimit;


    /**
     * 驱除策略
     * @since 0.0.2
     */
    private ICacheEvict<K,V> evict;

    public Cache(CacheContext<K, V> context) {
        this.map = context.map();
        this.sizeLimit = context.size();
        this.evict = context.cacheEvict();
    }

    public Cache(Map<K, V> map, int sizeLimit, ICacheEvict<K, V> evict) {
        this.map = map;
        this.sizeLimit = sizeLimit;
        this.evict = evict;
    }

    /**
     * 设置 map 实现
     * @param map 实现
     * @return this
     */
    public Cache<K, V> map(Map<K, V> map) {
        this.map = map;
        return this;
    }

    /**
     * 设置大小限制
     * @param sizeLimit 大小限制
     * @return this
     */
    public Cache<K, V> sizeLimit(int sizeLimit) {
        this.sizeLimit = sizeLimit;
        return this;
    }

    /**
     * 是否已经达到大小最大的限制
     * @return 是否限制
     * @since 0.0.2
     */
    private boolean isSizeLimit() {
        final int currentSize = this.size();
        return currentSize >= this.sizeLimit;
    }

    @Override
    @CacheInterceptor(refresh = true)
    public int size() {
        return map.size();
    }

    @Override
    @CacheInterceptor(refresh = true)
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @CacheInterceptor(refresh = true, evict = true)
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    @CacheInterceptor(refresh = true)
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }


    @Override
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * 重写hashMap的put方法，增加了驱逐策略，会在添加元素之前进行检查
     * @param key
     * @param value
     * @return
     */
    @Override
    public V put(K key, V value) {
        //1.1 尝试驱除
        CacheEvictContext<K,V> context = new CacheEvictContext<>();
        context.key(key).size(sizeLimit).cache(this);
        evict.evict(context);
        //2. 判断驱除后的信息
        if(isSizeLimit()) {
            throw new RuntimeException("当前队列已满，数据添加失败！");
        }
        //3. 执行添加
        return map.put(key, value);
    }



    @Override
    @CacheInterceptor(aof = true, evict = true)
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    @CacheInterceptor(aof = true)
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    @CacheInterceptor(refresh = true, aof = true)
    public void clear() {
        map.clear();
    }

    @Override
    @CacheInterceptor(refresh = true)
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    @CacheInterceptor(refresh = true)
    public Collection<V> values() {
        return map.values();
    }

    @Override
    @CacheInterceptor(refresh = true)
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

}