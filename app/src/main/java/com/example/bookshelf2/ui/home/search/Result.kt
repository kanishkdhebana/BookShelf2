package com.example.bookshelf2.ui.home.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.bookshelf2.model.BookItem
import com.example.bookshelf2.ui.components.BookCardWide
import com.example.bookshelf2.ui.components.BookShelfScaffold
import com.example.bookshelf2.ui.navigation.MainDestinations
import com.example.bookshelf2.ui.theme.BookShelfTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Result(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    searchViewModel: SearchViewModel,
    onBookSelect: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    val searchUiState by searchViewModel.searchUiState.collectAsState()

    BookShelfScaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = MainDestinations.RESULT,
                        style = MaterialTheme.typography.titleLarge,
                        color = BookShelfTheme.colors.selectedIconBorderFill
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BookShelfTheme.colors.iconInteractive
                        )
                    }
                },
                modifier = modifier,
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(
                        containerColor = BookShelfTheme.colors.uiBackground,
                        scrolledContainerColor = BookShelfTheme.colors.uiBackground
                    )
            )
        }
    ) { padding ->
        when(val state = searchUiState) {
            is SearchUiState.Loading -> {
                LoadingScreen(modifier.padding(padding))
            }

            is SearchUiState.Error -> {
                ErrorScreen(modifier.padding(padding), state)
            }

            is SearchUiState.Success -> {
                val books = state.books

                ResultScreen(
                    modifier = modifier.padding(padding),
                    books = books,
                    contentPadding = contentPadding,
                    searchViewModel = searchViewModel,
                    onBookSelect =  onBookSelect
                )
            }
        }
    }
}


@Composable
fun LoadingScreen(modifier: Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = BookShelfTheme.colors.selectedIconBorderFill
        )
    }
}

@Composable
fun ErrorScreen(modifier: Modifier, searchUiState: SearchUiState.Error) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: ${searchUiState.message}",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ResultScreen(
    modifier: Modifier,
    books: List<BookItem>,
    contentPadding: PaddingValues,
    searchViewModel: SearchViewModel,
    onBookSelect: (String) -> Unit,
) {
    if (books.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No results found.")
        }

    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(LayoutDirection.Ltr),
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = contentPadding.calculateBottomPadding() + 16.dp // Add extra bottom padding here
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(
                items = books,
                key = { book ->
                    book.id
                }
            ) { book ->
                Column {
                    BookCardWide(
                        book = book,
                        onClick = {
                            book.key?.let { book.coverId?.let { it1 ->
                                searchViewModel.updateBookKey(it,
                                    it1
                                )
                            } }
                            book.key?.let { onBookSelect(it) }

                        }
                    )
                }
            }
        }
    }
}
