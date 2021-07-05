package com.gmulhearn.didwebauthn.ui.didcomm.addcontacts

import androidx.navigation.fragment.findNavController
import com.gmulhearn.didwebauthn.ui.didcomm.AddContact.AddContactContract
import com.gmulhearn.didwebauthn.ui.didcomm.AddContact.AddContactFragment
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
