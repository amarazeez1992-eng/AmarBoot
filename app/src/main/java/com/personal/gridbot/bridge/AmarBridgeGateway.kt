package com.personal.gridbot.bridge

/**
 * Safe application-side gateway. It only creates a pending hand-off state;
 * transport and MT5 execution are deliberately outside this module.
 */
class AmarBridgeGateway {
    fun submitForMt5(commandId: String): String {
        require(commandId.isNotBlank()) { "commandId must not be blank" }
        return AmarBridgeContract.PENDING_MT5
    }
}
