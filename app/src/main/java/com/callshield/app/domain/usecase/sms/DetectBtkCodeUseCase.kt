package com.callshield.app.domain.usecase.sms

import javax.inject.Inject

class DetectBtkCodeUseCase @Inject constructor() {

    private val btkCodeRegex = Regex("""B\d{3,4}""")

    operator fun invoke(smsContent: String): String? =
        btkCodeRegex.find(smsContent)?.value
}
