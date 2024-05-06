# Runtime

This module contains submodules with classes, relevant during the runtime.

In the [annotations module](annotations), you will find the runtime relevant annotation api.

Within [runtime-libraries](runtime-libraries), you will find a lot of submodules again, each implementing small scale frameworks that are used within WireDI.
They contain functionalities on which WireDI is build but not dependent on WireDI.
This means that they can be used without WireDI as well.

The [runtime-environment](runtime-environment) then finally aggregates the whole logic for the runtime of WireDI.
It contains the whole logic for how to use WireDI at runtime.