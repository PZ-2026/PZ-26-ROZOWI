package pl.edu.ur.blokur.ui.cli

fun main(args: Array<String>) {
    println("Hello World!")

    if (args.isNotEmpty())
        args.forEach { println("$it \n") }

}