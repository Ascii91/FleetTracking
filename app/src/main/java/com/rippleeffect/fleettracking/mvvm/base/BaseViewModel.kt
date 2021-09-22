package com.rippleeffect.fleettracking.mvvm.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseViewModel<T, T2> : ViewModel() {

    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        //Network error
        exception.printStackTrace()
        when (exception) {
            is HttpException -> {
                showNetworkError()
            }
            is HttpURLConnection -> {
                showNetworkError()
            }
            is ConnectException -> {
                showNetworkError()
            }
            is UnknownHostException -> {
                showNetworkError()
            }
            is SocketTimeoutException -> {
                showNetworkError()
            }
            else -> {
                showServerError()
            }
        }
    }


    protected lateinit var _viewState: MutableLiveData<T>
    protected lateinit var _viewAction: MutableLiveData<T2>

    val viewState: LiveData<T>
        get() {
            if (!::_viewState.isInitialized) {
                _viewState = MutableLiveData()
            }
            return _viewState
        }


    val viewAction: LiveData<T2>
        get() {
            if (!::_viewAction.isInitialized) {
                _viewAction = MutableLiveData()
            }
            return _viewAction
        }

    protected fun showNetworkError() {

    }

    protected fun showServerError() {

    }


}