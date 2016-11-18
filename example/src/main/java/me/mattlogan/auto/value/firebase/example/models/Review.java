package me.mattlogan.auto.value.firebase.example.models;

import com.google.auto.value.AutoValue;

import java.util.Date;

import me.mattlogan.auto.value.firebase.adapter.FirebaseAdapter;
import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;

@AutoValue @FirebaseValue
public abstract class Review {

  public static Review create(String description, int rating, Date reviewDate) {
    return new AutoValue_Review(description, rating, reviewDate);
  }

  public abstract String description();

  public abstract int rating();

  @FirebaseAdapter(DateAdapter.class) public abstract Date dateOfReview();
}

