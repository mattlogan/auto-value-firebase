package me.mattlogan.auto.value.firebase.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import me.mattlogan.auto.value.firebase.TypeAdapter;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target(METHOD)
@Retention(SOURCE)
@Documented
public @interface FirebaseAdapter {
    Class<? extends TypeAdapter<?>> value();
}
