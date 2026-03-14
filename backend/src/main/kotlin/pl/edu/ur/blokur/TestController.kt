package pl.edu.ur.blokur.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/api/hello")
    fun hello() = mapOf("message" to "Hello World!!")
}