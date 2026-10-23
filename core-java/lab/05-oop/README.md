# Object design practice

Challenges 43–50 introduce inheritance, interfaces, overriding, and runtime
types. Follow them with [Payment Routing](PaymentRoutingContracts.java): retain
dependencies through interfaces, validate before side effects, protect wallet
state, and substitute an unrelated payment implementation.

Pair it with [Language Semantics](../10-core-contracts/LanguageSemantics.java)
for dispatch versus field/static hiding and initialization, and
[Compiler Contracts](../10-core-contracts/CompilerContracts.java) for access,
covariant returns, and checked-exception restrictions.

Explain when an interface, abstract class, or composed dependency fits the
problem. Show how another fee policy can be introduced without editing Checkout.
