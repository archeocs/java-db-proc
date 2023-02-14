# ProcJ

Trigger SQL procedures like regular Java methods. Check [integration test](https://github.com/archeocs/java-db-proc/blob/main/integration-tests/src/test/java/org/procj/it/ProcjIT.java) as example.

## Types conversions

Every time method with `@Procedure` annotation is executed, ProcJ uses following 
rules to convert execution result to declared return type:
 
 1. Single row output:
 
   * when return type is `void` then execution result is ignored,
 
   * when return type is `java.lang.Object` then first value from first row is returned,
 
   * when return type is primitive (`int`, `long`, `boolean`, etc.) or its object wrapper
     class then ProcJ **tries** to cast first value from first row to declared type. 
     If casting fails, then  exception is thrown,  
 
   * when return type is `java.lang.String` then ProcJ return result of `String.valueOf`
     with first value from first row as argument,
 
   * when return type is `java.util.Date`, `java.time.LocalDateTime`, `java.time.Instant`, 
     or `java.time.LocalDate` then ProcJ return first value from first row converted to declared type,
   
   * when return type is `java.time.OffsetDateTime` then ProcJ return first value from first row
     converted to OffsetDateTime at UTC time zone,
 
   * when return type is `java.util.HashMap` then first row is converted to `java.util.HashMap` 
     with column names as keys and row values as map values,
 
   * when return type is JavaBean then first row is converted to instance of declared type,
 
   * when return type is Java array then first row is returned as array. ProcJ **tries** 
     to cast each row value to declared array type
 
 2. When return type is declared as `java.util.ArrayList` or `java.util.HashSet` then
    called method will return all received rows converted with declared Parametrized type.
    generic type is undefined, or is defined as `java.lang.Object` then each row is represented 
    as `java.util.HashMap`
    
### Examples:

 - `Object getOne()` - returns first value from first row without any conversion,
 
 - `LocalDateTime getOne()` returns first value from first row after conversion 
   to `LocalDateTime` if possible,
   
 - `HashMap getRow()` - returns first row. Each value is mapped as `column_name = row value`,
 
 - `String[] getRow()` - returns first row after conversion of each value to `String`
 
 - `my.package.Book getBook()` - returns first row as instance of `my.package.Book` 

## Conversion rules

ProcJ tries to convert output values (OV) to return type (RT). The baseline rule is to avoid throwing
exception by all means. 

### Numeric and logical types

In first step ProcJ tries to return OV without any conversion, if it is instance of RT. In second 
step ProcJ will try to convert OV to `java.math.Bigdecimal`. If conversion is successful then 
converted value will be base for calculation of final result. Otherwise it is calculated from 
`BigDecimal.ZERO`.

 1. string value `yes` is converted to logical `Boolean.TRUE`
 2. logical `Boolean.TRUE` is converted to `BigDecimal.ONE`
 3. logical `Boolean.FALSE` is converted to `BigDecimal.ZERO`
 4. `BigDecimal.ONE` is converted to logical `Boolean.TRUE`
 5 `BigDecimal.ZERO` is converted to logical `Boolean.FALSE`
 6. string value other than `"yes` is converted to `Boolean.FALSE`
 7. `java.math.BigDecimal` is converted to numeric RT using provided instance methods
 8. string value `"true"` is converted to logical `Boolean.TRUE`
 9. other types are converted to BigDecimal from theirs string representation
 
 If RT represents primitive type and OT is `null`, then final result is calculated from `BigDecimal.ZERO`.
 If RT represents boxed type and OT is `null`, then final result will be `null`
 
 **Examples**
 
  | Return type     | Output value    | Final Value     | Rules       |
  | BigDecimal      | "any-value"     | BigDecimal.ZERO | (6) (3)     |
  | Long            | "Yes"           | 1L              | (1) (2)     |
  | Double          | true            | 1.0d            | (8) (7)     |
  | Long            | new Object()    | 0L              | (9) (6) (3) |
  | Boolean         | BigDecimal.ZERO | false           | (5)         |
