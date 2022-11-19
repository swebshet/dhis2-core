# Validation Idea 1

## Observations

### Surface Area

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

### Reporting Errors

Reporting errors in our validation hooks feels cumbersome. Even a simple validation needs to be aware of the type (TEI,
Enrollment, ...) and the UID of the entity on which it might only validate a simple value of a field like a UID.

Only because users get a structure like

```json
{
  "message": "Enrollment OrganisationUnit: `OrganisationUnit (PMa2VCrupOd)`, and Program: `Program (kla3mAPgvCH)`, dont match.",
  "errorCode": "E1041",
  "trackerType": "ENROLLMENT",
  "uid": "QDCNspWIwDi"
}
```

back for every error does not mean our code needs pass on the burden to every validation. The information
of `trackerType` and `uid` is redundant by the way as its embedded in the object report which already has that
information.

## Assumptions

* Validations are independent: right now the implementation assumes Validators are independent of each other. If this
  assumption does not hold true for all Validators we would need to adapt how Validators are connected i.e. build a tree
  rather than a list. Right now that is in the AggregatingValidator. How does `removeOnError` play into this again? Does
  this already invalidate this assumption?

## TODO

* think about returning an error with the error message args
  validators often provide args for the error message
  usually they also need the UID which I want to avoid as a simple
  Validator should not need to know about the
  root its validating the field on
* Allow passing an AggregatingValidator into an AggregatingValidator. This would allow grouping of Validations. Imagine
  we want to create different validations for create/update but some Validators should always be applied.
* Play with a more complex validations. Try to port more validations over.
* how to return one warning or one error? the orchestration needs to be able to distinguish between the two, so it can
  decide if we should stop the validation, not import a given entity