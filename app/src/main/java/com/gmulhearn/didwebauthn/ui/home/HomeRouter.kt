package com.gmulhearn.didwebauthn.ui.home

import androidx.annotation.VisibleForTesting
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.gmulhearn.didwebauthn.data.DidInfo
import com.gmulhearn.didwebauthn.data.indy.PairwiseContact
import javax.inject.Inject

/**
 * Home VIPER Router Implementation
 */
class HomeRouter @Inject constructor(
    internal val fragment: HomeFragment
) : HomeContract.Router {
    override fun toAddContact(didInfo: DidInfo) {
        val directions = HomeFragmentDirections.actionHomeFragmentToAddContactFragment(
            didInfo
        )
        findNavController().navigate(directions)
    }

    override fun toChat(pairwiseContact: PairwiseContact) {
        val directions = HomeFragmentDirections.actionHomeFragmentToChatFragment(
            pairwiseContact
        )

        findNavController().navigate(directions)
    }

    override fun toBrowser() {
        val directions = HomeFragmentDirections.actionHomeFragmentToBrowserFragment()
        findNavController().navigate(directions)
    }

    override fun toWalletInfo() {
        val directions = HomeFragmentDirections.actionHomeFragmentToWalletInfoFragment()

        findNavController().navigate(directions)
    }

    override fun toExternalSession() {
        val directions = HomeFragmentDirections.actionHomeFragmentToExternalSessionFragment()

        findNavController().navigate(directions)
    }

    @VisibleForTesting
    internal fun findNavController(): NavController {
        return fragment.findNavController()
    }

}
