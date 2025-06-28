package ru.cib.ragbooks.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Service

@Service
class IngestionService(
    private val vectorStore: VectorStore,
    @Value("\${app.pdf-path}")
    private val book: FileSystemResource
) {
    val log: Logger = LoggerFactory.getLogger(IngestionService::class.java)

   fun loadPdf() {
       val pdf = PagePdfDocumentReader(book)
       val pages = pdf.get()
       pages.chunked(10).forEach { chunk ->
           log.info("Loading page $chunk")
           val textSplitter = TokenTextSplitter
               .builder()
               .withChunkSize(1000)
               .withMinChunkLengthToEmbed(0)
               .withMinChunkSizeChars(0)
               .build()
           vectorStore.accept(textSplitter.apply(chunk))
           log.info("VectorStore Loaded with chunk!")
           Thread.sleep(500)
       }
       log.info("VectorStore Loaded with full data!")
   }

}