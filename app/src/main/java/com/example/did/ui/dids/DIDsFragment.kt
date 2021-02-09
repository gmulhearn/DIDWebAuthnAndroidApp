package com.example.did.ui.dids

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.did.R
import com.example.did.common.viewhelpers.animateVisibilityIn
import com.example.did.common.viewhelpers.animateVisibilityOut
import com.example.did.ui.wallets.WalletAdapter
import kotlinx.android.synthetic.main.fragment_d_i_ds.*
import kotlinx.android.synthetic.main.fragment_i_d_generation.*
import org.json.JSONObject
import javax.inject.Inject

/**
 * DIDs VIPER Fragment Implementation
 */
class DIDsFragment : Fragment(), DIDsContract.View {

    @Inject
    internal lateinit var presenter: DIDsContract.Presenter

    @VisibleForTesting
    internal val navigationArgs by navArgs<DIDsFragmentArgs>()

    internal var adapter =
        DIDAdapter { did ->
            didOnClick(did)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_d_i_ds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // TODO setup view, event listeners etc.

        walletTitle.text = JSONObject(navigationArgs.walletInfo.config).getString("id")
        genDIDButton.setOnClickListener {
            showSpinner()
            presenter.genDIDClicked()
        }

        DIDsList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        DIDsList.adapter = adapter

        // Notify Presenter that the View is ready
        presenter.viewLoaded(savedInstanceState)
    }

    private fun didOnClick(did: DIDsModels.DidDisplayModel) {

    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    override fun updateDidList(dids: MutableList<DIDsModels.DidDisplayModel>) {
        adapter.submitList(dids)
        hideSpinner()
    }

    override fun onGenerationError() {
        hideSpinner()
    }

    private fun showSpinner() {
        DIDGeneratingSpinner.animateVisibilityIn()
    }

    private fun hideSpinner() {
        DIDGeneratingSpinner.animateVisibilityOut()
    }

    // endregion

    // region View contract

    // TODO Add view contract overrides

    // endregion

}
