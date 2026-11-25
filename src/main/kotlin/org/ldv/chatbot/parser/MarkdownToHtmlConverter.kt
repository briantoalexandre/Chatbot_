package org.ldv.chatbot.parser

import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import org.owasp.html.PolicyFactory
import org.owasp.html.Sanitizers



object MarkdownToHtmlConverter {
    private val parser: Parser = Parser.builder().build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().build()


    fun convert(markdown: String): String {
        val document: Node = parser.parse(markdown)
        val rendering = renderer.render(document)
        val sanitized =  sanitizeHtml(rendering)
        return rendering
    }

    fun sanitizeHtml(html: String): String {
        val policy: PolicyFactory = Sanitizers.FORMATTING.and(Sanitizers.LINKS)
        return policy.sanitize(html)
    }


}

