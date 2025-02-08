package com.example.waraq.ui.admin


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.data.model.PaperItem
import com.example.waraq.databinding.FragmentAddItemBinding
import com.example.waraq.util.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class AddItemFragment : BaseFragment<FragmentAddItemBinding>(R.layout.fragment_add_item) {

    private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var pdfUri: Uri? = null
    private var coverUri: Uri? = null
    private var firestoreReference =
        Firebase.firestore.collection(Constants.FIRE_STORE_BOOKS_COLLECTION)
    private var storageReference = FirebaseStorage.getInstance().reference


    override fun setup() {
        binding.uploading = false
        openDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    pdfUri = result.data!!.data
                    binding.pdfFile.text =
                        DocumentFile.fromSingleUri(requireContext(), pdfUri!!)?.name
                }
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    coverUri = result.data!!.data
                    binding.cover.setImageURI(coverUri)
                    binding.coverCard.visibility = View.VISIBLE
                }
            }

    }

    override fun addCallbacks() {

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.apply {

            titleEt.doOnTextChanged { _, _, _, _ ->
                binding.titleTextInputLayout.isErrorEnabled = false
            }
            idEt.doOnTextChanged { _, _, _, _ ->
                binding.idInputLayout.isErrorEnabled = false
            }
            priceEt.doOnTextChanged { _, _, _, _ ->
                binding.priceTextInputLayout.isErrorEnabled = false
            }
            subjectEt.doOnTextChanged { _, _, _, _ ->
                binding.subjectTextInputLayout.isErrorEnabled = false
            }
            authorEt.doOnTextChanged { _, _, _, _ ->
                binding.authorTextInputLayout.isErrorEnabled = false
            }
            pagesEt.doOnTextChanged { _, _, _, _ ->
                binding.pagesTextInputLayout.isErrorEnabled = false
            }

            pdfFile.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("application/pdf")
                openDocumentLauncher.launch(intent)
            }

            selectCover.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(intent)

            }

            addButton.setOnClickListener {

                if (checkFields() && !binding.uploading!!) {
                    binding.uploading = true
                    val pdfStorageReference =
                        storageReference.child("pdf/" + UUID.randomUUID().toString())
                    pdfStorageReference.putFile(pdfUri!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            pdfStorageReference.downloadUrl.addOnSuccessListener { url ->
                                println("......................pdf Url:  $url")
                                addCoverToStorage(url.toString())
                            }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG)
                                        .show()
                                }

                        } else {
                            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

    }

    private fun addCoverToStorage(pdfUrl: String) {
        val coverStorageReference = storageReference.child("images/" + UUID.randomUUID().toString())
        coverStorageReference.putFile(coverUri!!).addOnCompleteListener {
            if (it.isSuccessful) {
                coverStorageReference.downloadUrl.addOnSuccessListener { coverUrl ->
                    println("......................cover Url:  $coverUrl")
                    addItemToFireStore(pdfUrl, coverUrl.toString())
                }

            } else {
                binding.uploading = false
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addItemToFireStore(pdfUrl: String, coverUrl: String) {
        binding.apply {
            firestoreReference.document(idEt.text.toString()).set(
                PaperItem(
                    id = idEt.text.toString(),
                    title = titleEt.text.toString(),
                    url = pdfUrl,
                    coverUrl = coverUrl,
                    university = universitySpinner.selectedItem.toString(),
                    faculty = facultySpinner.selectedItem.toString(),
                    grade = gradeSpinner.selectedItem.toString(),
                    semester = semesterSpinner.selectedItem.toString(),
                    subject = subjectEt.text.toString(),
                    author = authorEt.text.toString(),
                    pages = pagesEt.text.toString(),
                    price = priceEt.text.toString()
                )
            )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        resetFields()
                        Toast.makeText(
                            requireContext(),
                            "Item was added successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        binding.uploading = false
                        Toast.makeText(requireContext(), "Adding item failed", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }
    }

    private fun resetFields() {
        binding.apply {
            titleEt.text = null
            idEt.text = null
            subjectEt.text = null
            priceEt.text = null
            pagesEt.text = null
            authorEt.text = null
            pdfUri = null
            coverUri = null
            cover.setImageURI(null)
            coverCard.visibility = View.INVISIBLE
            pdfFile.text = "Select Pdf"
            uploading = false
        }
    }

    private fun checkFields(): Boolean {
        var isTitleEmpty = true
        var isIdEmpty = true
        var isAuthorEmpty = true
        var isSubjectEmpty = true
        var isPagesEmpty = true
        var isPriceEmpty = true
        var isPdfEmpty = true
        var isCoverEmpty = true

        binding.apply {
            if (titleEt.text?.isBlank() == true) {
                titleTextInputLayout.error = "empty field"
                isTitleEmpty = false
            }
            if (idEt.text?.isBlank() == true) {
                idInputLayout.error = "empty field"
                isIdEmpty = false
            }
            if (authorEt.text?.isBlank() == true) {
                authorTextInputLayout.error = "empty field"
                isAuthorEmpty = false
            }
            if (subjectEt.text?.isBlank() == true) {
                subjectTextInputLayout.error = "empty field"
                isSubjectEmpty = false

            }
            if (pagesEt.text?.isBlank() == true) {
                pagesTextInputLayout.error = "empty field"
                isPagesEmpty = false

            }
            if (priceEt.text?.isBlank() == true) {
                priceTextInputLayout.error = "empty field"
                isPriceEmpty = false

            }
            if (pdfUri == null) {
                isPdfEmpty = false
                Toast.makeText(requireContext(), "Pdf required", Toast.LENGTH_SHORT).show()
            }
            if (coverUri == null) {
                isCoverEmpty = false
                Toast.makeText(requireContext(), "Cover image required", Toast.LENGTH_LONG).show()
            }

            return isTitleEmpty && isIdEmpty && isAuthorEmpty && isSubjectEmpty && isPagesEmpty && isPriceEmpty && isPdfEmpty && isCoverEmpty
        }

    }

}