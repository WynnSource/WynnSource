package fyw.fyi.wynnsource.config.constraint

import fyw.fyi.wynnsource.datagen.lang.Translatable

/**
 * Result of a constraint validation.
 * @param valid Whether the value passed validation
 * @param errorMessage The error message to display if validation failed
 */
data class ValidationResult(
    val valid: Boolean,
    val errorMessage: Translatable? = null
) {
    companion object {
        val SUCCESS = ValidationResult(true)

        fun failure(errorMessage: Translatable) = ValidationResult(false, errorMessage)
    }
}
