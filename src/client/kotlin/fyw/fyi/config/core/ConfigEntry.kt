package fyw.fyi.config.core

import fyw.fyi.config.constraint.Constraint
import fyw.fyi.config.constraint.ValidationResult
import fyw.fyi.data.lang.Translatable
import kotlin.reflect.KClass

/**
 * Represents a single config entry with its metadata and value management.
 *
 * @param key The unique key for this config entry (used for serialization)
 * @param default The default value
 * @param type The Kotlin class of the value type
 * @param name The display name (for UI)
 * @param description The description/tooltip (for UI)
 */
class ConfigEntry<T : Any>(
    var key: String,
    val default: T,
    val type: KClass<T>,
    var name: Translatable? = null,
    var description: Translatable? = null
) {
    internal val constraints = mutableListOf<Constraint<T>>()
    internal val changeListeners = mutableListOf<(old: T, new: T) -> Unit>()

    // The value that has been saved/committed
    private var committedValue: T = default

    // The value currently being edited in the UI (not yet saved)
    private var pendingValue: T = default

    /**
     * Get the pending (currently edited) value.
     */
    fun getPending(): T = pendingValue

    /**
     * Set the pending value (called from UI when user edits).
     */
    fun setPending(value: T) {
        pendingValue = value
    }

    /**
     * Get the committed (saved) value.
     */
    fun getCommitted(): T = committedValue

    /**
     * Set the committed value directly (used when loading from file).
     */
    internal fun setCommitted(value: T) {
        committedValue = value
        pendingValue = value
    }

    /**
     * Commit the pending value to the committed value.
     * This triggers change listeners if the value changed.
     */
    fun commit() {
        if (pendingValue != committedValue) {
            val old = committedValue
            committedValue = pendingValue
            changeListeners.forEach { it(old, committedValue) }
        }
    }

    /**
     * Discard the pending value and revert to the committed value.
     */
    fun discardPending() {
        pendingValue = committedValue
    }

    /**
     * Reset the pending value to the default.
     */
    fun resetToDefault() {
        pendingValue = default
    }

    /**
     * Check if there are unsaved changes.
     */
    fun hasChanges(): Boolean = pendingValue != committedValue

    /**
     * Validate the pending value against all constraints.
     */
    fun validate(): ValidationResult {
        for (constraint in constraints) {
            val result = constraint.validate(pendingValue)
            if (!result.valid) return result
        }
        return ValidationResult.SUCCESS
    }

    /**
     * Add a constraint to this entry.
     */
    fun addConstraint(constraint: Constraint<T>) {
        constraints.add(constraint)
    }

    /**
     * Add a change listener that is called when the value is committed.
     */
    fun addChangeListener(listener: (old: T, new: T) -> Unit) {
        changeListeners.add(listener)
    }

    /**
     * Get all constraints for this entry.
     */
    fun getConstraints(): List<Constraint<T>> = constraints.toList()
}
