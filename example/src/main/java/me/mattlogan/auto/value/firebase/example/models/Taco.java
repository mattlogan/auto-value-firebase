package me.mattlogan.auto.value.firebase.example.models;

import com.google.auto.value.AutoValue;
import com.google.firebase.database.DataSnapshot;
import java.util.List;

import me.mattlogan.auto.value.firebase.annotation.FirebaseAdapter;
import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;

@AutoValue @FirebaseValue
public abstract class Taco {

  public static Taco create(String name, List<Ingredient> ingredients, Review review, Status status) {
    return new AutoValue_Taco(name, ingredients, review, status);
  }

  public static Taco create(DataSnapshot dataSnapshot) {
    return dataSnapshot.getValue(AutoValue_Taco.FirebaseValue.class).toAutoValue();
  }

  public Object toFirebaseValue() {
    return new AutoValue_Taco.FirebaseValue(this);
  }

  public abstract String name();

  public abstract List<Ingredient> ingredients();

  public abstract Review review();

  @FirebaseAdapter(StatusAdapter.class)
  public abstract Status status();
}
