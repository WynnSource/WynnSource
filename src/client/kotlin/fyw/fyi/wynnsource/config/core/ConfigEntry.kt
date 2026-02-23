package fyw.fyi.wynnsource.config.core

import fyw.fyi.wynnsource.config.constraint.Constraint
import fyw.fyi.wynnsource.config.constraint.ValidationResult
import fyw.fyi.wynnsource.datagen.lang.Translatable
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
    internal val loadListeners = mutableListOf<(new: T) -> Unit>() // includes change by config load

    // The value that has been saved/committed
    private var committedValue: T = default

    // The value currently being edited in the UI (not yet saved)
    private var pendingValue: T = default

    fun getPending(): T = pendingValue
    fun setPending(value: T) {
        pendingValue = value
    }

    fun getCommitted(): T = committedValue
    internal fun setCommitted(value: T) {
        committedValue = value
        pendingValue = value

        loadListeners.forEach { it(value) }
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

    fun discardPending() {
        pendingValue = committedValue
    }

    fun resetToDefault() {
        pendingValue = default
    }

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
     * Add a load listener that is called when the value is loaded from file.
     */
    fun addLoadListener(listener: (new: T) -> Unit) {
        loadListeners.add(listener)
    }


    fun getConstraints(): List<Constraint<T>> = constraints.toList()
}
