# ProcJ

## Converting types

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
 
   * when return data-time type (such as `java.util.Date`, `java.time.LocalDateTime`, etc.) then
     ProcJ return first value from first row converted to declared type,
 
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