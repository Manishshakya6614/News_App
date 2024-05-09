package com.manish.newsapp.util

sealed class Resource<T> (
    val data: T? = null, // this variable holds the result data of the operation that could be success or error
    val message: String? = null // message variable to store an error message in case of error
) {

    // Success class which is a subclass of Resource representing the Success state
    // it has a constructor that takes the result data (i.e.,data) and passes it to the Super class Resource
    class Success<T>(data: T): Resource<T>(data)

    // Error class which is a subclass of Resource representing the Error state
    // it has a constructor that takes the error message and result data (i.e.,data) and passes it to the Super class Resource
    class Error<T>(message: String, data: T? = null): Resource<T>(data, message)

    // Loading class which is a subclass of Resource representing the Loading state
    // it does not have any property, all it does is to indicate the super class that data is still being fetched
    class Loading<T>: Resource<T>()

}