package com.gmulhearn.didwebauthn.ui.retired.signing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.gmulhearn.didwebauthn.R
import com.gmulhearn.didwebauthn.common.viewhelpers.animateVisibilityIn
import com.gmulhearn.didwebauthn.common.viewhelpers.animateVisibilityOut
import kotlinx.android.synthetic.main.fragment_signing.*
import javax.inject.Inject

/**
 * Signing VIPER Fragment Implementation
 */
class SigningFragment : Fragment(), SigningContract.View {

    @Inject
    internal lateinit var presenter: SigningContract.Presenter

    @VisibleForTesting
    internal val navigationArgs by navArgs<SigningFragmentArgs>()

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
        return inflater.inflate(R.layout.fragment_signing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        signButton.setOnClickListener {
            showSpinner()
            presenter.signTextPressed(textToSign.text.toString())
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

    override fun updateSignedText(text: String) {
        hideSpinner()
        signedTextBox.text = text
    }

    private fun showSpinner() {
        signingSpinner.animateVisibilityIn()
    }

    private fun hideSpinner() {
        signingSpinner.animateVisibilityOut()
    }


    // endregion

    // region View contract

    // endregion

}
