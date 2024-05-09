package com.manish.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.manish.newsapp.util.Constants
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.manish.newsapp.R
import com.manish.newsapp.adapters.NewsAdapter
import com.manish.newsapp.databinding.FragmentHeadlinesBinding
import com.manish.newsapp.ui.NewsActivity
import com.manish.newsapp.util.Resource
import com.manish.newsapp.viewmodel.NewsViewModel

class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {

    lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentHeadlinesBinding

    // Both of these UI Components are a part of item_error layout
    private lateinit var retryButton: Button
    private lateinit var errorText: TextView

    private lateinit var itemHeadlinedError: CardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlinesBinding.bind(view)

        itemHeadlinedError = view.findViewById(R.id.itemHeadlinesError)

        // This is how we inflate another layout in fragment
        // We are doing this to access retryButton and errorText which is present in item_error layout
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)
        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        newsViewModel = (activity as NewsActivity).newsViewModel
        setUpHeadlinedRecyclerView()

       // When the user click on the news item, it will lead the user to web view
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(R.id.action_headlinesFragment_to_articleFragment,bundle)
        }

        // Conditions like if the response is success or error or loading, then what are we supposed to do
        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        // if the response is non null, then updates the data newsAdapter by submitting the list of articles from newsResponse to the adapter
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headlinesPage == totalPages
                        if (isLastPage) {
                            binding.recyclerHeadlines.setPadding(0, 0 ,0, 0)
                        }
                    }
                }
                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(activity, "Sorry Error: $it", Toast.LENGTH_LONG).show()
                        showErrorMessage(it)
                    }
                }
                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })
        retryButton.setOnClickListener {
            newsViewModel.getHeadlines("in")
        }
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private fun hideProgressBar() {
        // Hide progress bar when loading is done
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }
    private fun showProgressBar() {
        // Show progress bar when data is getting fetched
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    // This function is required when internet is working perfectly fine and hence hide the error message
    private fun hideErrorMessage() {
        itemHeadlinedError.visibility = View.INVISIBLE
        isError = false
    }
    private fun showErrorMessage(message: String) {
        itemHeadlinedError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    // Pagination helps to load and display small chunks of data at a time
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        // this method is called after the scroll is completed
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                newsViewModel.getHeadlines("in")
                isScrolling = false
            }
        }
        // this method is called when the scroll state changes
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setUpHeadlinedRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@HeadlinesFragment.scrollListener)
        }
    }
}