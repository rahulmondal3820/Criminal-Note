package com.example.criminalintent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.ListItemCrimeBinding
import java.util.UUID

class CrimeListAdapter(private val crimes:List<Crime>,private val onClicked:(crimeId:UUID)->Unit): RecyclerView.Adapter<CrimeHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
        val inflater= LayoutInflater.from(parent.context)

        val binding=ListItemCrimeBinding.inflate(inflater,parent, false)
        return CrimeHolder(binding)
    }

    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
        val crime=crimes[position]
       holder.bind(crime,onClicked)
    }

    override fun getItemCount(): Int {

        return crimes.size
    }
}