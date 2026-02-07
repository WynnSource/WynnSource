package fyw.fyi.wynnsource.config.delegate

import fyw.fyi.wynnsource.config.constraint.Constraint
import fyw.fyi.wynnsource.config.constraint.PredicateConstraint
import fyw.fyi.wynnsource.config.core.ConfigEntry
import fyw.fyi.wynnsource.data.lang.Translatable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Kotlin property delegate for config entries.
 * Provides a clean DSL for defining config options.
 *
 * Usage:
 * ```
 * var enabled by config(default = false)
 *     .name(translatable("...", "...", "..."))
 *     .description(translatable("...", "...", "..."))
 *     .onChange { old, new -> ... }
 * ```
 */
class ConfigDelegate<T : Any>(
    internal val entry: ConfigEntry<T>
) : ReadWriteProperty<Any?, T> {

    /**
     * Called when the delegate is first bound to a property via `by`.
     * This eagerly initializes the key from the property name,
     * ensuring the key is available before load() runs.
     */
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ConfigDelegate<T> {
        if (entry.key.isEmpty()) {
            entry.key = property.name
        }
        return this
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return entry.getCommitted()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        entry.setPending(value)
        // Validate before committing
        val result = entry.validate()
        if (!result.valid) {
            entry.discardPending()
            return
        }
        entry.commit()
    }

    /**
     * Set the display name for this config entry.
     */
    fun name(name: Translatable): ConfigDelegate<T> {
        entry.name = name
        return this
    }

    /**
     * Set the description/tooltip for this config entry.
     */
    fun description(description: Translatable): ConfigDelegate<T> {
        entry.description = description
        return this
    }

    /**
     * Add a change listener that is called when the value is committed.
     */
    fun onChange(listener: (old: T, new: T) -> Unit): ConfigDelegate<T> {
        entry.addChangeListener(listener)
        return this
    }

    /**
     * Add a custom constraint with a predicate function.
     */
    fun constraint(
        predicate: (T) -> Boolean,
        errorMessage: Translatable
    ): ConfigDelegate<T> {
        entry.addConstraint(PredicateConstraint(predicate, errorMessage))
        return this
    }

    /**
     * Add a pre-built constraint.
     */
    fun constraint(constraint: Constraint<T>): ConfigDelegate<T> {
        entry.addConstraint(constraint)
        return this
    }
}
