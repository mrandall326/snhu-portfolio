# CS 320 – Software Test, Automation & QA Portfolio

**Author:** Matthew Randall  
**Course:** CS 320 – Software Test, Automation & QA  
**Institution:** Southern New Hampshire University  
**Date:** June 2026

---

## Repository Contents

### Project One – Contact Service (Unit Testing)

| File | Description |
|---|---|
| `Contact.java` | Domain object enforcing field-level validation (null checks, length constraints) |
| `ContactService.java` | Service layer managing CRUD operations with unique ID enforcement |
| `ContactTest.java` | JUnit 5 unit tests for Contact object validation |
| `ContactServiceTest.java` | JUnit 5 unit tests for ContactService operations |

### Project Two – Summary and Reflections Report

| File | Description |
|---|---|
| `CS320_Project2_Summary_Reflections.pdf` | Final written report covering testing strategies, code coverage analysis, and QA methodology applied across all three service implementations |

---

## Reflection

### How can I ensure that my code, program, or software is functional and secure?

Ensuring that code is both functional and secure requires a disciplined, layered approach that goes well beyond simply running a program and confirming it does not crash. Throughout this course, I learned to treat testing as a first-class concern rather than an afterthought. In the contact service project, I wrote unit tests using JUnit 5 that validated not only the happy path — inputs that should succeed — but also the boundary conditions and invalid inputs that expose hidden assumptions in the code. Functional correctness means every requirement is covered by at least one test, and that the tests are specific enough to fail when the code drifts from its specification. Security follows a similar principle: defensive coding means never trusting input, validating lengths and null values at the point of entry, and designing objects that cannot be placed in an invalid state. The `ContactService` class, for example, enforced identifier uniqueness and rejected null fields at the method level, which means no downstream code could accidentally bypass those constraints. Across all three services — Contact, Task, and Appointment — I applied this same pattern consistently, and the result was a test suite of 99 tests with zero failures that I could run at any time with full confidence in the output.

### How do I interpret user needs and incorporate them into a program?

Interpreting user needs and translating them into working software is fundamentally an exercise in reading requirements precisely and resisting the urge to add assumptions. The project specifications for the contact, task, and appointment services each described constraints in plain language — field length limits, uniqueness rules, non-null requirements — and my job was to convert those sentences into test cases before writing a single line of implementation. This test-first mindset forced me to think like the end user and ask what the system *must* do, not just what it *can* do. When a requirement stated that a phone number must be exactly ten digits, I wrote tests for nine digits, eleven digits, null, and a valid ten-digit string — because a user's expectation is not just that valid input works, but that invalid input is caught gracefully and communicated clearly. Mapping those expectations into assertions before coding kept me honest and ensured the final implementation was shaped by the requirements rather than by whatever felt most convenient to build. The Summary and Reflections Report from Project Two extends this idea further, analyzing how different testing strategies — equivalence partitioning, boundary value analysis, and decision table testing — each serve as structured methods for translating real-world user expectations into verifiable test coverage.

### How do I approach designing software?

My approach to software design has shifted considerably through this course toward thinking in terms of contracts and responsibilities. Each class I wrote had a clearly defined scope: `Contact` held and validated its own data, `ContactService` managed the collection and enforced business rules, and the test classes verified those contracts independently. This separation of concerns made the codebase easier to reason about and made the tests more meaningful because each test targeted a single, well-defined behavior rather than a tangled mix of logic. I also came to see software design as something that must anticipate failure, not just success. Building in exception handling, input validation, and predictable error states is not pessimism — it is professionalism. When a caller passes an invalid ID to `deleteContact()`, the system should throw an `IllegalArgumentException` immediately and clearly, not silently fail or corrupt state downstream. That discipline, applied consistently across the Contact, Task, and Appointment services, is what made the codebase testable in the first place. Testability is not a property you bolt on at the end — it is a consequence of good design decisions made from the very beginning.

---

*This repository was created as part of the SNHU Computer Science program portfolio requirement for CS 320.*
