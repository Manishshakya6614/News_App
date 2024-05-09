package com.manish.newsapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.manish.newsapp.models.Article
import com.manish.newsapp.models.NewsResponse
import com.manish.newsapp.repository.NewsRepository
import com.manish.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(app: Application, val newsRepository: NewsRepository): AndroidViewModel(app) {

    // variable for displaying headlines
    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    // variable to track the current page number for the headline pagination
    var headlinesPage = 1
    // variable to store the last received response for headlines
    var headlinesResponse: NewsResponse? = null

    // variable for displaying search results
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    // variable to track the current page number for search result pagination
    var searchNewsPage = 1
    // variable to store the last received response for search results
    var searchNewsResponse: NewsResponse? = null
    // variable to store the new search query
    var newSearchQuery: String? = null
    // variable to store previous search query
    var oldSearchQuery: String? = null

    init {
        getHeadlines("in")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = resultResponse
                } else {
                    val oldArticles = headlinesResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    // it checks if the network request was successful, if successful then check if searchNewsResponse is null or
    // if the search query has changed and if true, then reset the page count to one, then update the searchQuery and
    // assign the result to searchNewsResponse
    // else increment the page count and merge the new articles with the existing articles in searchNewsResponse
    // then return a resource success containing the updated search news response and lastly if the network request was not successful
    // then return a resource error with error message
    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        newsRepository.insertArticle(article)
    }

    fun getFavouritesNews() = newsRepository.getFavNews()

    fun deleteArticles(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    // Function to check device has active internet connection or not
    fun hasInternetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    // This function checks internet connection specifically for headlines
    // this function fetches headlines based on country code
    // resource loading post a loading state to the headlines live data indicating that a network request is in progress
    // then it checks if there is an internet connection and if there is internet, then response will store the headlines fetched
    // from the repository and post the result to headlines live data after processing the response
    // but it there is no internet, then it post an error state to the headlines live data
    private suspend fun headlinesInternet(countryCode: String) {
        headlines.postValue(Resource.Loading())
        try {
            if (hasInternetConnection(this.getApplication())) {
                val response = newsRepository.getHeadline(countryCode, headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }

    // This function handles internet connection specifically for search News
    // This is same as headlines one
    private suspend fun searchNewsInternet(searchQuery: String) {
        newSearchQuery = searchQuery // this line updates the new searchQuery Variable with the provided search Query
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection(this.getApplication())) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect"))
                else -> searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }
}








