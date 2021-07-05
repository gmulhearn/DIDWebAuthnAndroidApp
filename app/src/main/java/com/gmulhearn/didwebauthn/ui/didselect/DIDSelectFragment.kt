package com.gmulhearn.didwebauthn.ui.didselect

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmulhearn.didwebauthn.R
import com.gmulhearn.didwebauthn.common.viewhelpers.animateVisibilityIn
import com.gmulhearn.didwebauthn.common.viewhelpers.animateVisibilityOut
import kotlinx.android.synthetic.main.did_seed_popup.*
import kotlinx.android.synthetic.main.fragment_d_i_ds.*
import javax.inject.Inject

/**
 * DIDSelect VIPER Fragment Implementation
 */
class DIDSelectFragment : Fragment(), DIDSelectContract.View {

    @Inject
    internal lateinit var presenter: DIDSelectContract.Presenter

    internal var adapter =
        DIDAdapter { did, tab  ->
            didTabOnClick(did, tab)
        }

    // region viper lifecycle

    override fun onAttach(context: Context) {
        inject()
        super.onAttach(context)
    }

    internal fun inject() {
        AndroidSupportInjection.inject(this)
    }

    // endregion

    // region view setup and state lifecycle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_d_i_ds, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        walletTitle.text = "MyWallet"
        genDIDButton.setOnClickListener {
            showSpinner()
            presenter.genDIDClicked()
        }

        seedButton.setOnClickListener {
            seedPopup.visibility = when (seedPopup.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        reveal_seed.setOnClickListener {
            seedText.visibility = View.VISIBLE
        }

        setSeed.setOnClickListener {
            presenter.seedSetAttempt(seedText.text.toString())
        }

        DIDSelectList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        DIDSelectList.adapter = adapter

        // Notify Presenter that the View is ready
        showWalletLoading()
        presenter.viewLoaded(savedInstanceState)
    }

    private fun didTabOnClick(did: DIDSelectModels.DidDisplayModel, tabClicked: String) {
        presenter.didTabClicked(did, tabClicked)
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    // endregion

    // region View contract

    override fun updateDidList(dids: MutableList<DIDSelectModels.DidDisplayModel>) {
        adapter.submitList(dids)
        hideSpinner()
    }

    override fun onGenerationError() {
        hideSpinner()
    }

    override fun onSeedWordSet(seedWords: String) {
        seedText.setText(seedWords)
    }

    override fun onWalletLoaded() {
        greyout.animateVisibilityOut()
        genDIDButton.isClickable = true
        loadingWalletText.animateVisibilityOut()
        hideSpinner()
    }

    private fun showWalletLoading() {
        greyout.animateVisibilityIn()
        genDIDButton.isClickable = false
        loadingWalletText.animateVisibilityIn()
        showSpinner()
    }

    private fun showSpinner() {
        DIDGeneratingSpinner.animateVisibilityIn()
    }

    private fun hideSpinner() {
        DIDGeneratingSpinner.animateVisibilityOut()
    }

    // endregion

}
