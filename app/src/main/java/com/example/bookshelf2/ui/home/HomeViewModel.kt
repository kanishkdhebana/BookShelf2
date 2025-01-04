package com.example.bookshelf2.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf2.BookshelfApplication
import com.example.bookshelf2.data.BookshelfRepository
import com.example.bookshelf2.model.BookItem
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

sealed interface BookshelfUiState {
    data class Success(val books: List<BookItem>): BookshelfUiState
    data object Error: BookshelfUiState
    data object Loading: BookshelfUiState
}

class HomeViewModel(
    private val bookshelfRepository: BookshelfRepository
): ViewModel() {

    var bookshelfUiState: BookshelfUiState by mutableStateOf(BookshelfUiState.Loading)
        private set

    private var _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    private var lastSearchTime = System.currentTimeMillis()

    fun updateSearchTerm(newTerm: String) {
        _searchTerm.value = newTerm
        debounceSearch(newTerm)
    }

    private fun debounceSearch(query: String) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastSearchTime < 300) {
            return  // Avoid making the request if the last request was too recent
        }

        lastSearchTime = currentTime
        getBooks(query)
    }

    private fun getBooks(query: String) {
        viewModelScope.launch {
            bookshelfUiState = BookshelfUiState.Loading
            bookshelfUiState = try {
                val response = bookshelfRepository.searchBooks(query)

                val books: List<BookItem> = response.docs.map { bookItem ->
                    val uniqueKey = UUID.randomUUID().toString().hashCode()

                     BookItem(
                         coverId = bookItem.coverId,
                         id = uniqueKey,
                         title = bookItem.title,
                         authors = bookItem.authors,
                         firstPublishYear = bookItem.firstPublishYear
                    )
                }

                BookshelfUiState.Success(books)
            } catch (e: IOException) {
                BookshelfUiState.Error
            } catch (e: HttpException) {
                BookshelfUiState.Error
            }
        }
    }



    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]

                if (application is BookshelfApplication) {
                    val bookshelfRepository = application.container.bookshelfRepository
                    HomeViewModel(bookshelfRepository = bookshelfRepository)
                } else {
                    throw IllegalStateException("BookshelfViewModel not initialized properly")
                }
            }
        }
    }
}
