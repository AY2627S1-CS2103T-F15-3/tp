---
  layout: default.md
  title: "Release Notes"
  pageNav: 3
---

# UniMate release notes

## Unreleased: contact viewing and update dates

### View previously added contacts

* Use `list` to return to every saved contact after a search, filter or favourites-only view.
* Listing now reports the total number of contacts, including `0 person(s) listed!` for an empty collection.
* Your current sort order is preserved. Listing leaves contact details and undo/redo history unchanged.
* `list` now rejects extra arguments with usage instructions; enter `list` by itself.

### See when contact details were last updated

* Every contact card now displays `Last updated: YYYY-MM-DD`, including contacts without an outdated-information reminder.
* New contacts receive the date when `add` succeeds. This also handles a command prepared on an earlier day.
* Actual changes to name, phone, email, address, major or class year record the computer's current local date.
* Unchanged edits, input rejected before a change, tags, notes, favourites, searches and listing keep the previous profile date.
* Dates remain saved across restarts. Undo and redo restore the date associated with the restored contact state.

### Documentation and compatibility

* Updated the User Guide with listing examples, feedback, validation and the meaning of update dates.
* Updated the Developer Guide with implementation details, the date-tracking class diagram and manual verification steps.
* Existing saved dates and contact data remain compatible. Missing dates in older files use the loading date as a baseline, which is saved after the next successful command; historical edit dates cannot be recovered.
* Existing automatic `update` reminders continue to use the basic-profile date and the one-calendar-year rule.
