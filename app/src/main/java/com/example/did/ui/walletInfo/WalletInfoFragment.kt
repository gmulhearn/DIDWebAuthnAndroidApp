package com.example.did.ui.walletInfo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import com.example.did.R
import com.example.did.common.WalletProvider
import kotlinx.android.synthetic.main.fragment_wallet_info.*
import javax.inject.Inject

/**
 * WalletInfo VIPER Fragment Implementation
 */
class WalletInfoFragment : Fragment(), WalletInfoContract.View {

    @Inject
    internal lateinit var presenter: WalletInfoContract.Presenter

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
        return inflater.inflate(R.layout.fragment_wallet_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // TODO setup view, event listeners etc.

        viewAllDidsBtn.setOnClickListener {
            presenter.viewDidsClicked(webAuthnFilter = false)
        }

        viewWebAuthnDids.setOnClickListener {
            presenter.viewDidsClicked(webAuthnFilter = true)
        }

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

    // endregion

    // region View contract

    override fun setInfoText(didsString: String) {
        infoText.text = didsString
    }
    // endregion

}
