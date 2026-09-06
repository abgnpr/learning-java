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

The filesystem exercises clean up their temporary files even when a TODO seam
throws. Do not replace streaming APIs with whole-file reads.
