package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs

class DatePickerFragment:DialogFragment() {
    private val args:DatePickerFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dataListener = DatePickerDialog.OnDateSetListener{_:DatePicker,year:Int,month:Int,day:Int->

            val resultDate=GregorianCalendar(year,month,day).time
            setFragmentResult(REQUEST_KEY_DATE, bundleOf(BUNDLE_KEY_DATE to resultDate))

        }

        val calender=Calendar.getInstance()
        calender.time=args.crimeDate
        val initializeYear=calender.get(Calendar.YEAR)
        val initializeMonth = calender.get(Calendar.MONTH)
        val initializeDay = calender.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            dataListener,
            initializeYear,
            initializeMonth,
            initializeDay
        )
    }

    companion object{
        const val REQUEST_KEY_DATE = "REQUEST_KEY_DATE"
        const val BUNDLE_KEY_DATE="BUNDLE_KEY_DATE"
    }
}