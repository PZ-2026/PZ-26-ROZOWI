package pl.edu.ur.blokur

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BlokurApplication

fun main(args: Array<String>) {
    runApplication<BlokurApplication>(*args)
}
