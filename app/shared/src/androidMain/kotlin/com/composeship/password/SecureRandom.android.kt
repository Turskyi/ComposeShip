package com.composeship.password

import java.security.SecureRandom

private val secureRandom = SecureRandom()

internal actual fun secureRandomInt(bound: Int): Int = secureRandom.nextInt(bound)
