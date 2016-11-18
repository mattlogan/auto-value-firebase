package me.mattlogan.auto.value.firebase.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import me.mattlogan.auto.value.firebase.adapter.TypeAdapter;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * An annotation that indicates the auto-value-firebase {@link TypeAdapter} to use to
 * serialize and deserialize the field.  The value must be set to a valid {@link TypeAdapter}
 * class.
 *
 * <pre>
 * <code>
 * {@literal @}AutoValue @FirebaseValue public abstract class Foo {
 *   {@literal @}FirebaseAdapter(DateTypeAdapter.class) public abstract Date date();
 * }
 * </code>
 * </pre>
 *
 * The generated code will instantiate and use the {@code DateTypeAdapter} class to serialize and
 * deserialize the {@code date()} property. In order for the generated code to instantiate the
 * {@link TypeAdapter}, it needs a public, no-arg constructor.
 */
@Target(METHOD)
@Retention(SOURCE)
@Documented
public @interface FirebaseAdapter {
    Class<? extends TypeAdapter<?, ?>> value();
}
