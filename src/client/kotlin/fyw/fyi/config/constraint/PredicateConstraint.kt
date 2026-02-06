package fyw.fyi.config.constraint

import fyw.fyi.data.lang.Translatable

/**
 * Constraint that validates a value using a predicate function.
 * This is the most flexible constraint type, allowing any custom validation logic.
 *
 * @param predicate The function that tests if a value is valid
 * @param errorMessage The error message to display when validation fails
 */
class PredicateConstraint<T>(
    private val predicate: (T) -> Boolean,
    private val errorMessage: Translatable
) : Constraint<T> {

    override fun validate(value: T): ValidationResult {
        return if (predicate(value)) {
            ValidationResult.SUCCESS
        } else {
            ValidationResult.failure(errorMessage)
        }
    }
}
