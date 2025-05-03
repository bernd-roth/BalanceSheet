package at.co.netconsulting.balancesheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.co.netconsulting.balancesheet.network.BalanceSheetRepository
import at.co.netconsulting.balancesheet.viewmodel.MainViewModel

class MainViewModelFactory(
    private val repository: BalanceSheetRepository,
    private val defaultReserve: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, defaultReserve) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}