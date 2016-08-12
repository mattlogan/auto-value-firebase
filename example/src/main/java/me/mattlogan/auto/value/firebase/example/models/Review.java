package me.mattlogan.auto.value.firebase.example.models;

import com.google.auto.value.AutoValue;
import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;

@AutoValue @FirebaseValue
public abstract class Review {

  public static Review create(String description, int rating) {
    return new AutoValue_Review(description, rating);
  }

  public abstract String description();

  public abstract int rating();
}
