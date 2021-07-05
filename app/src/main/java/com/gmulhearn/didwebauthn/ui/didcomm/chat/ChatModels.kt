package com.gmulhearn.didwebauthn.ui.didcomm.chat

import com.gmulhearn.didwebauthn.data.DIDCommMessage

/**
 * Chat display nodels, data models and errors for the module
 */
interface ChatModels {

    // region View display models

    // endregion

    // region Interactor Output data models and errors

    // endregion

}

data class MessageDisplayModel(val didCommMessage: DIDCommMessage, val isSender: Boolean)