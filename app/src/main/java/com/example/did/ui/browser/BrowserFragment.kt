package com.example.did.ui.browser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.SearchView
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import com.example.did.R
import kotlinx.android.synthetic.main.chat_input_item.view.*
import kotlinx.android.synthetic.main.fragment_browser.*
import javax.inject.Inject

/**
 * Browser VIPER Fragment Implementation
 */
class BrowserFragment : Fragment(), BrowserContract.View, SearchView.OnQueryTextListener {

    @Inject
    internal lateinit var presenter: BrowserContract.Presenter

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
        val v =  inflater.inflate(R.layout.fragment_browser, container, false)

        val webViewer = v.findViewById<WebView>(R.id.webView)
        webViewer.webViewClient = WebViewClient()
        webViewer.loadUrl("https://google.com")
        webViewer.settings.javaScriptEnabled = true

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        searchbar.setOnSearchClickListener {
            println(searchbar.query)
        }
        searchbar.setOnQueryTextListener(this)
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

    // SEARCHBAR LISTENER
    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            presenter.querySubmitted(it)
            // webView.loadUrl(query)
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
    // END SEARCHBAR LISTENER

    // endregion

    // region View contract

    override fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    // endregion
}

