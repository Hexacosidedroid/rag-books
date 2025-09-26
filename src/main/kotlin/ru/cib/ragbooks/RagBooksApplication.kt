package ru.cib.ragbooks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RagBooksApplication

fun main(args: Array<String>) {
    runApplication<RagBooksApplication>(*args)
}