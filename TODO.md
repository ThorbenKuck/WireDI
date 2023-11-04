# Pre-Release

### Processing

The processor requires a lot how high effort to work. So these changes will make it easier.

* Implement "Conditional" feature
  * The IdentifiableProvider should implement the "condition" function


### Properties

PropertyBinding should support nested classes and automatically resolve the correct property path


### WireRepository

Implement "Destroy" lifecycle
Implement scopes


Make the Aspect part a plugin
AspectHandler should gain a method "appliesTo"
  - returns boolean
  - parameters: AnnotationMetaData
  - returns true if the AnnotationMetaData contains the annotation we care about
  - ExecutionChain should only hold AspectHandler instances that return true

# Future

* "Strict" mode
  * This mode will do everything at compile time and have limited runtime customizable, but will fly at runtime.

You can open tickets, to add feature requests.