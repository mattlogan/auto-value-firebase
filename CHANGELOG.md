
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

Version 0.2.1 *8-17-2016*
----------------------------

- Make generated `AutoValue` class final if `isFinal` is true, otherwise make it abstract

Version 0.2.2 *11-4-2016*
----------------------------

- Update `AutoValue` to 1.3

Version 1.0.1 *11-18-2016*
--------------------------

- Add type adapter support


Version 1.1.0 *5-19-2017*
--------------------------

- Add `toMap()` method
