package uk.tanton.streaming.live.dynamo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Cache<U, T> {
    private static final Logger LOG = LogManager.getLogger(Cache.class);


    private final Function<U, T> valueLoader;
    private final LoadingCache<U, T> loadingCache;

    public Cache(Function<U, T> valueLoader) {
        this.valueLoader = valueLoader;
        loadingCache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<U, T>() {
                    @Override
                    public T load(U u) throws Exception {
                        LOG.info(String.format("Loading cache with value for {%s}", u.toString()));
                        return valueLoader.apply(u);
                    }
                });

        loadingCache.cleanUp();
    }

    public T get(U key) {
        try {
            return this.loadingCache.get(key);
        } catch (ExecutionException e) {
            return valueLoader.apply(key);
        }
    }

}
