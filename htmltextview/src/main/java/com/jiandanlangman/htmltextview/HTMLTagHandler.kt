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
        private val defaultATagHandler = ATagHandler()
        private val defaultImgTagHandler = ImgTagHandler()
        private val defaultBaseTagHandler = BaseTagHandler()
        private val defaultSpaceTagHandler = SpaceTagHandler()
        private val defaultBrTagHandler = BrTagHandler()

        private var resourcesProvider: ResourcesProvider? = null

        init {
            registerTagHandler("a", defaultATagHandler)
            registerTagHandler("span", defaultATagHandler)
            registerTagHandler("font", defaultATagHandler)
            registerTagHandler("img", defaultImgTagHandler)
            registerTagHandler("base", defaultBaseTagHandler)
            registerTagHandler("space", defaultSpaceTagHandler)
            registerTagHandler("br", defaultBrTagHandler)
        }

        internal fun registerTagHandler(tag: String, handler: TagHandler) {
            tagHandlers[tag] = handler
        }

        internal fun unRegisterTagHandler(tag: String) {
            tagHandlers.remove(tag)
            when (tag) {
                "a", "span", "font" -> registerTagHandler(tag, defaultATagHandler)
                "img" -> registerTagHandler(tag, defaultImgTagHandler)
                "base" -> registerTagHandler(tag, defaultBaseTagHandler)
                "space" -> registerTagHandler(tag, defaultSpaceTagHandler)
                "br" -> registerTagHandler(tag, defaultBrTagHandler)
            }
        }

        internal fun setResourcesProvider(imageGetter: ResourcesProvider?) {
            this.resourcesProvider = imageGetter
        }

        internal fun getResourcesProvider() = resourcesProvider

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
        val tag = localName.toLowerCase(Locale.ENGLISH)
        getTagHandler(tag)?.let {
            val attrs = HashMap<String, String>()
            for (i in 0 until atts.length) {
                val key = atts.getQName(i).toLowerCase(Locale.ENGLISH)
                attrs[key] = atts.getValue(key)
            }
            val style = Style.from(attrs.remove(Attribute.STYLE.value) ?: "")
            val background = Background.from(attrs.remove(Attribute.BACKGROUND.value) ?: "")
            if (it.isSingleTag())
                it.handleTag(target, tag, originalOutput!!, originalOutput!!.length, attrs, style, background)
            else
                tagRecorderList.add(TagRecorder(attrs, style, background, originalOutput!!.length))
        } ?: originalContentHandler?.startElement(uri, localName, qName, atts)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        val tag = localName.toLowerCase(Locale.ENGLISH)
        getTagHandler(tag)?.let {
            if(it.isSingleTag())
                return
            val tagRecorder = tagRecorderList.removeLast()
            it.handleTag(target, tag, originalOutput!!, tagRecorder.start, tagRecorder.attrs, tagRecorder.style, tagRecorder.background)
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

    private class TagRecorder(val attrs: Map<String, String>, val style: Style, val background: Background, val start: Int)

}