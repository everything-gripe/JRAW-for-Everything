package net.dean.jraw.models;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * All of the values in this enum can be returned by the reddit API, except for {@link #URL} and {@link #NONE}.
 * If {@code URL} is returned, then Reddit has created a thumbnail for specifically for that post. If
 * {@code NONE} is returned, then there is no thumbnail available.
 */
public enum ThumbnailType {

    /** For when a post is marked as NSFW */
    NSFW,

    /** For when reddit couldn't create one */
    DEFAULT,

    /** For self posts */
    SELF,

    /** No thumbnail */
    NONE,

    /** A custom thumbnail that can be accessed by calling {@link Submission#getThumbnail()} */
    URL;

    public static ThumbnailType parse(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return NONE;
        }

        try {
            return ThumbnailType.valueOf(value.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            // "thumbnail"'s value is a URL
            return ThumbnailType.URL;
        }
    }
}
