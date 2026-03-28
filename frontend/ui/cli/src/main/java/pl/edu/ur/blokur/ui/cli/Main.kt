package pl.edu.ur.blokur.ui.cli

import pl.edu.ur.blokur.application.usecase.TestAUseCase

fun main(args: Array<String>) {
    println("Hello World!")

    if (args.isNotEmpty())
        args.forEach { println("$it \n") }
    //val uc = TestAUseCase()


}