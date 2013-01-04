package com.sssprog.delicious.helpers;

//import java.util.Objects;

public class Tuple2<T1, T2> {
  public final T1 value1;
  public final T2 value2;

  public Tuple2(T1 value1, T2 value2) {
    this.value1 = value1;
    this.value2 = value2;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof Tuple2)) return false;
    @SuppressWarnings("unchecked")
	Tuple2<T1, T2> o = (Tuple2<T1, T2>)obj;
    if (!(value1 == null ? o.value1 == null : value1.equals(o.value1))) return false;
    if (!(value2 == null ? o.value2 == null : value2.equals(o.value2))) return false;
    return true;
  }
  
  public static <T1, T2> Tuple2<T1, T2> newInstance(T1 value1, T2 value2) {
	  return new Tuple2<T1, T2>(value1, value2);
  }

//  @Override
//  public int hashCode() {
//    int hash = 3;
//    hash = 17 * hash + Objects.hashCode(this.value1);
//    hash = 17 * hash + Objects.hashCode(this.value2);
//    return hash;
//  }
}