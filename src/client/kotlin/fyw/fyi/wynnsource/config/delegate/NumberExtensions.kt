@file:Suppress("unused")

package fyw.fyi.wynnsource.config.delegate

import fyw.fyi.wynnsource.config.constraint.RangeConstraint
import fyw.fyi.wynnsource.data.lang.LangRegistry
import fyw.fyi.wynnsource.data.lang.Translatable

/**
 * Extension functions for numeric config delegates.
 * Provides type-safe range constraints for Int, Long, Float, and Double.
 */

// ==================== Int Extensions ====================

/**
 * Add a range constraint for Int values.
 */
fun ConfigDelegate<Int>.range(
    min: Int,
    max: Int,
    errorMessage: Translatable? = null
): ConfigDelegate<Int> {
    val error = errorMessage ?: defaultRangeError(min, max)
    entry.addConstraint(RangeConstraint(min, max, error))
    return this
}

/**
 * Add a minimum value constraint for Int values.
 */
fun ConfigDelegate<Int>.min(
    min: Int,
    errorMessage: Translatable? = null
): ConfigDelegate<Int> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.min.$min",
        "值不能小于 $min",
        "Value cannot be less than $min"
    )
    entry.addConstraint(RangeConstraint(min, Int.MAX_VALUE, error))
    return this
}

/**
 * Add a maximum value constraint for Int values.
 */
fun ConfigDelegate<Int>.max(
    max: Int,
    errorMessage: Translatable? = null
): ConfigDelegate<Int> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.max.$max",
        "值不能大于 $max",
        "Value cannot be greater than $max"
    )
    entry.addConstraint(RangeConstraint(Int.MIN_VALUE, max, error))
    return this
}

// ==================== Long Extensions ====================

/**
 * Add a range constraint for Long values.
 */
fun ConfigDelegate<Long>.range(
    min: Long,
    max: Long,
    errorMessage: Translatable? = null
): ConfigDelegate<Long> {
    val error = errorMessage ?: defaultRangeError(min, max)
    entry.addConstraint(RangeConstraint(min, max, error))
    return this
}

/**
 * Add a minimum value constraint for Long values.
 */
fun ConfigDelegate<Long>.min(
    min: Long,
    errorMessage: Translatable? = null
): ConfigDelegate<Long> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.min.$min",
        "值不能小于 $min",
        "Value cannot be less than $min"
    )
    entry.addConstraint(RangeConstraint(min, Long.MAX_VALUE, error))
    return this
}

/**
 * Add a maximum value constraint for Long values.
 */
fun ConfigDelegate<Long>.max(
    max: Long,
    errorMessage: Translatable? = null
): ConfigDelegate<Long> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.max.$max",
        "值不能大于 $max",
        "Value cannot be greater than $max"
    )
    entry.addConstraint(RangeConstraint(Long.MIN_VALUE, max, error))
    return this
}

// ==================== Float Extensions ====================

/**
 * Add a range constraint for Float values.
 */
fun ConfigDelegate<Float>.range(
    min: Float,
    max: Float,
    errorMessage: Translatable? = null
): ConfigDelegate<Float> {
    val error = errorMessage ?: defaultRangeError(min, max)
    entry.addConstraint(RangeConstraint(min, max, error))
    return this
}

// ==================== Double Extensions ====================

/**
 * Add a range constraint for Double values.
 */
fun ConfigDelegate<Double>.range(
    min: Double,
    max: Double,
    errorMessage: Translatable? = null
): ConfigDelegate<Double> {
    val error = errorMessage ?: defaultRangeError(min, max)
    entry.addConstraint(RangeConstraint(min, max, error))
    return this
}

// ==================== Helper Functions ====================

private fun <T : Number> defaultRangeError(min: T, max: T): Translatable {
    return LangRegistry.translatable(
        "config.error.range.${min}_$max",
        "值必须在 $min 到 $max 之间",
        "Value must be between $min and $max"
    )
}
