package com.goatsoccer.manager.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.local.SessionManager
import com.goatsoccer.manager.data.model.User
import com.goatsoccer.manager.data.repository.AuthRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profile = MutableLiveData<Resource<User>>()
    val profile: LiveData<Resource<User>> = _profile

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _deactivateResult = MutableLiveData<Resource<Unit>>()
    val deactivateResult: LiveData<Resource<Unit>> = _deactivateResult

    val userRole: String get() = sessionManager.getUserRole() ?: "fan"

    init {
        loadProfile()
    }

    fun loadProfile() {
        _profile.value = Resource.Loading()
        viewModelScope.launch {
            _profile.value = authRepository.getMe()
        }
    }

    fun saveProfile(name: String) {
        if (name.isBlank()) return
        sessionManager.saveUser(
            id     = sessionManager.getUserId() ?: "",
            name   = name,
            email  = sessionManager.getUserEmail() ?: "",
            role   = sessionManager.getUserRole() ?: "fan",
            teamId = sessionManager.getMyTeamId()
        )
        _saveSuccess.value = true
    }

    fun deactivateAccount() {
        _deactivateResult.value = Resource.Loading()
        viewModelScope.launch {
            _deactivateResult.value = authRepository.deactivateAccount()
        }
    }
}
