package com.brentvatne.common.toolbox

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object RNVSimpleCache {
    // TODO: when to release? how to check if cache is released?
    var simpleCache: SimpleCache? = null
    var cacheDataSourceFactory: DataSource.Factory? = null

    fun setSimpleCache(context: Context, cacheSize: Int, factory: HttpDataSource.Factory) {
        if (cacheDataSourceFactory != null || cacheSize == 0) return
        simpleCache = SimpleCache(
            File(context.cacheDir, "RNVCache"),
            LeastRecentlyUsedCacheEvictor(
                cacheSize.toLong() * 1024 * 1024
            ),
            StandaloneDatabaseProvider(context)
        )
        cacheDataSourceFactory =
            CacheDataSource.Factory()
                .setCache(simpleCache!!)
                .setUpstreamDataSourceFactory(factory)
    }
}
