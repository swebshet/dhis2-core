# Validation Idea 3

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
Optional<Error> apply(TrackerBundle bundle, T input);
```

or

```java
Optional<Error> apply(T input);
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

## TODO

* refactor the ValidatorNode apply/visit
  what I want is it to build a tree on apply like it already suggests using return type Node<Optional<Error>>

* make it more readable and comparable in its use to idea1
* how can I implement the validateEach I had in idea1 for validating collections using simple validators?
  it would be cool if a ValidatorNode could create multiple Node<Optional<Error>> when applied/mapped/visited
* think about naming? feels a bit like a ValidatorNode is a functor and ... the visit method could actually be the map
  you pass it a function and it maps from one category to another
  there is also such a similarity between a node and a function in terms of andThen, ... maybe its just because a node
  is a wrapper of a function
* how can I generalize collecting the errors in a list? like Java Collections .collector(toList) style?
* Re-read the issue and some other difficulties we have
* Play with a more complex validations. Try to port more validations over.
* how can I generalize what I have more? would that even help?
* print the tree of validations :)
* how to return one warning or one error? the orchestration needs to be able to distinguish between the two, so it can
  decide if we should stop the validation, not import a given entity. How many warnings do we even have? I would argue that
  the duplicate notes case should actually be an error, or not?
