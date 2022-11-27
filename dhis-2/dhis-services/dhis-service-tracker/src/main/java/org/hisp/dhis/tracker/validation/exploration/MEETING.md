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

`AbstractTrackerDtoValidationHook` has more responsibilities like

* making sure a validation hook runs only if it's written for the current strategy (create/update/delete)
* removing an invalid entity from the payload, so it's not validated any
  further [see](https://github.com/dhis2/dhis2-core/blob/1c8287b0aa9334c31547c0f9685a7c1de3cb601b/dhis-2/dhis-services/dhis-service-tracker/src/main/java/org/hisp/dhis/tracker/validation/hooks/AbstractTrackerDtoValidationHook.java#L185-L189)

This makes our validation process hard to understand as we have the orchestration of the validation split between the
* `AbstractTrackerDtoValidationHook`
* [DefaultTrackerValidationService](https://github.com/dhis2/dhis2-core/blob/1c8287b0aa9334c31547c0f9685a7c1de3cb601b/dhis-2/dhis-services/dhis-service-tracker/src/main/java/org/hisp/dhis/tracker/validation/DefaultTrackerValidationService.java#L89-L113)

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

## Shortcomings

I think

* our validation interface needs to
  * guide us in writing more targeted validators. My idea would be a validator per error code.
  * prevent us from mutating the payload
  * TrackerBundle should be replaced by a dedicated Context that only contains what is needed by validators. It should
    only give read-only access to it. (lets discuss this separately)
* we need building blocks that allow us to decompose our complex validation into smaller validators

## Potential Ideas

I tried different interfaces to see what their effect is on building validators.

### Error reporter

If we stick with the error reporter approach we should at least narrow down the input that is validated by a validator.

#### Step 1

So I adapted our interface to [Validator](./reporter/step1/Validator.java)

```java
void apply( ErrorReporter reporter, T input );
```

Note: TrackerBundle (or a validation context) is omitted just to iterate more quickly on the variations. Just think its
there :)

I added some building blocks like

* [All](./reporter/step1/All.java) - run all Validators irrespective of whether one fails (can be run concurrently)
* [Each](./reporter/step1/Each.java) - run a Validator for type T on a Collection<T>

so we can build up something like [EnrollmentValidator](./reporter/step1/EnrollmentValidator.java) from smaller validators.

#### Step 2

Wanting something like

* [Seq](./reporter/step2/Seq.java) - run all Validators in order until the first one fails

evolved the interface from

```java
void apply( ErrorReporter reporter, T input );
```

to [Validator](./reporter/step2/Validator.java)

```java
boolean apply( ErrorReporter reporter, T input );
```

since we need a signal to know whether a Validator failed in order to stop execution. Currently, with fail-fast we throw
an exception in the error reporter to stop execution on the first error. I am not a fan of using exceptions for a normal
feature as it's not an exceptional case. Using exceptions for fail-fast works and at least only puts burden on the
orchestration of the validation to catch it. I did not want all our Validator functions to have to deal with any
exceptions. This is why I went for returning a boolean with false for failed. Downside is that you know have two actions
as a dev that you need to keep in sync adding an error and returning false.

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

[EnrollmentValidator](./reporter/step2/EnrollmentValidator.java) is already getting more capable.

### Function

Not being 100% satisfied with the above interfaces I thought of [Validator](./func/Validator.java). With context

```java
Optional<Error> apply(TrackerBundle bundle, T input);
```

or without

```java
Optional<Error> apply(T input);
```

The same building blocks can be written

* [All](./func/All.java)
* [Each](./func/Each.java)
* [Seq](./func/Seq.java)

The [Error](./func/Error.java) is actually a collection of errors (semi-group). The burden of aggregating errors is on
our building blocks. By providing APIs to only create Errors with one error in it, we make it less likely devs will write
validations returning more than one error. If for some reason that is needed it will still be possible.

### Comparison

```java
void apply( ErrorReporter reporter, T input );
```

* (-) lacks easy visibility into if one Validator failed: exceptions are thus needed to implement fail-fast or something
  like `Seq` (unless someone has an idea ;)
* (+) no need to concatenate errors to aggregate them
* (+) easy to build building blocks like `All` and `Each`

```java
boolean apply( ErrorReporter reporter, T input );
```

* (-) gives visibility into if one Validator failed which allows building something like `Seq` but introduces the need
  to return the correct value depending on whether an error was added
* (+) no need to concatenate errors to aggregate them
* (+) easy to build building blocks like `All`, `Each` and `Seq`

```java
Optional<Error> apply(T input);
```

* (-) need to concatenate errors in `Error`. I am unsure if this would be a performance issue. But how many validation
  errors do we typically return from a payload? This would only matter if we return millions? No error means no
  error/list is even created. By working on an easy-to-use API a dev would not even notice this fact when writing a
  Validator.
* (+) easy to build building blocks like `All`, `Each` and `Seq`
* (+) return the error and the signal that the validation failed at the same time
* (+) no need for 
