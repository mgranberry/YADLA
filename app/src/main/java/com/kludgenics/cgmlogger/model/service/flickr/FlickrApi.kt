package com.kludgenics.cgmlogger.model.service.flickr

import com.squareup.okhttp.OkHttpClient
import org.jetbrains.anko.AnkoLogger

import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.android.AndroidLog
import retrofit.client.OkClient
import retrofit.http.GET
import retrofit.http.Query

public abstract class FlickrSearch(private val client: OkClient): AnkoLogger {
    companion object {
        private val SERVER_URL = "https://api.flickr.com"
        private val API_KEY = "c4b6a207da7ffbf0b9e88ea5f42e8d04"
    }

    public data class SearchResult(val photos: SearchPhotos)
    public data class SearchPhotos(val photo: List<SearchPhotoResult>)
    public data class SearchPhotoResult(val id: String, val secret: String)
    public data class PhotoInfoResult(val photo: PhotoInfo)
    public data class PhotoInfo(val id: String, val farm: Int, val server: String, val secret: String, val owner: OwnerInfo)
    public data class OwnerInfo(val nsid: String, val realname: String)
    public data class Photo(val photoId: String, val photoUri: String, val ownerName: String, val attributionUri: String) {
        constructor (info: PhotoInfo): this(info.id,
                "https://farm${info.farm}.staticflickr.com/${info.server}/${info.id}_${info.secret}.jpg",
                info.owner.realname,
                "https://www.flickr.com/photos/${info.owner.nsid}/${info.id}")
    }

    interface FlickrApi {
        @GET("/services/rest/?method=flickr.photos.search&in_gallery=1&license=4,5,6,7,8&sort=relevance&content_type=1&media=photos")
        public fun search(@Query("text") search: String): SearchResult
        @GET("/services/rest/?method=flickr.photos.getInfo")
        public fun getInfo(@Query("photo_id") photoId: String, @Query("secret") photoSecret: String): PhotoInfoResult
        @GET("/services/rest/?method=flickr.photos.getInfo")
        public fun getInfo(@Query("photo_id") photoId: String): PhotoInfoResult
    }


    protected fun getFlickrSearch(): FlickrApi {
        return getRestAdapter().create<FlickrApi>(FlickrApi::class.java)
    }

    private fun getRestAdapter(): RestAdapter {
        val restAdapter = RestAdapter.Builder()
                .setEndpoint(SERVER_URL)
                .setRequestInterceptor { request ->
                    request.addQueryParam("api_key", API_KEY)
                    request.addQueryParam("format", "json")
                    request.addQueryParam("nojsoncallback", "1")
                }
                .setLogLevel(RestAdapter
                .LogLevel.FULL)
                .setLog(AndroidLog(loggerTag))
                .setClient(client)
                .build()
        return restAdapter
    }

}