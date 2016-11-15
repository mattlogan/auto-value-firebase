package me.mattlogan.auto.value.firebase.example.models;

import me.mattlogan.auto.value.firebase.TypeAdapter;

public class StatusAdapter implements TypeAdapter<Status, String> {
  @Override
  public Status fromFirebaseValue(String value) {
    if("cooked".equals(value)){
      return Status.COOKED;
    }
    else if("uncooked".equals(value)){
      return Status.UNCOOKED;
    }
    else {
      throw new IllegalStateException("unsupported");
    }
  }

  @Override
  public String toFirebaseValue(Status value) {
    switch (value){
      case COOKED:
        return "cooked";
      case UNCOOKED:
        return "uncooked";
      default:
        throw new IllegalStateException("unsupported");
    }
  }
}
