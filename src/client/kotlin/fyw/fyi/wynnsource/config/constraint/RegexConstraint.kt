package fyw.fyi.wynnsource.config.constraint

import fyw.fyi.wynnsource.data.lang.Translatable

/**
 * Constraint that validates a string matches a regular expression pattern.
 *
 * @param regex The compiled regex pattern to match against
 * @param errorMessage The error message to display when validation fails
 */
class RegexConstraint(
    private val regex: Regex,
    private val errorMessage: Translatable
) : Constraint<String> {

    /**
     * Create a RegexConstraint from a pattern string.
     */
    constructor(pattern: String, errorMessage: Translatable) : this(pattern.toRegex(), errorMessage)

    override fun validate(value: String): ValidationResult {
        return if (regex.matches(value)) {
            ValidationResult.SUCCESS
        } else {
            ValidationResult.failure(errorMessage)
        }
    }
}
