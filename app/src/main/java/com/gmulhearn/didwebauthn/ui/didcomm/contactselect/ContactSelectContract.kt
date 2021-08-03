package com.gmulhearn.didwebauthn.ui.didcomm.contactselect

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.gmulhearn.didwebauthn.data.DidInfo
import com.gmulhearn.didwebauthn.data.indy.PairwiseContact

/**
 * ContactSelectContract VIPER contract
 */
interface ContactSelectContract {

    /**
     * Passive view interface. This interface declares behaviors that can modify the View
     */
    interface View {
        fun updateContactList(contacts: List<PairwiseContact>)
        fun onWalletLoaded()
    }

    /**
     * Presenter for the module. It's sole purpose is to mediate between the View, Router and
     * Interactor concerns and to transform Interactor outputs to display models for presentation
     * on the View
     */
    interface Presenter {

        /**
         * Attach view to the Presenter. This is called on the [Fragment.onAttach] lifecycle event
         *
         * @param view the view being attached to the Presenter. Note: the view layout itself
         *  may not be setup at this time
         */
        fun attachView(view: View)

        /**
         * Detach view from the Presenter. This is called on the [Fragment.onDetach] lifecycle event
         */
        fun detachView()

        /**
         * View is inflated and ready. This is called on the [Fragment.onViewCreated] lifecycle event
         *
         * @param savedState state Bundle containing any existing state, or null if there was
         * not previous state saved
         */
        fun viewLoaded(savedState: Bundle?)

        /**
         * Module is about to go away. This is called on the [Fragment.onSaveInstanceState]
         * lifecycle event, which may happen before _or_ after the view layout is detached.
         *
         * @param outState state Bundle to write any current state to
         */
        fun saveState(outState: Bundle)
        fun addContactClicked()
        fun contactClicked(pairwiseContact: PairwiseContact)
        fun deleteClicked(pairwiseContact: PairwiseContact)
        fun browserClicked()
        fun walletInfoClicked()
        fun externalAuthClicked()
    }

    /**
     * Use-case interactor inputs for the module. Each input has one or more corresponding
     * outputs on the [InteractorOutput] interface
     */
    interface InteractorInput {

        /**
         * Attach output to the Interactor. This is called on the [Fragment.onAttach] lifecycle event
         *
         * @param output the Interactor Output to send results to
         */
        fun attachOutput(output: InteractorOutput)

        /**
         * Detach output from the Interactor. This is called on the [Fragment.onDetach] lifecycle event
         */
        fun detachOutput()

        /**
         * Load the initial data for this module
         *
         * @param savedState existing state to restore the Interactor with, or null if there was no
         * existing state
         */
        fun loadData(savedState: Bundle?)

        /**
         * Save pending state on the Interactor
         *
         * NOTE: This call has no equivalent output as it may be called outside of the VIPER lifecycle.
         *
         * @param outState Bundle to save Interactor state to
         */
        fun savePendingState(outState: Bundle)
        fun toAddContact()
        fun toChat(pairwiseContact: PairwiseContact)
        fun deleteContact(pairwiseContact: PairwiseContact)
    }

    /**
     * Outputs of the interactor inputs
     */
    interface InteractorOutput {

        /**
         * Called after [InteractorInput.loadData] completes successfully
         */
        fun loadDataResult() // TODO: define output parameters here, and optionally other output functions for error handling

        // TODO
        fun updateContactList(myContacts: List<PairwiseContact>)
        fun walletFinishedLoading()
    }

    /**
     * Declares all routes out of the module
     */
    interface Router {

        fun toAddContact(didInfo: DidInfo)
        fun toChat(pairwiseContact: PairwiseContact)
        fun toBrowser(didInfo: DidInfo)
        fun toWalletInfo()
        fun toExternalAuth()
        fun toExternalSession()
    }
}
