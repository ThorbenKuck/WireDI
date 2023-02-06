# Pre-Release

### Processing

The processor requires a lot how high effort to work. So these changes will make it easier.

* Add support for "Qualifier first" out of the box (i.e. without conflict strategy)
* Add support generic dependencies (based on best match).


### Provider

Currently, the processor does not support a Provider or IdentifiableProvider dependency. This must change

* Let the processor correctly understand and process the Provider interface
* Let the processor correctly understand and process the Identifiable Provider interface

# Future

* "Strict" mode
  * This mode will do everything at compile time and have limited runtime customizable, but will fly at runtime.

You can open tickets, to add feature requests.