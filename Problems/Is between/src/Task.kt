import java.lang.Integer.max
import java.lang.Math.min
import java.util.Scanner

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    val number = scanner.nextInt()
    val rangePart1 = scanner.nextInt()
    val rangePart2 = scanner.nextInt()
    println(number in min(rangePart1, rangePart2)..max(rangePart1, rangePart2))
}
