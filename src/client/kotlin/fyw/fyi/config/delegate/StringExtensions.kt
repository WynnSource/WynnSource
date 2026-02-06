@file:Suppress("unused")

package fyw.fyi.config.delegate

import fyw.fyi.config.constraint.PredicateConstraint
import fyw.fyi.config.constraint.RegexConstraint
import fyw.fyi.data.lang.LangRegistry
import fyw.fyi.data.lang.Translatable

/**
 * Extension functions for String config delegates.
 * Provides type-safe string constraints like regex, length limits, etc.
 */

/**
 * Add a regex pattern constraint.
 */
fun ConfigDelegate<String>.regex(
    pattern: String,
    errorMessage: Translatable? = null
): ConfigDelegate<String> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.regex",
        "格式不正确",
        "Invalid format"
    )
    entry.addConstraint(RegexConstraint(pattern, error))
    return this
}

/**
 * Add a regex pattern constraint with a compiled Regex.
 */
fun ConfigDelegate<String>.regex(
    regex: Regex,
    errorMessage: Translatable? = null
): ConfigDelegate<String> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.regex",
        "格式不正确",
        "Invalid format"
    )
    entry.addConstraint(RegexConstraint(regex, error))
    return this
}

/**
 * Add a maximum length constraint.
 */
fun ConfigDelegate<String>.maxLength(
    length: Int,
    errorMessage: Translatable? = null
): ConfigDelegate<String> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.maxlength.$length",
        "长度不能超过 $length 个字符",
        "Length cannot exceed $length characters"
    )
    entry.addConstraint(PredicateConstraint({ it.length <= length }, error))
    return this
}

/**
 * Add a minimum length constraint.
 */
fun ConfigDelegate<String>.minLength(
    length: Int,
    errorMessage: Translatable? = null
): ConfigDelegate<String> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.minlength.$length",
        "长度不能少于 $length 个字符",
        "Length cannot be less than $length characters"
    )
    entry.addConstraint(PredicateConstraint({ it.length >= length }, error))
    return this
}

/**
 * Add a constraint that the string must not be blank.
 */
fun ConfigDelegate<String>.notBlank(
    errorMessage: Translatable? = null
): ConfigDelegate<String> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.notblank",
        "不能为空",
        "Cannot be blank"
    )
    entry.addConstraint(PredicateConstraint({ it.isNotBlank() }, error))
    return this
}

/**
 * Add a URL format constraint.
 */
fun ConfigDelegate<String>.url(
    errorMessage: Translatable? = null
): ConfigDelegate<String> {
    val error = errorMessage ?: LangRegistry.translatable(
        "config.error.url",
        "必须是有效的 URL",
        "Must be a valid URL"
    )
    // Allow empty or valid URL format
    entry.addConstraint(
        PredicateConstraint(
            { it.isEmpty() || it.matches(Regex("^https?://.*")) },
            error
        )
    )
    return this
}
