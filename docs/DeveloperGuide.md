---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# UniMate Developer Guide

This guide describes the **v1.5 development implementation (unreleased)**. Use a matching source
revision and JAR for testing; the published v1.4 JAR does not include all features described here.

Use this guide to find the component boundaries, design decisions and checks for a change.

| Developer task | Start here |
| --- | --- |
| Understand startup and component boundaries | [Architecture](#architecture), then the UI, Logic, Model and Storage sections. |
| Change contact fields or validation | [Add prefixes](#parsing-add-prefixes), [academic fields](#adding-academic-fields) and [editing](#editing-existing-contacts-gui-and-execution-flow). |
| Change the displayed contacts or their order | [Search](#searching-by-academic-fields), [filter](#filtering-contacts-by-academic-fields-and-tags), [sort](#sorting-the-displayed-contacts) and [view status](#persistent-view-status). |
| Change card layout or keyboard interaction | [Contact card layout](#contact-card-layout). |
| Change deletion or contact notes | [Batch deletion](#deleting-one-or-more-contacts) and [contact notes](#contact-notes). |
| Change tags or reminders | [Tagging](#tagging-and-untagging-contacts) and [system reminders](#automatic-system-reminders). |
| Change undo/redo or command recall | [Contact data history](#recording-contact-data-history), [undo/redo](#undo-and-redo-commands) and [command history](#command-history-navigation). |
| Check requirements or test a release | [Requirements](#appendix-requirements) and [manual testing](#appendix-instructions-for-manual-testing). |

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Acknowledgements

* UniMate is adapted from [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative. We reused and adapted its application architecture, baseline contact-management code, tests, documentation structure and UML diagrams.
* [JavaFX](https://openjfx.io/) provides the desktop UI, and [Jackson](https://github.com/FasterXML/jackson) handles JSON persistence.
* [JUnit 5](https://docs.junit.org/5.14.4/overview.html) provides the automated testing framework.
* [MarkBind](https://markbind.org/) generates the documentation website, and [PlantUML](https://plantuml.com/) renders the UML diagrams.

--------------------------------------------------------------------------------------------------------------------

## Setting up, getting started

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## Design

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="300" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

#### Main components of the architecture

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

#### How the architecture components interact with each other

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="340" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiComponentDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts. For readability, the diagram omits `StatusBarFooter` and `HelpWindow`.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

Contact-card layout and expansion are described under [Contact card layout](#contact-card-layout).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
2. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
3. The command can communicate with the `Model` when it is executed (e.g., to delete a contact).
   A command that changes contact data also commits the change to the undo history.
4. The command returns a `CommandResult`. `LogicManager` then saves the address book through `Storage`
   before returning that result to the `UI`.

For an example interaction trace, including batch-index validation, see
[Deleting one or more contacts](#deleting-one-or-more-contacts).

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" />

How the parsing works:
* `AddressBookParser#parseCommand` dispatches on `commandWord.toLowerCase(Locale.ROOT)`, so all command words are case-insensitive regardless of the system locale. Argument capitalization is retained. Whitespace is normalized before dispatch except for `note`,
  whose parser receives the raw arguments so that internal note whitespace can be preserved. Prefix matching and argument validation retain their existing rules, and `Tag` equality remains case-sensitive.
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" alt="Model structure with academic fields and filtered and sorted contact views" />


The `Model` component,

* stores UniMate contact data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
  Each `Person` includes `Major`, `GraduationYear` and 'Notes' value objects in addition to its existing contact details and tags.
* selects contacts using a `FilteredList<Person>` and orders that view using a `SortedList<Person>`.
  `getFilteredPersonList()` returns `sortedPersons` as an unmodifiable `ObservableList<Person>`,
  so the UI and numbered commands use the same displayed order. The filtered view takes its source
  from `addressBook.getPersonList()`, and the sorted view takes the filtered view as its source.
  Changes to the saved contact list therefore update the display.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* keeps an in-memory history of contact data states in an `AddressBookHistory` object. The `undo` and `redo` commands use it to revert and reapply every successful change to contact data. See [Recording contact data history](#recording-contact-data-history).
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both UniMate contact data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonUserPrefsStorage#readUserPrefs` replaces explicitly null `guiSettings` with a default
`GuiSettings` after deserialization. Missing settings already use the `UserPrefs` default; valid
settings are preserved. This loading-boundary recovery keeps `UserPrefs#setGuiSettings(null)`
invalid and prevents a null setting from reaching startup model initialization. Reading does not
rewrite the file; the existing startup save persists the recovered defaults. Malformed JSON and
null root values still follow the existing `DataLoadingException` recovery path.

`FileUtil#writeToFile` writes UTF-8 content to a sibling temporary file, closes the stream, then
replaces the destination using `ATOMIC_MOVE`. It does not fall back to a direct overwrite when
atomic replacement is unsupported. A failed write, close or replacement preserves the saved file
and triggers temporary-file cleanup; cleanup failures are suppressed on the original exception.
`JsonAddressBookStorage` no longer pre-creates an empty destination. `FileUtilTest` covers partial
writes, replacement and cleanup failures, first saves, read-only files and symbolic links.

`LogicManager` keeps a snapshot of the initial contact data and updates it after each successful
save. `help` returns its UI action without saving. `exit` also skips contact storage when the current
contacts match that snapshot; otherwise, it retries saving and returns the exit flag only on success.
A failed exit adds `MESSAGE_UNSAVED_EXIT`, so `MainWindow` keeps the window open with the pending
contacts. `list` retains its save path for recovery. `LogicManagerUiActionTest` checks these state
transitions, and `StorageFailureUiTest` submits Help and Exit through the actual command box.

`JsonAdaptedPerson` stores each contact's academic fields as the JSON properties `major` and
`graduationYear`. Unspecified values are saved as empty strings. When loading older data files that
omit either property, it treats the missing value as an empty string and constructs an unspecified
`Major` or `GraduationYear`. Supplied non-empty values are validated before the contact is loaded.

`JsonSerializableAddressBook#toModelType` rejects null entries in `persons`, and
`JsonAdaptedPerson#toModelType` rejects null entries in a contact's tag list, using
`IllegalValueException`. `JsonAdaptedTag` also rejects a null tag name. `JsonAddressBookStorage`
converts these validation errors to `DataLoadingException`, rejecting the whole file rather than
returning a partial address book. Missing or null optional tag lists remain valid legacy data.
Null-entry errors identify one-based positions in the saved `persons` and `tags` arrays, rather than
GUI display indices. For example, `Contact entry 2 is null.` identifies a null contact, while
`Contact entry 2 is invalid: Tag entry 3 is null.` identifies a null tag. Contact conversion errors
include the contact position and retain the original validation exception as their cause.

`JsonUtil#readJsonFile` rejects a null JSON root as a `DataLoadingException`; it returns
`Optional.empty()` only for a missing file. `MainApp` therefore uses its existing empty-contact-list
recovery for invalid data, while missing files still use sample contacts. Loading and window-close
shutdown do not rewrite the invalid contact file. `MainAppTest` checks this recovery with isolated
storage and a stub UI; storage tests check null data, valid empty lists and legacy contacts.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## Implementation

This section describes some noteworthy details on how certain features are implemented.

### Contact card layout

`PersonListCard.fxml` groups contact details and academic fields separately, with captions styled
independently from their values in `DarkTheme.css`.

* `PersonCard` populates values, tags and presence indicators. `PersonListCell` creates each card using
  its displayed index and binds its preferred width to the list width, reserving space for the scrollbar.
* `ExpandableText` handles ellipses and click, Enter or Space expansion. `PersonListPanel` shares each
  contact's expanded-field state across recreated cells. This state is kept only in memory and does
  not change contact data or undo/redo history. The map uses value-equal `Person` objects as keys.
  A new contact state starts collapsed; an unchanged contact retains its state. Undo can reuse
  the entry for an earlier contact state and restore its expansion. Restarting the app clears the map.
* `ContactListNavigation` filters Tab and Shift+Tab at the window root. It builds the route from the
  command box, list and selected cell's expandable values, skipping the result display and other cards.
  `PersonCard` supplies values in reading order; focusable tags are included. The route is rebuilt on
  each press because scrolling and resizing can recycle cells or change which values are shortened.
  The list skin's JavaFX `VirtualFlow.scrollPixels` brings focused values into view.
  Shift+Tab reverses the route. An empty list returns focus to the command box. Up/Down selection uses
  JavaFX's list behaviour; command-history keys and Enter/Space expansion keep their existing handlers.
* `PersonCardPane` measures the card at its available width and stacks the detail groups below
  560 logical pixels of content width. `TagPane` measures and places tag rows using the same wrapping
  calculation. Cell and card clips prevent content from painting over neighbouring cards.

`PersonCardEdgeCaseTest` covers all 128 combinations of optional fields and 12 long-value cases.
`PersonCardLayoutTest` checks field captions and values at different widths. `ExpandableContactListTest`
checks expansion, collapse, cell recycling, resizing and scrolling to the end of expanded content.
`ContactListNavigationTest` checks forward/reverse traversal, selected-contact boundaries, arrow-key
selection, empty lists and focused long-tag styling.

<!-- @@author RayWHJ -->

### Contact notes

#### Structure and responsibilities

The class diagram shows how `NoteCommand` and `Person` reference `Notes`. Unrelated contact fields,
parsers and history internals are omitted.

<puml src="diagrams/NotesClassDiagram.puml" alt="Note command dependencies and the immutable note value stored by Person" />

`Notes` is an immutable value object; `Notes.EMPTY` represents no note. Supplied text must be non-blank,
on one line, free of control characters and at most 500 Unicode code points. Every `Person` holds exactly
one non-null `Notes` value. The relationships are ordinary associations because values can be shared
by contacts, commands and history snapshots.

`Person#withNotes` returns a replacement person with all other fields preserved, including favourite
status and `lastUpdated`. Notes affect full equality and hashing, but not contact identity. Reusing
the existing person-replacement mechanism keeps note changes compatible with undo/redo; `edit`, `tag`
and `untag` also preserve notes.

`JsonAdaptedPerson` stores the text as `notes` and validates it when loading; missing or null values
default to empty notes. `PersonCard` derives its orange `noteIcon` from `getNotes().isEmpty()` and
updates when the observable list replaces a person. Its tooltip suggests `shownote` using the current
displayed index; the icon itself is an indicator, while note text appears in command feedback.

#### Adding or replacing a note

`AddressBookParser` dispatches the three note commands to their parsers. `NoteCommandParser` treats
everything after the index as literal text. It uses `StringUtil.stripWhitespace` before validation
to trim Unicode whitespace, including non-breaking spaces that `String.strip()` misses. Internal
whitespace is preserved. The other two parsers accept only an index.

The sequence shows execution after parsing; validation-error paths, feedback formatting and storage
are omitted. `m:Model` is implemented by `ModelManager` and supplies the filtered, sorted displayed list.

<puml src="diagrams/NoteSequenceDiagram.puml" alt="Unchanged-note return or contact replacement with one history commit" />

1. Resolve the displayed index and compare `person.getNotes()` with the supplied value. Identical
   notes return unchanged feedback before any snapshot, replacement or history commit.
2. For a change, copy the address book, call `person.withNotes(notes)` and install the replacement
   with `Model#setPerson`, preserving the current filter and sort order.
3. Commit the previous state once and return a `CommandResult`. New-note feedback includes the saved
   text; replacement feedback also includes the old text and an undo hint. The history description
   omits that hint so it remains appropriate in undo/redo messages.

`LogicManager` saves through `Storage` after successful execution, including unchanged-note results.
Invalid input fails before mutation. Save errors leave the in-memory change and history intact;
see [history limitations](#appendix-known-limitations).

The existing result display wraps and scrolls long feedback. At INFO level, `LogicManager` logs the
command type and `MainWindow` logs `CommandResult#getLogSummary`, rather than raw input or full note
text. This does not describe logging at other levels.

#### Viewing, deleting and history behaviour

| Command | Behaviour after resolving the displayed contact |
| --- | --- |
| `note` | Replace changed text and commit once; identical text preserves both history stacks. |
| `shownote` | Return the saved text or empty-note feedback without changing data or history. |
| `delnote` | Replace a non-empty note with `Notes.EMPTY` and commit once; an empty note preserves history. |

All three preserve the current view. `delnote` reuses the illustrated snapshot/replacement flow, so
another sequence diagram would repeat it. Actual changes use [contact data history](#recording-contact-data-history);
read-only and unchanged results preserve redo.

`NotesCommandIntegrationTest` covers feedback, filtering, persistence and history;
`ContactNotesPreservationTest` checks other replacement commands, and `PersonCardNoteIconTest` checks
the indicator. For manual checks, see [Managing contact notes](#managing-contact-notes) and
[Undoing and redoing note changes](#undoing-and-redoing-note-changes).

<!-- @@author -->

### Parsing add prefixes

`AddCommandParser` passes the supported prefixes from `CliSyntax` to `ArgumentTokenizer`.
Only those prefixes, preceded by a space, delimit fields. Unrecognised prefix-like text stays inside
the preceding value and is validated as part of that field; it is not silently discarded or separately
classified as an unknown parameter.

For example, `add n/PrefixProbe a/NUS x/typo` supplies `NUS x/typo` to `ParserUtil.parseAddress`.
That address is valid and is saved literally. The same text inside a more restricted field may fail
that field's validation. Slash-containing addresses remain valid unless a recognised prefix preceded
by a space delimits a new argument. In `add`, `f/` is now a recognised prefix, so `a/Blockf/3` preserves
the literal address without favouriting the contact, `a/Block f/` supplies address `Block` and marks
the contact as a favourite, and `a/Block f/3` is rejected because the flag has a value. A space followed
by `f/` cannot represent literal address text in `add`. The UG warning and `AddCommandParserTest`
document and cover these boundaries.

The optional `f/` prefix is a flag with no value. `AddCommandParser` rejects values with the specific
message `The f/ flag does not take a value. Use f/ alone to mark the new contact as a favourite.` and rejects repeated
occurrences, then passes the flag's presence to the existing `Person` constructor as its initial
favourite state. `AddCommand` saves this state in the same history snapshot as the new contact;
no separate favourite command or history entry is needed. Its success message starts with
`New favourite contact added:` for favourites and `New person added:` otherwise. The same message
describes the addition in undo/redo history. After creation, `fav` and `unfav` change favourite status;
`edit` preserves it. `AddFavouriteIntegrationTest` verifies these messages, saved data, favourites views,
undo/redo, editing and rejected inputs, while `FavouriteIconTest` checks that
the heart appears immediately after adding with the flag.

### Adding academic fields

`CliSyntax` defines `m/` for a major and `c/` for a graduation year. `AddCommandParser` recognises
these prefixes alongside the existing contact fields and rejects repeated occurrences of either
academic prefix. Both fields are independently optional: an omitted prefix maps to `Major.EMPTY`
or `GraduationYear.EMPTY`, whose empty string value represents an unspecified field.

For supplied values, `ParserUtil.parseMajor` and `ParserUtil.parseGraduationYear` trim leading and
trailing whitespace and reject blank values. A major must contain non-whitespace text without control
characters; spaces and punctuation are allowed. A graduation year must match `20[0-9]{2}`, representing
a four-digit year from `2000` to `2099`. Invalid input produces a parse error before a contact is added.

The parser constructs a `Person` containing both academic values and passes it to `AddCommand`.
The command checks for an existing contact with the same name, ignoring case, before adding the person to the model.
`Person#isSamePerson` uses `String#equalsIgnoreCase` on `Name.fullName`, so the existing `Model#hasPerson`,
`UniquePersonList` and edit duplicate checks share this rule. `Name`, `Person#equals` and `Person#hashCode`
retain exact data equality, preserving capitalization changes for display, persistence and undo/redo.
`JsonSerializableAddressBook#toModelType` also rejects case-only duplicate names using its existing
`IllegalValueException` path. There is no automatic migration, deletion, merging or renaming. Users must
back up legacy files and distinguish conflicting names before loading. The existing `MainApp` startup
fallback opens an empty list on a loading error; subsequent saves can overwrite that file, so users must
close the window without entering commands and resolve conflicts in a copy of the backup first.
Even a successful view-only command can save the empty list over the invalid file; see
[data-file recovery](UserGuide.md#editing-the-data-file).
After a successful add, it records the previous contact data state in the model's history
(see [Recording contact data history](#recording-contact-data-history)).
The normal storage path then saves the contact, including its academic fields.

<!-- @@author RayWHJ -->

### Identifying an existing contact when add rejects a duplicate

`AddCommand` finds a saved contact using `Person#isSamePerson` and passes it to
`getDuplicateMessage`. `Messages#format` reports that contact's actual details, rather than values
from the rejected input.

| Existing contact | Index reported and recovery |
| --- | --- |
| Visible in the current filtered/sorted list | Its one-based displayed index. |
| Hidden, including an empty view | A labelled saved index; run `list`, then `sort reset`, before using it. |

Both recovery commands are necessary because `list` preserves sorting. Duplicate rejection occurs
before the snapshot, addition and history commit, so data, view, saved file and history stay unchanged.
The GUI retains the input for correction.

`AddCommandIntegrationTest` checks visible/hidden indices, case-only duplicates and preserved state;
`SortUiTest` checks the feedback and recovery instructions.

### Warning when an add introduces same contact details

After adding the contact, `AddCommand#getAddResult` calls
`SharedContactDetailsWarning#describeMatches` on the refreshed full list. Sorting is retained, so
warning indices match the current cards. Scanning after addition matters because a new contact can
shift sorted positions.

The helper excludes the added contact and compares non-empty phone numbers exactly and emails
ignoring case. It lists every match once, in displayed order, with the reason `phone`, `email` or
`phone and email`. These advisory comparisons do not change contact identity or stored values.

`SharedContactDetailsWarning#appendTo` appends the matches to the complete success feedback and
returns a `CommandResult.warning`. Add and edit share this helper to keep formatting consistent.
`MainWindow` passes the result's warning flag to `ResultDisplay`; the `warning` style in
`DarkTheme.css` makes the whole message amber. The existing text area handles wrapping, selection
and scrolling. The next ordinary result restores normal styling, and `getLogSummary()` excludes
the appended matches.

The addition remains successful and is saved normally. The history description contains only the
ordinary addition message: one undo removes the new contact, and redo restores it without repeating
the advisory. Existing contacts are not modified or merged.

`AddCommandIntegrationTest` covers matching, displayed indices, persistence and history;
`SortUiTest` checks styling and long feedback. See [Shared phone or email warnings on add](#shared-phone-or-email-warnings-on-add).

<!-- @@author -->

### Editing academic fields

`EditCommandParser` recognises `m/` (major) and `c/` (graduation year) alongside existing edit prefixes.
It rejects repeated academic prefixes and validates supplied values using `ParserUtil.parseMajor` and
`ParserUtil.parseGraduationYear`. An empty prefix value maps to `Major.EMPTY` or `GraduationYear.EMPTY`
to clear the optional field; an omitted prefix leaves the descriptor field null.

`EditCommand.EditPersonDescriptor` stores, copies and compares both academic fields. `EditCommand`
uses the supplied values when constructing the edited `Person`, retaining existing values for omitted fields.
The existing model update and JSON storage path save the edited values without a storage format change.
After a successful edit that changes the contact, `EditCommand` records the previous contact data state in the
model's history (see [Recording contact data history](#recording-contact-data-history)).

#### Editing existing contacts: GUI and execution flow

Edit outcomes depend on validation and whether the resulting contact differs:

| Outcome | Contact data and history | Display and input |
| --- | --- | --- |
| Invalid fields, index or duplicate name | Reject before mutation; preserve both history stacks. | Keep the view and input; show an error. |
| Valid, unchanged contact | Preserve contact data, update date and both history stacks. | Show all contacts and no-change feedback; clear input after saving succeeds. |
| Valid, changed contact | Replace the contact and record one history entry. | Show all contacts and changed-field feedback; clear input after saving succeeds. |

The sequence below focuses on contact replacement and history for
`edit 1 m/Mathematics c/ t/Project`; generic parsing, saving and UI refresh are described in the prose
rather than repeated as lifelines. Index `1` refers to the currently displayed list, including
`find`, `filter`, `listfav` and sorted views. `EditCommand` checks the index and duplicate name before
changing the model. A failed save does not roll back an applied change; see
[history limitations](#appendix-known-limitations).

<puml src="diagrams/EditContactSequenceDiagram.puml" alt="Edit command interaction with the model for changed and unchanged contacts" />

After replacing the person, `EditCommand` resets the filter to `PREDICATE_SHOW_ALL_PERSONS` and commits
the previous state for undo (unchanged data does not create another history entry). Any current sorting
still applies. `LogicManager` saves the updated address book before returning the result.
`MainWindow#executeCommand` refreshes the list and writes the feedback to `ResultDisplay`;
`CommandBox` then clears its text. `PersonListPanel` observes the model's list and builds `PersonCard`s
with the current indices. Cleared academic fields render as `Not specified` in the major and class year labels.

`EditCommand` compares the replacement `Person` with the original before mutation. If they are equal,
it resets the displayed list to all contacts and returns `No changes made to NAME. The supplied values
match the existing details.` without replacing the person or committing history. The date and both
history stacks, including any available redo, are preserved.

For an actual change, `formatChanges` returns `Edited NAME:` followed by changed fields in a fixed order:
name, phone, email, address, major and class year. Each line uses `Field: old value → new value`;
empty optional values display `Not specified`. Supplied fields whose values match are omitted.
If the tag sets differ, a `Tags replaced. Removed: ... . Added: ... .` line lists the sorted set differences
(`None` for an empty difference). An unchanged tag set adds no line, including when `t/` was supplied.

The unchanged-contact branch returns before `formatChanges`. Normal invalid input continues to use
parser validation and `CommandException`.

Email feedback compares stored values case-sensitively, while shared-email detection ignores case.
Consequently, a capitalization-only email edit is a real profile change with an email change line
and a refreshed date, but no new shared-email warning.

The existing full-contact `Edited person: ...` description is still passed to
`Model#commitAddressBook(previousState, message)`. Immediate change summaries are not stored in history;
undo/redo feedback and state restoration retain their previous behavior. `UndoRedoIntegrationTest`
verifies exact edit, undo and redo feedback and persisted contact data for tag replacement, clearing and
repeated edits, including no-op preservation of redo. Unit tests check field summaries and update dates;
`EditContactUiTest` checks rendered clearing feedback and successful no-op submissions.
After composing the change summary, `EditCommand#getEditResult` appends a shared-details warning
for changed phone/email values, using the refreshed displayed indices. Warnings are excluded
from the stored history description and do not block the edit.

`Email.MESSAGE_CONSTRAINTS` uses short newline-separated rules and
explicitly forbids consecutive local-part special characters; the validation regex is unchanged.
The regex has two known limitations, listed under [Planned enhancements](#appendix-planned-enhancements):
`DOMAIN_LAST_PART_REGEX` repeats the whole label pattern at least twice instead of checking the
final label's length, and the nested repeated groups can overflow the stack on extremely long input.
`ResultDisplay.fxml` enables text wrapping so narrow windows do not require horizontal scrolling.

The replacement `Person` retains notes and favourite status. Changes to name, phone, email, address,
major or graduation year update `lastUpdated`; a tag-only edit retains the previous date.
Parser and command validation failures show feedback and preserve input with the error style, without
changing the contact list or filter. A storage failure occurs after mutation and is reported as an error;
it does not roll back the in-memory edit.

GUI regression coverage is in `EditContactUiTest`: submissions through `MainWindow`, rendered fields,
filtered indices, clearing fields, preserved notes/favourite status, invalid edits and saved JSON.
`EditCommandTest` and `EditCommandParserTest` cover command and parser boundaries.

<!-- @@author RayWHJ -->

#### Warning when an edit introduces same contact details

After replacement and the full-list refresh, `EditCommand#getEditResult` enables matching only for
changed contact methods. Phone changes use `Phone#equals`; email changes use
`String#equalsIgnoreCase`. Unchanged methods, email-capitalization-only changes, cleared values and
unrelated edits therefore produce no new warning.

The command reuses the [shared warning helper and display behaviour](#warning-when-an-add-introduces-same-contact-details).
All field-change lines remain first, followed by any tag removal/addition summary, then the advisory.
Matches exclude the edited contact and use the refreshed sorted indices, including previously hidden contacts.

The edit is saved normally. Its history description remains `Edited person: ...`, so undo/redo
restores the complete contact without repeating the immediate change summary or warning.
Unchanged edits preserve both history stacks; notes, favourite status and omitted fields are retained.

`EditCommandSharedDetailsTest` checks matching rules, combined field/tag changes, persistence and
history. `EditContactUiTest` checks feedback order, styling and refreshed indices;
`SharedContactDetailsWarningTest` checks that multiline success feedback is preserved.
See [Shared phone or email warnings on edit](#shared-phone-or-email-warnings-on-edit) for manual tests.

<!-- @@author -->

<!-- @@author sel-couth-ene -->

### Searching by academic fields

##### Parsing

`FindCommandParser` trims user input and splits it on whitespace into keywords.
Input passed on to `PersonContainsKeywordsPredicate`. 

Blank input throws a `ParseException` with `FindCommand.MESSAGE_USAGE`.

Prefixes are not tokenized, so `find n/Alex` searches for the literal word `n/Alex`.

##### Matching

The predicate matches a `Person` if any keyword matches a full word in the person's `Name`, `Major` or `GraduationYear`, using `StringUtil.containsWordIgnoreCase`. 

1. Matching is case-insensitive, whole-word and `OR`-based across all three fields. Partial words do not match. (Note: exact matching on a whole field value is provided by `filter` instead)
   * `find Comp` does not match `Computer Science`
   * `find Computer Science` can return any contact whose major is `Data Science`

2. Field values are split on whitespace, so punctuation stays attached to a word:
   * `find Hons` does not match `Computer Science (Hons)`, but `find (Hons)` does

3. Names are alphanumeric, this affects majors only. 

4. An empty `Major` or `GraduationYear` never matches a keyword. 

5. `Not specified` placeholder is added only for display and is not stored.

6. `AddressBookParser` converts Unicode spaces in the arguments to ASCII spaces before `FindCommandParser` runs, so every keyword passed to `containsWordIgnoreCase` is a single word.

##### Updating the displayed list

`FindCommand` applies the predicate through `Model#updateFilteredPersonList`. [`filter`](#filtering-contacts-by-academic-fields-and-tags) reuses this path unchanged.

1. The new predicate replaces the previous one, so every `find` searches all saved contacts, not just the displayed ones.
   * `find` replaces any `filter` or `listfav` view
   * `list`, `add`, `edit`, `undo` and `redo` show all contacts again; the view is not saved, so a restart also shows all contacts

2. The `SortedList` comparator is untouched, so an active sort is kept.

3. The predicate implements `PersonFilter`, so its description appears in the view status.
   * `find alex david` shows `Filter: Search: alex OR david`

4. `find` changes no contact data and never calls `Model#commitAddressBook`, so undo and redo availability is preserved.

5. The model and storage format are unchanged.

##### Sequence diagram

The sequence diagram below shows how `find Alice Computer` is parsed and executed.

`AddressBookParser`, which creates the `FindCommandParser`, and the later save in `LogicManager` are omitted.

<puml src="diagrams/FindSequenceDiagram.puml" alt="Find command parsing and execution" />

##### Testing

1. `FindCommandParserTest` checks blank input and splitting on repeated whitespace.

2. `PersonContainsKeywordsPredicateTest` checks name, major and class-year matches, `OR` matching and case-insensitivity.
   * phone, email and address are checked to not be searched

3. `FindCommandTest` checks the result count, the filtered list and equality.

<!-- @@author -->

### Filtering contacts by academic fields and tags

`filter` updates the displayed list in the same way as [`find`](#searching-by-academic-fields):
`FilterCommand` passes its predicate to `Model#updateFilteredPersonList` and reports the number of
shown contacts. It also replaces the previous view, keeps the active sort and records no history.
This section covers only how `filter` differs.

| | `find` | `filter` |
| --- | --- | --- |
| Input | Free-text keywords split on whitespace; prefixes are literal words | `m/`, `c/` and `t/` prefixes; at least one, each at most once |
| Predicate | `PersonContainsKeywordsPredicate` | `ContactFilterPredicate` |
| Fields checked | Name, major, class year | Major, class year, tags |
| Combining | OR: any keyword may match | AND: every supplied criterion must match |
| Match type | Whole word, case-insensitive | Whole value, case-insensitive; class year by exact equality; a tag criterion matches any of the contact's tags |

##### Parsing

`FilterCommandParser` tokenizes only `m/`, `c/` and `t/` as criterion delimiters, then rejects a
non-empty preamble, missing criteria and repeated prefixes. Other prefix-like text stays within the
preceding value: `m/Mathematics w/ Computing` represents the complete major `Mathematics w/ Computing`,
as in `add` and `edit`. Each supplied value is validated with `ParserUtil.parseMajor`,
`parseGraduationYear` or `parseTag`; a
blank supplied value is invalid. An omitted criterion becomes an empty `Optional` and is ignored by
`ContactFilterPredicate#test`. Unknown text before the first criterion is a usage error; unknown text
within a year or tag fails that field's validation. Recognised criterion prefixes remain reserved syntax.

The sequence diagram below shows how `filter m/CS c/2027` is parsed. Execution is omitted because it
follows the [`find` sequence diagram](#searching-by-academic-fields), with `ContactFilterPredicate`
in place of `PersonContainsKeywordsPredicate`.

<puml src="diagrams/FilterSequenceDiagram.puml" alt="Filter command parsing into a ContactFilterPredicate" />

The diagram's separate unsupported-prefix scan is no longer performed; current tokenization and
field validation follow the rules above, allowing literal prefix-like text inside a major.

##### Interaction with tag commands

Unlike `find`, the predicate depends on tags. `tag` and `untag` keep the current predicate, so a
contact disappears from the results when its tags no longer match.

### Sorting the displayed contacts

`AddressBookParser` routes `sort` to `SortCommandParser`. The parser accepts either two tokens
(a field `name`, `major`, `year` or `updated`, followed by `asc` or `desc`) or the single keyword `reset`.
Keywords are case-insensitive. A field and direction produce `SortCommand` with
`PersonSortComparator.SortField` and `SortDirection` values; `reset` produces `SortResetCommand`.
Invalid input raises a `ParseException` with usage instructions before any view or data change.

The sequence below traces `sort year desc` from parsing to the Model sorting API.
It shows how the command supplies a comparator; list transformations and comparison rules are explained below.

<puml src="diagrams/SortSequenceDiagram.puml" alt="Parsing sort year desc and applying its comparator through the Model API" />

`SortCommand#execute` supplies a `PersonSortComparator` to `Model#updateSortedPersonList`.
`ModelManager` maintains this observable transformation chain:

```text
AddressBook / UniquePersonList -> FilteredList -> SortedList -> UI and numbered commands
```

`getFilteredPersonList()` retains its existing API name and now returns the sorted view. Consequently,
edit, delete, tag and untag obtain the same indexed `Person` that is shown on the matching contact card.
The `SortedList` object is retained for the model's lifetime, keeping existing UI observers attached.
Changing its comparator preserves the `FilteredList` predicate; changing the predicate preserves sorting.
`SortResetCommand` calls `Model#resetSortedPersonList`, which sets the comparator to `null`.
The same displayed list then follows source order without changing the predicate, saved data or history.

The comparator uses case-insensitive full-value ordering for names and majors. Graduation years are
four-digit strings, so their lexicographic order is chronological. Empty academic values become null
sorting keys. Direction is applied to the value comparator **before** `Comparator.nullsLast`, ensuring
that descending order does not move unspecified values to the front. Equal primary values use ascending
case-insensitive name order; names differing only in case compare equally.

For `UPDATED`, the comparator compares `Person#getLastUpdated()` as `LocalDate` values.
Ascending puts older dates first; descending reverses only the date comparison, keeping the
ascending name tie-break in both directions. Missing/null dates in older files remain unknown
and sort last in both directions using `Comparator.nullsLast`; unknown-date ties use ascending names.
The view status describes
this field as `Last updated`. Replacing a contact after an edit or restoring it through undo/redo
automatically reorders the displayed view using the restored date.

Sorting the view rather than `UniquePersonList` preserves storage order and history snapshots. As with
other successful commands, `LogicManager` still invokes storage, but the saved contacts remain in their
underlying order. Sort commands do not call `commitAddressBook`, so undo and redo availability is preserved.
Add/edit/undo/redo retain their existing behaviour of clearing the filter, while keeping the comparator.
Replacing, adding or removing source contacts automatically updates their sorted positions.
Restarting constructs a view without a comparator or predicate, restoring saved order and all contacts.

Comparator, model, command and parser unit tests cover ordering and validation. `SortCommandIntegrationTest`
checks command interactions, notes preservation and JSON persistence; `SortUiTest` checks the real command
box, rendered card numbering and feedback. `SortResetCommandTest` checks saved order, filter preservation
and redo availability after resetting. `UpdatedSortIntegrationTest` covers date ordering, ties,
filters, displayed-index editing, undo/redo and JSON reload; `SortUiTest` also checks date sorting
through the command box and rendered cards. See [Sorting contacts](#sorting-contacts) for manual checks.

#### Persistent view status

`ModelManager#viewStatusProperty` exposes a read-only string binding that observes the source list,
displayed list, predicate and comparator. It reports both counts and the active view settings, even
when changing a criterion or sort produces the same displayed contacts. `ContactFilterPredicate`,
`PersonContainsKeywordsPredicate` and `FavouritePredicate` implement `PersonFilter` to describe their
criteria; `PersonSortComparator` describes its field and direction.

`LogicManager` exposes the property through `Logic`. `MainWindow` passes it to `PersonListPanel`,
whose status label binds to it independently of `ResultDisplay`. Source changes update the counts;
commands that reset the predicate or comparator also update the status. The label wraps long criteria
and reserves their preferred height. View settings remain session-local and are not stored in JSON.

`ViewStatusTest` checks criteria, counts, command interactions and restart defaults.
`ViewStatusUiTest` checks persistent status after `shownote`, rejected commands, reset behavior,
favourites membership changes and long text in the actual JavaFX layout.

### Formatting academic fields in command messages

Commands that report a contact's full details use `Messages#format(Person)`. The formatter includes `Major` and
`Class year` before tags when those optional academic fields are specified. If either academic field is unspecified,
that field is omitted from the command message.

This centralizes full-contact formatting for add and delete results and stored edit history descriptions.
Immediate edit feedback uses `EditCommand#formatChanges` instead, so clearing an optional academic field
explicitly shows `Not specified` rather than omitting the changed field.

<!-- @@author RayWHJ -->

### Deleting one or more contacts

The sequence shows successful batch deletion after parsing. Resolve every target before mutation,
then commit once; parsing, validation-error branches and storage are omitted.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Capture original displayed targets, delete them and commit once" />

1. `DeleteCommandParser` splits whitespace-separated arguments and validates positive integers with
   `ParserUtil#parseIndex`. `DeleteCommand` stores the indices in input order.
2. The command copies `Model#getFilteredPersonList()`, checks every selection and captures the target
   `Person` references. Numeric duplicate detection also rejects equivalent indices such as `1 01`.
3. After validation, it copies the address book once and calls `Model#deletePerson` for each captured
   person. Resolving all targets first prevents shifting live indices from selecting the wrong contact.
4. It formats the result with `Messages#format`, calls `commitAddressBook(previousState, message)` once
   and returns a `CommandResult`. `LogicManager` then saves the completed batch once.

An invalid or repeated index rejects the whole batch before mutation, preserving contacts, view and
history. Out-of-range errors include the valid range; an empty view suggests `list` if saved contacts
exist, or adding a contact otherwise.

Deletion removes each selected person and all their saved fields, including notes. Single-contact
feedback starts with `Deleted person:`; batch feedback includes each original index in input order.
The result also serves as the history description. One undo/redo reverses/reapplies the batch and
shows the full list; deletion itself preserves the filter, sort order and all unselected contacts.

This design extends the existing single-contact operation without introducing separate batch-history
state. `DeleteCommandIntegrationTest` checks selection, rejection, persistence and one-step undo/redo;
`SortUiTest` checks feedback and card renumbering. See [Deleting contacts](#deleting-contacts) for manual tests.

<!-- @@author -->

### Listing all saved contacts

The `list` command exposes all contacts in the current address book, including persons loaded by
`JsonAddressBookStorage` on startup. It reuses the existing observable view rather than loading a second copy or
maintaining a separate list of earlier additions. Deleted contacts are absent from that address book and are not restored.
There is no separate creation timestamp or recently added view; `lastUpdated` tracks basic-profile maintenance
and cannot be used to reconstruct each contact's original creation date.

#### Parsing and execution

`AddressBookParser` dispatches `list` only when its argument string is blank; additional input raises a
`ParseException` with `Messages.MESSAGE_INVALID_COMMAND_FORMAT` and `ListCommand.MESSAGE_USAGE` before execution.
Surrounding whitespace is accepted, and the command word is case-insensitive, as for all commands.

For example, after `find ListAlice` shows one matching contact in a collection of ten:

1. `ListCommand#execute` applies `Model.PREDICATE_SHOW_ALL_PERSONS` through
   `Model#updateFilteredPersonList`, replacing the search predicate. The same path clears `filter` and `listfav` views.
2. `Model#getFilteredPersonList` exposes the restored view. Its name refers to the filtered-list API,
   but `ModelManager` returns the final `SortedList<Person>`, including any active comparator.
3. The command reads that view's size and returns `ListCommand.MESSAGE_SUCCESS`, a newline and the
   formatted `Messages.MESSAGE_PERSONS_LISTED_OVERVIEW`. Feedback reports ten contacts, rather than
   the previous one search result. An empty address book follows the same path and reports zero.

#### Observable view and persistence

`ModelManager` exposes a `SortedList<Person>` over its `FilteredList<Person>`, so changing only the predicate retains
the active comparator. The saved contact order is untouched. `PersonListPanel` observes this final view and creates
cards with one-based display indices. Existing cards provide contact details and reminders; listing does not replace
persons or alter their update dates.

The view status is derived from the same filtered and sorted lists. After `list`, it reads
`Showing N of N contacts | Filter: All contacts | Sort: ...`; the sort description remains the active
sort, or `Saved order` when there is no comparator. Display indices belong to this current view,
so they may differ from indices in the previous search results.

The command does not commit contact history, consume undo steps or discard redo states. `LogicManager` still uses its
existing save-after-success path, retaining the same contact data on disk. On reopening, storage reconstructs the saved
contacts and a new model starts without temporary filters or sort choices.

#### Design considerations and verification

Reusing the observable view avoids a second collection that could become inconsistent after an edit
or deletion. Keeping filtering separate from sorting lets `list` restore the collection without
discarding the user's chosen order. It also keeps a view change separate from contact-data history.

`ListCommandTest` covers empty and populated lists, restoring search/filter/favourite results, active sort preservation,
unchanged data and undo/redo availability. `AddressBookParserTest` checks whitespace and extra-argument rejection.
`LogicManagerTest` saves contacts, reopens their JSON through a fresh model, searches and lists them again, then verifies
both displayed and persisted contacts. See [Listing all persons](UserGuide.md#listing-all-persons-list).

### Displaying academic fields in the contact list

Contact cards display major and class year alongside the contact details when space allows,
and below them in narrow windows. Unspecified values appear as `Not specified`.
This affects display only; stored values are unchanged.
See [Contact card layout](#contact-card-layout) for layout and expansion behaviour.

### Automatic system reminders

Incomplete and update reminders share the same display path. The partial class diagram below
shows who evaluates the rules and who renders the result; contact value objects and storage
adapters are covered in the Model and feature sections.

<puml src="diagrams/SystemReminderClassDiagram.puml" alt="Person supplies reminder predicates, SystemTag derives applicable labels, and PersonCard renders them" />

`PersonCard` holds the displayed `Person` and requests its reminders from `SystemTag.forPerson`.
That method evaluates both predicates independently, so both labels may appear together.
Reminders are derived when rendering; they are not stored in `Person#getTags` or JSON.

| Reminder | Rule | Detailed explanation |
| --- | --- | --- |
| `INCOMPLETE` | Both phone and email are empty. | [Incomplete reminders](#automatic-incomplete-reminders). |
| `UPDATE` | The last update date is known and its one-year anniversary is before today. | [Update reminders](#automatic-update-reminders). |

Both reminders share the rendering and refresh path below.

* `PersonCard#refreshTags` clears the tag pane, recreates the user's tags, then appends each
  reminder with the `system-tag` style class (dark brown background, amber border and text in `DarkTheme.css`).
* A stored user tag named `incomplete` or `update` is shown as a grey `incomplete (custom)` or
  `update (custom)` tag, detected by `SystemTag.isSystemLabel`. It is not renamed or removed from JSON.
* After a successful command, `MainWindow` calls `PersonListPanel#refresh`, so every card evaluates
  its reminders again, even when the command changed no data.

Deriving reminders instead of storing them avoids stale tags and data migration, and stops `untag`
from permanently disabling a reminder.

### Automatic incomplete reminders

#### Class structure

<puml src="diagrams/IncompleteReminderClassDiagram.puml" alt="Class structure of automatic incomplete reminders" />

`Person` references one non-null `Phone` and one non-null `Email`; `SystemTag` derives `INCOMPLETE` only when both values are empty. These are plain associations, not compositions, because immutable values can be shared. Parsing, persistence and UI are omitted.

#### Design overview and responsibilities

`Person#isIncomplete` returns true only when both `getPhone().value` and `getEmail().value` are empty. No other field affects it.

| Class | Responsibility |
|-------|----------------|
| `Phone`, `Email` | Represent unspecified contact methods using immutable `EMPTY` values containing an empty string. Existing regexes still validate every non-empty value. |
| `Person` | Evaluates contact completeness without modifying contact state. |
| `AddCommandParser`, `ParserUtil` | Allow omitted phone/email prefixes, but reject explicitly blank or malformed supplied values in `add`. |
| `EditCommandParser`, `EditCommand` | Map empty `p/` and `e/` values to `EMPTY`, preserve omitted fields, and apply the existing basic-profile date rules. |
| `JsonAdaptedPerson` | Converts absent/null phone and email fields to empty values while retaining validation of supplied non-empty values. |

#### Add/edit behaviour and validation

<!-- @@author zruixuan -->

* **Model:** Phone and email objects remain non-null throughout the model.
  * `Phone.isValidPhone` and `Email.isValidEmail` accept exactly the empty string in addition to their previous valid values; whitespace-only values remain invalid at the model layer.
  * `ParserUtil.parsePhone` and `parseEmail` still reject a trimmed empty value, preserving malformed-input feedback when an optional prefix is explicitly supplied to `add`.
* **`add`:** `AddCommandParser` requires name and address, maps omitted phone/email prefixes to `Phone.EMPTY` and `Email.EMPTY`, and continues to reject repeated single-valued prefixes.
* **`edit`:** `EditCommandParser` uses the same clearing convention as major/class year: an empty prefix supplies `EMPTY`, while an omitted prefix leaves the descriptor field unset.
* **Validation:** All non-empty values still pass through `ParserUtil` and the original regex validation. An invalid value rejects the whole command before mutation.
* **Update date:** `EditCommand` already compares phone/email as basic fields, so filling or clearing either changes `lastUpdated` only when its value changes.
  * Existing person replacement preserves favourite status, tags, notes and academic fields.
  * Notes, favourite and tag commands retain the date.
  * Sorting, searching and list commands retain their existing view-only behaviour.

<!-- @@author -->

#### Rendering and refresh

Rendering is the [shared reminder path](#automatic-system-reminders). The `INCOMPLETE` tooltip asks the user to add a phone number or email address, and missing values display as `Not specified`. Filling either contact method removes `INCOMPLETE`; clearing the last one adds it. The same edit can independently remove `UPDATE`, because it refreshes the profile date.

#### JSON compatibility and design considerations

<!-- @@author zruixuan -->

The JSON shape is unchanged:

* Phone and email remain strings, and empty strings represent unspecified values.
* Missing/null values load as unspecified; malformed non-empty strings still fail validation.
* Older complete contacts load as before, and name/address and other field validation are unchanged.
* Favourite, notes, tags and `lastUpdated` retain their existing serialization and copy rules.

<!-- @@author -->

Reusing `EMPTY` value objects avoids nullable fields throughout commands and UI. Tests cover all four phone/email combinations, fill/clear transitions, failed input atomicity, legacy JSON, both reminders together and actual JavaFX rendering. See [Automatic incomplete reminders](UserGuide.md#automatic-incomplete-reminders).

### Tracking and displaying contact update dates

`Person#getLastUpdated()` exposes an immutable `LocalDate` representing creation or the most recent
actual change to the basic profile: name, phone, email, address, major and graduation year. Tags,
notes and favourite status retain the original date. Date values participate in full equality,
hashing and history snapshots, but do not affect name-based contact identity.

#### Assigning and preserving the date

`AddCommand` obtains `LocalDate.now(clock)` during successful execution, rather than relying on the
date from when its `Person` was constructed. `Person#withLastUpdated` creates a dated copy preserving
all other fields. This handles a command prepared before midnight but executed afterwards. The
default clock uses the computer's time zone; tests can supply a fixed clock. Duplicate detection
still occurs before mutation or history recording.

`EditCommand#createEditedPerson` compares the resulting six basic fields with the original values.
An actual change, including clearing a specified optional field, refreshes the date using its clock.
An unchanged edit or tag-only edit retains the previous date. Failed parsing, an invalid index and
duplicate-name rejection leave the stored contact unchanged. Undo and redo restore the recorded
date, rather than calculating a new date when restoring a snapshot.

For example, a contact starts with date `2024-02-29`. Submitting its existing phone or changing only
a tag keeps that date. A subsequent phone change on `2026-10-28` records `2026-10-28`. Undoing the
phone change restores `2024-02-29`, and redoing it restores `2026-10-28`, even if redo runs on a later day.

#### Display and JSON compatibility

`PersonCard` sets the always-visible `lastUpdated` label from `Person#getLastUpdated()` in
`PersonListCard.fxml`, producing `Last updated: YYYY-MM-DD` for ordinary four-digit years or
`Last updated: Unknown` when no date was recorded. Its tooltip
explains the scope of the date. Replacing a person updates the observable list cell, so edits,
undo/redo and reloaded data all display the corresponding date. The label is independent of the
derived `update` reminder and appears on recent contacts too.

The existing JSON `lastUpdated` string and `LocalDate.parse` validation are reused. Missing/null
dates remain unknown and are saved as JSON null until an actual basic-profile edit records a date.
Invalid supplied dates fail data loading.
No new JSON property or user-supplied date prefix is introduced.

#### Design considerations and verification

Storing the date on the immutable `Person` keeps it with contact data through persistence and history
restoration. Calculating it only for actual basic-field changes prevents a note, favourite or repeated
edit from making old profile information appear current. `LocalDate` matches the day-level display;
it does not provide a time of day or an interaction history. Preserving an unknown legacy date avoids
inventing an update when an older contact is loaded.

`LastUpdatedDisplayTest` exercises actual JavaFX cards and list cells for recent and older dates,
search/list transitions, annotation changes, edits, undo/redo, persistence and reopening saved data.
`UpdateReminderTest` additionally checks execution-time add dates, time-zone boundaries, failed
duplicate adds and the existing profile-edit rules. `LastUpdatedStorageTest` covers stored dates,
legacy files and invalid date rejection. See the [UG date rules](UserGuide.md#tracking-a-contact-s-latest-update-date)
and [manual date checks](#tracking-contact-update-dates) for the corresponding user-visible behaviour.

### Automatic update reminders

Update reminders use the [shared system-reminder structure](#automatic-system-reminders).
The preceding [date-tracking section](#tracking-and-displaying-contact-update-dates) explains how
commands assign and preserve the stored date; this section explains the reminder rule and rendering.

#### Class structure

<puml src="diagrams/UpdateReminderClassDiagram.puml" alt="Class structure of automatic update reminders" />

An actual basic-field edit refreshes `Person.lastUpdated`, `SystemTag` evaluates `needsUpdate`, and `PersonCard` renders the result. Parser, model and storage infrastructure are omitted.

How commands assign, preserve and store the date is covered in [Tracking and displaying contact update dates](#tracking-and-displaying-contact-update-dates). An unknown (null) date never triggers `UPDATE`.

#### Reminder logic

<!-- @@author zruixuan -->

`Person#needsUpdate(today)` requires a known date preceding today and its one-year anniversary to be strictly before today.

* Unknown dates produce no age-based reminder.
* Exactly one year produces no reminder.
* Calendar-year arithmetic uses `lastUpdated.plusYears(1)`, so a leap-day anniversary follows Java's calendar rule: `2024-02-29` reaches its anniversary on `2025-02-28`, with a reminder starting on `2025-03-01`.

<!-- @@author -->

`SystemTag.forPerson` appends `UPDATE` when this condition applies, independently of `INCOMPLETE`.

#### GUI implementation

Similar to incomplete reminders, rendering uses the [shared reminder path](#automatic-system-reminders). The differences:

* The `UPDATE` tooltip shows the recorded date.
* `PersonCard` evaluates the date with an injectable clock.
* Cards re-check the date only after a successful command. No timer refreshes an idle window, so a reminder that becomes due while UniMate sits idle appears after the next successful command.

#### Design considerations and verification

The rationale for deriving reminders is the same as for incomplete reminders. Dates are kept separate from favourite state and notes, so a future command that reconstructs a `Person` must carry all three forward unless it intentionally updates one.

<!-- @@author zruixuan -->

Tests cover:

* calendar boundaries
* profile edits and no-op edits
* JSON round trips and migration
* note/tag/favourite interactions
* undo/redo and actual command-box rendering

GUI tests run on the JavaFX application thread; the existing Linux CI virtual display ensures they execute when coverage is collected. See [Automatic update reminders](UserGuide.md#automatic-update-reminders) for user-facing behaviour.

<!-- @@author -->

### Favourite contacts

#### Class structure

<puml src="diagrams/FavouriteContactsClassDiagram.puml" alt="Class structure of favourite contacts and list filtering" />

<!-- @@author zruixuan -->

The diagram separates stored state from view selection.

* `FavCommand` and `UnfavCommand` replace a `Person` with a changed favourite flag, preserving other fields.
* `ListFavCommand` applies a `FavouritePredicate` that tests this flag without changing stored contacts or the current sorting.
* Dashed arrows show dependencies, not retained references or execution order.
* Shared command/filter supertypes, model infrastructure, persistence and UI classes are omitted to keep the feature view focused.

<!-- @@author -->

#### Operations and state

Favourite commands distinguish a stored state change from a view change:

| Operation | Effect | History and view |
| --- | --- | --- |
| `fav INDEX` | Replace the displayed contact with `favourite = true`; reject an existing favourite. | Record one history entry; retain the current filter and sort. |
| `unfav INDEX` | Replace the displayed contact with `favourite = false`; reject a non-favourite. | Record one history entry; retain the current filter and sort. |
| `listfav` | Apply `FavouritePredicate` to the existing contact view. | Preserve contact data and both history stacks; retain sorting. |
| `list` | Restore the show-all predicate. | Preserve contact data and both history stacks; retain sorting. |

Both indexed commands validate the displayed index before mutation and preserve the contact's other
fields, including its update date. Invalid indices and favourite-state errors leave the data, view
and history unchanged. A failed save does not roll back an applied change; see
[history limitations](#appendix-known-limitations).
The [filtering section](#filtering-contacts-by-academic-fields-and-tags) explains the shared view pipeline.

#### Design overview and relevant classes

<!-- @@author zruixuan -->

Favourite contacts use the existing command, model, storage and UI components.

* `Person` holds an immutable `boolean favourite`, exposed through `isFavourite()`.
* It defaults to `false` in the existing constructor; an overloaded constructor accepts an explicit state.
* Favourite status participates in `equals` and `hashCode`, while `isSamePerson` continues to identify contacts by name.

<!-- @@author -->

| Class | Role in this feature |
|-------|----------------------|
| `AddressBookParser`, `FavCommandParser`, `UnfavCommandParser` | Dispatch commands and validate displayed indices using `ParserUtil.parseIndex`. `listfav` argument validation is handled directly in `AddressBookParser`; there is no separate `ListFavCommandParser`. |
| `FavCommand`, `UnfavCommand` | Check the selected person's state and replace that person with a copy containing the new favourite value. |
| `ListFavCommand`, `ListCommand` | Select the favourite predicate or restore the existing show-all predicate. |
| `Model`, `ModelManager` | Expose the displayed list, replace stored persons, and apply predicates to the existing `FilteredList<Person>`. |
| `LogicManager`, `JsonAdaptedPerson` | Save successful command results through the existing storage path and convert the favourite value between JSON and `Person`. |
| `PersonCard`, `PersonListPanel` | Display the state and rebuild visible list-cell graphics when model contents change. |

#### Command execution flow

<!-- @@author zruixuan -->

`fav` and `unfav`:

1. `FavCommandParser` and `UnfavCommandParser` validate a one-based index.
2. The commands resolve it against `Model#getFilteredPersonList`, so they act on the current displayed order.
3. A valid operation replaces the selected `Person` through `Model#setPerson`, changing only the favourite flag and preserving all other fields, including notes and the update date.
4. Success feedback starts with `Added to favourites: ` or `Removed from favourites: `, followed by `Messages.format` output.

Errors:

* Invalid index syntax produces a `ParseException` with usage instructions.
* An out-of-range index produces `The person index provided is invalid.`
* Repeated `fav` and `unfav` errors include the selected contact's name, e.g., `Alice is already a favourite.` or `Alice is not a favourite.` This feedback change leaves favourite state transitions and Undo/Redo behaviour unchanged.
* Neither command resets the current filter.

`listfav` and `list`:

* `AddressBookParser` accepts `listfav` only without arguments.
* `ListFavCommand` applies a new `FavouritePredicate` to the existing filtered view and reports the count, or `No favourite contacts found.`
* The existing `ListCommand` restores `PREDICATE_SHOW_ALL_PERSONS`.
* Both replace the active filter without editing stored contacts or resetting the current sorting.

Because `fav` and `unfav` resolve their targets from that view, `unfav 1` after `listfav` acts on the first displayed favourite. Replacing it with a non-favourite causes the `FilteredList` to remove it immediately and reindex the remaining results. Existing `add` and `edit` behaviour still restores the full list; `tag` and `untag` retain the current predicate.

<!-- @@author -->

#### Data persistence

<!-- @@author zruixuan -->

* **JSON:** `JsonAdaptedPerson` writes the `favourite` boolean alongside the existing contact fields.
  * Its JSON creator accepts a nullable `Boolean` and uses `Boolean.TRUE.equals(favourite)`, so missing or null values in older data become `false`.
  * Conversion back to the model uses the constructor accepting the favourite state.
  * No favourite state is stored in ordinary tags.
* **Saving:** `LogicManager#execute` saves the address book after successful execution through the existing `Storage` API. As with other commands, a storage exception is reported after the in-memory model has changed; this path does not roll back the replacement.
* **Preserving the flag:** `EditCommand`, `TagCommand` and `UntagCommand` explicitly pass the original `person.isFavourite()` when reconstructing a person. This prevents unrelated updates from resetting the state.
  * When adding future contact fields or commands that copy a `Person`, preserve both the favourite state and all other unaffected fields in that copy and in JSON conversion.
* **Notes:** Notes and favourite status are preserved together by the combined `Person` constructor and JSON creator. `Person#withNotes` carries the existing favourite value through note changes, so note commands and their undo/redo history retain it.

<!-- @@author -->

#### GUI implementation

<!-- @@author zruixuan -->

* **Layout:** `PersonListCard.fxml` defines a `favouriteIcon` label next to the name, containing the solid heart character (`♥`).
  * `PersonCard` sets both its `visible` and `managed` properties from `person.isFavourite()`: ordinary contacts show no icon and reserve no icon space.
  * The fixed 24-by-24 label is centred, with a small margin beside the name; it has no click handler.
* **Style:** `DarkTheme.css` styles `#favouriteIcon` with `#e63946` for both text and the one-pixel circular border, a radius of 12, and a transparent background.
  * The ID selector overrides the general list-cell label colour, including for selected cells.
  * Ordinary tag styles are unaffected.
* **Refresh:** `PersonListPanel` observes the displayed list. Its `PersonListCell#updateItem` creates a new `PersonCard`, using `getIndex() + 1` for numbering.
  * Model replacement therefore updates the heart immediately.
  * Removing a person from the favourite filter updates the visible cards and indices without a separate favourite list or refresh command.

<!-- @@author -->

#### Design considerations

* **Separate state from tags:** A boolean gives favourite status one unambiguous value independent of tag names. Tag replacement or removal cannot accidentally remove favourite status, and no reserved custom tag name is needed.
* **Filter instead of copying or deleting data:** Reusing `FilteredList` keeps one source of contact data, preserves ordering, and lets `list` restore hidden contacts. It also gives indexed commands the same selection rule in every view.
* **Immutable replacement:** Commands reuse unchanged fields when constructing the replacement `Person`. This fits the existing observable model update path and avoids changes to unrelated contact attributes.

For user-facing formats and list-switching examples, see [Marking a favourite contact](UserGuide.md#marking-a-favourite-contact-fav), [Removing favourite status](UserGuide.md#removing-favourite-status-unfav) and [Listing favourite contacts](UserGuide.md#listing-favourite-contacts-listfav).

### Tagging and untagging contacts

#### Class structure

<puml src="diagrams/TagManagementClassDiagram.puml" alt="Class structure of tag and untag commands" />

<!-- @@author zruixuan -->

The diagram focuses on the shared tag representation:

* A `Person` references a set of ordinary `Tag` values, and `TagCommand` and `UntagCommand` replace the person with tags added or removed.
* Default and custom inputs resolve to the same `Tag` type; dynamic system reminders remain separate.

Reading the diagram:

* The association name reads "Person has Tag"; the `tags` role and `0..*` multiplicity belong at the `Tag` end.
* Small filled triangles beside association names indicate reading direction, whereas open arrowheads at association ends indicate navigation through stored references.
* Dashed arrows show dependencies, not execution steps.
* Parsing infrastructure and unrelated members and supertypes are omitted throughout these feature diagrams.

<!-- @@author -->

#### Current implementation

The `tag` command adds tags to a person in the currently displayed list. The `untag` command removes specified tags from that person. Both preserve the other contact fields and reuse the existing `Person` tag collection and storage format.

##### Relevant classes

| Class | Responsibility |
|-------|----------------|
| `LogicManager` | Parses and executes the command, then saves UniMate data through `Storage`. |
| `AddressBookParser` | Dispatches `tag` and `untag` to their respective command parsers. |
| `TagCommandParser` | Reads the person index, accepts repeated `d/` and `t/` values, and combines the parsed tags. |
| `UntagCommandParser` | Reads the person index and accepts one or more `t/` values to remove. |
| `ArgumentTokenizer`, `ArgumentMultimap`, `CliSyntax` | Identify the supported prefixes and retain all repeated values. `PREFIX_DEFAULT_TAG` is `d/`; `PREFIX_TAG` is `t/`. |
| `ParserUtil` | Validates indices and converts custom names or default indices into a `Set<Tag>`. |
| `DefaultTags`, `TagsCommand` | Provide the fixed default tag list and display its one-based numbering. |
| `TagCommand`, `UntagCommand` | Resolve the displayed person, compute the new tag set, replace the person through `Model#setPerson`, and return a `CommandResult`. |
| `Person`, `Tag` | Store tags as a set and define name validation and exact, case-sensitive tag equality. |
| `ModelManager` | Exposes the filtered person list and delegates person replacement to `AddressBook`. |
| `Messages`, `JsonAdaptedTag` | Format the updated person for feedback and represent each stored tag by its name, respectively. |

#### Execution flow

<!-- @@author zruixuan -->

1. The command parsers convert the displayed index and requested tags before execution. For example, `tag 1 d/2 t/NOC` resolves `d/2` to `Classmate` and combines it with the validated custom tag `NOC`; `untag` accepts only names supplied through `t/`.
2. Both commands resolve the contact against `Model#getFilteredPersonList` and replace it with a copy containing the updated tag set.
   * `TagCommand` adds requested tags; `UntagCommand` removes them.
   * All other contact fields are preserved.
   * The active filter and sort order remain in place, while the observable view reflects any changed membership.
3. Success feedback starts with `Tagged person: ` or `Untagged person: ` and includes `Messages.format` output.
4. The existing history and storage paths record and save actual changes; no-op tag operations do not create an undo step.

<!-- @@author -->

#### Validation and error handling

* `ParserUtil#parseIndex` trims input and accepts positive integers representable by a Java `int`, without a leading `+`. Invalid index syntax or missing tag prefixes produce a `ParseException` containing `Invalid command format!` and the corresponding command's usage text.
* `parseDefaultTags` checks the fixed range `1` to `5`. Invalid default indices produce `Default tag index must be an integer from 1 to 5. Use tags to view them.`
* `parseTag` trims the name and checks `Tag.isValidTagName`, which uses `\p{Alnum}+` (English letters and digits with the current regex flags). Empty names, internal spaces and punctuation produce `Tag names should be alphanumeric`.
* All tag values are parsed before a command is executed. An invalid value prevents the whole command from modifying the model. In `TagCommandParser`, default tag validation occurs before custom tag validation, regardless of their input order.
* A parsed person index beyond the filtered list size produces a `CommandException` with `The person index provided is invalid.` before any replacement. This also covers an empty displayed list.
* Unsupported prefixes are not separately classified: `ArgumentTokenizer` only recognizes the supplied prefixes. For example, `untag 1 d/1` fails index parsing, whereas `untag 1 t/NOC d/1` fails tag-name validation.
* Saving occurs after model mutation. `LogicManager` converts an `AccessDeniedException` or other `IOException` into a saving-related `CommandException`; it does not roll back the updated person.

#### Design considerations

**Aspect: Shared representation of default and custom tags**

Both kinds use the same `Person.tags` field (`Set<Tag>`). Default indices are input shortcuts, not a separate stored tag type: `d/1` and `t/Friend` resolve to equal `Tag` objects. `JsonAdaptedTag` stores only the name, so no default/custom flag or new storage field is required. Consequently, `untag 1 t/Friend` can remove a default tag by name.

**Aspect: Set-based, non-destructive updates**

<!-- @@author zruixuan -->

* `Person#getTags` returns an unmodifiable view.
* Each command stores a defensive `Set.copyOf` of its input, copies the person's tags for the update, and replaces the `Person` rather than mutating its exposed set.
* `addAll` preserves existing tags; `removeAll` preserves tags not requested for removal.
* Duplicate additions and absent removals are no-ops that still return success.
* Tag order is not guaranteed. Equality remains case-sensitive, so `NOC` and `noc` are distinct.

<!-- @@author -->

**Aspect: Compatibility with existing editing**

The current `EditCommand` still supports replacing the entire tag set through `edit INDEX t/TAG`, and clearing it with `edit INDEX t/`. These operations have not been removed. `tag` and `untag` provide incremental changes; an empty `t/` is rejected by both.

#### Long tag display and hover access

<!-- @@author zruixuan -->

* **Abbreviation:** `PersonCard#refreshTags` abbreviates ordinary tag names longer than `MAX_DISPLAYED_TAG_CHARACTERS` (20) before constructing their JavaFX `Label`: the first 20 characters are followed by `...`.
  * The full value remains in `Person#getTags`; custom/system name collisions retain their `(custom)` display suffix.
  * This explicit character threshold keeps display behaviour consistent despite platform font metrics.
  * `TagPane` limits each tag to the available content width. `ExpandableText` applies `OverrunStyle.ELLIPSIS` while collapsed and wraps the full name when expanded.
* **Tooltip:** `PersonCard#installTagTooltip` retains the complete original name in a wrapping tooltip with a maximum popup width of 400 logical pixels.
  * It keeps the Label's tooltip property for accessibility, uninstalls the default delayed mouse behaviour, and uses mouse-enter/exit/press handlers to show immediately and hide on exit or press. Labels participate in mouse picking.
  * Existing explanatory tooltips for custom names that match system reminders are retained.
  * `Tag` and its validation, equality and JSON representation are unchanged; only the presentation is abbreviated.
* **Tests:** Cross-platform rendering tests use actual JavaFX CSS, skins and layout at content widths of 560, 740 and 1100 logical pixels.
  * They check the visible rendered text and relative bounds rather than hard-coded glyph widths.
  * Resizing the content container directly avoids assumptions about native window decorations, usable monitor size or asynchronous window-manager resize timing.
  * Separate tests retain real-window command/refresh/storage checks and verify that mouse-enter opens the tooltip and mouse-exit closes it.
  * No platform is excluded from these checks when a display is available.

<!-- @@author -->

For command formats and user-facing examples, see [Adding tags](UserGuide.md#adding-tags-to-a-person-tag) and [Removing tags](UserGuide.md#removing-tags-from-a-person-untag). Manual checks are listed under [Tagging and untagging a person](#tagging-and-untagging-a-person).

<!-- @@author sel-couth-ene -->

### Recording contact data history

#### Current implementation

The model keeps an in-memory history of contact data. The user-facing `undo` and `redo` commands are built on this history (see [Undo and redo commands](#undo-and-redo-commands)).

1. Every command that changes contact data records the state from before its change.

2. The model can restore earlier or later states.

3. The history lasts only for the current session.
   * it is not saved to storage
   * a newly launched app starts with no undo or redo history

##### Relevant classes

| Class | Responsibility |
|-------|----------------|
| `AddressBookHistory` | Stores defensive `AddressBook` snapshots, each paired with a description of its change, in an undo stack and a redo stack, and restores them into the tracked `AddressBook`. |
| `Model` | Declares `commitAddressBook(ReadOnlyAddressBook, String)`, `canUndoAddressBook()`, `canRedoAddressBook()`, `getUndoDescription()`, `getRedoDescription()`, `undoAddressBook()` and `redoAddressBook()`. |
| `ModelManager` | Creates one `AddressBookHistory` for its `AddressBook` and delegates the methods above to it. |
| `AddCommand`, `EditCommand`, `DeleteCommand`, `ClearCommand`, `TagCommand`, `UntagCommand`, `FavCommand`, `UnfavCommand`, `NoteCommand`, `DeleteNoteCommand` | Copy the contact data before changing it, then call `Model#commitAddressBook` with that copy and the command's history description (`edit` retains the full-contact description, excluding its immediate change summary and duplicate-details warning). A note replacement uses `MESSAGE_REPLACED_DESCRIPTION`, which omits the `Use undo to restore the previous note.` hint, because the hint would be misleading in `Undid:` and `Redid:` messages. |
| `UndoCommand`, `RedoCommand` | Check history availability, read the description of the change being reversed or reapplied, restore the corresponding state through the model, and reset the displayed list to show all contacts. |

##### History state

`AddressBookHistory` holds:

* `undoStates`: the contact data from before each recorded change, most recent on top.
* `redoStates`: the contact data that has been undone, most recent on top.
* `currentState`: a copy of the contact data in the tracked `AddressBook`.

1. Each entry in either stack is a private `Change` record holding a snapshot and a short description of the change between it and the adjacent state.
   * e.g. `Tagged person: …​`

2. `getUndoDescription()` and `getRedoDescription()` return the description at the top of the corresponding stack.
   * they return an empty `Optional` if there is no valid state to restore

3. Every stored state is a copy made with `new AddressBook(...)`.
   * `Person` and its fields are immutable, so later changes to the model cannot alter a stored snapshot

#### Execution flow

##### Committing a change

Below is the flow for a successful `add n/Alex Tan …​`:

1. `AddCommand#execute` searches the saved person list using `Person#isSamePerson`. 
   * If the resulting `Optional<Person>` contains a duplicate, it throws a `CommandException`, nothing is recorded.
2. The command creates `previousState = new AddressBook(model.getAddressBook())`, then calls `Model#addPerson`.
3. The command formats its success message and calls `Model#commitAddressBook(previousState, message)`. 
   * `AddressBookHistory#commit` then:
      * returns without recording if `previousState` holds the same contacts as the current data;
      * clears `undoStates` if `previousState` differs from `currentState`, which means an unrecorded change happened earlier (see below);
      * pushes a copy of `previousState` with `message` onto `undoStates`, clears `redoStates`, and sets `currentState` to a copy of the current data.
4. The command returns the same message as its `CommandResult`. `LogicManager` saves the contact data to storage as before. History itself is not saved.

The activity diagram below summarises what happens when a command commits. The check for unrecorded changes is described in [Unrecorded changes](#unrecorded-changes).

<puml src="diagrams/CommitActivityDiagram.puml" alt="Commit decisions for unchanged data, stale undo history and a new history entry" />

A command that fails validation, such as a duplicate `add`, throws before copying the data and never reaches this flow.

##### Other data-changing commands

Every other command that changes contact data follows the same snapshot-and-commit pattern.

1. `EditCommand#execute` checks the displayed index and duplicate persons first, copies the data, calls `Model#setPerson`, and then commits.
   * an edit that supplies the person's current values returns the no-change message before snapshotting or committing, so both history stacks and the existing update date are preserved
   * `edit 1` is invalid because at least one field is required

2. `NoteCommand` and `DeleteNoteCommand` commit only actual note changes.
   * `NoteCommand` returns immediately for unchanged note text, without copying the address book, replacing the contact or committing history, so redo remains available
   * `DeleteNoteCommand` returns immediately for an empty note
   * viewing a note does not clear redo states

3. `DeleteCommand`, `ClearCommand`, `TagCommand`, `UntagCommand`, `FavCommand` and `UnfavCommand` copy the data after their validation checks, make the change through `Model#deletePerson`, `Model#setAddressBook` or `Model#setPerson`, and then commit.

4. Changes that leave the data unchanged are ignored by the commit, so no undo step is recorded and redo remains available.
   * `tag` with a tag the person already has
   * `untag` with a tag the person does not have
   * `clear` on an empty contact list

5. `fav` on a person who is already a favourite and `unfav` on one who is not fail before copying the data.

6. A `DeleteCommand` with multiple indices captures all target contacts before mutation and commits only after every selected contact has been deleted.
   * the whole batch consumes one undo step, with its feedback stored as the history description
   * invalid or repeated indices preserve both history stacks

##### Restoring a state

`Model#undoAddressBook()` and `Model#redoAddressBook()` move one entry between the stacks.

1. `Model#undoAddressBook()` pops the top of `undoStates`, pushes `currentState` onto `redoStates` with the popped entry's description, and restores the popped state. `Model#redoAddressBook()` does the reverse.
   * the description moves with its change between the stacks, so a redo names the same change that the matching undo named

2. Restoring calls `AddressBook#resetData` on the existing `AddressBook`.
   * the `AddressBook` object and the `FilteredList` built on it are the same objects before and after, and the UI updates through its existing observers
   * restoring a state does not record a new history entry

3. Both methods return `true` if a state was restored.
   * they return `false` and leave the data unchanged if there is nothing to restore

The example below shows how the stacks change.

#### Example usage scenario

The diagrams below show how the history changes as a user enters a sequence of commands. Each `abN` is an `AddressBook` snapshot, and each stack is drawn with its top entry first.

Step 1. The user launches a disposable, empty address book. 

`currentState` is a copy of its contacts, `ab0`, and both stacks are empty. The example uses a name that is not already saved.

Step 2. The user runs the following commands in order: 
`add n/Alex Tan a/NUS`
`find Alex Tan`
`edit 1 p/91234567`

The add and edit commands commit the state from before their changes: `add` pushes `ab0` onto `undoStates` and `currentState` becomes `ab1`; `edit` then pushes `ab1` and `currentState` becomes `ab2`. The intervening `find` changes only the view and records no history entry.

<puml src="diagrams/UndoRedoState0.puml" alt="History after add and edit" />

Step 3. The user runs `undo`. 

`ab2` is pushed onto `redoStates`, and `ab1` is popped from `undoStates` and restored.

<puml src="diagrams/UndoRedoState1.puml" alt="History after undo" />

Step 4. The user runs `redo`. 

This reverses Step 3: `ab1` is pushed back onto `undoStates`, and `ab2` is popped from `redoStates` and restored. The history is again as shown after Step 2.

Step 5. The user runs `undo` again, then runs `add n/Beth Lim …​`. 

This pushes `ab1` and clears `redoStates`, so the undone edit, `ab2`, can no longer be redone. `currentState` becomes `ab3`.

<puml src="diagrams/UndoRedoState2.puml" alt="History after undo, then a new add" />

Commands that do not change contact data, such as `list` and `find`, and commands that fail before changing anything, record nothing and leave all three parts of the history unchanged.

#### Unrecorded changes

Every current command that changes contact data commits its change.

A future command that changes the data but forgets to call `Model#commitAddressBook` would be dangerous: restoring an old snapshot after it would silently reverse that change as well.
* e.g. if such a command removed a contact after an `add`, undoing the add would also bring back the removed contact

As a safety net, `AddressBookHistory` compares `currentState` with the live contact data. If they differ, a change was made without being recorded, and the history is stale:

1. `canUndoAddressBook()` and `canRedoAddressBook()` return `false`.

2. `undoAddressBook()` and `redoAddressBook()` discard both stacks and return `false`, leaving the data unchanged.

3. The next data-changing commit discards the stale undo states before recording the new one.

Continuing the example above, suppose an unrecorded change now replaces the contacts in the model with `ab4`, so they no longer match `currentState`, `ab3`.
* the history is now stale
* a following user `undo` command is rejected with `There is no change to undo.`, rather than restoring `ab1` and reversing the unrecorded change; `UndoCommand` checks `canUndoAddressBook()` first, so it does not call `undoAddressBook()` and the stale stacks are left in place
* the stale stacks are only discarded when `undoAddressBook()` or `redoAddressBook()` is called directly on the model, or by the next data-changing commit

To the user, such a change only makes `undo` and `redo` report that there is nothing to undo or redo, never restore the wrong data. `UndoCommandTest` covers this by changing the model directly without committing.

#### Known limitations

The history has no size limit, and a failed save does not roll back the change. Both are described
in [Appendix: Known limitations](#appendix-known-limitations).

#### Design considerations

**Aspect: What is stored for each change**

* **Alternative 1 (current choice):** Store a copy of the whole contact data state.
  * Pros: Easy to implement and to verify, and works the same for any command.
  * Cons: Uses more memory as the number of contacts and history entries grows. The history is not trimmed (see [Known limitations](#appendix-known-limitations)).
* **Alternative 2:** Each command stores how to reverse itself (e.g. `add` stores the person to remove).
  * Pros: Uses less memory.
  * Cons: Every command needs its own correct reversal logic.

**Aspect: How the history is structured**

* **Alternative 1 (current choice):** An undo stack and a redo stack in a separate `AddressBookHistory` class.
  * Pros: Clearing redo history is a single `clear()` call. `AddressBook` stays unchanged, and history can be tested on its own.
  * Cons: `ModelManager` has to hold and delegate to another object.
* **Alternative 2:** A `VersionedAddressBook` subclass with a list of states and a current-state pointer.
  * Pros: Keeps history and data in one object.
  * Cons: Discarding redo states requires trimming the list after the pointer, and `AddressBook` gains history responsibilities.

**Aspect: Which commands are undoable**

* **Alternative 1 (current choice):** Every command that changes contact data commits its change.
  * Pros: Users can rely on one rule: any change can be undone, including `delete` and `clear`, where mistakes are most costly. Each command needs only a snapshot and a commit call.
  * Cons: A full snapshot is stored for every change, including small ones such as `fav`.
* **Alternative 2:** Only some commands, such as `add` and `edit`, are undoable; the others make earlier history unavailable.
  * Pros: Fewer snapshots are stored.
  * Cons: Users must remember which commands can be undone, and a non-undoable command also blocks undoing earlier changes. This was the earlier behaviour, and it was reported as confusing for `fav` and `unfav`.

**Aspect: How unrecorded changes affect history**

* **Alternative 1 (current choice):** Detect unrecorded changes by comparing the expected state with the live data.
  * Pros: A future command that forgets to commit cannot cause an old snapshot to silently reverse its change.
  * Cons: Each history check compares the whole contact list. If an unrecorded change is later reversed exactly, the history becomes valid again, which is safe because the data is identical.
* **Alternative 2:** Trust every data-changing command to commit, with no check.
  * Pros: Avoids the comparison on every history check.
  * Cons: A single missing commit would let `undo` restore the wrong data, and the model alone cannot detect it because many commands share `Model#setPerson`.

### Undo and redo commands

#### Current implementation

The `undo` and `redo` commands let users revert and reapply any change to contact data using the history described in [Recording contact data history](#recording-contact-data-history).

##### Relevant classes

| Class | Responsibility |
|-------|----------------|
| `AddressBookParser` | Recognises the `undo` and `redo` command words. Throws a `ParseException` with `MESSAGE_INVALID_COMMAND_FORMAT` and the command's usage if any arguments follow the command word. |
| `UndoCommand` | Checks `Model#canUndoAddressBook()`, reads `Model#getUndoDescription()`, calls `Model#undoAddressBook()`, and shows all contacts. |
| `RedoCommand` | Checks `Model#canRedoAddressBook()`, reads `Model#getRedoDescription()`, calls `Model#redoAddressBook()`, and shows all contacts. |

Neither command has its own parser class, because neither takes any arguments.

#### Execution flow

##### Undo flow

Given below is the flow for `undo`:

1. `LogicManager#execute` passes the input to `AddressBookParser#parseCommand`. If anything follows the command word, such as in `undo 1`, a `ParseException` is thrown and the model is never reached.
2. The parser returns a new `UndoCommand`, and `LogicManager` calls `UndoCommand#execute(model)`.
3. The command checks `Model#canUndoAddressBook()`. If it returns `false`, the command throws a `CommandException` with `UndoCommand.MESSAGE_FAILURE`. The exception propagates out of `LogicManager#execute` before the save step, so neither the data nor the data file changes.
4. Otherwise, the command reads the description of the change about to be reversed with `Model#getUndoDescription()`, then calls `Model#undoAddressBook()`, which restores the previous state in place. It then calls `Model#updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS)` so that every restored contact is visible, even if a `find`, `filter` or `listfav` view was active, and returns a `CommandResult` with `UndoCommand.MESSAGE_SUCCESS` formatted with that description.
5. `LogicManager` saves the restored contact data through `Storage#saveAddressBook`, as it does for other contact-data commands.

The sequence diagram below shows steps 1 to 4 for `execute("undo")`, including the failure path when there is nothing to undo. The save to storage in step 5 is omitted.

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="Interactions inside the Logic component for the `undo` command" />

##### Redo flow

`redo` follows the same flow using `RedoCommand`, `Model#canRedoAddressBook()`, `Model#getRedoDescription()` and `Model#redoAddressBook()`.

1. Neither command calls `Model#commitAddressBook`.

2. Restoring a state moves it between the undo and redo stacks without adding a new entry.
   * repeated `undo` keeps moving back through earlier states
   * `redo` does not clear the remaining redo states

##### Save failures

1. If saving fails after a successful undo or redo, the in-memory state has already been restored.

2. `LogicManager` reports the file error, as it does for other commands.

3. The history remains consistent with the in-memory data.

##### Feedback messages

| Situation | Message |
|-----------|---------|
| `undo` succeeds | `Undid: ` followed by the undone command's stored description (`edit` uses full contact details, without the change summary or duplicate-details warning), e.g. `Undid: Tagged person: …​` |
| `redo` succeeds | `Redid: ` followed by the redone command's stored description (`edit` uses full contact details, without the change summary or duplicate-details warning), e.g. `Redid: Added to favourites: …​` |
| Nothing to undo, including stale history | `There is no change to undo.` |
| Nothing to redo, including stale history | `There is no change to redo. An undone change cannot be redone after another change has been made.` |
| Arguments after `undo` or `redo` | `Invalid command format!`, followed by the command's usage |

1. The description names the original change, not the restored values.

2. Note replacements omit the `Use undo to restore the previous note.` hint.

#### Design considerations

**Aspect: Where the availability check happens**

* **Alternative 1 (current choice):** The command checks `canUndoAddressBook()` or `canRedoAddressBook()` first and throws a `CommandException` if it returns `false`.
  * Pros: The failure is reported through the usual command error path. The `boolean` returned by `undoAddressBook()` and `redoAddressBook()` only acts as a safety net.
  * Cons: The command makes an extra availability-check call before restoring history.
* **Alternative 2:** Call `undoAddressBook()` or `redoAddressBook()` directly and use the returned `boolean` to choose the message.
  * Pros: No separate availability-check call. Description lookup and view reset are still needed.
  * Cons: The command's decision depends on the result of a call that may also discard stale history as a side effect, which is harder to follow.

**Aspect: How the undone or redone change is described**

* **Alternative 1 (current choice):** Each command passes its history description (`edit` uses full contact details rather than its immediate change summary) to `commitAddressBook`, and the history stores it with the snapshot.
  * Pros: Most commands reuse their success message without new wording. Edit retains its established full-contact description so undo/redo identifies the affected contact without repeating field or tag differences.
  * Cons: Messages that include a full `Messages#format` of the person are long. Each command must remember to pass a description, although the two-argument signature makes it impossible to commit without one.
* **Alternative 2:** Work out the description by comparing the two snapshots, e.g. finding the contact that was added or removed.
  * Pros: Commands need no change.
  * Cons: The comparison cannot tell a `tag` from an `edit` that only changes tags, and it needs separate logic for every kind of change.

**Aspect: Handling extra arguments**

* **Alternative 1 (current choice):** Reject input such as `undo 1` with a parse error.
  * Pros: Users are not misled into thinking `undo 3` undoes three changes or a specific contact, and no history changes by accident.
  * Cons: Inconsistent with `help`, `exit` and `clear`, which ignore extra parameters.
* **Alternative 2:** Ignore extra parameters, as `help` does.
  * Pros: Consistent with the other commands that take no parameters.
  * Cons: `undo 3` would silently undo only one change.

**Aspect: What is displayed after restoring**

* **Alternative 1 (current choice):** Show all contacts after every undo or redo.
  * Pros: The restored or removed contact is always visible, and indices from an earlier filtered list are not reused by mistake.
  * Cons: The user loses the current `find` filter.
* **Alternative 2:** Keep the current filter.
  * Pros: The user keeps their search context.
  * Cons: The restored change may be hidden by the filter, so it can look as if nothing happened.

#### Testing

Undo and redo are tested at each layer:

| Test class | What it checks |
|------------|----------------|
| `AddressBookHistoryTest`, `ModelManagerTest` | Committing, undoing and redoing snapshots, the descriptions returned for undo and redo, ignoring unchanged commits, clearing redo states on a new commit, and stale history after unrecorded changes. |
| `AddCommandTest`, `AddCommandIntegrationTest`, `EditCommandTest` | Successful `add` and `edit` commands record exactly one history entry, while failed or unchanged ones record none. |
| `UndoCommandTest`, `RedoCommandTest` | Each command on its own against a model: success messages naming the undone or redone change, failure messages, the order of repeated undos and redos, redo invalidation, and resetting the filtered list. `UndoCommandTest` also undoes and redoes each of `delete`, `clear`, `tag`, `untag`, `fav`, `unfav` and `note`, checks that unchanged results such as `clear` on an empty list record nothing, and checks stale history after a change made directly through the model. |
| `AddressBookParserTest` | `undo` and `redo` are parsed, and extra arguments are rejected with the usage message. |
| `LogicManagerTest`, `UndoRedoIntegrationTest` | Full command strings through `LogicManager#execute`: parser, command, model history and JSON storage together. |

`UndoRedoIntegrationTest` starts each test from the typical contacts saved to a temporary data file.

1. After every command, it checks the full contact list in the model, the contacts read back from the data file, and the feedback message.

2. For failing commands, it also checks that `canUndoAddressBook()` and `canRedoAddressBook()` are unchanged.

3. Its scenarios include:
   * chains of adds and edits
   * redo invalidation by a new change
   * failed commands between undo and redo
   * interaction with `list`, `find`, `tags` and `help`
   * reopening the saved data with no history

<!-- @@author -->

--------------------------------------------------------------------------------------------------------------------

### Command history navigation

These activities separate older-command recall from newer-command recall and draft restoration.
Prefix matching keeps the initial prefix until editing or chronological navigation resets it.

<puml src="diagrams/CommandHistoryUpActivityDiagram.puml" alt="Older-command recall with a retained search prefix" />

<puml src="diagrams/CommandHistoryDownActivityDiagram.puml" alt="Newer-command recall, inactive-search handling and draft restoration" />

`CommandBox` owns a session-local `CommandHistory`. A `KEY_PRESSED` event filter on its text field
consumes Up/Down events, recalls the corresponding text and moves the caret to its end.
When recalled text differs from the current input, the existing text listener clears the error style.
Recalling an identical string does not trigger that listener. Browsing does not execute a command or
change the result display or contact list.

`CommandHistory` stores submissions in order, including duplicates and failed commands. `CommandBox`
records non-empty input before calling the executor, so parse and execution errors remain recallable.
Each submission resets the navigation position to the end. On the first Up from the end, history saves
the current draft; Down past the newest entry restores it. Navigation stops at the oldest entry, and
Down at the draft position leaves input unchanged. Editing recalled input does not change stored entries.
History is held in memory and is not written to contact data or preferences.

Alt+Up/Down (Option+Up/Down on macOS) calls `previousMatching` / `nextMatching` on the same
history cursor. `previousMatching` captures the current input as a case-sensitive `startsWith` prefix;
subsequent searches retain it, skipping unrelated entries. No older match leaves the cursor unchanged.
After the newest match, `nextMatching` restores the saved draft and ends the search. Without an active
search, Alt+Down is a no-op. Editing input, submitting a command or using chronological arrows resets
the prefix. `CommandBox` guards programmatic recalls so its text listener resets the search only for
external edits. Switching navigation modes keeps the original draft and the current history position.

Internally, `position` is zero-based in `0..commands.size()`, inclusive; the upper bound denotes
the draft rather than a stored command. The GUI converts stored positions to one-based labels; a
no-match search preserves the cursor and therefore the label. Both activity diagrams include the label update,
even when no text changes or a shortcut leaves the search inactive.

`CommandHistory#getPosition()` exposes a one-based chronological index (zero at the draft), and
`size()` includes all submissions, even duplicates. After each navigation event, `CommandBox` updates
the adjacent `historyPositionLabel` to `History i/N`. A 200 ms `FadeTransition` restarts from 35% to
full opacity on each press, including boundary presses and identical recalls. At the draft and on Enter,
the transition stops and the label becomes invisible and unmanaged so the input reclaims its space.
Failed submissions also hide the label. Prefix search displays absolute positions rather than match counts.
This recall history stores command strings independently of `AddressBookHistory`: recalling a failed
command is valid even though that command created no contact-data undo entry.

`CommandHistoryTest` covers boundaries, duplicates, exact prefixes, mode switching and draft state. `CommandBoxTest` exercises the real
FXML text field on the JavaFX thread: Up/Down event consumption, caret placement, draft restoration,
failed submissions, error styling, Alt shortcuts, edited prefixes and the absence of executor calls during browsing.
For manual UI verification:

1. Submit `list`, then an invalid command. Press Up twice and verify both can be recalled in reverse order.
2. Press Up again at the oldest entry; verify it stays displayed. Press Down to return to the newest entry.
3. Return to the draft position, type a partial command, then press Up and Down; verify the draft is restored.
4. Edit a recalled command and submit it; verify the new submission is the newest entry.
5. Submit `add n/Alice a/NUS`, `list`, `add n/Bob a/NUS`, then `list`. Type `add ` and use Alt+Up twice:
   verify Bob then Alice is recalled, skipping List entries. Alt+Down twice restores Bob then `add `.
6. Verify no matches leave input unchanged, and ordinary arrows still visit all entries after prefix search.
7. Submit `list` three times. Press Up repeatedly and verify the label shows distinct absolute positions,
   pulses even at the oldest entry, then disappears when Down restores the draft or Enter submits input.
8. Restart UniMate and verify both navigation modes leave the input unchanged until a command is submitted.

## Coming soon

The features in this section are not implemented in the current release. The descriptions and diagrams below are proposals for future development.

### \[Proposed\] Data archiving

Data archiving is planned for a future version. Its implementation details have not yet been specified.


--------------------------------------------------------------------------------------------------------------------

## Documentation, logging, testing, dev-ops

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## Appendix: Requirements

### Product scope

**Target user profile**:

* is a university student with many university-related contacts
* needs to retain and maintain personal contact information
* prefers desktop apps
* can type fast and prefers typing to mouse interactions
* is reasonably comfortable using CLI apps
* manages their own contact collection

**Value proposition**: Organise university contacts and find the right person by name or university-related context using typed commands.

### User stories

These user stories include requirements for the v1.5 development version and future iterations.
Priority indicates importance, not implementation status. See the [User Guide](UserGuide.md#features)
for implemented features and its version notice before testing a downloaded JAR.

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …         | I want to …                                                                     | So that I can …                                                 |
|----------|----------------|---------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `* * *`  | new user       | see usage instructions                                                          | understand UniMate's capabilities and learn its commands        |
| `* * *`  | user           | add a person with contact information and optional major and graduation year     | retain their contact details and academic background                             |
| `* * *`  | user           | view all my stored contacts                                                     | review the people in my collection                              |
| `* * *`  | user           | view a contact's details                                                        | retrieve their information when needed                          |
| `* * *`  | user           | delete one or more contacts in one command                                      | remove unwanted entries together                               |
| `* * *`  | user           | edit a contact's information                                                    | keep their details up to date                                   |
| `* * *`  | returning user | access previously saved contacts                                                | continue using my collection without recreating it              |
| `* * *`  | user           | categorise contacts by module, major, year, nationality, CCA, event, or project | organise people by their university-related context             |
| `* * *`  | user           | assign multiple categories to one contact                                       | represent different contexts without creating duplicate records |
| `* * *`  | user           | find contacts by name                                                           | retrieve someone's details without browsing the entire list     |
| `* * *`  | user           | filter contacts by university-related context                                   | find relevant people when I cannot remember their names         |
| `* *`    | user           | identify contacts from the same tutorial group                                  | contact the appropriate classmates                              |
| `* *`    | user           | save or replace a short note for a contact                                      | remember useful information, including how we met               |
| `* *`    | user           | view a contact's saved note                                                     | recall its details when needed                                 |
| `* *`    | user           | remove a contact's note while keeping the contact                               | discard outdated context without losing contact details        |
| `* *`    | user           | identify the saved contact behind a duplicate-add error                         | check the existing entry before deciding what to change         |
| `* *`    | user           | be warned when an addition or edit shares another contact's phone or email      | check possible mistakes while keeping intentional shared details |
| `* *`    | user           | distinguish academic contacts from personal contacts                            | focus on the group relevant to my current activity              |
| `* *`    | user           | mark and retrieve favourite contacts                                            | quickly access people I contact frequently                      |
| `* *`    | user           | undo a recent contact change                                                    | recover from an accidental change, such as a deletion or clear  |
| `* *`    | user           | redo an undone contact change                                                   | restore an intended change without entering it again            |
| `* *`    | user           | recall previously entered commands                                              | repeat or modify commands without retyping them                 |
| `* *`    | user           | use short command aliases                                                       | perform frequent operations with fewer keystrokes               |
| `* *`    | user           | autocomplete partially entered commands                                         | reduce typing effort and spelling mistakes                      |
| `* *`    | new user       | receive suggestions after an invalid command                                    | correct mistakes without repeatedly consulting the usage guide  |
| `* *`    | user           | sort contacts by a chosen attribute                                             | review my collection in a useful order                          |
| `* *`    | user           | see automatic labels for missing optional contact information                   | identify records that need more details                         |
| `* *`    | user           | see when a contact was last updated                                             | judge how recently their information was maintained             |
| `* *`    | user           | see automatic labels for contacts not updated for over a year                   | identify potentially outdated records                           |
| `*`      | user           | identify contacts I have not interacted with for a long time                    | decide whom to reconnect with or remove                         |
| `*`      | user           | attach a follow-up task and deadline to a contact                               | remember what I need to do for that person and when             |
| `*`      | user           | open a contact's recorded GitHub or Telegram profile                            | access their external profile without searching separately      |


### Use cases

(For all use cases below, the **System** is `UniMate` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Add a contact**

**MSS**

1.  User requests to add a new contact 
2.  User provides the person's name and address, with optional contact and academic details
3.  UniMate validates the provided contact information
4.  UniMate adds the contact 
5.  UniMate informs the user that the contact has been added

    Use case ends.

**Extensions**

* 2a. User does not provide all required information.

    * 2a1. UniMate informs user that the required information is missing.

      Use case ends.

* 3a. UniMate detects invalid contact information.

    * 3a1. UniMate informs user which information is invalid.
      
      Use case ends.

* 3b. UniMate detects a duplicate contact with the same name.

    * 3b1. UniMate shows the existing contact's details and its displayed index, or a labelled saved
      index with instructions to reveal the contact if hidden by the current filter.
      
      Use case ends.

<!-- @@author RayWHJ -->

* 5a. The added contact shares a non-empty phone number or email with another contact.

    * 5a1. UniMate appends an advisory identifying the matches to the success feedback.

      Use case ends.

<!-- @@author -->

**Use case: View contacts**

**MSS**

1.  User requests to view all existing contacts.
2.  UniMate shows all existing contacts.

    Use case ends.

**Extensions**

* 2a. There are no existing contacts.

    * 2a1. UniMate shows an empty contact list.

      Use case ends.

<!-- @@author RayWHJ -->

**Use case: Delete contacts**

**MSS**

1.  User views contacts, optionally narrowing or sorting the list.
2.  User requests to delete one or more contacts from the displayed list.
3.  UniMate validates all selections, then deletes all selected contacts as one change.
4.  UniMate reports the deleted contacts, including their original displayed positions for a batch.

    Use case ends.

**Extensions**

* 1a. No contacts are displayed.

    * 1a1. UniMate shows an empty list. A deletion attempt suggests listing saved contacts or adding a contact.

      Use case ends.

* 3a. A selection is invalid or repeated.

    * 3a1. UniMate reports the problem without deleting any selected contact or changing history.

      Use case resumes at step 2.

**Use case: Save a contact note**

**MSS**

1. User selects a displayed contact and supplies note text.
2. UniMate validates the selection and text.
3. UniMate saves the note and reports its text, including the previous text when replacing a note.

   Use case ends.

**Extensions**

* 2a. The selection or text is missing or invalid.

    * 2a1. UniMate reports the problem without changing contacts or history.

      Use case resumes at step 1.

* 2b. The supplied text matches the existing note.

    * 2b1. UniMate reports that the note is unchanged, preserving undo and redo history.

      Use case ends.

Viewing a note uses the same contact selection and validation, then returns the text or an empty-note
message. Deleting a note clears only its text and records one change; an already empty note leaves
data and history unchanged. Actual note changes support the undo and redo use cases below.

<!-- @@author -->

**Use case: Edit a contact**

**MSS**

1. User views contacts and requests a change to one displayed contact, supplying updated details.
2. UniMate validates the selected contact and supplied values.
3. UniMate applies the changes and informs the user which values changed.

   Use case ends.

**Extensions**

* 2a. The selection or supplied values are invalid, or the name belongs to another contact.
  * 2a1. UniMate reports the problem without changing contacts.
    Use case resumes at step 1.
* 2b. All supplied values already match.
  * 2b1. UniMate reports that no change was made, preserving the contact and its history.
    Use case ends.

<!-- @@author RayWHJ -->

* 3a. The edit introduces a shared non-empty phone number or email.
  * 3a1. UniMate appends an advisory identifying matching contacts to the success feedback.
    Use case ends.

<!-- @@author -->

<!-- @@author sel-couth-ene -->

**Use case: Search for contacts**

**MSS**

1.  User requests to find contacts using one or more keywords.
2.  UniMate searches the names, majors and class years of all saved contacts.
3.  UniMate shows the contacts that match at least one keyword as a whole word, ignoring case, and informs the user how many were found.

    Use case ends.

**Extensions**

* 1a. User does not provide any keyword.

    * 1a1. UniMate informs the user that the command format is invalid and shows its usage.

      Use case ends.

* 3a. No contact matches any keyword.

    * 3a1. UniMate shows an empty contact list and informs the user that 0 contacts were found.

      Use case ends.

* 3b. User wants to act on a found contact.

    * 3b1. User uses the contact's displayed index in a command such as `edit`, `delete` or `fav`.

      Use case ends.

**Use case: Undo a change**

**MSS**

1.  User changes the contacts, for example by adding, editing, deleting, tagging or favouriting a contact.
2.  User requests to undo the most recent change.
3.  UniMate restores the contacts to their state before that change.
4.  UniMate shows all contacts and informs the user that the change has been undone.

    Use case ends.

**Extensions**

* 2a. There is no change to undo in the current session.

    * 2a1. UniMate informs the user that there is no change to undo.

      Use case ends.

* 2b. User provides extra input after the command.

    * 2b1. UniMate informs the user that the command format is invalid and shows its usage.

      Use case ends.

* 4a. User wants to revert an earlier change.

    * 4a1. User requests to undo again.

      Use case resumes at step 2.

**Use case: Redo an undone change**

**MSS**

1.  <u>User undoes a change (Use case: Undo a change).</u>
2.  User requests to redo the most recently undone change.
3.  UniMate reapplies that change.
4.  UniMate shows all contacts and informs the user that the change has been restored.

    Use case ends.

**Extensions**

* 1a. User changes the contacts again before requesting a redo.

    * 1a1. UniMate discards the undone changes.

      Use case resumes at step 2.

* 2a. There is no undone change to redo.

    * 2a1. UniMate informs the user that there is no change to redo.

      Use case ends.

* 2b. User provides extra input after the command.

    * 2b1. UniMate informs the user that the command format is invalid and shows its usage.

      Use case ends.

* 4a. User wants to reapply further undone changes.

    * 4a1. User requests to redo again.

      Use case resumes at step 2.

<!-- @@author -->

### Non-Functional Requirements

These are release targets, not completed verification. Record results using the
[release testing checklist](ReleaseTesting.md).

1. UniMate should work on Windows, Linux and macOS with Java 25 (the prescribed JDK+FX on macOS).
2. With 1,000 stored contacts, representative `list`, `find`, `filter`, `sort`, `edit`, `undo` and
   `redo` commands should finish within one second on the recorded reference environment.
   Measure submission through visible feedback/list update and disk saving; record hardware,
   runtime, display scaling and cold/warm state so the target can be assessed.
3. Primary contact-management workflows should be usable entirely with the keyboard. Focus must
   remain visible, and documented navigation must return to the command box.
4. UniMate should work without an Internet connection.
5. UniMate should preserve saved contact information between application sessions.
6. Invalid command input should not corrupt or modify existing contact data.
7. Invalid command input should produce a meaningful error without crashing.

### Operating constraints

UniMate supports only one running instance at a time. Concurrent use by multiple instances is
unsupported, and behaviour in that situation is undefined. Users are informed of this restriction
in the [Quick start](UserGuide.md#quick-start).

### Glossary

* **Category**: A grouping used to organise contacts by a shared attribute or context, such as a module, major, CCA, event, or project.
* **CCA (Co-Curricular Activity)**: An organised student activity outside academic classes, such as a sports club, performing arts group, or student society.
* **CLI (Command-Line Interface)**: An interface in which users type text commands to perform operations. UniMate accepts these commands through the command box in its graphical window.
* **Contact**: A record representing a person in the user's collection, containing their name and contact information. Also referred to as a person in this guide.
* **Contact information**: Details recorded about a person, such as their phone number, email address, and postal address.
* **Contact list index**: The one-based position of a contact in the currently displayed list, used by commands to select that contact. It is not a permanent identifier for the person.
* **Graduation year / Class year**: The calendar year in which a person graduates or expects to graduate, such as 2027 (class of 2027). This differs from their current year of study. UniMate accepts years from 2000 to 2099.
* **GUI (Graphical User Interface)**: An interface that presents information through visual elements such as windows, lists, and buttons.
* **Major**: A student's principal academic field of study, such as Computer Science.
* **Module**: A university course or unit of study, typically identified by a code such as CS2103T. Here, it refers to an academic course rather than a software component.
* **Private contact detail**: A contact detail that is not meant to be shared with others.
* **Tag**: A text label attached to a contact to help organise the contact collection. A contact can have multiple tags.
* **Tutorial group**: A group of students assigned to attend the same tutorial session for a particular module.
* **University-related context**: Information that connects a contact to university life, such as their module, major, year of study, CCA, or the event or project through which the user knows them.
* **Year of study**: A student's current stage in their university programme, such as Year 1 or Year 2, rather than a calendar year.

--------------------------------------------------------------------------------------------------------------------

## Appendix: Instructions for manual testing

Given below are instructions to test the v1.5 development version manually. Use a separate test folder and test contacts, as these checks modify saved data. Ensure the test contact names do not already exist before each scenario; use different unique names when repeating a scenario.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Choosing a test scenario

Use a fresh disposable folder for each section unless it explicitly continues an earlier scenario.
Sample contacts may load on first launch; an empty folder does not mean an empty contact list.
Use `clear` only when a scenario requests an empty list, and only in the disposable copy.
Within a numbered scenario, perform the steps in order. Use unique test names and re-find the contact
after an edit, undo, redo or restart before using its displayed index again.

| Area | Checks |
| --- | --- |
| Basic commands and validation | The corresponding add, edit, search, filter, sort, delete and tag sections below. |
| Cards and physical keys | [Contact-list interaction](#testing-contact-list-interaction) and [command recall](#testing-command-history). |
| Contact flags and derived labels | [Favourites](#testing-favourites) and [reminders](#testing-derived-reminders). |
| Notes and history | [Managing notes](#managing-contact-notes), [undo/redo](#undoing-and-redoing-changes) and the note-history cases. |
| Persistence and errors | [Saving data](#saving-data) and [save-failure recovery](#testing-save-failure-recovery). |

### Launch and shutdown

1. Initial launch

   1. Ensure Java 25 is installed (the prescribed JDK+FX on macOS). Build the revision under test,
      or use its matching v1.5 JAR once published. Copy `unimate.jar` into an empty, writable test folder.

   2. Open a terminal, change to that folder, and run `java -jar unimate.jar`.
      Expected: A window titled `UniMate` opens with sample contacts. Unspecified academic fields display `Not specified`. The window size may not be optimal.

   3. Enter `exit`.
      Expected: The application window closes normally.
<br>
2. Saving window preferences

   1. Relaunch the app with `java -jar unimate.jar`. Resize the window to an optimal size, move it to a different location, and close it.

   2. Relaunch the app from the same test folder with `java -jar unimate.jar`.
       Expected: The most recent window size and location are retained.
<br>
3. Help window without an internet connection

   **Result:** Passed on macOS, Linux and Windows (v1.5 development build).

   1. Turn off Wi-Fi and unplug any network cable. Launch the app.
   2. Press F1, or run `help`.
      Expected: The help window opens and shows the User Guide's web address.
   3. Click **Copy URL**, close the help window, then paste into the command box.
      Expected: The exact User Guide address is pasted. UniMate shows no network error. Clear the
      command box and turn the network back on.

### Testing contact-list interaction

1. Add `add n/UiCheck a/NUS` and the following long-value contact, using names not already saved:

   ```text
   add n/UiLongCheck Long Contact Long Contact Long Contact Long Contact Long Contact Long Contact Long Contact p/999999999999999999999999999999999999999999999999999999999999999999999999 e/averylongstudentemailaddressforcheckingthecontactcardlayout@example.com a/Lower Kent Ridge Road Lower Kent Ridge Road Lower Kent Ridge Road Lower Kent Ridge Road Lower Kent Ridge Road Lower Kent Ridge Road m/Computer Science Computer Science Computer Science Computer Science Computer Science c/2027 t/LongTagForCheckingCollapsedAndExpandedContactCardLayout12345678901234567890
   ```
   <br>
2. Run `find UiCheck UiLongCheck`. Resize between a wide window and a narrow window.
   Expected: Wide cards use two columns; narrow cards stack the academic fields below contact details.
   `UiCheck` shows `Not specified` for missing optional fields. Long values end in ellipses inside
   their own fields and do not overlap captions, neighbouring fields or the next card.
<br>
3. Click each shortened value, including the long name and tag, then click again.
   Expected: Expansion reveals the complete value inside a taller card; collapse restores the compact
   layout. Scrolling and resizing retain expansion. Use the list scrollbar to reach the end of expanded
   content. For a long list, add disposable contacts and check scrollbar dragging and smaller arrow steps.
<br>
4. From the command box, press Tab, then use Up/Down to select the long contact. Follow the
   [UG keyboard route](UserGuide.md#using-the-contact-list) forward and backward with Tab/Shift+Tab.
   Expected: The list selection has an orange border. Only expandable values on the selected contact
   enter the route, including its long tag. Each focused value has an orange dotted outline;
   Enter/Space toggles it. The route returns to the command box after the last value and reverses
   correctly. Other cards and short values are skipped. Navigation does not submit a command.
<br>
5. Expand a value, run `list`, then change that contact's address using its current displayed index.
   Expected: `list` preserves expansion; changed contact data can collapse it. Undo may restore the
   retained expansion state of the earlier contact. Restart collapses all values.
<br>
6. In a separate empty disposable book, press Tab and Shift+Tab in the command box.
   Expected: Focus stays in the command box because there is no contact to select.

### Testing favourites

1. In an empty disposable book, add `add n/FavouriteCheck a/NUS`, run `find FavouriteCheck`, then `fav 1`.
   Expected: A red heart appears. `listfav` contains the contact. Its other details and update date stay unchanged.
<br>
2. Run `fav 1` again.
   Expected: An already-favourite error; no additional history change.
<br>
3. Run `unfav 1`, then `unfav 1` again and `listfav`.
   Expected: The first command removes the heart, the repeated command reports the existing status,
   and `listfav` reports no favourites.
<br>
4. Run `undo`, then `find FavouriteCheck`.
   Expected: The heart returns. Run `redo` and find the contact again: the heart is absent.
<br>
5. Run `fav 0` and `unfav 9999`, then restart and find the contact.
   Expected: Invalid-index errors change nothing; the saved unfavourited state persists.

### Testing derived reminders

1. Add `add n/ReminderCheck a/NUS t/update t/incomplete` and run `find ReminderCheck`.
   Expected: An amber `incomplete` system reminder and grey `update (custom)` / `incomplete (custom)`
   tags coexist. There is no age-based reminder for a newly added contact.
<br>
2. Run `edit 1 p/91234567`, find the contact again, then run `edit 1 p/ e/`.
   Expected: Providing a contact method removes `incomplete`; clearing both restores it.
<br>
3. Close the app and back up the disposable JSON file. Change only ReminderCheck's `lastUpdated`,
   relaunch and find it for each row below. Close before each file edit; retain valid ISO dates.

   | Stored date | Expected system reminder |
   | --- | --- |
   | JSON `null`, or property omitted | No age-based `update`; the date is unknown. |
   | A date whose one-year anniversary is today, where such a date exists | No age-based `update`. |
   | A known date whose one-year anniversary is before today | `update` and `incomplete`, alongside both custom tags. |

   Calculate anniversaries using the [calendar rule](UserGuide.md#automatic-update-reminders),
   rather than keeping fixed dates that become stale. A leap-day boundary is also covered by automated date tests.
<br>
4. With the older-date fixture, run `untag 1 t/update t/incomplete`.
   Expected: Custom tags disappear; the two automatic reminders remain and the saved date is unchanged.
<br>
5. Run `edit 1 p/92345678`, then find the contact.
   Expected: A current update date, no age reminder and no incomplete reminder. Restore the backup afterwards.

### Testing command history

1. Submit `list`, `tags`, then `list`. Type `find ` without submitting it.
   Expected: Up recalls `list`, then `tags`; Down recalls `list`, then restores the draft `find `.
   The result display and contacts do not change during recall.
<br>
2. Submit `sort name asc`, `list`, `sort name desc`, then `list`. Type `sort ` without submitting.
   Expected: Alt+Up (Option+Up on macOS) recalls descending then ascending sort commands, retaining
   the original `sort ` prefix. Alt+Down recalls descending then restores `sort `.
   Another Alt+Down without an active search leaves the draft unchanged.
<br>
3. Recall a match and edit its text, then start another prefix search. Also try ordinary Up/Down.
   Expected: Editing starts a new search at the current position; ordinary navigation ends prefix
   matching and proceeds chronologically. Neither route executes the recalled command.
<br>
4. Submit an invalid command, then press Up. Also press Enter with the input completely empty, and
   separately submit only spaces.
   Expected: The failed command is recallable. Completely empty input adds no entry. Whitespace-only
   input is reported as an invalid command and is added as a recallable entry. Restart clears history.

### Testing the saved-contact listing

1. Restoring all saved contacts from other views

   1. Prerequisites: Use a disposable test copy. Add `add n/ListAlice a/NUS m/Computing` and
      `add n/ListBob a/Clementi m/Business`. Run `find ListAlice`, then `fav 1`.
   2. Run `find ListAlice`, then `list`.
      Expected: Every saved contact appears, including both test contacts. Feedback starts with `Listed all persons.`
      and the next line reports `N person(s) listed!`, where `N` equals the full displayed count.
      The status above the cards shows `Showing N of N contacts | Filter: All contacts | Sort: Saved order`
      if no sort was applied.
   3. Run `filter m/Computing`, then `list`; repeat with `listfav`, then `list`.
      Expected: Each `list` restores all contacts and their current details. Favourite hearts and update dates remain unchanged.
   4. Run `find NoSuchListContact`, then `list`.
      Expected: The search has no results, but `list` still restores every saved contact and reports the full count.
<br>
2. Sorting, persistence and history

   1. Run `sort name desc`, then `find ListAlice`, then `list`.
      Expected: Every contact returns in descending name order, numbered consecutively from `1`.
      The status shows `Filter: All contacts` and still ends with `Sort: Name descending`.
   2. Close and relaunch the app from the same folder, then run `list`.
      Expected: Both test contacts and all other saved details remain available. The temporary sort is reset to saved order.
   3. Run `add n/ListUndo a/NUS`, then `undo`, then `list`, then `redo`.
      Expected: `ListUndo` is absent after undo and list, and returns after redo; listing does not clear redo history.
   4. Run `list`, then `undo`.
      Expected: The add of `ListUndo` is undone; listing did not create an undo step.
<br>
3. Empty address books and invalid input

   1. In the disposable test copy only, run `clear`, then `list`.
      Expected: An empty list with `Listed all persons.` and `0 person(s) listed!`.
   2. Add `add n/ListValidation a/NUS`, then run `find NoSuchListContact`. Try `list all`, `list 1` and
      `list t/Friend`.
      Expected: Each shows `Invalid command format!` with `list` usage instructions. The empty filtered view and
      saved `ListValidation` contact remain unchanged.
   3. Run `  list  ` with surrounding spaces.
      Expected: `ListValidation` is displayed and the count is `1 person(s) listed!`.

### Identifying duplicate additions

1. Visible duplicates in a sorted and filtered view

   1. Prerequisites: Use a disposable test copy and ensure `DuplicateAlpha` and `DuplicateBeta` do not
      already exist. Add `add n/DuplicateAlpha p/91234567 e/original@example.com a/NUS m/Computing c/2027 t/DuplicateDemo`
      and `add n/DuplicateBeta a/Clementi t/DuplicateDemo`.
   2. Run `filter t/DuplicateDemo`, then `sort name desc`.
      Expected: `DuplicateBeta` is displayed at index 1 and `DuplicateAlpha` at index 2.
   3. Run `add n/duplicatealpha p/98765432 e/new@example.com a/Another address m/Law c/2029`.
      Expected: The duplicate is rejected. The error includes `Existing contact at displayed index 2:`
      and the saved `DuplicateAlpha` details: phone `91234567`, email `original@example.com`, address
      `NUS`, major `Computing` and class year `2027`. The two displayed cards and their order remain
      unchanged. The submitted command remains in the command box for correction.
<br>
2. Hidden duplicates and saved indices

   1. Run `find DuplicateBeta`, then repeat the duplicate `add` above.
      Expected: Only `DuplicateBeta` remains displayed. The error identifies the hidden saved
      `DuplicateAlpha` contact, labels its index as `saved index N`, and instructs `list`, then `sort reset`.
   2. Run those two commands in order and inspect index `N`.
      Expected: That card is `DuplicateAlpha`, with all its original details. The saved index is
      valid in this restored full list and saved order.
   3. Run `find NoDuplicateFeedbackMatch`, ensuring it matches no contact, then repeat the duplicate `add`.
      Expected: The empty view is preserved, and the error still identifies the same saved contact.
   4. Clean up the disposable contacts using `find DuplicateAlpha`, `delete 1`, then `find DuplicateBeta`, `delete 1`.

### Shared phone or email warnings on add

1. Multiple matches and refreshed displayed indices

   1. Prerequisites: Use an empty disposable address book. Add:

      * `add n/Zara p/91919191 e/zara172@example.com a/NUS`
      * `add n/Charlie p/92929292 e/shared172@example.com a/Clementi`
      * `add n/Morgan a/Dover`

   2. Run `sort name asc`, then `find Charlie`.
      Expected: Only Charlie is displayed.
   3. Run `add n/Amy p/91919191 e/SHARED172@EXAMPLE.COM a/NUS`.
      Expected: The addition succeeds. The normal `New person added: ...` message remains complete and
      includes Amy's saved details. An appended line in the same feedback box reads
      `Warning - duplicate details detected: #2 Charlie (email); #4 Zara (phone)`.
      Both lines are amber with no coloured border. The full list is Amy, Charlie, Morgan, Zara.
      Morgan's missing fields produce no match. Narrow the window to about 640 pixels. The result
      area retains its 100-pixel height; both lines can be selected and copied. Scrolling is needed
      only when the wrapped feedback exceeds that area.
   4. Run `undo`, then `redo`.
      Expected: One undo removes Amy and one redo restores Amy. Their feedback describes the addition
      in light grey without repeating the warning, and Zara, Charlie and Morgan retain their original details.
   5. Close and reopen the disposable copy, then run `find Amy`.
      Expected: Amy was saved with the entered phone and uppercase email. Sorting and filtering
      return to their usual restart defaults.
<br>
2. Empty details and both fields matching the same contact

   1. Run `add n/NoDetails a/NUS`.
      Expected: The contact is added with ordinary light grey success feedback. Its missing phone and email
      do not trigger a warning about Morgan's missing details.
   2. Run `add n/AnotherZara p/91919191 e/zara172@example.com a/Dover`.
      Expected: The addition succeeds with a warning. Zara appears once with the reason
      `phone and email`; Amy is also listed for sharing the phone number.
   3. While a warning is showing, run `sort name sideways`, then `list`.
      Expected: The invalid command replaces the warning with the usual light grey error feedback and input
      styling. `list` shows ordinary light grey success feedback. Neither command changes saved contacts.
   4. Repeat a shared-details addition with a deliberately long address (for example, ten repetitions
      of Lower Kent Ridge Road) in the disposable copy, then narrow the window.
      Expected: The complete ordinary success message and appended warning appear amber in the
      original text area, starting at the top. All names, displayed indices and reasons remain
      complete; the existing vertical scrollbar reveals any wrapped overflow.

### Shared phone or email warnings on edit

1. Changed methods and refreshed indices

   1. Prerequisites: Use a disposable copy with no contacts named `EditWarning Alice` or `EditWarning Bob`.
      Add `add n/EditWarning Alice p/91112222 e/alice.editwarning@example.com a/NUS`, then
      `add n/EditWarning Bob p/93334444 e/bob.editwarning@example.com a/NUS`.
   2. Run `find EditWarning`, then `sort name asc`.
      Expected: Alice is index 1 and Bob is index 2 within the two-contact view.
   3. Run `edit 2 p/91112222`.
      Expected: The edit succeeds and the input clears. `Edited EditWarning Bob:` and the changed
      phone line appear first, followed by `Warning - duplicate details detected: #N EditWarning Alice (phone)`.
      `N` is Alice's current displayed index in the refreshed full sorted list, not necessarily her
      previous index in the two-contact view. The complete feedback appears amber in the original feedback box,
      with no coloured border or additional warning control.
   4. Run `undo`, then `redo`.
      Expected: One undo restores Bob's previous phone and one redo restores the shared phone.
      Both use ordinary light grey edit-history feedback without repeating the warning. Alice remains unchanged.
   5. Run `find EditWarning`, confirm Bob's displayed index, then edit that index with
      `e/ALICE.EDITWARNING@EXAMPLE.COM t/StudyGroup`.
      Expected: `Edited EditWarning Bob:` is followed by the email difference, then
      `Tags replaced. Removed: None. Added: StudyGroup.` The final warning line identifies Alice for
      `(email)` only; the already-shared unchanged phone is not reported as newly shared. All feedback
      lines appear amber in the original feedback box. The uppercase email and replacement tags are saved unchanged.
   6. Run `undo`, then `redo`.
      Expected: One undo restores both Bob's previous email and tags; redo restores both changes.
      Feedback remains light grey and includes only the base edited-contact details, not the tag summary
      or duplicate-details warning.
   7. Rename Bob while changing a contact method to another shared value, then inspect the full sorted list.
      Expected: The change summary identifies his new name and lists only actual differences; the warning
      still uses the matching contacts' refreshed indices even if the renamed contact has moved.
<br>
2. Unchanged, unrelated and empty values

   1. Before each check, find the edited contact and use its actual displayed index.
   2. Repeat the current phone value, change only the major with `m/Mathematics`, or change only
      the email's capitalization.
      Expected: Light grey changed-field feedback for actual changes, or `No changes made to NAME.
      The supplied values match the existing details.` for an unchanged edit. Existing shared values
      do not cause a warning. An unchanged edit preserves undo and redo.
   3. Clear a contact method with `p/` or `e/`.
      Expected: Ordinary light grey success feedback; empty values do not match other empty contact methods.
   4. Change only tags with `t/StudyGroup`, then clear them with `t/`, checking the displayed index before each edit.
      Expected: A changed tag set produces the removed/added summary in light grey, with `None`
      for an empty difference. An unchanged set produces no tag summary. Existing shared phone/email values do not trigger a warning for these tag-only edits.
   5. Close and reopen the disposable copy.
      Expected: Edited values persist; the warnings did not modify or merge any other contact.

### Adding contacts with prefix-like address text

1. Unknown prefix-like text and correction

   1. Prerequisites: Use a disposable test copy. Ensure no contacts named `PrefixProbe` or `SlashProbe`
      already exist; choose different unique names if repeating the checks.
   2. Run `add n/PrefixProbe a/NUS x/typo`.
      Expected: The contact is added successfully with address `NUS x/typo`. The success feedback and
      contact card include that address; there is no automatic unknown-prefix warning.
   3. Run `find PrefixProbe`, confirm the test contact is the only result, then `edit 1 a/NUS`.
      Expected: The address becomes `NUS`; the name and other contact details remain unchanged.
<br>
2. Legitimate slash-containing address

   1. Run `add n/SlashProbe a/Block A/B, Unit 01/02`.
      Expected: The contact is added with the complete address `Block A/B, Unit 01/02`.
   2. Close and relaunch the disposable copy from the same folder, then run `find SlashProbe`.
      Expected: The complete slash-containing address remains saved. Run `find PrefixProbe` to verify
      that its corrected address `NUS` also remains saved.

### Adding academic fields

1. Valid values and independently optional fields

   1. Prerequisites: Use a test copy of the app in a separate folder. Ensure the names used below do
      not already exist; use different unique names if repeating the tests.
   2. Test case: `add n/AcademicBoth p/91234567 e/both@example.com a/10 Kent Ridge Road m/Computer Science c/2027`
      Expected: A contact is added with major `Computer Science` and class year `2027`. Both values
      appear in the contact card and the success message.
   3. Test case: `add n/AcademicMajor p/91234567 e/major@example.com a/10 Kent Ridge Road m/Mathematics`
      Expected: The contact has major `Mathematics` and an unspecified class year.
   4. Test case: `add n/AcademicYear p/91234567 e/year@example.com a/10 Kent Ridge Road c/2028`
      Expected: The contact has class year `2028` and an unspecified major.
   5. Test case: `add n/AcademicNeither p/91234567 e/neither@example.com a/10 Kent Ridge Road`
      Expected: The contact is added with both academic fields unspecified. Each unspecified field
      is displayed as `Not specified` in the contact card and omitted from the success message.
   6. Repeat the first command with a new unique name and `c/2000`, then another unique name and
      `c/2099`.
      Expected: Both boundary years are accepted.
<br>
2. Invalid values and repeated prefixes

   1. Prerequisites: Ensure `AcademicInvalid` does not exist. Use
      `add n/AcademicInvalid p/91234567 e/invalid@example.com a/10 Kent Ridge Road` as the base command
      and append each suffix below separately.
   2. Test cases: `m/`, `m/   `, `c/`, `c/   `.
      Expected: A major or class-year validation error is shown. No contact is added.
   3. Test cases: `c/1999`, `c/2100`, `c/202`, `c/20270`, `c/abcd`.
      Expected: A class-year validation error is shown. No contact is added.
   4. Test cases: `m/Computer Science m/Mathematics`, `c/2027 c/2028`.
      Expected: A repeated-prefix error is shown. No contact is added, and existing contacts are unchanged.
<br>
3. Saving, reloading and older data files

   1. Prerequisites: Complete the valid-input tests above in the test copy of the app.
   2. Close and relaunch the app from the same folder, then run `list`.
      Expected: All four test contacts retain their supplied academic values and unspecified fields.
   3. Close the app and back up the test copy's `data/unimate.json`. In that test file, remove the
      `major` and `graduationYear` properties from the `AcademicBoth` contact, leaving valid JSON.
      Relaunch the app and run `find AcademicBoth`.
      Expected: The contact loads with its original name, phone, email, address and tags, and both
      academic fields show `Not specified`. Other contacts retain their academic values. Restore the
      backup with the app closed when finished.

### Editing academic fields

1. Updating and clearing optional fields

   1. Prerequisites: Add a test contact using `add n/AcademicEdit p/91234567 e/edit@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/StudyGroup`.
   2. Before **each** edit below, run `find AcademicEdit` and confirm that it is the only result. Successful edits return the display to the full contact list, so the search must be repeated before editing index `1` again.
   3. Test case: `edit 1 m/Mathematics`.
      Expected: The major changes to `Mathematics`; class year remains `2027`. Other contact details and `StudyGroup` are preserved.
   4. Test case: `edit 1 c/2028`.
      Expected: Class year changes to `2028`; major remains `Mathematics`.
   5. Test case: `edit 1 m/`.
      Expected: The major displays `Not specified`; class year remains `2028`.
   6. Test case: `edit 1 c/`.
      Expected: Both academic fields display `Not specified`. Other contact details and tags remain unchanged.
   7. Close and relaunch the app from the same folder, then run `find AcademicEdit`.
      Expected: The cleared fields remain unspecified.
<br>
2. Rejecting invalid edits

   1. Prerequisites: Run `find AcademicEdit`.
   2. Test cases: `edit 1 c/1999`, `edit 1 c/abcd`, `edit 1 m/Computer Science m/Mathematics`.
      Expected: A class-year validation error or repeated-prefix error is shown. The contact is unchanged.
   3. Test case: `edit 1 m/Mathematics c/2100`.
      Expected: A class-year validation error is shown. Neither academic field changes, including the valid major supplied in the same command.

<!-- @@author sel-couth-ene -->

### Searching by academic fields

1. Matching names, majors and class years

   1. Prerequisites: Add these two test contacts:

      * `add n/SearchComputing p/91234567 e/computing@example.com a/10 Kent Ridge Road m/Computer Science c/2027`
      * `add n/SearchBusiness p/91234568 e/business@example.com a/11 Kent Ridge Road m/Business c/2028`

   2. Test case: `find computer`.
      Expected: `SearchComputing` is included and `SearchBusiness` is excluded. 
      
      Matching is case-insensitive; other contacts with matching names or academic fields may also appear. The view status line shows `Filter: Search: computer`.

   3. Test case: `find 2028`.
      Expected: `SearchBusiness` is included and `SearchComputing` is excluded.
   4. Test case: `find SearchComputing 2028`.
      Expected: Both test contacts appear because matching any keyword is sufficient.
   5. Test case: `find comp`.
      Expected: Neither test contact appears; partial words do not match `Computer` or `SearchComputing`.
   6. Test case: `find 28`.
      Expected: `SearchBusiness` does not appear; `28` is not a whole word of the class year `2028`.
   7. Test case: `find n/SearchComputing`.
      Expected: `0 person(s) listed!`. Prefixes are not supported, so `n/SearchComputing` is searched for as one word.
   8. Test case: `find 91234567`.
      Expected: `SearchComputing` does not appear; phone numbers are not searched.
   9. Test case: `find`.
      Expected: `Invalid command format!` with the usage of `find`. The displayed list is unchanged.
<br>
2. Punctuation in majors

   1. Prerequisites: `add n/SearchHonours a/NUS m/Computer Science (Hons)`.
   2. Test case: `find Hons`, then `find (Hons)`.
      Expected: `find Hons` does not list `SearchHonours`; `find (Hons)` does. Words are separated by spaces only.
<br>
3. Searching all contacts, not just the displayed ones

   1. Test case: `find SearchBusiness`, then `find SearchComputing`.
      Expected: The second command lists `SearchComputing` even though it was hidden by the first search.
   2. Test case: `sort name desc`, then `find SearchComputing SearchBusiness`.
      Expected: `SearchComputing` is listed before `SearchBusiness`; the sort is kept. Run `sort reset` afterwards.
   3. Test case: `list`.
      Expected: The full contact list is displayed again, including both test contacts and contacts without academic fields.
<br>
4. Searching does not affect undo and redo

   1. Test case: `add n/SearchUndo a/NUS`, then `find SearchUndo`, then `undo`.
      Expected: `find` lists `SearchUndo`. `undo` removes `SearchUndo`, starting with `Undid: New person added: SearchUndo;`, and the full contact list is shown instead of the search result.
   2. To clean up the earlier test contacts, run `find SearchComputing SearchBusiness SearchHonours` and delete each listed test contact by its index.

<!-- @@author -->

### Filtering contacts

1. Combined criteria, independent fields and full-value matching

   1. Prerequisites: Use a disposable test copy of UniMate and restart it before this test so no sort is active.
      Ensure no existing contact uses the names or custom tag `FilterDemo` below. Add these contacts:

      * `add n/FilterMath p/91234567 e/math@example.com a/Kent Ridge m/Mathematics c/2027 t/FilterDemo`
      * `add n/FilterComputing p/92345678 e/computing@example.com a/Clementi m/Computer Science c/2027 t/FilterDemo`
      * `add n/FilterLater p/93456789 e/later@example.com a/Dover m/Computer Science c/2028 t/FilterDemo`
      * `add n/FilterUnknown p/94567890 e/unknown@example.com a/Buona Vista t/FilterDemo`

   2. Run `filter m/computer science c/2027 t/filterdemo`.
      Expected: Only `FilterComputing` appears, numbered `1`, with `1 person(s) listed!`.
      All three criteria must match, and major/tag case differences are ignored.
   3. Run `filter c/2027 t/FilterDemo`.
      Expected: `FilterMath` and `FilterComputing` appear in that order. The previous major criterion
      is replaced, and `FilterUnknown` is excluded because its class year is unspecified.
   4. Run `filter m/Computer t/FilterDemo`.
      Expected: An empty displayed list and `0 person(s) listed!`; partial majors do not match.
   5. Run `filter t/FilterDemo`, then `list`.
      Expected: All four demo contacts appear after the filter, including `FilterUnknown`.
      `list` restores every contact, including those outside the demo group.
<br>
2. Invalid input preserves the current view

   1. Run `filter c/2027 t/FilterDemo` and note the two results.
   2. Try each command separately: `filter`, `filter m/`, `filter c/2100`, `filter t/`,
      `filter t/FilterDemo t/Friend`, `filter n/FilterMath`.
      Expected: A usage, value-validation or repeated-prefix error. The same two results remain,
      and no saved contact changes.
<br>
3. Displayed indices, changing membership and restarting

   1. Run `filter c/2028 t/FilterDemo`, then `untag 1 t/FilterDemo`.
      Expected: `FilterLater` disappears from the results but remains saved. Run `find FilterLater`
      to see it, then `tag 1 t/FilterDemo` to restore its tag.
   2. Run `filter c/2028 t/FilterDemo`, then `delete 1`.
      Expected: Only `FilterLater` is deleted. Other contacts remain, even though the deleted contact
      was not first in the complete address book.
   3. Run `filter m/Computer Science t/FilterDemo`, then `edit 1 m/Mathematics`.
      Expected: `FilterComputing` changes major and the full list is shown, as for existing edits.
      Reapply the same filter: no demo contact matches now.
   4. Run `filter t/FilterDemo`, close and relaunch the app from the same folder.
      Expected: The full list is shown. Saved contacts and the changes above remain; the filter is temporary.

<br>
4. Majors containing prefix-like text

   1. Add `add n/FilterLiteral a/NUS m/Mathematics w/ Computing c/2027 t/FilterDemo`,
      using an unused name.
   2. Run `filter m/Mathematics w/ Computing c/2027 t/FilterDemo`.
      Expected: `FilterLiteral` appears with `1 person(s) listed!`, assuming no other contact has the
      same complete major, class year and tag. The `w/` text remains part of the major; `c/` and `t/`
      still start separate criteria.

### Sorting contacts

1. Directions, ties and unspecified fields

   1. Prerequisites: Use a disposable copy of UniMate. Ensure the following names and tag `SortDemo`
      do not already exist. Add these contacts, then run `filter t/SortDemo`:

      * `add n/SortGamma p/91234567 e/gamma@example.com a/Dover m/business c/2027 t/SortDemo`
      * `add n/SortUnknown p/92345678 e/unknown@example.com a/Clementi t/SortDemo`
      * `add n/SortBeta p/93456789 e/beta@example.com a/Kent Ridge m/Business c/2027 t/SortDemo`
      * `add n/sortAlpha p/94567890 e/alpha@example.com a/Utown m/Zoology c/2028 t/SortDemo`

   2. Run `sort name asc`, then `sort name desc`.
      Expected: Ascending shows `sortAlpha`, `SortBeta`, `SortGamma`, `SortUnknown`; descending reverses
      that order. The cards are numbered `1` to `4`. Feedback identifies the field and direction.
   3. Run `sort major asc`, then `sort major desc`.
      Expected: Ascending shows `SortBeta`, `SortGamma`, `sortAlpha`, `SortUnknown`.
      Descending shows `sortAlpha`, `SortBeta`, `SortGamma`, `SortUnknown`.
      Major case differences are ignored, equal majors use ascending names, and unspecified majors stay last.
   4. Run `sort year asc`, then `sort year desc`.
      Expected: The same two orders as the major checks above. `SortUnknown` stays last in both.
   5. Run `filter t/SortDemo m/Business`, then `sort name desc`.
      Expected: Only `SortGamma` and `SortBeta` appear, in that order.
      Run `list`: all contacts return in descending name order. Reapply `filter t/SortDemo` for later checks.
<br>
2. Rejected input and displayed indices

   1. Run `sort name asc`. Try `sort`, `sort name`, `sort phone asc`, `sort year ascending`,
      `sort reset asc` and `sort major asc desc` separately.
      Expected: Usage errors; the same four demo contacts remain in ascending name order.
   2. Run `edit 2 c/2029`.
      Expected: `SortBeta` changes year, even though it was third in the saved demo order.
      The full list appears in ascending name order. Run `filter t/SortDemo`, then `sort year asc`:
      the order is `SortGamma`, `sortAlpha`, `SortBeta`, `SortUnknown`.
   3. Run `undo`, then `filter t/SortDemo`.
      Expected: `SortBeta` returns to `2027`; the order is `SortBeta`, `SortGamma`, `sortAlpha`, `SortUnknown`.
      Run `redo`, then reapply the same filter: the edited order returns. Sorting and filtering have not
      consumed the undo entry or cleared the redo opportunity.
   4. Run `sort name desc`, then `delete 1`.
      Expected: Only `SortUnknown` is removed. The remaining demo contacts are renumbered in descending
      name order; contacts outside `SortDemo` are unaffected.
<br>
3. Empty results and restarting

   1. Run `filter t/SortDemo m/NoSuchMajor`, then `sort year desc`.
      Expected: An empty list and successful sort feedback. Run `filter t/SortDemo`:
      the remaining demo contacts appear in descending year order.
   2. Close and relaunch UniMate from the same folder, then run `filter t/SortDemo`.
      Expected: The demo contacts return in saved order: `SortGamma`, `SortBeta`, `sortAlpha`.
      The edit and deletion remain saved, while the previous sort and filter do not survive restarting.
<br>
4. Reset order and persistent status

   1. Prerequisites: Complete the sorting checks above in the disposable copy. The remaining demo
      contacts are `SortGamma`, `SortBeta` and `sortAlpha` in saved order.
   2. Run `filter t/SortDemo`, then `sort name asc`.
      Expected: `sortAlpha`, `SortBeta`, `SortGamma`. The status shows `Showing 3 of N contacts`
      (`N` is the total saved count), `Filter: Tag: SortDemo` and `Sort: Name ascending`.
   3. Run `shownote 1`.
      Expected: The result display shows the note or no-note feedback; the status remains unchanged.
   4. Run `sort reset` twice.
      Expected: Both commands succeed. Only the three demo contacts remain visible, in saved order:
      `SortGamma`, `SortBeta`, `sortAlpha`. The status shows `Sort: Saved order`.
   5. Run `sort name desc`, then `list`.
      Expected: All contacts return, still sorted by descending name. The status shows equal counts,
      `Filter: All contacts` and `Sort: Name descending`. Run `sort reset` to restore saved order.
   6. Run `filter t/SortDemo`, then try `sort reset desc`.
      Expected: A usage error; the active filter, saved order and status remain unchanged.
   7. Run `filter m/Computer Science and Information Systems c/2027 t/SortDemo` and narrow the window.
      Expected: Long status text wraps within the window; all criteria and the sort description remain
      readable. Zero matches, if any, show `Showing 0 of N contacts` without deleting saved contacts.
<br>
5. Latest update date

   1. Prerequisites: Use a disposable copy with no contacts named `UpdatedAlpha`, `UpdatedBeta` or
      `UpdatedGamma`, and no tag `UpdateSortDemo`. Add contacts in this order:

      * `add n/UpdatedGamma p/91234567 a/NUS t/UpdateSortDemo`
      * `add n/UpdatedBeta p/92345678 a/NUS t/UpdateSortDemo`
      * `add n/UpdatedAlpha p/93456789 a/NUS t/UpdateSortDemo`

      Close UniMate. In the disposable `data/unimate.json`, set only these contacts' `lastUpdated`
      properties: `UpdatedGamma` to `2025-01-01`, and `UpdatedBeta` and `UpdatedAlpha` to `2024-12-31`.
      Relaunch and run `filter t/UpdateSortDemo`. Confirm the dates on the cards before continuing.
   2. Run `sort updated asc`, then `sort updated desc`.
      Expected: Ascending shows `UpdatedAlpha`, `UpdatedBeta`, `UpdatedGamma`; descending shows
      `UpdatedGamma`, `UpdatedAlpha`, `UpdatedBeta`. Both directions break equal dates by ascending
      names. Only the three matching contacts appear. Status ends with `Sort: Last updated ascending`
      or `Sort: Last updated descending`, and card numbers match the displayed order.
   3. Run `sort updated asc`, then `edit 1 p/99999999`, then `filter t/UpdateSortDemo`.
      Expected: `UpdatedAlpha` changes phone and receives today's date, moving after the two older contacts.
      It was third in saved order but is targeted using the first displayed card.
      Run `sort updated desc`: `UpdatedAlpha`, `UpdatedGamma`, `UpdatedBeta` appear.
   4. Run `undo`, then `filter t/UpdateSortDemo`.
      Expected: `UpdatedAlpha` regains its original phone and date; descending order becomes
      `UpdatedGamma`, `UpdatedAlpha`, `UpdatedBeta`. Run `sort updated asc`, then `redo`, then reapply
      the filter: the edited phone/date return, with `UpdatedBeta`, `UpdatedGamma`, `UpdatedAlpha`
      in ascending order. Sorting has not consumed undo history or cleared redo.
   5. Try `sort updated`, `sort updated ascending` and `sort updated desc extra` separately.
      Expected: Usage errors; the filter, order, dates and status remain unchanged.
      Run `sort reset`: only the matching contacts remain, in saved order
      `UpdatedGamma`, `UpdatedBeta`, `UpdatedAlpha`.
   6. Close and reopen UniMate, then run `filter t/UpdateSortDemo`.
      Expected: The same saved order and edited phone/date remain. The temporary sort is reset and
      status ends with `Sort: Saved order`.

   7. Close UniMate and remove `UpdatedGamma`'s `lastUpdated` property from the disposable data file.
      Relaunch, run `filter t/UpdateSortDemo`, then try `sort updated asc` and `sort updated desc`.
      Expected: `UpdatedGamma` shows `Last updated: Unknown` and stays last in both directions.
      Saving and restarting preserve its unknown date; an actual basic-profile edit records today's date.

<!-- @@author RayWHJ -->

### Deleting contacts

Run these scenarios in order using disposable data. Add the following unique test contacts:

* `add n/BatchDeleteA a/NUS t/BatchDeleteDemo`
* `add n/BatchDeleteB a/NUS m/Computing c/2027 t/BatchDeleteDemo`
* `add n/BatchDeleteC a/NUS t/BatchDeleteDemo`

1. Original indices and one-step history

   1. Run `filter t/BatchDeleteDemo`, then `sort name asc`.<br>
      Expected: A, B and C appear at indices 1, 2 and 3.
   2. Run `delete 3 1`.<br>
      Expected: Only B remains, renumbered 1. Feedback starts with `Deleted 2 contacts:` and lists C
      with original index 3, then A with original index 1. Contacts outside the filter are unaffected.
   3. Run `undo`, reapply the filter, then `redo` and reapply the filter again.<br>
      Expected: One undo restores all three contacts; one redo removes A and C again. Undo/redo shows
      the full list before the filter is reapplied.
   4. Run `undo`, then `filter t/BatchDeleteDemo` to restore all three and leave redo available.

2. Whole-batch rejection

   1. Try `delete 1 4`, `delete 1 01`, `delete 1 0` and `delete 1,3` separately.<br>
      Expected: Every command is rejected; all three contacts, their view and saved data remain unchanged.
      The first reports the valid range 1 to 3; the second reports a repeated index. No valid selection
      is partially deleted.
   2. Run `redo`, then reapply the filter.<br>
      Expected: Only B remains, proving that rejection preserved redo.
   3. Run `undo`, then reapply the filter before continuing.

3. Single deletion, persistence and empty results

   1. Run `delete 2`.<br>
      Expected: B is removed. Feedback starts with `Deleted person:` and includes its major and class year.
   2. Restart from the same folder, run `filter t/BatchDeleteDemo`, then `sort name asc`.<br>
      Expected: Only A and C remain; the deletion was saved.
   3. Run `find NoBatchDeleteMatch`, then `delete 1`.<br>
      Expected: No displayed contact is deleted; the error suggests `list` to reveal saved contacts.
   4. Reapply the filter and run `delete 1 2` to clean up.<br>
      Expected: No demo contacts remain; other contacts are untouched.

For the empty-address-book boundary, use a separate disposable copy: run `clear`, then `delete 1`.
The error should suggest adding a contact.

<!-- @@author -->

### Tagging and untagging a person

1. Listing and assigning default tags

   1. Test case: `tags`.
      Expected: The result lists `1. Friend`, `2. Classmate`, `3. Teammate`, `4. Tutor` and `5. Prof`, in that order.
   2. Test case: `add n/DefaultTagCheck p/91234567 e/defaulttags@example.com a/10 Kent Ridge Road d/1 d/2 t/StudyGroup`.
      Expected: The contact has `Friend`, `Classmate` and `StudyGroup`. Tag display order may vary.
   3. Test case: `tags extra`.
      Expected: A usage error is shown; contacts and tags are unchanged.
<br>
2. Adding and removing tags

   1. Prerequisites: Run `list` with at least one contact present. Note the first person's original details and tags; use a test contact because the following steps change its tags.
   2. Test case: `tag 1 d/2 t/NOC`
      Expected: `Classmate` and `NOC` are present alongside all original tags. Other contact fields are unchanged. Feedback starts with `Tagged person: ` and includes the updated details.
   3. Repeat `tag 1 d/2 t/NOC`.
      Expected: No duplicate tags appear; the command still succeeds.
   4. Test case: `untag 1 t/NOC t/Classmate`
      Expected: Both named tags are removed, other tags and contact fields remain, and feedback starts with `Untagged person: `.
   5. Repeat `untag 1 t/NOC t/Classmate`.
      Expected: Success with no further tag changes.
<br>
3. Invalid input and filtered indices

   1. Run `tag 1 t/NOC` so that the contact has a tag to preserve on rejection.
   2. Test cases: `tag 1 d/6`, `tag 1 t/`, `untag 1`, `untag 1 t/NOC t/`.
      Expected: The corresponding default-index, tag-name or usage error described in the UG; no tags change, including `NOC` in the last example.
   3. Run `find KEYWORD`, replacing `KEYWORD` with a full name word matching an existing contact, then `tag 1 t/NOC` and `untag 1 t/NOC`.
      Expected: Both affect the first search result and retain the search filter.
   4. Run either command with an index greater than the displayed list size and a valid tag.
      Expected: `The person index provided is invalid.` No contact changes.

<!-- @@author sel-couth-ene -->

### Undoing and redoing changes

Perform these scenarios in order; each one relies on the contacts left by the previous one.

1. Undoing and redoing an add

   1. Test case: `add n/UndoCheck p/91234567 e/undo@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/StudyGroup`, then `undo`.
      Expected: `UndoCheck` is removed and the full contact list is shown. The result display starts with `Undid: New person added: UndoCheck;`, followed by its original details.
   2. Test case: `redo`.
      Expected: `UndoCheck` reappears with all of its details, including its major, class year and tag. The result display starts with `Redid: New person added: UndoCheck;`, followed by its original details.
   3. Close and relaunch the app from the same folder, then run `find UndoCheck`.
      Expected: `UndoCheck` is present; the redone state was saved.
   4. Test case: `undo`.
      Expected: `There is no change to undo.` Undo history is not kept between sessions, so `UndoCheck` remains.
<br>
2. Undoing and redoing several edits

   1. Before **each** edit below, run `find UndoCheck` and confirm that it is the only result. Successful edits return the display to the full contact list, so the search must be repeated before editing index `1` again.
   2. Run `edit 1 p/98765432 c/2028`.
      Expected: The status message starts with `Edited UndoCheck:` and lists only actual changes. `UndoCheck` has phone `98765432` and class year `2028`, and still has the tag `StudyGroup`. The full contact list is shown.
   3. Run `find UndoCheck`, then `edit 1 t/Mentor`.
      Expected: The status message starts with `Edited UndoCheck:` and lists only actual changes. Running `find UndoCheck` again shows phone `98765432`, class year `2028` and only the tag `Mentor`. No other contact has changed.
   4. Test case: `undo`.
      Expected: The tag `StudyGroup` replaces `Mentor`; the phone and class year stay `98765432` and `2028`. The full contact list is shown instead of the search result. The message is `Undid: Edited person: UndoCheck; …​; Tags: [Mentor]`: it shows the details from after the undone edit, not the restored ones.
   5. Test case: `undo` again.
      Expected: The phone and class year return to `91234567` and `2027`.
   6. Test case: `redo`, then `redo` again.
      Expected: The first `redo` restores phone `98765432` and class year `2028`; the second restores the tag `Mentor`.
<br>
3. Redo is unavailable after a new change

   1. Test case: `undo`, then `add n/RedoCheck p/92345678 e/redo@example.com a/20 Kent Ridge Road`, then `redo`.
      Expected: The `undo` restores `UndoCheck`'s `StudyGroup` tag and `RedoCheck` is then added. The `redo` fails with `There is no change to redo. An undone change cannot be redone after another change has been made.` and the contacts are unchanged.
<br>
4. Commands that do not affect history

   1. Test case: `undo`.
      Expected: `RedoCheck` is removed.
   2. Run `add n/UndoCheck p/93456789 e/other@example.com a/30 Kent Ridge Road`, `add n/Incomplete`, `list`, `find UndoCheck` and `tags`.
      Expected: The first two fail with a duplicate-person error and an invalid-command-format error. No command changes any contact.
   3. Test case: `redo`.
      Expected: `RedoCheck` is added back. The failed and non-modifying commands did not discard the undone change.
   4. Test cases: `undo 1`, `redo 1`.
      Expected: An invalid-command-format error with the command's usage is shown, and no contact changes.
<br>
5. Undoing deletions, tags, favourites and clear

   1. Run `find RedoCheck` and confirm that it is the only result. Then run `delete 1`.
      Expected: `RedoCheck` is deleted.
   2. Test case: `undo`.
      Expected: `RedoCheck` is back and the full contact list is shown.
   3. Test case: `redo`.
      Expected: `RedoCheck` is deleted again.
   4. Run `undo` to bring `RedoCheck` back, then `find RedoCheck`, `tag 1 t/Undoable`, then `fav 1`.
      Expected: `RedoCheck` has the tag `Undoable` and is a favourite.
   5. Test case: `undo`, then `undo` again.
      Expected: The first `undo` removes the favourite status; the second removes the `Undoable` tag.
   6. Test case: `clear`, then `undo`.
      Expected: All contacts are removed by `clear`, then restored by `undo`.
   7. To clean up, run `find UndoCheck`, then `delete 1`, then `find RedoCheck`, then `delete 1`.

<!-- @@author -->

<!-- @@author RayWHJ -->

### Managing contact notes

Add `add n/NoteCheck a/NUS`, then run `find NoteCheck`. Use this unique contact throughout.
After an edit, undo, redo or restart, run `find NoteCheck` again before using index `1`.

1. Save, replace and read

   1. Run `shownote 1`.<br>
      Expected: `No notes have been added to this contact.` No note icon is visible.
   2. Run `note 1 Met during CS2103T`, then `shownote 1`.<br>
      Expected: Saved-note feedback contains the text; an orange icon appears at the card's top right.
      `shownote` returns the same text without changing the view.
   3. Run `note 1 Project teammate`.<br>
      Expected: Replacement feedback includes the previous text, saved text and undo hint.
      Saving the same text again reports `Note unchanged for NoteCheck.`.

2. Preserve other fields and reload

   1. Run `edit 1 p/91234567`, then `find NoteCheck`, `tag 1 t/NoteDemo`, `untag 1 t/NoteDemo`
      and `shownote 1`.<br>
      Expected: The note remains `Project teammate` after each contact replacement.
   2. Restart from the same folder, run `find NoteCheck`, then `shownote 1`.<br>
      Expected: The note, icon and edited phone number remain.

3. Whitespace, validation and long feedback

   1. Copy `note 1  Hello ` (the spaces around `Hello` are non-breaking), then run `shownote 1`.<br>
      Expected: Exactly `Hello` is saved. Whitespace inside a note remains unchanged.
   2. Try `note 1`, `note 0 Text`, `shownote 1 extra`, `delnote 1 extra`, an out-of-range index
      and a note longer than 500 characters.<br>
      Expected: Informative errors; the existing note and other details remain unchanged.
   3. Save a long note and replace it with another, each within 500 characters; narrow the window.<br>
      Expected: Previous and saved text wrap and remain fully readable by vertical scrolling.
      New INFO log entries contain the summary without either note's text.

4. Delete only the note

   1. Run `delnote 1`, then `shownote 1`.<br>
      Expected: The note and icon disappear, the filtered contact remains, and `shownote` reports no note.
   2. Repeat `delnote 1`.<br>
      Expected: `This contact has no note to delete.` Restart and re-find the contact to confirm the note stays empty.

Use the [note-history scenarios](#undoing-and-redoing-note-changes) to check undo/redo and unchanged results.

### Undoing and redoing note changes

Add `add n/NoteHistoryCheck a/NUS`. Before each scenario, and after every undo, redo or restart,
run `find NoteHistoryCheck` before using index `1`. Do not insert unrelated data-changing commands.

1. Creation, replacement and deletion

   1. Run `delnote 1`, `note 1 First note`, `note 1 Second note`, then `delnote 1`.
   2. Undo three times, re-finding the contact and running `shownote 1` after each.<br>
      Expected: `Second note`, then `First note`, then no note. The replacement history message
      omits the hint to use undo. The icon follows note presence.
   3. Redo three times with the same checks.<br>
      Expected: `First note`, then `Second note`, then no note; other details stay unchanged.

2. Unchanged text preserves redo

   1. Run `note 1 First note`, `note 1 Second note`, then `undo`; re-find the contact.
   2. Run `note 1 First note`, `shownote 1`, then `redo`; re-find and run `shownote 1`.<br>
      Expected: The unchanged result and read operation preserve redo; the final note is `Second note`.

3. Deleting an empty note preserves redo

   1. Run `delnote 1`, `note 1 Restore me`, then `undo`; re-find the contact.
   2. Run `delnote 1`, `shownote 1`, then `redo`; re-find and run `shownote 1`.<br>
      Expected: Deleting the empty note changes nothing; redo restores `Restore me`.

4. A new change clears redo

   1. Run `note 1 First note`, `note 1 Second note`, then `undo`; re-find the contact.
   2. Run `note 1 Replacement note`, `redo`, then `shownote 1`.<br>
      Expected: No change is available to redo; the note remains `Replacement note`.

5. View reset and session history

   1. Run `note 1 Before undo`, `note 1 After undo`, then `undo`.<br>
      Expected: The full list appears. Re-find the contact: its note is `Before undo`.
   2. Run `redo`.<br>
      Expected: The full list appears again. Re-find the contact: its note is `After undo`.
   3. Restart, run `find NoteHistoryCheck`, then `shownote 1`, `undo` and `redo`.<br>
      Expected: `After undo` is saved, but session history is empty and both history commands report no available change.

<!-- @@author -->

### Tracking contact update dates

Use a disposable test copy in a separate folder. Close the app before changing its `data/unimate.json`
and retain a backup outside the `data` folder. These checks cover the date itself; reminder boundaries
are covered under [Testing derived reminders](#testing-derived-reminders).

Before each indexed `edit`, `note`, `fav` or `tag` command below, run `find DateCheck` and confirm
the test contact is the only result. An `edit` returns to the full list even when its values are unchanged,
so repeat the search before the next indexed command.

1. Add a test contact using `add n/DateCheck a/NUS p/93456789`, run `find DateCheck` and inspect its card.
   Expected: `Last updated: YYYY-MM-DD` shows your computer's current date even without an `update` reminder.
   Hover over the date: its tooltip explains that basic fields affect it, while tags, notes and favourites do not.
<br>
2. Close the app, back up `data/unimate.json`, and set only that test contact's `lastUpdated` to `2024-02-29`.
   Relaunch and run `find DateCheck`.
   Expected: The saved date is displayed exactly; the `update` reminder appears if it is over one calendar year old.
<br>
3. Run `edit 1 p/93456789`, then `find DateCheck`, then `note 1 Follow up`, `fav 1` and `tag 1 t/DateDemo`.
   Expected: The date remains `2024-02-29`. Re-entering the same phone and changing annotations do not refresh it.
<br>
4. Run `edit 1 p/94567890`, then `find DateCheck`.
   Expected: The card shows today's date, the phone changes, and an outdated `update` reminder disappears.
<br>
5. Run `undo`, then `find DateCheck`.
   Expected: The original phone and `2024-02-29` return. Run `redo` and `find DateCheck`:
   the new phone and the edit date return.
<br>
6. Run `edit 1 p/94567890` again and try `edit 0 p/95678901`.
   Expected: The unchanged edit and failed input leave the saved date unchanged.
<br>
7. Restart the app from the same folder and run `find DateCheck`.
   Expected: The edited phone and saved date remain.
<br>
8. Close the app and remove only DateCheck's `lastUpdated` property from the disposable JSON file.
   Relaunch, run `find DateCheck`, then `list` and close the app. Relaunch and locate DateCheck again.
   Expected: The date shows `Unknown` on both launches. Viewing and saving do not invent a date.
   Repeat with `"lastUpdated": null` instead of an omitted property; the result is the same.
<br>
9. With DateCheck still showing `Unknown`, run `find DateCheck`, then `edit 1 p/94567890`.
   Expected: The phone is unchanged, so the date remains `Unknown`. Run `find DateCheck`, then
   `edit 1 p/` to clear its existing phone.
   Expected: The phone becomes `Not specified` and the date becomes today's date, because clearing
   a specified basic field is a real change. Restart and locate DateCheck to confirm both were saved.
   Remove the test contact or restore the backup with the app closed afterwards.

### Saving data

Use only the separate test folder for these checks. The paths below are relative to the folder from which you run `java -jar unimate.jar`. Close the app before changing data files. Keep a backup outside the `data` folder and restore it with the app closed after each scenario.

1. Saving and reloading a contact

   1. Test case: `add n/SaveCheck p/91234567 e/save@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/StudyGroup`.
   2. Close and relaunch the app from the same folder, then run `find SaveCheck`.
      Expected: The contact retains all details, including its major, class year and tag.
   3. Close the app and back up `data/unimate.json` before continuing.
<br>
2. Missing data file

   1. With the app closed, move the test copy's `data/unimate.json` to a backup location so that no file remains at that exact path.
   2. Relaunch the app.
      Expected: The sample contacts appear. The backed-up contacts are not loaded, and no data file has been written yet.
   3. Run `list`.
      Expected: `data/unimate.json` is created with the sample contacts. Close the app and restore the backup before the next scenario.
<br>
3. Malformed JSON

   1. With the app closed and the valid backup retained, replace the contents of the test copy's `data/unimate.json` with `not valid JSON`.
   2. Relaunch the app without entering a command.
      Expected: The app opens with an empty contact list. The malformed file remains unchanged on disk; the loading error is recorded in the application log.
   3. Run `list`.
      Expected: The command succeeds and replaces the malformed file with valid JSON containing an empty person list. Close the app and restore the valid backup.
<br>
4. Valid JSON containing an invalid contact

   1. With the app closed and the valid backup retained, change `SaveCheck`'s `graduationYear` value to `"1999"` in the test file. Keep the JSON syntax valid.
   2. Relaunch the app without entering a command.
      Expected: The whole data file is rejected and the app opens with an empty contact list. It does not partially load the other contacts or silently correct the invalid year. The invalid file remains unchanged at this point.
   3. Close the window or run `exit` without changing any contacts, then restore the valid backup.
      Expected: The invalid file remains unchanged; exiting an unchanged session does not save contacts.
<br>
5. Null entries and null JSON roots

   1. With the app closed, use a disposable test folder and retain the valid backup. Test each of these
      file contents separately in `data/unimate.json`:

      * `{"persons":[null]}`
      * `{"persons":[{"name":"Null Tag","address":"NUS","tags":[null]}]}`
      * `null`
   2. Relaunch the app without entering a command.
      Expected: The GUI opens with an empty contact list, and the loading error is logged.
      For the first two files, the log identifies `Contact entry 1 is null.` and
      `Contact entry 1 is invalid: Tag entry 1 is null.`, respectively.
      The original invalid file remains unchanged.
   3. Close the window using its close button and inspect the file.
      Expected: It still contains the original invalid data. Restore the backup before the next case.
   4. Repeat with a valid contact followed by a null entry in the same `persons` array.
      Expected: The whole file is rejected; the valid contact is not partially loaded. The logged
      error is `Contact entry 2 is null.`.
   5. Repeat with a valid contact followed by a contact whose `tags` array is `["friends",null]`.
      Expected: The whole file is rejected, and the log identifies
      `Contact entry 2 is invalid: Tag entry 2 is null.`. Positions count entries from 1 in saved-file order.
   6. Separately test `{"persons":[]}` and a valid older contact with omitted optional fields.
      Expected: The empty list loads normally, and the older contact loads with unspecified optional details.
<br>
6. Loading a file saved under the old filename

   1. With the app closed and the valid backup retained, place a copy of the valid contact file at `data/addressbook.json` and ensure `data/unimate.json` is absent. If the old filename is already in use, perform this scenario in another test folder.
   2. Relaunch the app without entering a command.
      Expected: Sample contacts appear. UniMate reads `data/unimate.json` and does not automatically migrate `data/addressbook.json`.
   3. Close the window using its close button. Copy `data/addressbook.json` to `data/unimate.json`, then relaunch and run `find SaveCheck`.
      Expected: The saved contact and its academic fields and tag load correctly. The old file is unchanged. Restore the backup when finished.

<br>
7. Help, exit and pending changes with read-only contact storage

   1. With the app closed and a valid disposable contact file backed up, make only that file read-only
      using the permission steps below. Relaunch without changing any contacts.
   2. Run `help`, close the Help window, then run `exit`.
      Expected: Help opens and exit closes the app without a contact-save error.
   3. Relaunch with the file still read-only. Run `add n/SavePending a/NUS`, using an unused name,
      then run `help`.
      Expected: The add shows a saving error but keeps the contact in memory; Help still opens.
   4. Close Help and run `exit`.
      Expected: The main window stays open and explains that contact changes remain unsaved.
   5. Restore write permission while the app remains open, then run `exit` again and relaunch.
      Expected: Exit saves successfully; `find SavePending` retrieves the saved contact.
      Close the app and restore the backup before continuing.

### Testing a permission-denied save

**Result:** Passed on macOS, Linux and Windows (v1.5 development build). On macOS, the permission
message appeared, `PermCheck` was shown in memory, and the read-only file was left unchanged.

Use a disposable test folder only.

1. Launch the app and run `list` once so that `data/unimate.json` exists, then close the app.
2. Make the data file read-only:
   * macOS/Linux: run `chmod 444 data/unimate.json` from the test folder.
   * Windows: right-click `data\unimate.json`, choose **Properties**, tick **Read-only**, then click **OK**.
3. Relaunch the app and run `add n/PermCheck a/NUS`.
   Expected: The contacts load normally at startup. The command shows `Could not save data to file ...
   due to insufficient permissions to write to the file or the folder.`, followed by the explanation that
   the change is applied but not saved. `PermCheck` appears in the list.
4. Close the app and open `data/unimate.json` in a text editor.
   Expected: It does not contain `PermCheck`.
5. Make the file writable again (`chmod 644 data/unimate.json`, or untick **Read-only** on Windows).
   Relaunch, run `add n/PermCheck a/NUS`, close and relaunch.
   Expected: The command succeeds, and `PermCheck` is still present after relaunching.

### Testing save-failure recovery

Use only a disposable copy with a valid backup; this scenario intentionally blocks writing.

1. Launch normally and run `list` once to create `data/unimate.json`. Keep UniMate open.
   Move that file to a safe backup filename and create an **empty directory** named `unimate.json`
   at its former path. This blocks saving without relying on OS-specific permission commands.
<br>
2. Run `add n/SaveFailureCheck a/NUS`.
   Expected: A save error replaces success feedback, but the contact appears in memory. The error
   explains that the change is unsaved and that `undo` can revert it. The command text remains for correction.
<br>
3. Replace the input with `undo`, then `redo`, while the directory still blocks saving.
   Expected: Undo removes the contact in memory, restoring the saved state, so its save error has no
   unsaved-change note. Redo restores the contact in memory and reports another save error naming
   `undo` as the reversal. No successful save has occurred.
<br>
4. Remove only the empty blocking directory and restore the backed-up file to its original path.
   Run `list`, then restart and run `find SaveFailureCheck`.
   Expected: `list` saves the current in-memory contacts successfully. The newly added contact is
   present after restart. Retain the original backup until the whole scenario is finished.

For startup loading failures, use the separate [saving-data scenarios](#saving-data): close the
window without commands before repairing a malformed file. That is different from this deliberately
blocked save while a valid session is already running.

--------------------------------------------------------------------------------------------------------------------

## Appendix: Planned enhancements

Team size: 6

Each item below is a planned fix for a limitation listed in
[Appendix: Known limitations](#appendix-known-limitations).

1. **Make email validation stack-safe for extremely long input.**
   `Email.VALIDATION_REGEX` uses nested repeated groups such as `([+_.-][^\W_]+)*`. Java's regex
   engine matches these recursively, so an email with thousands of repeated segments (e.g.,
   `"a."` repeated 5,000 times followed by `a@example.com`) throws `StackOverflowError`. The exact
   threshold depends on the thread stack size and JVM, so no fixed limit is documented. Such a value
   can be accepted when added in the UI and saved, but the same check then fails while
   `JsonAdaptedPerson#toModelType` loads the file, so the next launch terminates in `MainApp#init`.
   The input is far outside any real email address, so this is a robustness limitation rather than a
   normal-use failure.
   *Planned fix:* replace the repeated-group regex with an iterative check that splits the local part
   and domain on their separators and validates each segment, and add a test that saves an accepted
   long email, then launches the app in a fresh JVM against that saved file and checks that startup
   succeeds. A reload through another storage object in the same JVM is not enough, because the
   overflow depends on the thread stack and the real failure occurs in `MainApp#init`. Catching `StackOverflowError` alone is not
   planned, as it would hide the design problem.

2. **Check the final domain label's length separately from its syntax.**
   `DOMAIN_LAST_PART_REGEX` is `(label){2,}$`, which requires the label *pattern* to repeat at least
   twice, not the final label to have at least two characters. As a result, `a@x.y-z` and `a@a-b` are
   rejected although they satisfy the stated rules, while `a@ab-c` and `a@a-bc` are accepted. The
   command is rejected cleanly without changing data. The User Guide currently states the narrower
   accepted rule.
   *Planned fix:* match the final label with the ordinary label pattern and check its length with a
   separate condition, so the validator matches `Email.MESSAGE_CONSTRAINTS`.

3. **Warn about, and protect, a data file that fails to load.**
   When `Storage#readAddressBook` throws `DataLoadingException`, `MainApp#initModelManager` logs a
   warning and starts with an empty `AddressBook`, but the UI shows nothing. A later successful
   contact save overwrites the original file with the current in-memory contacts, destroying the user's
   only copy of it.
   *Planned fix:* show a startup message in the result display naming the file and the problem, and
   copy the original file to a backup name (e.g., `unimate.json.bak`) before any save replaces it.
   All-or-nothing contact validation is kept.

4. **Report, instead of silently removing, control characters in pasted input.**
   `Notes` rejects tabs and line breaks, but the command box is a single-line JavaFX `TextField`,
   which strips them from pasted text before `CommandBox#handleCommandEntered` runs. Pasting
   `Left<TAB>Right` into `note 1 ...` therefore saves `LeftRight`. The parser-level rejection still
   applies to text that reaches it by other routes. The User Guide documents the stripping.
   *Planned fix:* add a `TextFormatter` to the command box that replaces pasted tabs and line breaks
   with spaces, so words stay separated and the user can see the result before pressing Enter.

5. **Show a focus outline on the message area.**
   Ctrl+Tab moves focus to the `ResultDisplay` text area, but nothing on screen shows it. The
   `.result-display` rule in `DarkTheme.css` replaces the text area's background with one flat colour
   and zero insets, and JavaFX draws a text area's focus ring as part of that background. This falls
   short of the requirement that focus remain visible (non-functional requirement 3). Keyboard
   scrolling and Ctrl+Shift+Tab still work, and the User Guide explains how to check focus.
   *Planned fix:* add a `.result-display:focused` rule that draws a border in the existing
   `-fx-focus-color` (orange), matching the outline used for expandable contact values.

--------------------------------------------------------------------------------------------------------------------

## Appendix: Known limitations

This appendix lists every known limitation of the current release. Limitations users can meet are
also listed in the User Guide's [Known issues and limitations](UserGuide.md#known-issues-and-limitations).

### Limitations with a planned fix

These are described in detail, with their planned fixes, in
[Appendix: Planned enhancements](#appendix-planned-enhancements).

| Limitation | Planned enhancement |
| --- | --- |
| An extremely long email can overflow the regex stack, so a saved file stops the next launch. | 1 |
| A final domain label containing a hyphen needs two characters on one side, so `a@x.y-z` is rejected. | 2 |
| A data file that fails to load gives no warning, and a later successful save overwrites it. | 3 |
| The command box silently removes pasted tabs and line breaks. | 4 |
| The message area shows no focus outline. | 5 |

### Other limitations

1. **The history has no size limit.**
   * every recorded change stores a full copy of the contact list, with no automatic cap or eviction of the oldest snapshots
   * undo and redo still move entries between the stacks, and a new change clears the redo branch
   * memory use grows with the number of contacts times the number of changes in a session
   * this is acceptable for the expected size of a student's contact list; a future version could cap the number of entries and drop the oldest snapshot when the cap is reached

2. **A failed save does not roll back the change.**
   * contact-data commands commit to history before `LogicManager#execute` saves to storage; an `IOException` becomes a saving-related `CommandException`
   * the in-memory change and history remain, while the saved file is protected by the temporary-file replacement described under [Storage](#storage-component)
   * `LogicManager` compares the current immutable person list with its initial state or last successful save to identify pending changes
   * a failed changed command gets `MESSAGE_UNSAVED_CHANGE`, naming `redo` after `undo` and `undo` otherwise; a failed retry such as `list` gets `MESSAGE_PENDING_UNSAVED_CHANGES` when earlier changes still need saving
   * returning to the saved state through undo clears the pending-change condition, so no unsaved-change note is added in that case
   * `list` retries saving all current contacts; Help remains available, while Exit retries pending saves and stays open if they still fail
   * the user guide documents this under [Saving the data](UserGuide.md#saving-the-data)

3. **Ctrl+Tab into the message area is not implemented by UniMate.**
   * `ContactListNavigation#handleTab` consumes only plain Tab and Shift+Tab. It returns early when Ctrl, Alt or Meta is held, leaving Ctrl+Tab to JavaFX's default focus traversal.
   * JavaFX binds Ctrl+Tab and Ctrl+Shift+Tab to "next/previous control", because a text area normally keeps plain Tab for typing.
   * The default order follows `MainWindow.fxml`: command box, then `ResultDisplay`, then the contact list. `ResultDisplay`'s `TextArea` is focus-traversable by default, so Ctrl+Tab from the command box reaches it.
   * The route is fragile: reordering the layout, making the text area non-traversable or consuming Ctrl+Tab in `handleTab` would remove it silently. No automated test covers it.
   * A future version could handle Ctrl+Tab explicitly in `ContactListNavigation` and add a GUI test for the command box → message area → command box route.

4. **Untested requirements.**
   * The one-second response target with 1,000 contacts (non-functional requirement 2) has not been measured end to end, including UI rendering.
