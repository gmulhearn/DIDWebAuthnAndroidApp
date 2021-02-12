package com.example.did.ui.didcomm.contactselect

import androidx.annotation.VisibleForTesting
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import com.example.did.ui.didselect.DIDSelectFragmentDirections
import org.hyperledger.indy.sdk.did.Did
import javax.inject.Inject

/**
 * ContactSelect VIPER Router Implementation
 */
class ContactSelectRouter @Inject constructor(
    internal val fragment: ContactSelectFragment
) : ContactSelectContract.Router {
    override fun toAddContact(didInfo: DidInfo, walletInfo: WalletInfo) {
        val directions = ContactSelectFragmentDirections.actionContactSelectFragmentToAddContactFragment(
            didInfo,
            walletInfo
        )
        findNavController().navigate(directions)
    }

    @VisibleForTesting
    internal fun findNavController(): NavController {
        return fragment.findNavController()
    }

}
