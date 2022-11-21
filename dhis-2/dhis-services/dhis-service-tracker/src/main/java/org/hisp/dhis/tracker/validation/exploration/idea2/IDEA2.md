# Validation Idea 2


## Assumptions

* Validations are NOT independent:

TODO

right now the implementation assumes Validators are independent of each other. If this
assumption does not hold true for all Validators we would need to adapt how Validators are connected i.e. build a tree
rather than a list. Right now that is in the AggregatingValidator. How does `removeOnError` play into this again? Does
this already invalidate this assumption?

## TODO

* Re-read the issue and some other difficulties we have
* Play with a more complex validations. Try to port more validations over.
* how to return one warning or one error? the orchestration needs to be able to distinguish between the two, so it can
  decide if we should stop the validation, not import a given entity. How many warnings do we even have? I would argue that
  the duplicate notes case should actually be an error, or not?