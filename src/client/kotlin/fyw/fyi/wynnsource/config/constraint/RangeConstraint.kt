package fyw.fyi.wynnsource.config.constraint

import fyw.fyi.wynnsource.datagen.lang.Translatable

/**
 * Constraint that validates a numeric value is within a specified range.
 * Can be used with Int, Long, Float, Double, etc.
 *
 * @param min The minimum allowed value (inclusive)
 * @param max The maximum allowed value (inclusive)
 * @param errorMessage The error message to display when validation fails
 */
class RangeConstraint<T : Comparable<T>>(
    private val min: T,
    private val max: T,
    private val errorMessage: Translatable
) : Constraint<T> {

    override fun validate(value: T): ValidationResult {
        return if (value in min..max) {
            ValidationResult.SUCCESS
        } else {
            ValidationResult.failure(errorMessage)
        }
    }

    /**
     * Get the minimum value of the range.
     * Useful for UI components like sliders.
     */
    fun getMin(): T = min

    /**
     * Get the maximum value of the range.
     * Useful for UI components like sliders.
     */
    fun getMax(): T = max
}
