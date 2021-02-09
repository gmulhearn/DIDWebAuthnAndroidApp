package com.example.indytest.ui.wallets

import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavController
import com.example.indytest.data.DidInfo
import com.example.indytest.data.WalletInfo
import com.example.indytest.ui.wallets.IDGenerationModels.*

/**
 * IDGeneration VIPER Router Implementation
 */
class IDGenerationRouter @Inject constructor(
    private val fragment: IDGenerationFragment
) : IDGenerationContract.Router {

    override fun showSigning(
        didInfo: DidInfo,
        walletInfo: WalletInfo
    ) {
        val directions = IDGenerationFragmentDirections.actionIDGenerationFragmentToSigningFragment(
            didInfo,
            walletInfo
        )
        findNavController().navigate(directions)
    }

    override fun toDIDs(walletInfo: WalletInfo) {
        val directions = IDGenerationFragmentDirections.actionIDGenerationFragmentToDIDsFragment(
            walletInfo
        )

        findNavController().navigate(directions)
    }


    @VisibleForTesting
    internal fun findNavController(): NavController {
        return fragment.findNavController()
    }
}
