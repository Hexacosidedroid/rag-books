package ru.cib.ragbooks.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.cib.ragbooks.service.ChatService
import ru.cib.ragbooks.service.IngestionService

@RestController
class AiController(
    private val ingestionService: IngestionService,
    private val chatService: ChatService,
) {

    @GetMapping("/load")
    fun load(): String {
        ingestionService.loadPdf()
        return "PDF LOADED"
    }

    @GetMapping("/ask")
    fun answer(@RequestParam question: String) = chatService.ask(question)
}