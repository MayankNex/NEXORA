package com.example

import com.example.ui.components.ContentSegment
import com.example.ui.components.parseMarkdownSegments
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testMarkdownCodeBlockParsing() {
        val markdown = "Here is some code:\n```kotlin\nval x = 42\n```\nAnd some trailing text."
        val segments = parseMarkdownSegments(markdown)

        assertEquals(3, segments.size)
        assertTrue(segments[0] is ContentSegment.Text)
        assertTrue(segments[1] is ContentSegment.Code)
        assertTrue(segments[2] is ContentSegment.Text)

        val codeSeg = segments[1] as ContentSegment.Code
        assertEquals("kotlin", codeSeg.language)
        assertEquals("val x = 42", codeSeg.code)
    }

    @Test
    fun testPlainTextParsing() {
        val markdown = "Hello NEXORA AI without code blocks."
        val segments = parseMarkdownSegments(markdown)
        assertEquals(1, segments.size)
        assertTrue(segments[0] is ContentSegment.Text)
        assertEquals("Hello NEXORA AI without code blocks.", (segments[0] as ContentSegment.Text).content)
    }
}
