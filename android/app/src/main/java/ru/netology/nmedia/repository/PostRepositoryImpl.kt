package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit


class PostRepositoryImpl: PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }


    override fun getAll(callback: PostRepository.Callback<List<Post>>) {
        val request: Request = Request.Builder().url("${BASE_URL}/api/slow/posts").build()

        requestAndParsePosts(request, callback)
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder().post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts").build()

        requestAndParsePost(request, callback)
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Unit>) {
        val request: Request =
            Request.Builder().delete().url("${BASE_URL}/api/slow/posts/$id").build()
        request(request, callback)
    }

    override fun likeById(id: Long, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder().post(gson.toJson(id).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts/$id/likes").build()
        requestAndParsePost(request, callback)
    }

    override fun unlikeById(id: Long, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder().delete(gson.toJson(id).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts/$id/likes").build()
        requestAndParsePost(request, callback)
    }

    private fun requestAndParsePost(request: Request, callback: PostRepository.Callback<Post>) {
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    callback.onSuccess(gson.fromJson(body, Post::class.java))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    private fun requestAndParsePosts(
        request: Request, callback: PostRepository.Callback<List<Post>>
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    callback.onSuccess(gson.fromJson(body, typeToken.type))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    private fun request(request: Request, callback: PostRepository.Callback<Unit>) {
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(Unit)
                response.close()
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }
}