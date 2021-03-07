package com.example.did.data

import com.google.gson.annotations.SerializedName

class Invitation(
    @SerializedName("@type") val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/connections/1.0/invitation",
    @SerializedName("@id") val id: String,
    val label: String,
    val recipientKeys: List<String>,
    val serviceEndpoint: String,
    val routingKeys: List<String>
) {
    
}