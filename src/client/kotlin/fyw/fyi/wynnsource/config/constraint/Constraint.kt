package fyw.fyi.wynnsource.config.constraint

/**
 * Interface for config value constraints.
 * Constraints validate that a config value meets certain criteria.
 */
interface Constraint<T> {
    /**
     * Validate the given value against this constraint.
     * @param value The value to validate
     * @return The validation result
     */
    fun validate(value: T): ValidationResult
}
