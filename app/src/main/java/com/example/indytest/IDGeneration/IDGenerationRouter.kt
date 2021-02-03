package com.example.indytest.IDGeneration

import androidx.annotation.VisibleForTesting
import com.example.indytest.Signing.SigningFragment
import org.hyperledger.indy.sdk.wallet.Wallet
import javax.inject.Inject
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavController
import com.example.indytest.IDGeneration.IDGenerationModels.*

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


    @VisibleForTesting
    internal fun findNavController(): NavController {
        return fragment.findNavController()
    }
}
