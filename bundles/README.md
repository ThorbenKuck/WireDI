# Bundles

Bundles are modules that contain preconfigured jars that can be included in your project.
They can contain auto configurations for WireDI or other frameworks.

For example, the logback-bundle bundles logback and the respected default layout.
By adding this to your project, logback will be set as the slf4j implementation, without the requirement to do anything else.

While the integrations focus on integrating different technologies with WireDI, bundles are fully preconfigured jars.
They might have runtime dependencies on other libraries, but they are not required to.
Instead, they try to be as self-contained as possible.