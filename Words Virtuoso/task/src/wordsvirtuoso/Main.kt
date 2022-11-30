package wordsvirtuoso

import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random
import kotlin.system.exitProcess

fun checkFile(filename: String, mode: String): List<String> {
    try {
        return File(filename).readLines().map { it.lowercase() }
    } catch (exc: FileNotFoundException) {
        println("Error: The $mode file $filename doesn't exist.")
        exitProcess(status = -1)
    }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Error: Wrong number of arguments.")
        exitProcess(status = -1)
    }

    val fileAll = checkFile(args[0], "words")
    val fileCandidates = checkFile(args[1], "candidate words")

    checkWords(fileAll, args[0])
    checkWords(fileCandidates, args[1])

    val missingWords = fileCandidates.filter { can ->
        fileAll.none { it == can } }

    if (missingWords.isNotEmpty()) {
        println("Error: ${missingWords.size} candidate words are not included in the ${args[0]} file.")
        exitProcess(status = -1)
    }
    println("Words Virtuoso")

    var secretWord: String

    newGame@ while (true) {
        secretWord = try {
            fileCandidates[Random.nextInt(0, fileCandidates.size)]
        } catch (exc: IllegalArgumentException) {
            fileCandidates[0]
        } catch (exc: IndexOutOfBoundsException) {
            println("No entries in file")
            exitProcess(status = -1)
        }

        var turn = 0
        val attempts = mutableListOf<String>()
        val attemptsFull = mutableListOf<String>()
        val used = mutableSetOf<Char>()

        oldGame@ while (true) {
            println("Input a 5-letter word:")
            val start = System.nanoTime()

            when (val turnWord = readln().lowercase()) {
                "exit" -> {
                    println("The game is over."); exitProcess(status = 0)
                }
                else -> {
                    turn++
                    val result = checkInput(
                        turnWord,
                        fileAll,
                        secretWord,
                        turn,
                        start,
                        attempts,
                        attemptsFull,
                        used)
                    if (result) {
                        //used.addAll(result.second.toSet())
                        continue@oldGame
                    } else {
                        //println(result.first)
                        continue@newGame
                    }
                }
            }
        }
    }
}

fun checkInput(input: String,
               fileWords: List<String>,
               word: String,
               attempt: Int,
               start: Long,
               attempts: MutableList<String>,
               attemptsFull: MutableList<String>,
               used: MutableSet<Char>): Boolean {

    val tempWord = "_____".toCharArray()
    var result = true

    if (input.length != 5) {
        println("The input isn't a 5-letter word.") //to used
        result = false
    } else if (!Regex("^[a-z]+\$").matches(input)) {
        println("One or more letters of the input aren't valid.") //to used
        result = false
    } else if (input.toSet().size < input.length) {
        println("The input has duplicate letters.") //to used
        result = false
    } else if (!fileWords.contains(input)) {
        println("The input word isn't included in my words list.") //to used
        result = false
    } else if (input == word) {
        if (attempt == 1) {
            for (c in word.uppercase()) {
                print("\u001B[48:5:10m${c}\u001B[0m")
            }
            println()
            println("Correct!")
            println("Amazing luck! The solution was found at once.")
        } else {
            printAttempts(attempts, attemptsFull)
            for (c in word.uppercase()) {
                print("\u001B[48:5:10m${c}\u001B[0m")
            }

            println("Correct!")
            println("The solution was found after $attempt tries in ${(System.nanoTime() - start) * 1_000_000} seconds.")
        }
        exitProcess(status = 0)
    } else {

        //val setUsed = used.toCharArray().filter { it.isLetter() }.toMutableSet()

        for ((i, c) in input.withIndex()) {
            if (!word.contains(c)) {
                used.add(input[i])
            } else if (word[i] == c) {
                tempWord[i] = c.uppercaseChar()
            } else if (word.contains(c)) {
                tempWord[input.indexOf(c)] = c.lowercaseChar()
            }
        }
        attempts.add(tempWord.joinToString(""))
        attemptsFull.add(input)

        printAttempts(attempts, attemptsFull)
        println("\n")

/*        for (letter in used.filter { it.isLetter() }.sorted().joinToString("").uppercase()) {
            print("\u001B[48:5:14m${letter}\u001B[0m")
        }*/
        println("\u001B[48:5:14m${used.filter { it.isLetter() }.sorted().joinToString("").uppercase()}\u001B[0m")
        println("\n")
    }
    return result
}

fun printAttempts(attempts: List<String>, attemptsFull: List<String>) {
    for ((l, att) in attempts.withIndex()) {
        for ((s, a) in att.withIndex()) {
            when (a) {
                '_' -> print("\u001B[48:5:7m${attemptsFull[l][s].uppercaseChar()}\u001B[0m") //grey
                else -> {
                    if (a.isUpperCase()) {
                        print("\u001B[48:5:10m${a.uppercaseChar()}\u001B[0m")   //green
                    } else {
                        print("\u001B[48:5:11m${a.uppercaseChar()}\u001B[0m") //yellow
                    }
                }
            }
        }
        println()
    }
}

fun checkWords(file: List<String>, filename: String) {
    val invalidWords = mutableListOf<String>()

    for (line in file) {
        if (!Regex("^[a-z]+\$").matches(line) ||
            line.length != 5 ||
            line.toSet().size < line.length) invalidWords.add(line)
    }

    if (invalidWords.size == 0) {
        //println("All words are valid!")
    } else {
        println("Error: ${invalidWords.size} invalid words were found in the $filename file.")
        exitProcess(status = -1)
    }
}
