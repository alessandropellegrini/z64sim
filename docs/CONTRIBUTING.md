# Guide for Contributing to z64sim

Thanks for your interest in z64sim!

This guide is intended to give you some advices on how to contribute to the
project, ensuring that your patches are consistent with the code base and
easy to maintain by people which will come after you.

If you want to contribute to z64sim, these are the essential steps:

* Fork a new branch named `hotfix/*` from `master` if you want to provide
  a bugfix for a bug in production, or named `feature/*` from `develop` if
  you want to provide a new feature.
* Implement your changes, trying to adhere to coding styles as much as
  possible. Write tests!
* Test everything using `mvn test`.
* Create a pull request to `develop` or `master`. A `hotfix/*` branch
  should be merged in both.

Thanks a lot for your help!

We rely on clang-format to specify the formatting style for the project.
Please see `.clang-format` and use it in your IDE.
