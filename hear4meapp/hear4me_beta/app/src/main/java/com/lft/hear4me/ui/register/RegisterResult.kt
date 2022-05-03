package com.lft.hear4me.ui.register


/**
 * Authentication result : success (user details) or error message.
 */
data class RegisterResult(
    val success: Int? = null,
    val error: Int? = null
)