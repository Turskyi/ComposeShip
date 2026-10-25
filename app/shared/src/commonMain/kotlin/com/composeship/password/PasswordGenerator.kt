package com.composeship.password

internal expect fun secureRandomInt(bound: Int): Int

data class PasswordOptions(
    val includeNumbers: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeUppercase: Boolean = true,
    val beginWithLetter: Boolean = true,
    val excludeSimilarCharacters: Boolean = true,
    val excludeDuplicateCharacters: Boolean = true,
    val excludeSequentialCharacters: Boolean = true
)

fun generatePasswords(
    length: Int = 16,
    quantity: Int = 4,
    options: PasswordOptions = PasswordOptions()
): List<String> = generatePasswords(length, quantity, options, ::secureRandomInt)

fun canGeneratePasswords(length: Int, quantity: Int, options: PasswordOptions): Boolean {
    if (length !in 4..64 || quantity !in 1..20) return false
    val groups = characterGroups(options)
    if (groups.isEmpty() || length < groups.size) return false
    val letters = groups.filter { it.isLetterGroup }.joinToString("") { it.characters }
    if (options.beginWithLetter && letters.isEmpty()) return false
    val alphabet = groups.joinToString("") { it.characters }
    return !options.excludeDuplicateCharacters || length <= alphabet.length
}

internal fun generatePasswords(
    length: Int,
    quantity: Int,
    options: PasswordOptions,
    randomInt: (Int) -> Int
): List<String> {
    require(length in 4..64) { "Password length must be between 4 and 64." }
    require(quantity in 1..20) { "Quantity must be between 1 and 20." }

    val selectedGroups = characterGroups(options)
    require(selectedGroups.isNotEmpty()) { "Select at least one character type." }
    val letters = selectedGroups.filter { it.isLetterGroup }.joinToString("") { it.characters }
    require(!options.beginWithLetter || letters.isNotEmpty()) {
        "Beginning with a letter requires lowercase or uppercase characters."
    }
    require(length >= selectedGroups.size) { "Password length is too short for the selected character types." }

    val alphabet = selectedGroups.joinToString("") { it.characters }
    require(!options.excludeDuplicateCharacters || length <= alphabet.length) {
        "Password length is too long when duplicate characters are excluded."
    }

    return List(quantity) {
        var attempts = 0
        var generated: String? = null
        while (generated == null) {
            attempts++
            check(attempts <= 10_000) { "Unable to generate a password with these settings." }

            val password = StringBuilder(length)
            if (options.beginWithLetter) {
                password.append(letters[randomInt(letters.length)])
            } else {
                password.append(alphabet[randomInt(alphabet.length)])
            }

            for (group in selectedGroups) {
                if (password.none { it in group.characters }) {
                    val candidate = pickCharacter(group.characters, password, options, randomInt) ?: break
                    password.append(candidate)
                }
            }

            while (password.length < length) {
                val candidate = pickCharacter(alphabet, password, options, randomInt) ?: break
                password.append(candidate)
            }

            if (password.length == length) generated = password.toString()
        }
        generated
    }
}

private data class CharacterGroup(val characters: String, val isLetterGroup: Boolean)

private fun characterGroups(options: PasswordOptions): List<CharacterGroup> {
    val groups = buildList {
        if (options.includeNumbers) add("23456789" to false)
        if (options.includeLowercase) add("abcdefghjkmnpqrstuvwxyz" to true)
        if (options.includeUppercase) add("ABCDEFGHJKMNPQRSTUVWXYZ" to true)
    }
    return groups.map { (characters, isLetterGroup) ->
        val expandedCharacters = if (options.excludeSimilarCharacters) characters else characters + when {
            characters == "23456789" -> "01"
            isLetterGroup && characters.first().isLowerCase() -> "ilo"
            else -> "ILO"
        }
        CharacterGroup(expandedCharacters, isLetterGroup)
    }
}

private fun pickCharacter(
    candidates: String,
    password: StringBuilder,
    options: PasswordOptions,
    randomInt: (Int) -> Int
): Char? {
    val available = candidates.filter { candidate ->
        (!options.excludeDuplicateCharacters || candidate !in password) &&
            (!options.excludeSequentialCharacters || password.isEmpty() ||
                !areSequential(password.last(), candidate))
    }
    return available.takeIf { it.isNotEmpty() }?.get(randomInt(available.length))
}

private fun areSequential(first: Char, second: Char): Boolean =
    first.code - second.code == 1 || second.code - first.code == 1
