# Exploring alternatives for tracker validation

## Observations

### Surface Area

Our types often have a large surface area. This is true for validation as well.

Current [TrackerValidationHook](./../TrackerValidationHook.java) interface

```java
    void validate( ValidationErrorReporter report, TrackerBundle bundle );
```

It takes in a reporter which allows us to add multiple errors and warnings. Almost all of our validation hooks are
responsible for more than one [error code](./../../report/TrackerErrorCode.java).

Sample validation hooks which are hard to understand and test

* [PreCheckSecurityOwnershipValidationHook](./../hooks/PreCheckSecurityOwnershipValidationHook.java) 589 lines
* [PreCheckDataRelationsValidationHook](./../hooks/PreCheckDataRelationsValidationHook.java) 499 lines

[TrackerBundle](./../../../tracker/bundle/TrackerBundle.java) is a type with a huge list of methods both for
reading and writing! It also contains the entire payload sent by the user.

Since not every validation hook is actually concerned with every one of our types like TEI, enrollment, ... we let them
extend [AbstractTrackerDtoValidationHook](./../hooks/AbstractTrackerDtoValidationHook.java) which
provides methods like

```java
public void validateEnrollment( ValidationErrorReporter reporter, TrackerBundle bundle, Enrollment enrollment )
```

which hooks override to get a single TEI, enrollment, ...

## Shortcomings

I think

* our validation interface needs
  * allow or even guide us in writing more targeted validators. My idea would be a validator per error code.
  * ideally prevent us from mutating the payload
  * TrackerBundle should be replaced by a dedicated Context that only contains what is needed by validators. It should
    only give read-only access to it. (lets discuss this separately)
* we need building blocks that allow us to decompose our complex validation into smaller validators

## Potential Ideas

I tried different interfaces to see what their effect is on building validations.

TODO
* start with the reporter/step1 than step2
* then comes the rest of the doc
* check the other markdown docs and put the thoughts in here
* create a presentation that only contains the interfaces with some of the words I mention so its easier to discuss
  use this doc as my speaker deck :)

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