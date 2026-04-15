# README And Install Guidance Spec

## Goal and Context

Provide a repository README that helps both:

- a developer understand how to build and run the project locally
- an early user understand how to install the first GitHub release artifact and
  what limitations to expect before using it

This feature covers repository-level user guidance, not the GitHub release
publication itself.

## Concrete Examples

### Example 1: Early Tester Lands On The Repository

Input:
- A user opens the repository before downloading the first alpha release.

Expected behavior:
- The README explains what the app does.
- The README explains the supported screenshot scope.
- The README makes it clear that the current release is alpha quality.

### Example 2: User Wants To Install The Release Artifact

Input:
- A user downloads the first GitHub release artifact.

Expected behavior:
- The README provides basic install guidance suitable for the distributed
  artifact.
- The guidance is specific enough that an early tester can understand how to
  use the release artifact.

### Example 3: Developer Wants To Build The App Locally

Input:
- A developer lands on the repository and wants to run the app or verify the
  build locally.

Expected behavior:
- The README explains the minimum local build/run path.
- The README points to the relevant Gradle commands or Android project entry
  path.

### Example 4: User Expects Broader Support Than Exists

Input:
- A user assumes the app supports multiple templates or non-Chinese
  screenshots.

Expected behavior:
- The README clearly states the currently supported scope and known
  limitations.
- The README does not imply unsupported coverage.

## Requirements

### Repository Introduction

- The repository MUST include a top-level README for the first GitHub release.
- The README MUST describe:
  - what the app does
  - that it is an Android app
  - that processing is local and on-device
  - that the first release is alpha quality

### Developer Build And Run Guidance

- The README MUST include a basic local build path for developers.
- The README MUST include a basic local run path for developers, such as the
  Android Studio project entry or the relevant Gradle/app entry path.
- The README SHOULD identify the main verification commands a maintainer is
  expected to run before preparing a release candidate.

### Supported Scope And Limitations

- The README MUST describe the currently supported screenshot scope.
- The README MUST state that unsupported screenshots are rejected.
- The README MUST state any known release limitation that materially affects the
  main user path, including manual hero review when that limitation still
  exists.
- The README MUST NOT claim support for additional templates, non-Chinese
  screenshots, cloud sync, or server OCR.

### Install Guidance

- The README MUST include basic install guidance for the intended GitHub release
  artifact.
- The README MUST identify where a user should obtain the release artifact.
- The README SHOULD include enough troubleshooting guidance to prevent common
  first-release confusion around installation or scope.

### Privacy And Product Behavior

- The README MUST state that the app is local-first and processes screenshots
  on-device.
- The README SHOULD summarize the high-level user flow:
  import screenshot, review extracted data, then save locally.

## Interface Expectations

- A first-time repository visitor should be able to understand:
  - whether the app is relevant to them
  - how to build or run the project locally if they are evaluating the repo as
    a developer
  - what Android release artifact to look for
  - what screenshot support exists today
  - what limitations to expect before installing

## Error-State Expectations

- If the README is missing developer build/run guidance, install guidance, or
  supported-scope information, the first GitHub release MUST NOT be treated as
  properly documented.
- If the README describes broader support than the product actually provides,
  that mismatch MUST be corrected before the release is considered ready.

## Edge Cases

- The README explains the app but does not explain how to build or run it
  locally.
- The README explains the app but does not explain how to obtain or install the
  release artifact.
- The README explains installation but omits the alpha-quality warning.
- The README lists known limitations but forgets to mention unsupported
  screenshot rejection.
- The README and release notes diverge on supported scope.

## Non-Goals

- Writing full developer contribution or architecture documentation.
- Defining the GitHub repository description/tagline.
- Publishing the GitHub release itself.
- Solving the hero extraction limitation in this feature.

## Acceptance Criteria

- The repository contains a README suitable for first-release users.
- The README explains developer build/run steps, install steps, supported
  scope, privacy/local-first behavior, and key limitations.
- The README stays aligned with the alpha release positioning.
- The README does not overclaim support beyond the current verified product.

## Gotchas

- None yet.
