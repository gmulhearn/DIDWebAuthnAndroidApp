package com.gmulhearn.didwebauthn.ui.didcomm.chat

import androidx.navigation.fragment.findNavController
import javax.inject.Inject

/**
 * Chat VIPER Router Implementation
 */
class ChatRouter @Inject constructor(
    private val fragment: ChatFragment
) : ChatContract.Router {
    override fun back() {
        fragment.findNavController().popBackStack()
    }
}
