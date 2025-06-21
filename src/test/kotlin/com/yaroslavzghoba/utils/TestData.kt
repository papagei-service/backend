package com.yaroslavzghoba.utils

import com.yaroslavzghoba.model.CardCollectionRequest
import com.yaroslavzghoba.model.CardRequest
import com.yaroslavzghoba.model.RegistrationCredentials

@Suppress("unused")
object TestData {

    val FIRST_REGISTRATION_CREDENTIALS =
        RegistrationCredentials(username = "admin", displayName = "Admin", password = "qwerty")
    val SECOND_REGISTRATION_CREDENTIALS =
        RegistrationCredentials(username = "papagei", displayName = "Papagei", password = "password")
    val THIRD_REGISTRATION_CREDENTIALS =
        RegistrationCredentials(username = "yaroslav", displayName = "Yaroslav", password = "123")

    const val FIRST_SALT = "IspmCrSEWVnexqRbNUCcLVtzCsjUIazQMKqNMkym"
    const val SECOND_SALT = "QCeXyYrxAihQSdQtOGIrfkrJQCGSQyyx"
    const val THIRD_SALT = "zXKBokNMxhqDCbiIpplLmgYJeEWsilndZXkdGQVPIyWPD"

    val FIRST_COLLECTION_REQUEST = CardCollectionRequest(
        id = null,
        title = "Основи іспанської",
        description = "Початковий рівень іспанської мови.",
        knownLanguage = "uk",
        learningLanguage = "es",
    )
    val SECOND_COLLECTION_REQUEST = CardCollectionRequest(
        id = null,
        title = "German verbs",
        description = null,
        knownLanguage = "en",
        learningLanguage = "de",
    )
    val THIRD_COLLECTION_REQUEST = CardCollectionRequest(
        id = null,
        title = "Japoński: zwroty przydatne w podróży",
        description = "Przydatne zwroty dla podróżujących po Japonii.",
        knownLanguage = "pl",
        learningLanguage = "ja",
    )

    val FIRST_CARD_REQUEST = CardRequest(
        id = null,
        knownLanguageText = "Bonjour",
        learningLanguageText = "Hola",
        notes = "Salutation type",
        lastAnsweredAt = null,
        showNextTimeAt = null,
        correctAnswersInRow = 0,
    )
    val SECOND_CARD_REQUEST = CardRequest(
        id = 102L,
        knownLanguageText = "Дякую",
        learningLanguageText = "Danke schön",
        notes = null,
        lastAnsweredAt = null,
        showNextTimeAt = null,
        correctAnswersInRow = 0
    )
    val THIRD_CARD_REQUEST = CardRequest(
        id = null,
        knownLanguageText = "a cat",
        learningLanguageText = "猫 (Neko)",
        notes = null,
        lastAnsweredAt = null,
        showNextTimeAt = null,
        correctAnswersInRow = 0
    )
}