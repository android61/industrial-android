package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.lang.Exception

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {

        // Начинаем загрузку
        _data.value = FeedModel(loading = true)
        repository.getAll(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(value: List<Post>) {
                _data.postValue(FeedModel(posts = value, empty = value.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })


    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(value: Post) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Exception) {
                    Log.e("Error", "post ${it.id} couldn't be saved: $e")
                }
            })
        }
        edited.value = empty
    }


    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        repository.likeById(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(value: Post) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty()
                            .map {
                                if (it.id == id) value else it
                            }
                    )
                )

            }

            override fun onError(e: Exception) {
                Log.e("Error", "post $id couldn't be liked: $e")
            }
        })
    }


    fun unlikeById(id: Long) {
        repository.unlikeById(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(value: Post) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty()
                            .map {
                                if (it.id == id) value else it
                            }
                    )
                )
            }


            override fun onError(e: Exception) {
                Log.e("Error", "post $id couldn't be unliked: $e")
            }
        })
    }


    fun removeById(id: Long) {
        repository.removeById(id, object : PostRepository.Callback<Unit> {
            val old = _data.value?.posts.orEmpty()
            override fun onSuccess(value: Unit) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }

        })
    }
    fun edit(post: Post) {
        edited.value = post
    }


}