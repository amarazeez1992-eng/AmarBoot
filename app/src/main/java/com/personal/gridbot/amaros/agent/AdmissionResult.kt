package com.personal.gridbot.amaros.agent

data class AdmissionResult(
    val admitted: List<AdmittedFinding>,
    val rejected: List<AdmittedFinding>
) {
    init {
        require(admitted.all { it.state == AdmissionState.ADMITTED })
        require(rejected.all { it.state == AdmissionState.REJECTED })
    }
}
