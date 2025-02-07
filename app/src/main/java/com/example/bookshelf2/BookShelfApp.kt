@file:OptIn(
    ExperimentalSharedTransitionApi::class
)

package com.example.bookshelf2

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.bookshelf2.ui.components.BookShelfScaffold
import com.example.bookshelf2.ui.components.BookShelfSnackbar
import com.example.bookshelf2.ui.components.rememberBookShelfScaffoldState
import com.example.bookshelf2.ui.home.BookShelfBottomBar
import com.example.bookshelf2.ui.home.HomeSections
import com.example.bookshelf2.ui.home.addHomeGraph
import com.example.bookshelf2.ui.home.bookdetails.BookDetails
import com.example.bookshelf2.ui.home.bookdetails.nonSpatialExpressiveSpring
import com.example.bookshelf2.ui.home.bookdetails.spatialExpressiveSpring
import com.example.bookshelf2.ui.home.composableWithCompositionLocal
import com.example.bookshelf2.ui.home.search.Result
import com.example.bookshelf2.ui.home.search.SearchViewModel
import com.example.bookshelf2.ui.navigation.MainDestinations
import com.example.bookshelf2.ui.navigation.rememberBookShelfNavController
import com.example.bookshelf2.ui.theme.BookShelfTheme

@Composable
fun BookShelfApp() {
    BookShelfTheme {
        val bookShelfNavController = rememberBookShelfNavController()
        val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory)

        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this
            ) {
                NavHost(
                    navController = bookShelfNavController.navController,
                    startDestination = MainDestinations.HOME_ROUTE
                ) {
                    composableWithCompositionLocal(
                        route = MainDestinations.HOME_ROUTE
                    ) {
                        MainContainer(
                            modifier = Modifier,
                            //onBookSelected = {  }, //bookShelfNavController::navigateToBookDetail,
                            onSearchClicked = bookShelfNavController::navigateToResult,
                            searchViewModel = searchViewModel
                        )
                    }

                    composableWithCompositionLocal(
                        route = "${MainDestinations.RESULT}/{query}",
                        arguments = listOf(
                            navArgument("query") { type = NavType.StringType }
                        )
                    ) {
                        Result(
                            searchViewModel = searchViewModel,
                            onBookSelect = { bookKey ->
                                bookShelfNavController.navigateToBookDetail(bookKey, it)
                            },
                            onBackPressed = { bookShelfNavController.upPress() }
                        )
                    }

                    composableWithCompositionLocal(
                        route = "${MainDestinations.BOOK_DETAIL_ROUTE}/works/{${MainDestinations.BOOK_ID_KEY}}",
                        arguments = listOf(
                            navArgument(MainDestinations.BOOK_ID_KEY) {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        BookDetails(
                            searchViewModel = searchViewModel,
                            onBackPressed = { bookShelfNavController.upPress() }
                        )
                    }

//                    composableWithCompositionLocal(
//                        route = "${MainDestinations.BOOK_DETAIL_ROUTE}/{}",
//                        arguments = listOf(
//                            navArgument("coverId") { type = NavType.IntType }
//                        )
//                    ) {
//                        BookDetails(
//                            searchViewModel = searchViewModel,
//                            coverId = it.arguments?.getInt("coverId") ?: 0,
//                        )
//                    }

//                    composableWithCompositionLocal(
//                        route = "${MainDestinations.BOOK_DETAIL_ROUTE}/" +
//                                "{${MainDestinations.BOOK_ID_KEY}}" +
//                                "?origin={${MainDestinations.ORIGIN}}",
//
//                        arguments = listOf(
//                            navArgument(MainDestinations.BOOK_ID_KEY) {
//                                type = NavType.LongType
//                            }
//                        ),
//                    ) { backStackEntry ->
//                        val arguments = requireNotNull(backStackEntry.arguments)
//                        val bookId = arguments.getLong(MainDestinations.BOOK_ID_KEY)
//                        val origin = arguments.getString(MainDestinations.ORIGIN)
//
//                        BookDetails(
//                            bookId = bookId,
//                            origin = origin ?: "",
//                            upPress = bookShelfNavController::upPress
//                        )
//                    }
                }
            }
        }
    }
}


@Composable
fun MainContainer(
    modifier: Modifier = Modifier,
    //onBookSelected: (Long, String, NavBackStackEntry) -> Unit,
    searchViewModel: SearchViewModel,
    onSearchClicked: (String, NavBackStackEntry) -> Unit
) {
    val scaffoldState = rememberBookShelfScaffoldState()
    val nestedNavController = rememberBookShelfNavController()
    val navBackStackEntry by nestedNavController.navController.currentBackStackEntryAsState()
    val currentRoute =  navBackStackEntry?.destination?.route
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    BookShelfScaffold(
        topBar = {
            CustomTopBar(
                currentRoute = currentRoute ?: HomeSections.FEED.route
            )
        }
        ,
        bottomBar = {
            with(animatedVisibilityScope) {
                with(sharedTransitionScope) {
                    BookShelfBottomBar(
                        tabs = HomeSections.entries.toTypedArray(),
                        currentRoute = currentRoute ?: HomeSections.FEED.route,
                        navigateToRoute = nestedNavController::navigateToBottomBarRoute,
                        modifier = Modifier
                            .renderInSharedTransitionScopeOverlay(
                                zIndexInOverlay = 1f,
                            )
                            .animateEnterExit(
                                enter = fadeIn(nonSpatialExpressiveSpring()) + slideInVertically(
                                    spatialExpressiveSpring()
                                ) {
                                    it
                                },
                                exit = fadeOut(nonSpatialExpressiveSpring()) + slideOutVertically(
                                    spatialExpressiveSpring()
                                ) {
                                    it
                                }
                            )
                    )
                }
            }
        },
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(
                hostState = it,
                modifier = Modifier.systemBarsPadding(),
                snackbar = { snackbarData -> BookShelfSnackbar(snackbarData) }
            )
        },
        snackBarHostState = scaffoldState.snackBarHostState
    ) { padding ->
        NavHost(
            navController = nestedNavController.navController,
            startDestination = HomeSections.FEED.route
        ) {
            addHomeGraph(
                //onBookSelected = onBookSelected,
                onSearchClick = onSearchClicked,
                searchViewModel = searchViewModel,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(currentRoute: String, modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(
                text = when (currentRoute) {
                    HomeSections.FEED.route -> "Home"
                    HomeSections.SEARCH.route -> "Search"
                    HomeSections.LIBRARY.route -> "Favorites"
                    HomeSections.PROFILE.route -> "Settings"
                    else -> "BookShelf"
                },
                style = MaterialTheme.typography.titleLarge,
                color = BookShelfTheme.colors.selectedIconBorderFill
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors()
            .copy(
                containerColor = BookShelfTheme.colors.uiBackground,
                scrolledContainerColor = BookShelfTheme.colors.uiBackground
            )
    )
}


val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }


@Preview(showBackground = true)
@Composable
fun CustomTopBarPreview() {
    BookShelfTheme {
        CustomTopBar(currentRoute = HomeSections.FEED.route)
    }
}

@Preview(showBackground = true)
@Composable
fun CustomTopBarPreviewDark() {
    BookShelfTheme(darkTheme = true) {
        CustomTopBar(currentRoute = HomeSections.FEED.route)
    }
}