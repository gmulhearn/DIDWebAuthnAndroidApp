package com.gmulhearn.didwebauthn.core.transport.relay

import com.gmulhearn.didwebauthn.core.protocols.DIDCommProtocols
import javax.inject.Inject

class DIDCommRelay @Inject constructor(
    private val relay: RelayRepository
) {

    private val didComm = DIDCommProtocols(relay)

    fun createPostboxForDID(did: String) {

    }

    fun subscribeToDIDCommMessages(did: String) {

    }
}