# Transaction report integration

[TransactionReport.java](TransactionReport.java) is a single-file exercise that
connects the essential Core Java contracts. Read its CSV and duplicate-policy
contract before writing any implementation. Complete parsing, in-memory reporting,
then file ownership in that order.

```bash
java 14-integration/TransactionReport.java
```

The checks cover tenant-scoped logical keys, identical versus conflicting
duplicates, exact signed totals, stable ranking, immutable detached output,
physical line numbers, parse causes, UTF-8, empty input, and missing files.

After it passes, choose one change and implement it without weakening checks:

- Report within an Instant range, explicitly defining inclusive boundaries.
- Rank only the top N accounts while preserving deterministic ties.
- Replace duplicate rejection with a result that distinguishes accepted,
  repeated, and conflicting rows. Explain how that changes the public contract.

This exercise establishes in-memory reporting behavior. Database transactions,
durable idempotency, and event delivery belong to the separate Spring/Kafka
practice tracks.
