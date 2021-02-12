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

    override fun toDIDSelect(walletInfo: WalletInfo) {
        val directions = WalletsFragmentDirections.actionWalletsFragmentToDIDSelectFragment(
            walletInfo
        )

        findNavController().navigate(directions)
    }


    @VisibleForTesting
    internal fun findNavController(): NavController {
        return fragment.findNavController()
    }
}
