package me.mattlogan.auto.value.firebase.example.models;

import com.google.auto.value.AutoValue;
import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;

@AutoValue @FirebaseValue
public abstract class Ingredient {

  public static Ingredient create(String name, int spiciness) {
    return new AutoValue_Ingredient(name, spiciness);
  }

  public abstract String name();

  public abstract int spiciness();
}
