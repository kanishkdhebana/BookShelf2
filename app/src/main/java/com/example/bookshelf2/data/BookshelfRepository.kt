package com.example.bookshelf2.data

import com.example.bookshelf2.model.BookSearchResponse
import com.example.bookshelf2.network.BookShelfApiService
import retrofit2.Call

interface BookshelfRepository {
    suspend fun searchBooks(query: String): BookSearchResponse
}

class NetworkBookShelfRepository(
    private val bookShelfApiService: BookShelfApiService
): BookshelfRepository {
    override suspend fun searchBooks(query: String): BookSearchResponse =
        bookShelfApiService.searchBooks(query, 1)

}
