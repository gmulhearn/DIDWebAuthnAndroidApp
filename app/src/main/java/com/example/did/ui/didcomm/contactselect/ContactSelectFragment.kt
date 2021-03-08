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
import com.example.did.ui.didselect.DIDAdapter
import kotlinx.android.synthetic.main.fragment_contact_select.*
import kotlinx.android.synthetic.main.fragment_d_i_ds.*
import javax.inject.Inject

/**
 * ContactSelect VIPER Fragment Implementation
 */
class ContactSelectFragment : Fragment(), ContactSelectContract.View {

    @Inject
    internal lateinit var presenter: ContactSelectContract.Presenter

    @VisibleForTesting
    internal val navigationArgs by navArgs<ContactSelectFragmentArgs>()

    internal var adapter =
        ContactAdapter { pairwiseContact ->
            contactClicked(pairwiseContact)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // TODO setup view, event listeners etc.

        addContactButton.setOnClickListener {
            presenter.addContactClicked()
        }

        ContactSelectList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        ContactSelectList.adapter = adapter

        // Notify Presenter that the View is ready
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

    // endregion

    // region View contract

    // TODO Add view contract overrides

    // endregion

}
