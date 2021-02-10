package com.example.did.ui.didselect

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
import kotlinx.android.synthetic.main.fragment_d_i_ds.*
import org.json.JSONObject
import javax.inject.Inject

/**
 * DIDSelect VIPER Fragment Implementation
 */
class DIDSelectFragment : Fragment(), DIDSelectContract.View {

    @Inject
    internal lateinit var presenter: DIDSelectContract.Presenter

    @VisibleForTesting
    internal val navigationArgs by navArgs<DIDSelectFragmentArgs>()

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

        DIDSelectList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        DIDSelectList.adapter = adapter

        // Notify Presenter that the View is ready
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

    override fun updateDidList(dids: MutableList<DIDSelectModels.DidDisplayModel>) {
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
