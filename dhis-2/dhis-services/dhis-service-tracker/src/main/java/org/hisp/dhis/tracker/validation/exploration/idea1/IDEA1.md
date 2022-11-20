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

back for every error does not mean our code needs pass on the burden to every validation.

## Assumptions

* Validations are independent: right now the implementation assumes Validators are independent of each other. If this
  assumption does not hold true for all Validators we would need to adapt how Validators are connected i.e. build a tree
  rather than a list. Right now that is in the AggregatingValidator. How does `removeOnError` play into this again? Does
  this already invalidate this assumption?

## TODO

* Allow passing an AggregatingValidator into an AggregatingValidator. This would allow grouping of Validations. Imagine
* Extract a group of Validators in a UID Validator in EnrollmentValidator
  we want to create different validations for create/update but some Validators should always be applied.
* Re-read the issue and some other difficulties we have
* Play with a more complex validations. Try to port more validations over.
* how to return one warning or one error? the orchestration needs to be able to distinguish between the two, so it can
  decide if we should stop the validation, not import a given entity. How many warnings do we even have? I would argue that
  the duplicate notes case should actually be an error, or not?

## Follow Up

* EnrollmentNoteValidationHook and ValidationUtils#validateNotes
  * We mutate notes in the validation hook!
  * We issue a warning for a duplicate note and discard it? Why is this not an error?

* [E1048](https://github.com/dhis2/dhis2-core/blob/258ebcb66e2acf3caa224e779f23e82e68093ca4/dhis-2/dhis-services/dhis-service-tracker/src/main/java/org/hisp/dhis/tracker/report/TrackerErrorCode.java#L71)

"Object: `{0}`, uid: `{1}`, has an invalid uid format."

We print

```json
    "errorReports": [
      {
        "message": "Object: `invalid`, uid: `invalid`, has an invalid uid format.",
        "errorCode": "E1048",
        "trackerType": "ENROLLMENT",
        "uid": "invalid"
      }
    ],
```

I suggest we change it to 

```json
    "errorReports": [
      {
        "message": "Property `enrollment` contains an invalid UID `invalid`. Valid format is ...",
        "errorCode": "E1048",
        "trackerType": "ENROLLMENT",
        "uid": "invalid"
      }
    ],
```
* [EnrollmentDateValidationHook](https://github.com/dhis2/dhis2-core/blob/258ebcb66e2acf3caa224e779f23e82e68093ca4/dhis-2/dhis-services/dhis-service-tracker/src/main/java/org/hisp/dhis/tracker/validation/hooks/EnrollmentDateValidationHook.java#L87-L90)

We call `LocalDate.now()` in our validation hook. Validations should be pure functions IMHO. No mutations (apart from
adding an error for now), so time should also not have an effect. The "current" time should be part of the validation
context. Right now that is the TrackerBundle. We should work on creating a small, read-only ValidationContext.
Having the current time in the context makes testing easy and validation behave consistently for the user. Imagine
multiple validations comparing a date to `LocalDate.now()` called in each validation. The error message will contain
different timestamps for now.

* We have too many error codes. Some of them can be more generic and reusable like

  E1121( "Missing required tracked entity property: `{0}`." ),
  E1122( "Missing required enrollment property: `{0}`." ),
  E1123( "Missing required event property: `{0}`." ),
  E1124( "Missing required relationship property: `{0}`." ),

one code would be enough

E1121( "Missing required property `{0}`." )

if needed we can write

E1121( "Missing required property `{0}` on `{1}`." )

might not be necessary as our error report contains the "trackerType" field.
