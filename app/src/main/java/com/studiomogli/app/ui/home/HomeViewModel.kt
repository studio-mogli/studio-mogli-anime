package com.studiomogli.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studiomogli.app.data.model.AnimeItem
import com.studiomogli.app.data.model.EpisodeItem
import com.studiomogli.app.data.model.GitHubUpdateInfo
import com.studiomogli.app.data.model.StreamResult
import com.studiomogli.app.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = AnimeRepository()

    private val _trending = MutableStateFlow<List<AnimeItem>>(emptyList())
    val trending: StateFlow<List<AnimeItem>> = _trending

    private val _popular = MutableStateFlow<List<AnimeItem>>(emptyList())
    val popular: StateFlow<List<AnimeItem>> = _popular

    private val _topRated = MutableStateFlow<List<AnimeItem>>(emptyList())
    val topRated: StateFlow<List<AnimeItem>> = _topRated

    private val _searchResults = MutableStateFlow<List<AnimeItem>>(emptyList())
    val searchResults: StateFlow<List<AnimeItem>> = _searchResults

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedAnime = MutableStateFlow<AnimeItem?>(null)
    val selectedAnime: StateFlow<AnimeItem?> = _selectedAnime

    private val _episodes = MutableStateFlow<List<EpisodeItem>>(emptyList())
    val episodes: StateFlow<List<EpisodeItem>> = _episodes

    private val _isLoadingEpisodes = MutableStateFlow(false)
    val isLoadingEpisodes: StateFlow<Boolean> = _isLoadingEpisodes

    private val _currentStream = MutableStateFlow<StreamResult?>(null)
    val currentStream: StateFlow<StreamResult?> = _currentStream

    private val _preferredLangCode = MutableStateFlow("en")
    val preferredLangCode: StateFlow<String> = _preferredLangCode

    private val _updateInfo = MutableStateFlow<GitHubUpdateInfo?>(null)
    val updateInfo: StateFlow<GitHubUpdateInfo?> = _updateInfo

    init {
        loadHomeFeed()
        checkUpdates()
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            _trending.value = repository.fetchCategory("TRENDING_DESC")
            _popular.value = repository.fetchCategory("POPULARITY_DESC")
            _topRated.value = repository.fetchCategory("SCORE_DESC")
            _isLoading.value = false
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searchResults.value = repository.searchAnime(query)
        }
    }

    fun selectAnime(anime: AnimeItem) {
        _selectedAnime.value = anime
        _episodes.value = emptyList()
        viewModelScope.launch {
            _isLoadingEpisodes.value = true
            _episodes.value = repository.fetchEpisodesMultiProvider(anime)
            _isLoadingEpisodes.value = false
        }
    }

    fun playEpisode(episode: EpisodeItem, audioCategory: String, langCode: String, onReady: () -> Unit) {
        _preferredLangCode.value = langCode
        viewModelScope.launch {
            val result = repository.fetchStreamUrl(episode, category = audioCategory)
            _currentStream.value = result
            onReady()
        }
    }

    private fun checkUpdates() {
        viewModelScope.launch {
            _updateInfo.value = repository.checkForGitHubUpdate()
        }
    }
}
