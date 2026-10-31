package com.composeship.ui.base64

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.MainViewModel

@Composable
fun Base64EncoderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.base64Tab.collectAsStateWithLifecycle()
    Base64Screen(
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = { viewModel.selectBase64Tab(it) }
    )
}

@Composable
fun Base64EncoderScreen(modifier: Modifier = Modifier) {
    Base64Screen(modifier = modifier)
}
