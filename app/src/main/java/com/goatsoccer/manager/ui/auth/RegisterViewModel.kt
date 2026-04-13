package com.goatsoccer.manager.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.model.InviteLookupResult
import com.goatsoccer.manager.data.repository.AuthRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<Resource<String>>()
    val registerResult: LiveData<Resource<String>> = _registerResult

    private val _inviteLookup = MutableLiveData<InviteLookupResult?>()
    val inviteLookup: LiveData<InviteLookupResult?> = _inviteLookup

    fun lookupInvite(email: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            _inviteLookup.value = authRepository.lookupInvite(email)
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String, role: String) {
        val isInvited = inviteLookup.value?.found == true
        when {
            !isInvited && name.isBlank() -> { _registerResult.value = Resource.Error("Name is required"); return }
            email.isBlank() -> { _registerResult.value = Resource.Error("Email is required"); return }
            password.isBlank() -> { _registerResult.value = Resource.Error("Password is required"); return }
            password != confirmPassword -> { _registerResult.value = Resource.Error("Passwords do not match"); return }
            password.length < 6 -> { _registerResult.value = Resource.Error("Password must be at least 6 characters"); return }
        }
        // For invited players the name comes from the invite lookup, role is always "player"
        val finalName = if (isInvited) (inviteLookup.value?.name ?: name) else name
        val finalRole = if (isInvited) "player" else role
        _registerResult.value = Resource.Loading()
        viewModelScope.launch {
            _registerResult.value = authRepository.register(finalName, email, password, finalRole)
        }
    }
}
