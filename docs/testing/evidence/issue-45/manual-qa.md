# Issue #45 Manual QA

This Issue changes documentation policy only. No Level 5 application startup or Level 6 HTTP observation is required.

The manual policy check is that a STRICT PR with QA focused and Level 3~6 PASS remains blocked whenever `quality-gates` is unavailable, pending, or FAIL. The replacement for QA's local Level 1 full regression is CI, not a weaker local assertion.
