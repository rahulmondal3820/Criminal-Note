package com.example.criminalintent

import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.ListItemCrimeBinding
import java.util.UUID

class CrimeHolder(private val binding:ListItemCrimeBinding): RecyclerView.ViewHolder(binding.root) {

    fun bind(crime: Crime,onClicked:(crimeId:UUID)->Unit) {
        binding.crimeTitle.text = crime.title
        binding.crimeDate.text = DateFormat.format("EEEE,MMM,dd,yyyy",crime.date)


       binding.root.setOnClickListener{
           onClicked(crime.id)
       }
        binding.crimeSolved.visibility = if (crime.isSolved) {
            View.VISIBLE
        }else{
            View.GONE
        }
    }
}