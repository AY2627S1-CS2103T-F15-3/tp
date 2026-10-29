---
  layout: default.md
  title: "Release testing"
  pageNav: 3
---

# Release testing

Test the **same candidate JAR** on Windows, Linux and macOS before publishing it. Record the commit,
JAR SHA-256, OS version, CPU architecture, Java/JavaFX version and display scaling with each result.
CI passing on all three platforms is useful evidence, but does not replace native desktop checks.

## Before testing

1. Build the candidate with Java 25: `./gradlew check coverage shadowJar`.
1. Use an empty disposable folder on each computer. Copy the same `build/libs/unimate.jar` into it.
1. Use Java 25, including the [prescribed JDK+FX on macOS](https://se-education.org/guides/tutorials/javaInstallationMac.html).
1. Launch from that folder with `java -jar unimate.jar`. Do not reuse your real contact data.
1. Follow the [DG manual tests](DeveloperGuide.md#appendix-instructions-for-manual-testing).
   Check the guide version against the candidate; v1.4 does not contain all v1.5 features.

## Native desktop checks

Record pass/fail and a screenshot for each OS, rather than assuming identical fonts or focus behaviour.

| Area | Procedure and expected result |
| --- | --- |
| Resizing | Test wide and minimum-width windows, then maximize and restore. Cards must stay within the list and values must remain reachable. |
| Resolution and scaling | At 1920×1080 or higher, check 100% and 125% for a comfortable experience. At 1280×720 or higher and 150%, all functions must remain usable. Record the actual settings and check text, icons, focus outlines and scrollbars. 200% is optional. |
| Keyboard | Follow the UG Tab/Shift+Tab route with physical keys. Only the selected contact's expandable values join the route; the outline must identify focus. |
| Expansion | Expand and collapse long names, tags and each field using mouse and Enter/Space. Scroll to the end, resize and check that adjacent cards do not overlap. |
| History | Check Up/Down draft restoration and Alt+Up/Down (Option on macOS). Repeated submissions occupy separate `History i/N` positions; prefix search skips positions, boundary presses pulse the label, and returning to the draft or submitting a failed command hides it. History keys must not execute commands. |
| Feedback | Check changed-field edit summaries, optional-field clearing (`Not specified`), sorted tag differences and unchanged edits that preserve redo. An email-capitalization-only edit shows a change without a new shared-email warning. Submit long note replacements and shared-detail warnings. Read/select the whole message and scroll to its last line; the next ordinary result replaces it. |
| Storage | Run the DG save/load failure cases in a disposable folder. After a loading error, close the window without executing commands before repairing the file. |
| Restart | Close and reopen. Contacts persist; filters, sorting, command history and expansions return to their documented restart state. |

## Online guide checks

Build the HTML guides from the same revision. Inspect them in current Chrome and Firefox on Windows,
Linux and macOS at the required resolutions/scales above. Check the sidebar and command links,
tables, actual screenshots and UML labels at normal reading size. A diagram that renders successfully
still needs visual inspection for readable text, crossings and clipping. Check both guides from a
fresh build so cached or incomplete assets cannot hide a publication problem.

<box type="info" seamless>

**Note:** Judge diagram readability at a stated browser window width. The text column is about 940px
wide at 1280px. At 1300px the right-hand page outline appears and the column drops to about 660px,
returning to about 960px only at 1600px. In that range, wide diagrams are scaled down. This comes from
the site layout, not from an individual diagram. See the [documentation guide](Documentation.md) for
the column widths.
</box>

## Performance and release record

The DG's 1,000-contact and one-second response targets are requirements, not measured guarantees.
Create a valid disposable 1,000-contact file, restart with it, then measure representative `list`,
`find`, `filter`, `sort`, `edit`, `undo` and `redo` operations on each supported desktop. Record the
hardware, fixture, cold/warm state and observed times. Include rendering and disk saving when timing
an end-to-end command; a parser-only benchmark does not establish UI responsiveness.

Before publishing, record remaining failures and limitations, check the final release notes and
UG/DG against the actual candidate, and verify the uploaded JAR matches the tested SHA-256. Replace
the guides' unreleased notice and `MainApp.VERSION` development suffix only when cutting that release.
