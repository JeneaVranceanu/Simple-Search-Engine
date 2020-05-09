// TODO: provide three functions here

fun generate(functionName: String): (Int) -> Int =
        when (functionName) {
            "identity" -> { arg -> arg }
            "half" -> { arg -> arg / 2 }
            else -> { _ -> 0 }
        }