package org.ldv.chatbot.parser

import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object MarkdownToHtmlConverter {
    private val parser: Parser = Parser.builder().build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

    fun convert(markdown: String): String {
        val document: Node = parser.parse(markdown)
        return renderer.render(document)
    }
}