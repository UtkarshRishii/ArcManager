package com.arcmanager.presentation.screens.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.BankAccount
import com.arcmanager.domain.repository.BankAccountRepository
import com.arcmanager.presentation.components.*
import com.arcmanager.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BankAccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<BankAccount> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class BankAccountsViewModel @Inject constructor(
    private val bankAccountRepository: BankAccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankAccountsUiState())
    val uiState: StateFlow<BankAccountsUiState> = _uiState.asStateFlow()

    init {
        loadBankAccounts()
    }

    fun loadBankAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bankAccountRepository.getBankAccounts().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> _uiState.update { it.copy(isLoading = false, accounts = result.data, error = null) }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            when (val result = bankAccountRepository.deleteBankAccount(accountId)) {
                is Result.Success -> loadBankAccounts()
                is Result.Error -> _uiState.update { it.copy(error = result.message) }
                is Result.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    viewModel: BankAccountsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var accountToDelete by remember { mutableStateOf<BankAccount?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBankAccounts()
    }

    accountToDelete?.let { account ->
        ConfirmDialog(
            title = "Delete Bank Account",
            message = "Are you sure you want to remove ${account.displayName}?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteAccount(account.id)
                accountToDelete = null
            },
            onDismiss = { accountToDelete = null }
        )
    }

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Bank Accounts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToAddAccount,
                    containerColor = PrimaryViolet,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Outlined.Add, "Add Bank Account")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    uiState.isLoading && uiState.accounts.isEmpty() -> {
                        LoadingState(message = "Loading bank accounts...")
                    }
                    uiState.accounts.isEmpty() -> {
                        EmptyState(
                            title = "No Bank Accounts",
                            description = "Add your business or personal bank accounts to track where payments are received.",
                            icon = Icons.Outlined.AccountBalance,
                            actionLabel = "+ Add Bank Account",
                            onActionClick = onNavigateToAddAccount,
                            modifier = Modifier
                                .padding(Dimens.ScreenPadding)
                                .align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(uiState.accounts, key = { it.id }) { account ->
                                BankAccountCard(
                                    account = account,
                                    onDeleteClick = { accountToDelete = account }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BankAccountCard(
    account: BankAccount,
    onDeleteClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = PrimaryViolet.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x1C1C1C2E))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color(0x35FFFFFF), Color(0x0CFFFFFF))),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryVioletSubtle)
                        .border(1.dp, PrimaryViolet.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = PrimaryVioletBright,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = account.accountName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = account.bankName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (!account.accountLast4.isNullOrBlank()) {
                        Text(
                            text = "A/C: •••• ${account.accountLast4}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = PrimaryVioletLight
                        )
                    }
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete",
                    tint = StatusDangerBright.copy(alpha = 0.8f)
                )
            }
        }
    }
}
