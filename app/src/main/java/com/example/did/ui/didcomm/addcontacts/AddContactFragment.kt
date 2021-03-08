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
import com.budiyev.android.codescanner.*
import com.example.did.R
import com.google.android.material.snackbar.Snackbar
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

    private lateinit var codeScanner: CodeScanner

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

        val scannerView = requireActivity().findViewById<CodeScannerView>(R.id.previewView)
        codeScanner = CodeScanner(requireContext(), scannerView)
    }

    override fun onResume() {
        super.onResume()
        presenter.viewLoaded(null)
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
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

    override fun setupCamera() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                println("QR code scanned: ${it.text}")
                presenter.qrCodeRead(it.text)
            }
        }
        codeScanner.errorCallback = ErrorCallback.SUPPRESS

        codeScanner.startPreview()
    }

    override fun showSnackbar(status: String) {
        Snackbar.make(this.requireView(), status, Snackbar.LENGTH_SHORT).show()
    }

    // endregion

    // region View contract

    // TODO Add view contract overrides

    // endregion

}
