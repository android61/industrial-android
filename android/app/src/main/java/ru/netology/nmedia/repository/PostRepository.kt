package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    fun getAll(callback: Callback<List<Post>>)
    fun save(post: Post, callback: Callback<Post>)
    fun removeById(id: Long, callback: Callback<Unit>)
    fun likeById(id: Long, callback: Callback<Post>)
    fun unlikeById(id: Long, callback: Callback<Post>)


    interface Callback<T> {
        fun onSuccess(result: T)
        fun onError(exception: Exception)
    }
}