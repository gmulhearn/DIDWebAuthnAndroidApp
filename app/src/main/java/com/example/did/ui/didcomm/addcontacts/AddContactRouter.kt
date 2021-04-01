package com.example.did.ui.didcomm.AddContact

import androidx.navigation.fragment.findNavController
import javax.inject.Inject

/**
 * AddContact VIPER Router Implementation
 */
class AddContactRouter @Inject constructor(
    private val fragment: AddContactFragment
) : AddContactContract.Router {
    override fun back() {
        fragment.findNavController().popBackStack()
    }
}
