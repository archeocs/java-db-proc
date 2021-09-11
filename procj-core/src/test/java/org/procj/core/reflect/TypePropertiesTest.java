package org.procj.core.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class TypePropertiesTest {

  @Test
  public void shouldRecognizeVoidType() {
    TypeProperties ut = returnType("voidMethod");
    assertThat(ut.isVoid()).isTrue();
  }

  @Test
  public void shouldRecognizeSetType() {
    TypeProperties ut = returnType("setMethod");
    assertThat(ut.isSet()).isTrue();
  }

  @Test
  public void shouldRecognizeSetTypeInSubclass() {
    TypeProperties ut = returnType("hashSetMethod");
    assertThat(ut.isSet()).isTrue();
  }

  @Test
  public void shouldRecognizeListType() {
    TypeProperties ut = returnType("listMethod");
    assertThat(ut.isList()).isTrue();
  }

  @Test
  public void shouldRecognizeCollectionType() {
    TypeProperties ut = returnType("listMethod");
    assertThat(ut.isCollection()).isTrue();
  }

  @Test
  public void shouldRecognizeListTypeInSubclass() {
    TypeProperties ut = returnType("arrayListMethod");
    assertThat(ut.isList()).isTrue();
  }

  @Test
  public void shouldRecognizeMapType() {
    TypeProperties ut = returnType("mapMethod");
    assertThat(ut.isMap()).isTrue();
  }

  @Test
  public void shouldRecognizeMapTypeInSubclass() {
    TypeProperties ut = returnType("treeMapMethod");
    assertThat(ut.isMap()).isTrue();
  }

  @Test
  public void shouldRecognizeObjectType() {
    TypeProperties ut = returnType("objectMethod");
    assertThat(ut.isObject()).isTrue();
  }

  @Test
  public void shouldNotRecognizeSubclassOfObjectAsObject() {
    TypeProperties ut = returnType("treeMapMethod");
    assertThat(ut.isObject()).isFalse();
  }

  @Test
  public void shouldRecognizePrimitiveInt() {
    TypeProperties ut = returnType("intMethod");
    assertThat(ut.isInteger()).isTrue();
  }

  @Test
  public void shouldRecognizeInteger() {
    TypeProperties ut = returnType("integerMethod");
    assertThat(ut.isInteger()).isTrue();
  }

  @Test
  public void shouldRecognizeBoolean() {
    TypeProperties ut = returnType("booleanMethod");
    assertThat(ut.isBoolean()).isTrue();
  }

  @SneakyThrows
  private TypeProperties returnType(String method) {
    Method m = SampleMethods.class.getMethod(method);
    return new TypeProperties(m.getGenericReturnType());
  }
}
