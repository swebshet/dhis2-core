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
