package net.dean.jraw.http

import net.dean.jraw.JrawUtils
import net.dean.jraw.http.SimpleHttpLogger.Companion.DEFAULT_LINE_LENGTH
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.PrintStream
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Bare-bones implementation of HttpLogger
 *
 * This logger will print the request's method ("GET", "POST", etc.) and URL, as well as the response's base media type
 * (e.g. "application/json") and body. All output is truncated unless [maxLineLength] is less than 0.
 *
 * Here's an example of the 12th request logged by a [SimpleHttpLogger]
 *
 * ```
 * [12 ->] POST https://oauth.everything.gripe/foo?bar=baz
 *         form: form=foo
 *               abc=123
 * [12 <-] 200 application/json: '{"foo":"bar"}'
 * ```
 *
 * @see DEFAULT_LINE_LENGTH
 */
class SimpleHttpLogger @JvmOverloads constructor(
    /** The maximum amount of characters per line */
    val maxLineLength: Int = DEFAULT_LINE_LENGTH,
    /** How to print messages */
    val out: LogAdapter = PrintStreamLogAdapter(System.out)
) : HttpLogger {
    private val counter: AtomicInteger = AtomicInteger(1)

    init {
        if (maxLineLength >= 0 && maxLineLength <= ELLIPSIS.length)
            throw IllegalArgumentException("maxLineLength must be less than 0 or greater than ${ELLIPSIS.length}")
    }

    override fun request(r: HttpRequest, sent: Date): HttpLogger.Tag {
        val id = counter.getAndIncrement()

        val tag = "[$id ->]"

        out.writeln(truncate("$tag ${r.method} ${r.url}", maxLineLength))

        val form = parseForm(r)
        if (!form.isEmpty()) {
            logMap(
                baseIndentLength = tag.length,
                header = "form:",
                pairs = form
            )
        }

        return HttpLogger.Tag(id, sent)
    }

    override fun response(tag: HttpLogger.Tag, res: HttpResponse) {
        val contentType = formatContentType(res.raw)
        val body = res.body.replace("\n", "")
        val formattedTag = "[<- ${tag.requestId}]"

        out.writeln(truncate("$formattedTag ${res.code} $contentType: '$body'", maxLineLength))
    }

    private fun formatContentType(res: Response): String {
        val type = res.body?.contentType() ?: return NO_CONTENT_TYPE
        return type.type + '/' + type.subtype
    }

    private fun parseForm(r: HttpRequest): Map<String, String> {
        if (r.body == null) return mapOf()

        val type = r.body.contentType()!!
        // Make sure we have URL-encoded data before we try to parse it
        if (type.type != "application" || type.subtype != "x-www-form-urlencoded") return mapOf()

        return JrawUtils.parseUrlEncoded(readRequestBody(r.body))
    }

    private fun readRequestBody(body: RequestBody): String {
        val buff = Buffer()
        body.writeTo(buff)
        return buff.readUtf8().replace("\n", "")
    }

    private fun logMap(baseIndentLength: Int, header: String, pairs: Map<String, String>) {
        var needsHeader = true
        val baseIndent = " ".repeat(baseIndentLength)
        for ((k, v) in pairs) {
            val prefix = if (needsHeader) header else " ".repeat(header.length)
            if (needsHeader) needsHeader = false
            out.writeln(truncate("$baseIndent $prefix $k=$v", maxLineLength))
        }
    }

    /** */
    companion object {
        internal val ELLIPSIS = "(...)"
        private val NO_CONTENT_TYPE = "<no content type>"
        /** 200 character limit per line. Enough to view the full request and get the gist of the response. */
        const val DEFAULT_LINE_LENGTH = 200

        @JvmStatic
        private fun truncate(str: String, limit: Int): String {
            return when {
                limit < 0 -> return str
                str.length > limit -> str.substring(0, limit - ELLIPSIS.length) + ELLIPSIS
                else -> str
            }
        }
    }
}
