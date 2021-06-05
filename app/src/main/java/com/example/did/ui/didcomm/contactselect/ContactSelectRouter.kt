package com.example.did.ui.didcomm.contactselect

import androidx.annotation.VisibleForTesting
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.did.data.DidInfo
import com.example.did.data.PairwiseContact
import com.example.did.ui.didselect.DIDSelectFragmentDirections
import javax.inject.Inject

/**
 * ContactSelect VIPER Router Implementation
 */
class ContactSelectRouter @Inject constructor(
    internal val fragment: ContactSelectFragment
) : ContactSelectContract.Router {
    override fun toAddContact(didInfo: DidInfo) {
        val directions = ContactSelectFragmentDirections.actionContactSelectFragmentToAddContactFragment(
            didInfo
        )
        findNavController().navigate(directions)
    }

    override fun toChat(pairwiseContact: PairwiseContact) {
        val directions = ContactSelectFragmentDirections.actionContactSelectFragmentToChatFragment(
            pairwiseContact
        )

        findNavController().navigate(directions)
    }

    override fun toBrowser(didInfo: DidInfo) {
        val directions = ContactSelectFragmentDirections.actionContactSelectFragmentToBrowserFragment(
            didInfo // TODO - remove this.
        )
        findNavController().navigate(directions)
    }

    override fun toWalletInfo() {
        val directions = ContactSelectFragmentDirections.actionContactSelectFragmentToWalletInfoFragment()

        findNavController().navigate(directions)
    }

    override fun toExternalAuth() {
        val directions = ContactSelectFragmentDirections.actionContactSelectFragmentToExternalAuthFragment()

        findNavController().navigate(directions)
    }

    @VisibleForTesting
    internal fun findNavController(): NavController {
        return fragment.findNavController()
    }

}
