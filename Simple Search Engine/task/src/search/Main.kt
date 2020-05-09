package search

import java.io.File
import java.lang.RuntimeException
import java.util.*

enum class MenuOption(val number: Int, val title: String) {
    SEARCH(1, "Search information"),
    PRINT_ALL(2, "Print all data"),
    EXIT(0, "Exit"),
    UNKNOWN(-1, "Unknown");

    companion object {
        fun forValue(value: Int) = values().firstOrNull { it.number == value } ?: UNKNOWN
        fun options() = values().dropLast(1)
    }
}

sealed class SearchStrategy(val value: String) {
    object All : SearchStrategy("all")
    object Any : SearchStrategy("any")
    object None : SearchStrategy("none")

    companion object {
        fun forValue(value: String) =
                when (value.toLowerCase()) {
                    All.value -> All
                    Any.value -> Any
                    None.value -> None
                    else -> throw RuntimeException("Selected strategy does not exist")
                }
    }
}

class IndexedData(private val file: File) {

    private val invertedIndexMap = mutableMapOf<String, MutableList<Int>>()

    override fun toString() = file.readText()

    init {
        var index = 0
        file.forEachLine { line ->
            line.split(Regex("\\s"))
                    .forEach {
                        val key = it.toLowerCase()

                        if (!invertedIndexMap.containsKey(key)) {
                            invertedIndexMap[key] = mutableListOf()
                        }

                        invertedIndexMap[key]!!.add(index)
                    }

            index++
        }

        invertedIndexMap.forEach {
            it.value.sort()
        }
    }

    operator fun get(value: String): List<Int> = invertedIndexMap[value] ?: emptyList()

    private fun readLines(predicate: (String, Int) -> Boolean): List<String> {
        val listOfLinesSelected = mutableListOf<String>()
        var index = 0
        file.forEachLine {
            if (predicate(it, index)) {
                listOfLinesSelected.add(it)
            }
            index++
        }
        return listOfLinesSelected
    }

    private fun searchAny(searchQuery: List<String>): List<String> {
        val linesToSelect = searchQuery.map { this[it] }.flatten().toSortedSet()
        return readLines { _, i -> linesToSelect.contains(i) }
    }

    private fun searchAll(searchQuery: List<String>): List<String> {
        val indicesOfLinesToExtract = mutableSetOf<Int>()
        searchQuery.forEach { queryValue ->
            val indices = this[queryValue]
            indicesOfLinesToExtract.addAll(indices)
            indicesOfLinesToExtract.removeIf { !indices.contains(it) }
        }

        return readLines { _, i -> !indicesOfLinesToExtract.contains(i) }
    }

    private fun searchAnyBut(searchQuery: List<String>): List<String> {
        val linesToAvoid = searchQuery.map { this[it] }.flatten().toSortedSet()
        return readLines { _, i -> !linesToAvoid.contains(i) }
    }

    fun find(value: String, strategy: SearchStrategy): List<String> {
        val unfilteredQuery = value.split(Regex("\\s")).map { it.toLowerCase() }
        val searchQuery = removeDuplicatesAndBlankValues(unfilteredQuery)

        return when (strategy) {
            SearchStrategy.Any -> searchAny(searchQuery)
            SearchStrategy.All -> searchAll(searchQuery)
            SearchStrategy.None -> searchAnyBut(searchQuery)
        }
    }
}

private lateinit var data: IndexedData
private val scanner = Scanner(System.`in`)

fun main(args: Array<String>) {

    val fileName = getFileName(args)
    if (fileName != null)
        data = IndexedData(File(fileName))

    var isTerminated = false
    while (!isTerminated) {
        printMenu()
        isTerminated = handleSelectedOption(scanner.nextInt())
    }
}

fun removeDuplicatesAndBlankValues(values: List<String>): List<String> {
    val resultList = mutableListOf<String>()
    values.forEach {
        if (!resultList.contains(it) && it.isNotBlank()) {
            resultList.add(it)
        }
    }
    return resultList
}

fun getFileName(args: Array<String>) =
        if (args.contains("--data"))
            args[args.indexOf("--data") + 1]
        else
            null

/**
 * Handles selected option, if any option found for given number.
 * Returns true only if EXIT option was selected.
 */
fun handleSelectedOption(input: Int): Boolean {
    scanner.nextLine() // Read empty line after reading Int
    when (MenuOption.forValue(input)) {
        MenuOption.SEARCH -> searchData()
        MenuOption.PRINT_ALL -> printoutAllData()
        MenuOption.UNKNOWN -> println("Incorrect option! Try again.")
        MenuOption.EXIT -> {
            println("Bye!")
            return true
        }
    }
    return false
}

fun searchData() {
    println("Select a matching strategy:")
    val strategy = SearchStrategy.forValue(scanner.nextLine())

    println("Enter a name or email to search all suitable people.")
    val searchFor = scanner.nextLine()
    val searchResults = data.find(searchFor, strategy)

    if (searchResults.isEmpty()) {
        println("No matching people found.\n")
    } else {
        println("${searchResults.size} persons found:")
        println(searchResults.reduce { acc, s -> "$acc\n$s" })
    }
}

fun printoutAllData() {
    println("=== List of people ===")
    println(data)
}

fun printMenu() {
    println("=== Menu ===")
    MenuOption.options().forEach { println("${it.number}. ${it.title}.") }
}