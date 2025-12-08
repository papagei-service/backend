package space.zghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the body of a request to insert an example.
 *
 * @param knownLanguageText Example of usage in a language the user knows.
 * @param learningLanguageText Example of usage in a language the user want to learn.
 */
@Serializable
data class ExampleInsertRequest(
    @SerialName("known_language_text") val knownLanguageText: String,
    @SerialName("learning_language_text") val learningLanguageText: String,
)