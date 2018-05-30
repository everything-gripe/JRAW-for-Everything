package net.dean.jraw.databind

import com.squareup.moshi.*
import net.dean.jraw.models.Comment
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Message
import net.dean.jraw.models.NestedIdentifiable
import java.lang.reflect.Type

/**
 * This class exists to solve an issue due to reddit's legacy codebase.
 *
 * The JSON structure for a comment may look something like this:
 *
 * ```json
 * {
 *   "kind": "t1",
 *   "data": {
 *     "id": "abcdef",
 *     "body": "but muh standardized json api",
 *     ...
 *   }
 * }
 * ```
 *
 * The "data" node has a key called replies that is a Listing of Comments for all direct children of that comment.
 * The structure when the comment has replies is what you'd expect: a standard Listing. However, when there are no
 * replies, the value is not a JSON object, but rather an empty string. Moshi takes issue with this and fails to parse
 * comments with no replies. Since it's mathematically impossible for a tree with at least one node to NOT have a leaf
 * somewhere in that tree, an Exception is inevitable.
 *
 * This class produces a very simple wrapping JsonAdapter that returns an empty Listing if the next value is an empty
 * string. Otherwise, it delegates the deserialization work to the next JsonAdapter capable of handling Listings.
 *
 * @author Matthew Dean
 */
class RepliesAdapterFactory : JsonAdapter.Factory {
    /** @inheritDoc */
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return when (type) {
            COMMENT_TYPE -> {
                val delegate = moshi.nextAdapter<Listing<Comment>>(this, type, annotations)
                CommentRepliesAdapter(delegate)
            }
            MESSAGE_TYPE -> {
                val delegate = moshi.nextAdapter<Listing<Message>>(this, type, annotations)
                MessageRepliesAdapter(delegate)
            }
            else -> null
        }
    }

    private class CommentRepliesAdapter(private val delegate: JsonAdapter<Listing<Comment>>) : JsonAdapter<Listing<Comment>>() {
        override fun toJson(writer: JsonWriter, value: Listing<Comment>?) {
            delegate.toJson(writer, value)
        }

        override fun fromJson(reader: JsonReader): Listing<Comment>? {
            if (reader.peek() == JsonReader.Token.STRING) {
                val contents = reader.nextString()
                if (contents.isNotEmpty())
                    throw IllegalArgumentException("Expected a Listing<Comment> or an empty string, got \"$contents\" instead.")
                return Listing.empty()
            }
            return delegate.fromJson(reader)
        }
    }

    private class MessageRepliesAdapter(private val delegate: JsonAdapter<Listing<Message>>) : JsonAdapter<Listing<Message>>() {
        override fun toJson(writer: JsonWriter, value: Listing<Message>?) {
            delegate.toJson(writer, value)
        }

        override fun fromJson(reader: JsonReader): Listing<Message>? {
            if (reader.peek() == JsonReader.Token.STRING) {
                val contents = reader.nextString()
                if (contents.isNotEmpty())
                    throw IllegalArgumentException("Expected a Listing<Message> or an empty string, got \"$contents\" instead.")
                return Listing.empty()
            }
            return delegate.fromJson(reader)
        }
    }

    /** */
    companion object {
        @JvmStatic
        private val COMMENT_TYPE = Types.newParameterizedType(Listing::class.java, NestedIdentifiable::class.java)

        @JvmStatic
        private val MESSAGE_TYPE = Types.newParameterizedType(Listing::class.java, Message::class.java)
    }
}
