Wanting something like Seq evolved the interface from

```java
void apply( ErrorReporter reporter, T input );
```

to

```java
boolean apply( ErrorReporter reporter, T input );
```

since we need a signal to know whether a Validator failed in order to stop execution. Currently, with fail-fast we throw
an exception in the error reporter to stop execution on the first error. I am not a fan of using exceptions for a normal
feature as it's not an exceptional case. Using exceptions for fail-fast works and at least only puts burden on the
orchestration of the validation to catch it. I did not want all our Validator functions to have to deal with any
exceptions. This is why I went for returning a boolean with false for failed.

To make it easier to write small Validator functions I decided to adjust the error reporter from.

```java
public ErrorReporter add( String error ) {
        ...
    return this;
}
```

to

```java
public boolean add( String error ) {
        ...
        return false;
}
```

This has the advantage returning directly on adding the error like

```java
return reporter.add( error );
```

This makes it easier to write lambda validations as well. It also saves you from thinking about whether to return
false/true which can also be confusing. Which value means what? We could obviously introduce another type, but then you
can't simply write

```java
if (!validator.apply(reporter, input))
```

It's also nice that it discourages adding more than one error by making it a little harder than with a fluent style. As
my goal is for us to write small Validators that only add one error. We can build up more complex Validators using the
helper/aggregate Validators like All/Seq/Each.