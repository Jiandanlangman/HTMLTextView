package com.jiandanlangman.htmltextview

import android.text.Editable
import android.text.Html
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import org.xml.sax.XMLReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal class HTMLTagHandler(private val target: HTMLTextView) : Html.TagHandler, ContentHandler {

    internal companion object {

        private const val HTML_TAG = "html"

        private val tagHandlers = HashMap<String, TagHandler>()

        init {
            registerTagHandler("a", ATagHandler())
            registerTagHandler("img", ImgTagHandler())
        }

        internal fun registerTagHandler(tag: String, handler: TagHandler) {
            tagHandlers[tag] = handler
        }

        private fun getTagHandler(tag: String) = tagHandlers[tag]

    }

    private val tagRecorderList = ArrayList<TagRecorder>()
    private var count = 0
    private var originalContentHandler: ContentHandler? = null
    private var originalOutput: Editable? = null

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        if (HTML_TAG.equals(tag, true)) {
            if (opening) {
                if (count == 0) {
                    originalContentHandler = xmlReader.contentHandler
                    xmlReader.contentHandler = this
                    originalOutput = output
                }
                count++
            } else {
                count--
                if (count == 0) {
                    xmlReader.contentHandler = originalContentHandler
                    originalContentHandler = null
                    originalOutput = null
                }
            }
        }
    }

    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        getTagHandler(localName.toLowerCase(Locale.ENGLISH))?.let {
            val attrs = HashMap<String, String>()
            val styles = HashMap<String, String>()
            for(i in 0 until atts.length) {
                val key = atts.getQName(i).toLowerCase(Locale.ENGLISH)
                attrs[key] = atts.getValue(key)
            }
            (attrs.remove(com.jiandanlangman.htmltextview.Attributes.STYLE.value) ?: "").split(";").forEach {
                val keyValue = it.split(":")
                styles[keyValue[0].toLowerCase(Locale.ENGLISH)] = if (keyValue.size > 1) keyValue[1] else ""
            }
            tagRecorderList.add(TagRecorder(attrs, styles, originalOutput!!.length))
        } ?: originalContentHandler?.startElement(uri, localName, qName, atts)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        val tag = localName.toLowerCase(Locale.ENGLISH)
        getTagHandler(tag)?.let {
            val tagRecorder = tagRecorderList.removeLast()
            it.handleTag(target, tag, originalOutput!!, tagRecorder.start, tagRecorder.attrs, tagRecorder.styles)
        } ?: originalContentHandler?.endElement(uri, localName, qName)
    }

    override fun setDocumentLocator(locator: Locator?) = originalContentHandler?.setDocumentLocator(locator) ?: Unit

    override fun startDocument() = originalContentHandler?.startDocument() ?: Unit

    override fun endDocument() = originalContentHandler?.endDocument() ?: Unit

    override fun startPrefixMapping(prefix: String?, uri: String?) = originalContentHandler?.startPrefixMapping(prefix, uri) ?: Unit

    override fun endPrefixMapping(prefix: String?) = originalContentHandler?.endPrefixMapping(prefix) ?: Unit

    override fun characters(ch: CharArray?, start: Int, length: Int) = originalContentHandler?.characters(ch, start, length) ?: Unit

    override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) = originalContentHandler?.ignorableWhitespace(ch, start, length) ?: Unit

    override fun processingInstruction(target: String?, data: String?) = originalContentHandler?.processingInstruction(target, data) ?: Unit

    override fun skippedEntity(name: String?) = originalContentHandler?.skippedEntity(name) ?: Unit

    private class TagRecorder(val attrs:Map<String, String>, val styles:Map<String,String>, val start:Int)

}