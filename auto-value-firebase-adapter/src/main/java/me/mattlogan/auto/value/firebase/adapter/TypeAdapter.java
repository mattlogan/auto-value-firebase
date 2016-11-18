package me.mattlogan.auto.value.firebase.adapter;

public interface TypeAdapter<T, V> {
    T fromFirebaseValue(V value);

    V toFirebaseValue(T value);
}
