package me.mattlogan.auto.value.firebase.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import me.mattlogan.auto.value.firebase.R;
import me.mattlogan.auto.value.firebase.example.models.Ingredient;
import me.mattlogan.auto.value.firebase.example.models.Review;
import me.mattlogan.auto.value.firebase.example.models.Taco;

public class MainActivity extends AppCompatActivity {

  ChildEventListener tacosEventListener;
  DatabaseReference tacosRef;
  DatabaseReference tacoRef;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    tacosRef = FirebaseDatabase.getInstance().getReference();

    tacosEventListener = new ChildEventListener() {
      @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d("testing", "onChildAdded: " + Taco.fromDataSnapshot(dataSnapshot));
      }

      @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {

      }

      @Override public void onChildRemoved(DataSnapshot dataSnapshot) {

      }

      @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {

      }

      @Override public void onCancelled(DatabaseError databaseError) {

      }
    };

    tacosRef.addChildEventListener(tacosEventListener);

    List<Ingredient> ingredients = new ArrayList<>();
    ingredients.add(Ingredient.create("Cactus", 3));
    ingredients.add(Ingredient.create("Peppers", 5));
    ingredients.add(Ingredient.create("Cheese", 1));
    ingredients.add(Ingredient.create("Flour tortilla", 1));

    Review review = Review.create("Amazing taco.", 5);

    Taco taco = Taco.create("Kalimari Desert", ingredients, review);

    tacoRef = tacosRef.push();
    tacoRef.setValue(taco.toFirebaseValue());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    tacoRef.removeValue();
    tacosRef.removeEventListener(tacosEventListener);
  }
}
