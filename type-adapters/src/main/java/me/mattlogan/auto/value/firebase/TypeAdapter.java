package me.mattlogan.auto.value.firebase;

public interface TypeAdapter<T> {
    T fromFirebaseValue(String value);

    String toFirebaseValue(T value);
}
