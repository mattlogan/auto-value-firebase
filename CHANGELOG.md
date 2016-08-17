
Version 0.1.0 *8-12-2016*
----------------------------

- Initial release

Version 0.1.1 *8-13-2016*
----------------------------

- Removed dependency on auto-value-firebase-annotation package

Version 0.2.0 *8-16-2016*
----------------------------

- Remove `final` modifier from generated `AutoValue` class
- Remove generated constructor in `AutoValue` class with `FirebaseValue` parameter and replace it with a `toAutoValue()` method in the generated `FirebaseValue` class
- These two changes allow this extension be compatible with other extensions
