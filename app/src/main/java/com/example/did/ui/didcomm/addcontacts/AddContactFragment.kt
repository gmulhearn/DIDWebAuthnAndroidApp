package com.example.did.ui.didcomm.AddContact

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
import com.example.did.R
import kotlinx.android.synthetic.main.fragment_add_contact.*
import javax.inject.Inject

/**
 * AddContact VIPER Fragment Implementation
 */
class AddContactFragment : Fragment(), AddContactContract.View {

    @Inject
    internal lateinit var presenter: AddContactContract.Presenter

    @VisibleForTesting

    internal val navigationArgs by navArgs<AddContactFragmentArgs>()

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
        return inflater.inflate(R.layout.fragment_add_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // TODO setup view, event listeners etc.

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

    override fun showQR(bitmap: Bitmap) {
        qrCode.setImageBitmap(bitmap)
    }

    // endregion

    // region View contract

    // TODO Add view contract overrides

    // endregion

}
