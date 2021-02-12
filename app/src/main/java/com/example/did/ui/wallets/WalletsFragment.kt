package com.example.did.ui.wallets

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.did.R
import com.example.did.common.viewhelpers.animateVisibilityIn
import com.example.did.common.viewhelpers.animateVisibilityOut
import javax.inject.Inject
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_i_d_generation.*

/**
 * Wallets VIPER Fragment Implementation
 */
class WalletsFragment : Fragment(), WalletsContract.View {

    @Inject
    internal lateinit var presenter: WalletsContract.Presenter

    @VisibleForTesting
    internal var adapter =
        WalletAdapter { wallet ->
            walletOnClick(wallet)
        }

    // region viper lifecycle

    override fun onAttach(context: Context) {
        inject()
        super.onAttach(context)
    }

    internal fun inject() {
        AndroidSupportInjection.inject(this) // TODO inject with the default Application injector or the Kit injector if inside a UI Kit
    }

    // endregion

    // region view setup and state lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_i_d_generation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // TODO setup view, event listeners etc.
        view.findViewById<Button>(R.id.genWalletButton).setOnClickListener {
            showSpinner()
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
            presenter.genWalletPressed(walletNameTextBox.text.toString())
        }

        WalletsList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        WalletsList.adapter = adapter

        // Notify Presenter that the View is ready
        presenter.viewLoaded(savedInstanceState)
    }

    private fun walletOnClick(wallet: WalletsModels.WalletDisplayModel) {
        presenter.walletClicked(wallet.walletID)
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    override fun updateWalletList(wallets: MutableList<WalletsModels.WalletDisplayModel>) {
        adapter.submitList(wallets)
        println(wallets)
        hideSpinner()
    }

    private fun showSpinner() {
        generatingSpinner.animateVisibilityIn()
    }

    private fun hideSpinner() {
        generatingSpinner.animateVisibilityOut()
    }

    // endregion

    // region View contract

    // TODO Add view contract overrides

    override fun onGenerationError() {
        hideSpinner()
    }

    // endregion

}
