package com.example.waraq.ui.admin

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.data.model.ItemId
import com.example.waraq.databinding.FragmentAssignItemBinding
import com.example.waraq.util.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AssignItemFragment : BaseFragment<FragmentAssignItemBinding>(R.layout.fragment_assign_item) {


    override fun setup() {

    }

    override fun addCallbacks() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.assignButton.setOnClickListener {
            if (binding.assignCodeEt.text?.isNotBlank() == true) {
                handleAssignCode(binding.assignCodeEt.text.toString())
            }
        }

    }

    private fun handleAssignCode(assignCode: String) {
        val assignMap = hashMapOf<String, String>()
        val userAssignList = assignCode.split(',')
        userAssignList.forEach { userAssignCode ->
            val userCodeList = userAssignCode.split(':')
            if (userCodeList.size == 2) {
                val userEmail = userCodeList[0]
                val itemId = userCodeList[1]
                assignMap[userEmail] = itemId
            }
        }
        if (assignMap.isNotEmpty()) {
            binding.loading = true
            assignUsers(assignMap)
        }
    }

    private fun assignUsers(assignMap: HashMap<String, String>) {
        var successfulItemCount = 0
        assignMap.forEach { mapItem ->
            val userEmail = mapItem.key
            val itemId = mapItem.value
            Firebase.firestore.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(userEmail)
                .collection(Constants.FIRE_STORE_USER_BOOKS_COLLECTION).document(itemId).set(
                    ItemId(itemId)
                ).addOnSuccessListener {
                    successfulItemCount++
                    if(successfulItemCount==assignMap.size)
                    {
                        binding.loading=false
                        Toast.makeText(requireContext(),"Items was assigned successfully",Toast.LENGTH_SHORT).show()
                    }
                }

        }
        }
    }

