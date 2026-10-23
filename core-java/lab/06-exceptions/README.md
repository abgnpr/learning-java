# Exception contracts

Use the division and power exercises for catch/throw mechanics, then complete
[Exception Boundary](ExceptionBoundaryContracts.java). Preserve the low-level
cause, translate the specific failure your boundary owns, and let unrelated
programming failures escape.

[Resource Ownership](../11-io-time/ResourceSuppressionOwnership.java) drills
reverse close order and suppressed exceptions. [Compiler Contracts](../10-core-contracts/CompilerContracts.java)
drills checked-exception restrictions on overrides and callers.
[Language Semantics](../10-core-contracts/LanguageSemantics.java) exposes how a
return in finally can replace a result or hide a failure.

Explain the difference between cause and suppressed failure, and demonstrate
why returning a default value after every exception corrupts an API contract.
