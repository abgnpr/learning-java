# Java version feature labs

These labs turn the code-facing Java 17, 21, and 25 additions in
[java-versions.md](../../java-versions.md) into executable practice. Each file
states its minimum JDK; no preview flags are required.

| # | Release | Challenge | Focus |
|---:|---:|---|---|
| 114 | 17 | [Random Generator Selection](RandomGeneratorSelection.java) | named `RandomGenerator` algorithms and deterministic seeds |
| 115 | 21 | [Pattern Switch Routing](PatternSwitchRouting.java) | sealed exhaustiveness, record patterns, guards, and `null` |
| 116 | 21 | [Sequenced Collection Views](SequencedCollectionViews.java) | first/last operations and a live reversed view |
| 117 | 25 | [Scoped Value Context](ScopedValueContext.java) | bounded context, rebinding, and automatic restoration |
| 118 | 25 | [Flexible Constructor Validation](FlexibleConstructorValidation.java) | validation and computation before `super(...)` |
| 119 | 25 | [Compact Source and Module Import](CompactModuleCatalog.java) | compact source files, instance `main`, and `import module` |

## Run by baseline

- **JDK 17+:** `java RandomGeneratorSelection.java`

- **JDK 21+:** `java PatternSwitchRouting.java` and
  `java SequencedCollectionViews.java`

- **JDK 25+:** run each of the remaining three files with `java FileName.java`.

The guide uses short command drills for GC, JFR, AOT, encapsulation, and removed
platform components. Those changes affect JVM operation or migration and do
not benefit from a TODO-based application-code lab.
