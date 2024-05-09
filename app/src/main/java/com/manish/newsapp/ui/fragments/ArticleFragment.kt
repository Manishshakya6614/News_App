package com.manish.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.manish.newsapp.R
import com.manish.newsapp.databinding.FragmentArticleBinding
import com.manish.newsapp.ui.NewsActivity
import com.manish.newsapp.viewmodel.NewsViewModel

class ArticleFragment : Fragment(R.layout.fragment_article) {

    private lateinit var newsViewModel: NewsViewModel
    private lateinit var binding: FragmentArticleBinding

    // ArticleFragmentArgs is an automatically generated class by safe arguments plugin
    // basically this class has to pass arguments between fragments in a safe way
    // navArgs() is an extensive function provided by navigation component and it is used to retrieve the argument passed to a fragment
    private val args: ArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        val article = args.article


        binding.webView.apply {
            // webViewClient is responsible for handling various events in the webView when a new Url is about to be loaded
            webViewClient = WebViewClient()
            article?.url?.let { url ->
                loadUrl(url)
            }
        }

        binding.fab.setOnClickListener {
            newsViewModel.addToFavourites(article)
            // To inform the user that article has been added we will create a Snack bar
            Snackbar.make(view, "Added to Favourites", Snackbar.LENGTH_SHORT).show()
        }

    }
}