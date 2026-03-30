package pl.edu.ur.blokur

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    excludeName = [
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
    ]
)
class BlokurApplication

fun main(args: Array<String>) {
    runApplication<BlokurApplication>(*args)
}
