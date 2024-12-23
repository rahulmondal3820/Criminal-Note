package com.example.criminalintent

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.criminalintent.databinding.FragmentCrimeDetailBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_FORMAT = "EEEE, MMM,dd"

class CrimeDetailFragment : Fragment() {

    private val args: CrimeDetailFragmentArgs by navArgs()
    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels() {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val selectSuspect =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
            uri?.let { parseContactSelection(it) }

        }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()){didTakePhoto:Boolean->


        if (didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime {old->
                old.copy(photoFileName = photoName)
            }
            Toast.makeText(context,"update photo data ",Toast.LENGTH_SHORT).show()
        }
    }
    private var _binding: FragmentCrimeDetailBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it null. Is the view visible?"
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{

        _binding = FragmentCrimeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    private var photoName:String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }
            crimeSolve.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }

            }

            crimeSuspect.setOnClickListener { View ->
                selectSuspect.launch(null)
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val selectSuspectIntent = selectSuspect.contract.createIntent(requireContext(),null)
            crimeSuspect.isEnabled = canResolverIntent(selectSuspectIntent)


            crimeCamera.setOnClickListener{
                photoName = "IMG_${timeStamp}.JPG"
                val photoFile =File(requireContext().applicationContext.filesDir,photoName)
                val photoUri = FileProvider.getUriForFile(requireContext(),"com.example.criminalintent.fileprovider",photoFile)

                takePhoto.launch(photoUri)
            }

            val captureImageIntent = takePhoto.contract.createIntent(requireContext(),Uri.parse(""))
            crimeCamera.isEnabled = canResolverIntent(captureImageIntent)

              
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeDetailViewModel.crime.collect() { crime ->
                    crime?.let {
                        updateUi(it)
                    }

                }
            }
        }
        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE) { _, bundle ->
            val newDate = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date

            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun updateUi(crime: Crime) {


        binding.apply {
            if (crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }
            crimeDate.text = DateFormat.format(DATE_FORMAT, crime.date).toString()
            crimeSolve.isChecked = crime.isSolved
            crimeDate.setOnClickListener {
                findNavController().navigate(

                    CrimeDetailFragmentDirections.selectDate(crime.date)
                )


            }
            crimeReport.setOnClickListener { view ->
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_suspect))
                }

                val chooserIntent =
                    Intent.createChooser(reportIntent, getString(R.string.send_report))

                startActivity(chooserIntent)
            }

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_report_no_suspect)
            }
            updatePhoto(crime.photoFileName)

        }
    }

    private fun getCrimeReport(crime: Crime): String {
        val solveString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspectText = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(
            R.string.crime_report, crime.title, dateString, solveString, suspectText
        )
    }

    private fun parseContactSelection(contactUri: Uri) {
        val queryField = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        val queryCursor =
            requireActivity().contentResolver.query(contactUri, queryField, null, null, null)
        queryCursor?.use { cursor ->

            if (cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }
        }
    }

    private fun canResolverIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val activityResolver: ResolveInfo? =
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return activityResolver != null


    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.crimePhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir,it)
            }
            if (photoFile?.exists() == true) {

                binding.crimePhoto.doOnLayout { measuredView->

                    val scaledBitmap = getScaleBitmap(photoFile.path,measuredView.width,measuredView.height)

                    binding.crimePhoto.setImageBitmap(scaledBitmap)
                    binding.crimePhoto.tag=photoFile

                    Log.d("photoView","have photo${photoFileName}")
                }


            }else{
                binding.crimePhoto.setImageBitmap(null)
                binding.crimePhoto.tag= null
                Log.d("photoView","not have any photo")
            }
        }

    }
}