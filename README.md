AutoValue: Firebase Extension
========

`auto-value-firebase` is an [AutoValue] extension for generating value classes that are compatible with [Firebase Realtime Database].

Usage
-----

Just add `auto-value-firebase` to your project and add the `@FirebaseValue` annotation to your existing `@AutoValue` annotated classes.

This extension creates `Firebase Realtime Database` compatible static inner classes, each named `FirebaseValue`, inside your generated `AutoValue` classes. From there, it's easy to convert your `AutoValue` object to a `FirebaseValue` object and vice versa via generated constructors.

### Example

Here's a simple `AutoValue` class called `Taco`:

```java
@AutoValue
public abstract class Taco {

  public static Taco create(String name, List<Ingredient> ingredients, Review review) {
    return new AutoValue_Taco(name, ingredients, review);
  }

  public abstract String name();

  public abstract List<Ingredient> ingredients();

  public abstract Review review();
}
```

From here, we just add the `@FirebaseValue` annotation. This generates our `FirebaseValue` class as a static inner class of `AutoValue_Taco`.

```java
@AutoValue @FirebaseValue
public abstract class Taco {

  public static Taco create(String name, List<Ingredient> ingredients, Review review) {
    return new AutoValue_Taco(name, ingredients, review);
  }

  // Create AutoValue_Taco instance from AutoValue_Taco.FirebaseValue instance
  public static Taco create(DataSnapshot dataSnapshot) {
    AutoValue_Taco.FirebaseValue taco = dataSnapshot.getValue(AutoValue_Taco.FirebaseValue.class);
    return new AutoValue_Taco(taco);
  }

  // Create AutoValue_Taco.FirebaseValue instance from AutoValue_Taco instance
  public Object toFirebaseValue() {
    return new AutoValue_Taco.FirebaseValue(this);
  }

  public abstract String name();

  public abstract List<Ingredient> ingredients();

  public abstract Review review();
}
```

In addition to the empty constructor required by `Firebase Realtime Database`, the generated `FirebaseValue` class includes a constructor that takes your `AutoValue` class as an argument. Conversely, your `AutoValue` class now also includes a constructor that takes a `FirebaseValue` as its argument.

The `create(DataSnapshot)` and `toFirebaseValue` methods aren't required, but this is how I'd recommend converting your `AutoValue` objects to and from their corresponding `FirebaseValue` objects.

### Supported types

This extension can generate `FirebaseValue` classes that contain any types that `Firebase Realtime Database` supports as described in their [documentation] **except for nested collections**.

For example, `List<Ingredient>` is supported but `List<List<Ingredient>>` is not. This might come in a later release.

### Firebase annotations

`Firebase Realtime Database` provides four annotations to configure the mapping from your value classes to the cloud database and back. You can annotate your methods with `@Exclude` or `@PropertyName`, and you can annotate your classes with `@IgnoreExtraProperties` or `@ThrowOnExtraProperties`.

If you want these annotations to be included in your generated `FirebaseValue` class, just add them to your abstract `AutoValue` class.

### Naming

This extension uses the method names from your `AutoValue` class to generate the fields and getters on the generated `FirebaseValue` class.

As a result, it's required that you **don't** prefix your getters with `get`.


Download
--------

This extension should be included as an `apt` dependency (if you're using [android-apt]).

The `@FirebaseValue` annotation is packaged separately, and should be included as a `provided` dependency.

```groovy
apt 'me.mattlogan.auto.value:auto-value-firebase:0.1.1'
provided 'me.mattlogan.auto.value:auto-value-firebase-annotation:0.1.1'
```


License
-------

    Copyright 2016 Matt Logan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [AutoValue]: https://github.com/google/auto/tree/master/value
 [Firebase Realtime Database]: https://firebase.google.com/docs/database/
 [android-apt]: https://bitbucket.org/hvisser/android-apt
 [documentation]: https://firebase.google.com/docs/database/android/save-data
