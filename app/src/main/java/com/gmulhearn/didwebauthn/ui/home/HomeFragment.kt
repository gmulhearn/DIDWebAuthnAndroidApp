package com.gmulhearn.didwebauthn.ui.home

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
import com.gmulhearn.didwebauthn.data.indy.PairwiseContact
import kotlinx.android.synthetic.main.fragment_contact_select.*
import javax.inject.Inject

/**
 * Home VIPER Fragment Implementation
 */
class HomeFragment : Fragment(), HomeContract.View {

    @Inject
    internal lateinit var presenter: HomeContract.Presenter

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
        AndroidSupportInjection.inject(this)
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

        addContactButton.setOnClickListener {
            presenter.addContactClicked()
        }

        browserButton.setOnClickListener {
            presenter.browserClicked()
        }

        externalSeshButton.setOnClickListener {
            presenter.externalSessionClicked()
        }

        walletInfoBtn.setOnClickListener {
            presenter.walletInfoClicked()
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

    // endregion

}
