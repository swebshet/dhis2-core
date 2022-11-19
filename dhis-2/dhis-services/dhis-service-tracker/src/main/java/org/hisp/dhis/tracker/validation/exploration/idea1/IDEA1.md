# Validation Idea 1

Is based on the observations that our types often have a large surface area. This is true for validation as well.

TrackerValidationHook interface

```java
    void validate( ValidationErrorReporter report, TrackerBundle bundle );
```

It takes in a reporter which allows us to add errors or warnings. This means our validations often add multiple errors
each actually belonging to a (mostly) independent error.

TrackerBundle is also a type with a huge list of methods both for reading and writing! It also contains the entire
payload sent by the user.

This is why I wanted to explore a Validation interface like

```java
Optional<E> apply(TrackerBundle bundle, T input);
```

or

```java
Optional<E> apply(T input);
```

if no context is needed. I added the TrackerBundle just because I did not want to spend time on thinking about a new
narrow interface like a ValidationContext. We definitely should before implementing any new solution.

## Assumptions

* Validations are independent: right now the implementation assumes Validators are independent of each other. If this
  assumption does not hold true for all Validators we would need to adapt how Validators are connected i.e. build a tree
  rather than a list. Right now that is in the AggregatingValidator. How does `removeOnError` play into this again? Does
  this already invalidate this assumption?

## TODO

* how to apply a validation on a Collection of the type a Validation is able to work on
* think about returning an error with the error message args
  validators often provide args for the error message
  usually they also need the UID which I want to avoid as a simple
  Validator should not need to know about the
  root its validating the field on
* how to build a more DLS like version? For validations that are super common like is this field a UID?
* play with a more complex validation
* how to return one warning or one error? the orchestration needs to be able to distinguish between the two, so it can
  decide if we should stop the validation, not import a given entity