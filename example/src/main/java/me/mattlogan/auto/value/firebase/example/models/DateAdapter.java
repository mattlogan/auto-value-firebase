package me.mattlogan.auto.value.firebase.example.models;

import java.util.Date;

import me.mattlogan.auto.value.firebase.TypeAdapter;

public class DateAdapter implements TypeAdapter<Date, Long> {
  @Override
  public Date fromFirebaseValue(Long value) {
    return new Date(value);
  }

  @Override
  public Long toFirebaseValue(Date value) {
    return value.getTime();
  }
}
