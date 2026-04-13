package com.goatsoccer.manager.ui.roasts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goatsoccer.manager.data.model.Roast
import com.goatsoccer.manager.data.repository.RoastRepository
import com.goatsoccer.manager.util.Resource
import kotlinx.coroutines.launch

class RoastsViewModel(private val roastRepository: RoastRepository) : ViewModel() {

    private val _roasts = MutableLiveData<Resource<List<Roast>>>()
    val roasts: LiveData<Resource<List<Roast>>> = _roasts

    private val _postResult = MutableLiveData<Resource<Roast>>()
    val postResult: LiveData<Resource<Roast>> = _postResult

    init {
        fetchRoasts()
    }

    fun fetchRoasts() {
        _roasts.value = Resource.Loading()
        doFetchRoasts()
    }

    fun silentRefresh() = doFetchRoasts()

    private fun doFetchRoasts() {
        viewModelScope.launch {
            _roasts.value = roastRepository.getRoasts()
        }
    }

    fun postRoast(targetType: String, targetId: String, content: String) {
        if (content.isBlank()) { _postResult.value = Resource.Error("Roast cannot be empty"); return }
        _postResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = roastRepository.createRoast(targetType, targetId, content)
            _postResult.value = result
            if (result is Resource.Success) fetchRoasts()
        }
    }

    fun deleteRoast(id: String) {
        viewModelScope.launch {
            roastRepository.deleteRoast(id)
            fetchRoasts()
        }
    }
}
