package com.example.criminalintent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.math.log

private  const val T= "CrimeListView"
class CrimeListViewModel: ViewModel() {
private  val crimeRepository= CrimeRepository.get()
    private  val _crimes :MutableStateFlow<List<Crime>> = MutableStateFlow(emptyList())
    val crimes:StateFlow<List<Crime>>
        get() = _crimes.asStateFlow()

    init {
        Log.d(T, "init start")
        viewModelScope.launch {
crimeRepository.getCrimes().collect{
    _crimes.value=it
           }
        }
    }

    suspend fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

}