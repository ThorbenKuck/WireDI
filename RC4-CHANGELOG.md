# WireDI Release Candidate 4 Changelog

## Overview

This release focuses on significant architecture enhancements, improved scoping mechanisms, and a more robust container API. It represents a major step towards a stable, production-ready dependency injection framework.

## Major Changes

### Refactored Container Architecture

- Renamed `WireRepository` to `WireContainer` throughout the codebase to better reflect its purpose
- Introduced `WireContainerBuilder` for a more flexible container initialization
- Enhanced `WireContainer` initialization flow with better state management
- Improved container teardown process to properly clean up resources

### Scope System Overhaul

- Complete redesign of scope management system:
  - Added `AnnotationBasedScopeProvider` for annotation-driven scoping
  - Introduced `JoinedScopeProvider` for composing multiple scope providers
  - Added `SimpleScopeProvider` for straightforward scope mapping
  - Created `ScopeMetadata` for scope annotation metadata
- Added new scoping annotations and supporting infrastructure:
  - `@Prototype` annotation for prototype-scoped beans
  - `@ScopeMetadata` for scope definition
  - `ScopeType` enumeration for scope categorization
- Implemented efficient scope caching mechanisms
- Improved scope lifecycle management with proper activation/deactivation hooks

### Bean Management Improvements

- Migrated from old `BeanContainer` to the new scope-based architecture
- Removed deprecated bean implementations:
  - `AbstractBean`
  - `Bean` (replaced with a new implementation in a different package)
  - `BeanContainer` and related classes
  - `EmptyModifiableBean`
  - `ModifiableBean`
  - `UnmodifiableBean`
- Enhanced `BeanFactory` with better error handling and lifecycle management

### Condition Evaluation Enhancements

- Added `ConditionEvaluationContext` and `ConditionEvaluationReporter` for better condition handling
- Improved `ConditionEvaluation` with more detailed reporting
- Refined condition plugins with better batch and single condition handling

### Provider Management

- Added `ProviderCatalogErrorReporter` for improved error reporting during provider initialization
- Enhanced provider type handling with better generic support
- Optimized provider resolution for better performance

### Test Infrastructure

- Added new test cases for scopes and scope providers
- Enhanced test utilities for better integration testing
- Improved test coverage for critical components

### Processor Improvements

- Added `ScopeProcessorPlugin` for handling scope-related annotations
- Enhanced processor plugins architecture for better extensibility
- Added `ScopeMethod` for improved code generation related to scopes

### Security and Messaging Enhancements

- Improved cryptographic algorithm support in security module
- Enhanced messaging infrastructure with better error handling
- Added `TestConsumer` for simplified messaging testing

### Configuration and Diagnostics

- Added global debug property (`wiredi.debug`) for easier troubleshooting
- Improved diagnostic output during container initialization
- Enhanced configuration property handling

## Minor Changes and Bug Fixes

- Fixed various minor bugs in the messaging component
- Improved error messages throughout the codebase
- Enhanced documentation and code comments
- Performance optimizations in critical paths
- Simplified API for better developer experience

## Migration Notes

- Replace any uses of `WireRepository` with `WireContainer`
- Update scope-related code to use the new scope provider API
- Replace uses of removed bean classes with their new counterparts
- Update condition evaluation code to leverage the new condition evaluation context

## Future Directions

- Further stabilize API for production release
- Enhance documentation and examples
- Improve performance in critical areas
- Add more integration points with popular frameworks

---

*This release contains numerous internal improvements and optimizations not explicitly mentioned in this changelog.*