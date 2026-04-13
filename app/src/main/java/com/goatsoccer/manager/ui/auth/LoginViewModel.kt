package com.goatsoccer.manager.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.repository.AuthRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Resource<String>>()
    val loginResult: LiveData<Resource<String>> = _loginResult

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = Resource.Error("Email and password are required")
            return
        }
        _loginResult.value = Resource.Loading()
        viewModelScope.launch {
            _loginResult.value = authRepository.login(email, password)
        }
    }

    fun isLoggedIn() = authRepository.isLoggedIn()
}
