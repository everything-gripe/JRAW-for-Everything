package net.dean.jraw.databind;

import com.ryanharter.auto.value.moshi.MoshiAdapterFactory;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Add this factory to your {@code Moshi.Builder} to enable serializing all {@code @AutoValue} models. See
 * <a href="https://github.com/rharter/auto-value-moshi#factory">here</a> for more details.
 */
@MoshiAdapterFactory
public class ModelAdapterFactory implements JsonAdapter.Factory {

    AutoValueMoshi_ModelAdapterFactory factory = new AutoValueMoshi_ModelAdapterFactory();

    public static JsonAdapter.Factory create() {
        return new ModelAdapterFactory();
    }

    @Nullable
    @Override
    public JsonAdapter<?> create(@NotNull Type type, @NotNull Set<? extends Annotation> annotations, @NotNull Moshi moshi) {
        JsonAdapter<?> adapter = factory.create(type, annotations, moshi);
        return adapter == null ? null : adapter.serializeNulls();
    }
}
