package me.mattlogan.auto.value.firebase.example.models;

import com.google.auto.value.AutoValue;
import com.google.firebase.database.DataSnapshot;
import java.util.List;
import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;

@AutoValue @FirebaseValue
public abstract class Taco {

  public static Taco create(String name, List<Ingredient> ingredients, Review review) {
    return new AutoValue_Taco(name, ingredients, review);
  }

  public static Taco fromDataSnapshot(DataSnapshot dataSnapshot) {
    return new AutoValue_Taco(dataSnapshot.getValue(AutoValue_Taco.FirebaseValue.class));
  }

  public Object toFirebaseValue() {
    return new AutoValue_Taco.FirebaseValue(this);
  }

  public abstract String name();

  public abstract List<Ingredient> ingredients();

  public abstract Review review();
}
