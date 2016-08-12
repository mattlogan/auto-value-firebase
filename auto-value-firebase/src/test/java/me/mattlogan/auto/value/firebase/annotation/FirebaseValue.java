package me.mattlogan.auto.value.firebase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a copy of the FirebaseValue annotation from the separate
 * auto-value-firebase-annotation module. This is a bit easier than
 * including auto-value-firebase-annotation as a transitive dependency.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface FirebaseValue {
}
