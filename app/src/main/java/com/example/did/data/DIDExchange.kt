package com.example.did.data

class Invitation(
    val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/connections/1.0/invitation",
    val id: String,
    val label: String,
    val recipientKeys: List<String>,
    val serviceEndpoint: String,
    val routingKeys: List<String>
) {
    
}