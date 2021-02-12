package com.example.did.ui.didselect

import androidx.annotation.VisibleForTesting
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import com.example.did.ui.wallets.WalletsFragmentDirections
import javax.inject.Inject

/**
 * DIDSelect VIPER Router Implementation
 */
class DIDSelectRouter @Inject constructor(
    private val fragment: DIDSelectFragment
) : DIDSelectContract.Router {
    override fun toSigning(didInfo: DidInfo, walletInfo: WalletInfo) {
        val directions = DIDSelectFragmentDirections.actionDIDSelectFragmentToSigningFragment(
            didInfo,
            walletInfo
        )
        findNavController().navigate(directions)
    }

    override fun toContacts(didInfo: DidInfo, walletInfo: WalletInfo) {
        val directions = DIDSelectFragmentDirections.actionDIDSelectFragmentToContactSelectFragment(
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
