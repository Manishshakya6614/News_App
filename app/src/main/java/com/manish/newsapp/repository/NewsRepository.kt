package com.manish.newsapp.repository

import com.manish.newsapp.api.RetrofitInstance
import com.manish.newsapp.db.ArticleDatabase
import com.manish.newsapp.models.Article

class NewsRepository(val db: ArticleDatabase) {

    suspend fun getHeadline(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getHeadLines(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    suspend fun insertArticle(article: Article) = db.getArticleDao().insert(article)

    fun getFavNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}