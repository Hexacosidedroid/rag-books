package ru.cib.ragbooks.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatClientBuilder: ChatClient.Builder,
    private val vectorStore: VectorStore
) {
    val log: Logger = LoggerFactory.getLogger(IngestionService::class.java)
    private companion object {
        const val SYSTEM_PROMPT_TEMPLATE = """
            You are a Docker security expert. Answer ONLY using the provided context.
            Rules:
            1. If question is unrelated to Docker - respond "I only answer Docker questions"
            2. If context doesn't contain needed information - say "No relevant data in my documentation"
            3. Be extremely concise and technical
            4. ALWAYS reference page number if available
            Context from Docker Security Guide:
            {context}
        """
    }
    private val systemPromptTemplate = SystemPromptTemplate(SYSTEM_PROMPT_TEMPLATE)

    fun ask(question: String): String? {
        if (!isDockerRelated(question)) {
            log.warn("Non-Docker question detected: $question")
            return "I specialize only in Docker-related topics. Please ask about Docker, its architecture or security."
        }
        val relevantDocs = vectorStore.similaritySearch(
            SearchRequest
                .builder()
                .similarityThreshold(0.8)
                .topK(6)
                .query(question)
                .build()
        )
        if (relevantDocs.isNullOrEmpty() || !isDockerContext(relevantDocs)) {
            log.warn("No relevant Docker context found for: $question")
            return "I couldn't find relevant Docker information. Please ask a Docker-specific question."
        }

        val context = relevantDocs.joinToString("\n")
        val systemMessage = systemPromptTemplate.create(mapOf("context" to context))
        log.info("Prompt for model: $systemMessage \n------\n $question")
        return chatClientBuilder
//            .defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
            .build()
            .prompt()
            .system(systemMessage.contents)
            .user(question)
            .call()
            .content()
    }

    private fun isDockerRelated(question: String): Boolean {
        val dockerKeywords = listOf(
            "docker", "container", "image", "registry", "daemon",
            "compose", "swarm", "kubernetes", "security"
        )
        return dockerKeywords.any { keyword ->
            question.contains(keyword, ignoreCase = true)
        }
    }

    private fun isDockerContext(docs: List<Document>): Boolean {
        return docs.any { doc ->
            doc.text!!.contains("docker", ignoreCase = true) ||
                    doc.text!!.contains("container", ignoreCase = true)
        }
    }
}