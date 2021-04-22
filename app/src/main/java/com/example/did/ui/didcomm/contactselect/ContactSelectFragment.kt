package com.example.did.ui.didcomm.contactselect

import android.content.Context
import android.graphics.Bitmap
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
import com.example.did.data.PairwiseContact
import kotlinx.android.synthetic.main.fragment_contact_select.*
import javax.inject.Inject

/**
 * ContactSelect VIPER Fragment Implementation
 */
class ContactSelectFragment : Fragment(), ContactSelectContract.View {

    @Inject
    internal lateinit var presenter: ContactSelectContract.Presenter

    internal var adapter =
        ContactAdapter(
            { pairwiseContact -> contactClicked(pairwiseContact) },
            { pairwiseContact -> deleteClicked(pairwiseContact) }
        )

    private fun deleteClicked(pairwiseContact: PairwiseContact) {
        presenter.deleteClicked(pairwiseContact)
    }

    private fun contactClicked(pairwiseContact: PairwiseContact) {
        presenter.contactClicked(pairwiseContact)
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
        return inflater.inflate(R.layout.fragment_contact_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // TODO setup view, event listeners etc.

        addContactButton.setOnClickListener {
            presenter.addContactClicked()
        }

        browserButton.setOnClickListener {
            presenter.browserClicked()
        }

        ContactSelectList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        ContactSelectList.adapter = adapter

        // Notify Presenter that the View is ready
        showWalletLoading()
        presenter.viewLoaded(savedInstanceState)
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    override fun updateContactList(contacts: List<PairwiseContact>) {
        adapter.submitList(contacts)
    }

    override fun onWalletLoaded() {
        greyout.animateVisibilityOut()
        addContactButton.isClickable = true
        loadingWalletText.animateVisibilityOut()
        hideSpinner()
    }

    private fun showWalletLoading() {
        greyout.animateVisibilityIn()
        addContactButton.isClickable = false
        loadingWalletText.animateVisibilityIn()
        showSpinner()
    }

    private fun showSpinner() {
        walletLoadingSpinner.animateVisibilityIn()
    }

    private fun hideSpinner() {
        walletLoadingSpinner.animateVisibilityOut()
    }

    // endregion

    // region View contract

    // TODO Add view contract overrides

    // endregion

}
