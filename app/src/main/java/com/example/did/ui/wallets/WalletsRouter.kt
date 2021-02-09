package com.example.did.ui.wallets

import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavController
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo

/**
 * Wallets VIPER Router Implementation
 */
class WalletsRouter @Inject constructor(
    private val fragment: WalletsFragment
) : WalletsContract.Router {

    override fun showSigning(
        didInfo: DidInfo,
        walletInfo: WalletInfo
    ) {
        val directions = WalletsFragmentDirections.actionWalletsFragmentToSigningFragment(
            didInfo,
            walletInfo
        )
        findNavController().navigate(directions)
    }

    override fun toDIDs(walletInfo: WalletInfo) {
        val directions = WalletsFragmentDirections.actionWalletsFragmentToDIDsFragment(
            walletInfo
        )

        findNavController().navigate(directions)
    }


    @VisibleForTesting
    internal fun findNavController(): NavController {
        return fragment.findNavController()
    }
}
