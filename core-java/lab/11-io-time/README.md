# I/O and date/time

These challenges use actual temporary files, explicit encodings, deterministic
clocks, and named time zones. They are designed to expose boundary behavior
that in-memory happy paths miss.

| # | Challenge | Focus |
|---:|---|---|
| 88 | [Streaming Text File Analytics](StreamingTextAnalytics.java) | `Path`, `Files`, buffered text, charset, streaming |
| 89 | [Buffered Binary Codec](BufferedBinaryCodec.java) | binary format, buffering, validation |
| 90 | [Resource Suppression and Ownership](ResourceSuppressionOwnership.java) | try-with-resources, close order, suppressed failures |
| 91 | [Clock-driven Temporal Snapshot](ClockDrivenTime.java) | `Instant`, `LocalDate`, `Duration`, `Period`, `Clock` |
| 92 | [Zoned Scheduling Across DST](ZonedDstScheduling.java) | parsing, locale, gaps, overlaps, elapsed time |
| 134 | [Date Boundary Contracts](DateBoundaryContracts.java) | strict validation, calendar/week years, year-of-era, monthly anchor-day scheduling |

The filesystem exercises clean up their temporary files even when a TODO seam
throws. Do not replace streaming APIs with whole-file reads.

For date practice, complete Clock → Date Boundaries → DST. Predict the fixed
pattern scenarios before running Date Boundaries, and explain why a February
clamp must not permanently change a monthly schedule's intended day. DST
scheduling also checks that strict parsing rejects impossible calendar dates.
