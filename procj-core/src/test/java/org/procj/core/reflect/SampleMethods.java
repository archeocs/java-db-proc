package org.procj.core.reflect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;

public interface SampleMethods {

  @ProcedureConfig(name = "test-procedure")
  void voidMethod();

  @TxRollback
  Set<String> setMethod();

  HashSet<String> hashSetMethod();

  List<String> listMethod();

  ArrayList<String> arrayListMethod();

  Map<String, String> mapMethod();

  TreeMap<Number, Object> treeMapMethod();

  Object objectMethod();

  @TxCommit
  int intMethod();

  boolean booleanMethod();

  Integer integerMethod();

  String toString();

  int hashCode();

  String toString(Object x);

  int hashCode(Object x);

  boolean equals(Object other);

  boolean equals(String other);

  boolean equals(Object o1, Object o2);
}
