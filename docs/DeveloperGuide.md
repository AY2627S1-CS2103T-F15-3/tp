---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# UniMate Developer Guide

This guide describes the v1.4 implementation.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* UniMate is adapted from [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative. We reused and adapted its application architecture, baseline contact-management code, tests, documentation structure and UML diagrams.
* [JavaFX](https://openjfx.io/) provides the desktop UI, and [Jackson](https://github.com/FasterXML/jackson) handles JSON persistence.
* [JUnit 5](https://docs.junit.org/5.14.4/overview.html) provides the automated testing framework.
* [MarkBind](https://markbind.org/) generates the documentation website, and [PlantUML](https://plantuml.com/) renders the UML diagrams.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores UniMate contact data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
  Each `Person` includes `Major`, `GraduationYear` and 'Notes' value objects in addition to its existing contact details and tags.
* selects contacts using a `FilteredList<Person>` and orders that view using a `SortedList<Person>`.
  It exposes the sorted view as an unmodifiable `ObservableList<Person>` shared by the UI and numbered
  commands. Both transformations observe the saved contact list, so data changes update the display.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* keeps an in-memory history of contact data states in an `AddressBookHistory` object. The `undo` and `redo` commands use it to revert and reapply successful `add` and `edit` changes. See [Recording contact data history](#recording-contact-data-history).
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in the contact list model, and each `Person` references tags from that list. This lets UniMate maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F15-3/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both UniMate contact data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAdaptedPerson` stores each contact's academic fields as the JSON properties `major` and
`graduationYear`. Unspecified values are saved as empty strings. When loading older data files that
omit either property, it treats the missing value as an empty string and constructs an unspecified
`Major` or `GraduationYear`. Supplied non-empty values are validated before the contact is loaded.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Contact notes

`Person` stores an immutable `Notes` value. `Notes.EMPTY` represents an absent note, and existing constructors default to it. Supplied notes contain non-whitespace text on a single line, with no control characters and at most 500 Unicode code points. `Person#withNotes` returns a new contact while preserving all other fields. Notes participate in full equality and hashing, but not in contact identity. Existing `edit`, `tag` and `untag` replacement paths preserve them.

`JsonAdaptedPerson` saves the text under `notes`. Missing or null values in older files become empty notes. Invalid stored note text produces the usual data-loading failure. The UI's presence indicator derives from `Person#getNotes().isEmpty()` and updates when the observable contact list replaces a person; note text is displayed through ordinary command feedback.

`AddressBookParser` routes `note`, `shownote` and `delnote` to their respective parsers. `NoteCommandParser` splits the first index from the remaining text and validates that text without interpreting field prefixes. `ShowNoteCommandParser` and `DeleteNoteCommandParser` accept only an index. Each command resolves the index against `Model#getFilteredPersonList()` and preserves the current filter.

`NoteCommand` overwrites the contact's note. `DeleteNoteCommand` replaces it with `Notes.EMPTY`; deleting an already empty note reports an informational message. Both record actual data changes through the existing model history. `ShowNoteCommand` returns the note, or an empty-note message, in a standard `CommandResult`; it does not mutate contact data or history. `LogicManager` retains its normal save-after-success behaviour for all commands.

Note creation, replacement and deletion are supported by the implemented [`undo` and `redo` commands](#undo-and-redo-commands). Saving identical note text is a no-op for history: `AddressBookHistory#commit` compares the previous and resulting data and returns without adding an undo state or clearing redo states. Deleting an already empty note returns before changing the model or committing. Saving unchanged note text, deleting an already empty note, and viewing a note with `shownote` preserve any available redo history.

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
The command checks for an existing contact with the same name before adding the person to the model.
After a successful add, it records the previous contact data state in the model's history
(see [Recording contact data history](#recording-contact-data-history)).
The normal storage path then saves the contact, including its academic fields.

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

### Searching by academic fields

`FindCommandParser` splits the input into keywords and passes them to `NameContainsKeywordsPredicate`.
Despite its name, the predicate matches a `Person` if any keyword matches a full word in the person's
`Name`, `Major` or `GraduationYear`, using `StringUtil.containsWordIgnoreCase`. Matching is therefore
case-insensitive, whole-word and `OR`-based across all three fields. An empty `Major` or `GraduationYear`
never matches a keyword. `FindCommand` applies the predicate through `Model#updateFilteredPersonList`;
the model and storage format are unchanged.

### Filtering contacts by academic fields and tags

`FilterCommandParser` recognises the existing `m/`, `c/` and `t/` prefixes, requires at least one,
and rejects repeated criteria, a non-empty preamble and unsupported single-letter prefixes.
It uses `ParserUtil.parseMajor`, `parseGraduationYear` and `parseTag` to trim and validate supplied
values. An omitted criterion is represented by an empty `Optional`; a supplied blank value is invalid.
As with other prefixed commands, prefix-like tokens are reserved syntax rather than literal major text.

`ContactFilterPredicate` holds the optional `Major`, `GraduationYear` and `Tag` criteria.
Its `test(Person)` combines them using AND. Major and tag comparisons use full-value,
case-insensitive matching; graduation years use exact equality. A tag criterion succeeds if any of
that person's tags matches. This differs from `NameContainsKeywordsPredicate`, which uses OR
matching over whole-word keywords in names, majors and graduation years.

`AddressBookParser` routes `filter` to `FilterCommandParser`. `FilterCommand` then passes the predicate
to `Model#updateFilteredPersonList` and reports the size of `Model#getFilteredPersonList`.
The existing observable list updates `PersonListPanel`, including the displayed indices;
no additional controls or storage fields are needed. Commands that use displayed indices continue
to obtain their targets from that same list.

A filter replaces the previous `find` or `filter` predicate. `list` restores all contacts;
`add` and `edit` keep their existing behaviour of showing all contacts after success. Tag and untag
preserve the predicate, so a contact can disappear from the results when its tags no longer match.
Filtering changes no contact data and records no history entry, preserving undo and redo availability.
The active sort is preserved when the filter changes. Filters are not persisted and the full list
is shown on restart.

### Sorting the displayed contacts

`AddressBookParser` routes `sort` to `SortCommandParser`. The parser requires exactly two lowercase
tokens: a field (`name`, `major`, `year`) and a direction (`asc`, `desc`). It constructs `SortCommand`
with `PersonSortComparator.SortField` and `SortDirection` values. Invalid input raises a `ParseException`
with usage instructions before any view or data change.

`SortCommand#execute` supplies a `PersonSortComparator` to `Model#updateSortedPersonList`.
`ModelManager` maintains this observable transformation chain:

```text
AddressBook / UniquePersonList -> FilteredList -> SortedList -> UI and numbered commands
```

`getFilteredPersonList()` retains its existing API name and now returns the sorted view. Consequently,
edit, delete, tag and untag obtain the same indexed `Person` that is shown on the matching contact card.
The `SortedList` object is retained for the model's lifetime, keeping existing UI observers attached.
Changing its comparator preserves the `FilteredList` predicate; changing the predicate preserves sorting.

The comparator uses case-insensitive full-value ordering for names and majors. Graduation years are
four-digit strings, so their lexicographic order is chronological. Empty academic values become null
sorting keys. Direction is applied to the value comparator **before** `Comparator.nullsLast`, ensuring
that descending order does not move unspecified values to the front. Equal primary values use ascending
case-insensitive name order; names differing only in case compare equally.

Sorting the view rather than `UniquePersonList` preserves storage order and history snapshots. As with
other successful commands, `LogicManager` still invokes storage, but the saved contacts remain in their
underlying order. Sort commands do not call `commitAddressBook`, so undo and redo availability is preserved.
Add/edit/undo/redo retain their existing behaviour of clearing the filter, while keeping the comparator.
Replacing, adding or removing source contacts automatically updates their sorted positions.
Restarting constructs a view without a comparator or predicate, restoring saved order and all contacts.

Comparator, model, command and parser unit tests cover ordering and validation. `SortCommandIntegrationTest`
checks command interactions, notes preservation and JSON persistence; `SortUiTest` checks the real command
box, rendered card numbering and feedback. See [Sorting contacts](#sorting-contacts) for manual checks.

### Formatting academic fields in command messages

Commands that report a contact's full details use `Messages#format(Person)`. The formatter includes `Major` and
`Class year` before tags when those optional academic fields are specified. If either academic field is unspecified,
that field is omitted from the command message.

This centralizes command-message formatting for add, edit, and delete results while preserving the independent
optional behavior of major and class year.

### Deleting contacts with academic fields

`DeleteCommand` identifies the target `Person` from `Model#getFilteredPersonList()` using the displayed index.
It then calls `Model#deletePerson`, which removes the entire `Person` object from the `AddressBook`, including
the stored `Major`, `GraduationYear`, and tags.

After deletion, `DeleteCommand` uses `Messages#format(Person)` to include the deleted contact's details in the
command result.

### Viewing previously added contacts

The `list` command exposes all contacts in the current address book, including persons loaded by
`JsonAddressBookStorage` on startup. It reuses the existing observable view rather than loading a second copy or
maintaining a separate list of earlier additions. Deleted contacts are absent from that address book and are not restored.

`AddressBookParser` dispatches `list` only when its argument string is blank; additional input raises a
`ParseException` with `Messages.MESSAGE_INVALID_COMMAND_FORMAT` and `ListCommand.MESSAGE_USAGE` before execution.
Surrounding whitespace is accepted, and the command word remains case-sensitive.

`ListCommand#execute` applies `Model.PREDICATE_SHOW_ALL_PERSONS` through `Model#updateFilteredPersonList`, replacing
the predicate established by `find`, `filter` or `listfav`. It then reads the restored view's size and returns
`ListCommand.MESSAGE_SUCCESS` followed by a newline and `Messages.MESSAGE_PERSONS_LISTED_OVERVIEW`. Calculating the
count after replacing the predicate ensures feedback describes every restored contact, even when the previous view
was empty. An empty address book reports zero successfully.

`ModelManager` exposes a `SortedList<Person>` over its `FilteredList<Person>`, so changing only the predicate retains
the active comparator. The saved contact order is untouched. `PersonListPanel` observes this final view and creates
cards with one-based display indices. Existing cards provide contact details and reminders; listing does not replace
persons or alter their update dates.

The command does not commit contact history, consume undo steps or discard redo states. `LogicManager` still uses its
existing save-after-success path, retaining the same contact data on disk. On reopening, storage reconstructs the saved
contacts and a new model starts without temporary filters or sort choices.

`ListCommandTest` covers empty and populated lists, restoring search/filter/favourite results, active sort preservation,
unchanged data and undo/redo availability. `AddressBookParserTest` checks whitespace and extra-argument rejection.
`LogicManagerTest` saves contacts, reopens their JSON through a fresh model, searches and lists them again, then verifies
both displayed and persisted contacts. See [Listing all persons](UserGuide.md#listing-all-persons-list).

### Displaying academic fields in the contact list

`ListCommand` resets the model's filtered person list to `PREDICATE_SHOW_ALL_PERSONS`. `PersonListPanel` observes
that list and creates a `PersonCard` for every visible `Person`, so the card layout determines which fields appear
after `list` as well as in filtered results produced by `find`.

`PersonCard` reads the existing `Major` and `GraduationYear` value objects and formats them as labeled rows.
`PersonListCard.fxml` places those rows below the contact details. Because both fields are optional, an empty value
is rendered as `Not specified` instead of leaving an ambiguous blank line. This is a presentation-only change:
`ListCommand`, the model, and the JSON storage format are unchanged.

### Automatic incomplete reminders

#### Class structure

<puml src="diagrams/IncompleteReminderClassDiagram.puml" alt="Class structure of automatic incomplete reminders" />

`Person` references one non-null `Phone` and one non-null `Email`; an empty value represents an unspecified contact method. The parsers implement the generic `Parser` interface and map omitted add fields or cleared edit fields to the corresponding `EMPTY` constants. `SystemTag` depends on `Person#isIncomplete`, while `PersonCard` uses the derived result for rendering. The JSON adapter reconstructs contact values rather than storing system reminders. The `Phone` and `Email` associations are not compositions because immutable values, including `EMPTY`, can be shared between contacts.

#### Design overview and responsibilities

Incomplete reminders reuse the dynamic system-tag pipeline without adding a stored tag or a command. `Person#isIncomplete` returns true only when both `getPhone().value` and `getEmail().value` are empty. Address, academic fields, user tags, notes and favourite status are irrelevant to this predicate.

| Class | Responsibility |
|-------|----------------|
| `Phone`, `Email` | Represent unspecified contact methods using immutable `EMPTY` values containing an empty string. Existing regexes still validate every non-empty value. |
| `Person` | Evaluates contact completeness without modifying contact state. |
| `AddCommandParser`, `ParserUtil` | Allow omitted phone/email prefixes, but reject explicitly blank or malformed supplied values in `add`. |
| `EditCommandParser`, `EditCommand` | Map empty `p/` and `e/` values to `EMPTY`, preserve omitted fields, and apply the existing basic-profile date rules. |
| `SystemTag` | Independently appends `UPDATE` and `INCOMPLETE` in `forPerson`, and identifies custom-name collisions using `isSystemLabel`. |
| `JsonAdaptedPerson` | Converts absent/null phone and email fields to empty values while retaining validation of supplied non-empty values. |
| `PersonCard`, `PersonListPanel`, `MainWindow` | Render and refresh system labels using the existing observable contact list and successful-command refresh path. |

#### Add/edit behaviour and validation

Phone and email objects remain non-null throughout the model. `Phone.isValidPhone` and `Email.isValidEmail` accept exactly the empty string in addition to their previous valid values; whitespace-only values remain invalid at the model layer. `ParserUtil.parsePhone` and `parseEmail` still reject a trimmed empty value, preserving malformed-input feedback when an optional prefix is explicitly supplied to `add`.

`AddCommandParser` requires name and address, maps omitted phone/email prefixes to `Phone.EMPTY` and `Email.EMPTY`, and continues to reject repeated single-valued prefixes. `EditCommandParser` uses the same clearing convention as major/class year: an empty prefix supplies `EMPTY`, while an omitted prefix leaves the descriptor field unset. All non-empty values still pass through `ParserUtil` and the original regex validation. An invalid value rejects the whole command before mutation.

`EditCommand` already compares phone/email as basic fields, so filling or clearing either changes `lastUpdated` only when its value changes. Existing person replacement preserves favourite status, tags, notes and academic fields. Notes, favourite and tag commands retain the date. Sorting, searching and list commands retain their existing view-only behaviour.

#### System tags, rendering and refresh

`SystemTag.forPerson` evaluates the age and completeness predicates independently and returns at most one of each reminder. A profile may therefore have both labels. `PersonCard#refreshTags` clears and rebuilds the pane, preserving ordinary blue labels, and appends each derived reminder with the existing `system-tag` CSS class. The existing yellow background (`#F4C542`), dark text (`#222222`), padding, font and corner radii are reused without changing favourite or note icons. Missing phone/email display as `Not specified`.

`SystemTag.isSystemLabel` distinguishes both reserved display names. A user's stored `incomplete` tag is shown as blue `incomplete (custom)` with a tooltip explaining `t/incomplete`; it is neither renamed nor removed from JSON. Tag commands still operate only on user tags. The incomplete reminder has a contact-detail tooltip rather than the date tooltip used by `UPDATE`.

Successful commands use the existing `MainWindow` to `PersonListPanel.refresh` path, and model replacements update the observable list. Filling either contact method removes `INCOMPLETE`; clearing the last one adds it. A successful actual basic-field edit also refreshes the profile date and can independently remove `UPDATE`. If the edit leaves both methods empty, `INCOMPLETE` remains applicable.

#### JSON compatibility and design considerations

The JSON shape is unchanged: phone and email remain strings and empty strings represent unspecified values. Missing/null values load as unspecified; malformed non-empty strings still fail validation. Older complete contacts load as before, and name/address and other field validation are unchanged. Favourite, notes, tags and `lastUpdated` retain their existing serialization and copy rules.

Derived reminders require no migration flag or synchronization with a stored system-tag collection. Reusing the existing value objects avoids nullable fields throughout commands and UI. Keeping custom tags separate preserves user data and prevents `untag` from permanently disabling a reminder. Tests cover all four combinations, fill/clear transitions, failed input atomicity, legacy JSON, both reminders together and actual JavaFX command-box rendering. See [Automatic incomplete reminders](UserGuide.md#automatic-incomplete-reminders).

### Automatic update reminders

#### Class structure

<puml src="diagrams/UpdateReminderClassDiagram.puml" alt="Class structure of automatic update reminders" />

`EditCommand` extends `Command`, holds a copied `EditPersonDescriptor` and a `Clock`, and replaces a person through the `Model` interface. The private `createEditedPerson` method determines whether a basic field actually changed. `Person` stores the date; `SystemTag` queries that date through `needsUpdate`; `PersonCard` renders the resulting label. `JsonAdaptedPerson` converts between the model date and its JSON string. The dashed dependencies on `SystemTag` do not imply a stored collection of reminders in `Person`.

#### Design overview and relevant classes

The reminder is derived from a contact's basic-profile modification date rather than stored as a user tag. This keeps reminders consistent with the current date without requiring a new command or background scheduler.

| Class | Responsibility |
|-------|----------------|
| `Person` | Stores immutable `LocalDate lastUpdated`, exposes `getLastUpdated()`, and evaluates `needsUpdate(LocalDate)`. |
| `SystemTag` | Returns applicable derived reminders through `forPerson(Person, LocalDate)`, including `UPDATE` and `INCOMPLETE`. |
| `EditCommand` | Detects actual basic-field changes and obtains today's date from an injectable `Clock`. |
| `TagCommand`, `UntagCommand`, `FavCommand`, `UnfavCommand` | Carry the previous date through contact replacement. |
| `JsonAdaptedPerson` | Stores an ISO date string and converts it back to `LocalDate`, including legacy-data handling. |
| `PersonCard` | Rebuilds ordinary labels and system reminders using its injectable clock. |
| `PersonListPanel`, `MainWindow` | Refresh visible cards after successful command execution, including commands that leave model data unchanged. |

#### Date tracking and persistence

The existing `Person` constructor overloads initialize unspecified dates using `LocalDate.now()`. The full constructor accepts notes, favourite status and an explicit date, allowing immutable copies to retain all independent contact state. The date participates in equality, hashing and contact history snapshots. `Person#withNotes` preserves it.

`EditCommand#createEditedPerson` compares the resulting name, phone, email, address, major and graduation year with the original values. If at least one differs, it uses `LocalDate.now(clock)`; otherwise it retains the existing date. Tag-only edits and unsuccessful edits do not reset the date. Date injection makes anniversary tests reproducible. Tag, note and favourite changes retain the previous date, while read-only commands only change the view or feedback. Existing undo/redo restores the full recorded person, including the date.

`JsonAdaptedPerson` serializes `lastUpdated` as an ISO string such as `2026-10-18` and parses it with `LocalDate.parse` on loading. Missing or null dates use the current loading date as a migration baseline, not a historical claim. Normal saving persists that baseline for subsequent loads. Invalid supplied dates raise `IllegalValueException`, which the existing storage loading path reports as invalid data. Notes, favourite status and ordinary tags retain their existing JSON properties and validation.

#### Reminder logic

`Person#needsUpdate(today)` requires the saved date to precede today and its one-year anniversary to be strictly before today. Exactly one year produces no reminder. Calendar-year arithmetic uses `lastUpdated.plusYears(1)`, so a leap-day anniversary follows Java's calendar rule: `2024-02-29` reaches its anniversary on `2025-02-28`, with a reminder starting on `2025-03-01`.

`SystemTag.forPerson` appends one `UPDATE` reminder when the age condition applies, independently of `INCOMPLETE`. It does not mutate `Person#getTags` or write automatic labels to JSON. This representation can accommodate other system reminders later.

#### GUI implementation

`PersonCard#refreshTags` clears the tag pane, recreates the sorted user labels and appends the current `SystemTag` labels. Each system label has the `system-tag` style class and a tooltip showing the recorded date. `#tags .label.system-tag` in `DarkTheme.css` overrides only background (`#F4C542`) and text (`#222222`), inheriting ordinary tag padding, font size and corner radii. Favourite and note icons remain independent.

An ordinary tag whose exact stored name is `update` is rendered as blue `update (custom)`, with a tooltip explaining that it is managed using `t/update`. The underlying name, validation, filtering and storage remain unchanged. Rebuilding the pane prevents duplicate system labels and lets a custom tag coexist with the reminder.

After `LogicManager#execute` succeeds, `MainWindow` calls `PersonListPanel#refresh`, which calls `ListView#refresh`. Recreated cards evaluate the current date again even when the contact data did not change. An actual basic-profile edit both replaces the model person and triggers this refresh, removing an outdated reminder immediately. No timer refreshes an idle window.

#### Design considerations and verification

Dynamic reminders avoid stale stored tags, prevent user tag removal from disabling a date-based reminder, and preserve legacy custom tags without silently deleting data. Dates remain separate from favourite state and notes; future commands that reconstruct a person must carry all three forward unless intentionally updating one.

Tests cover calendar boundaries, profile edits and no-op edits, JSON round trips and migration, note/tag/favourite interactions, undo/redo and actual command-box rendering. GUI tests run on the JavaFX application thread; the existing Linux CI virtual display ensures they execute when coverage is collected. See [Automatic update reminders](UserGuide.md#automatic-update-reminders) for user-facing behaviour.

### Favourite contacts

#### Class structure

<puml src="diagrams/FavouriteContactsClassDiagram.puml" alt="Class structure of favourite contacts and list filtering" />

The four commands inherit from `Command` and use the `Model` interface. `FavCommand` and `UnfavCommand` replace immutable persons with a changed favourite flag; `ListFavCommand` and the existing `ListCommand` change only the view predicate. `ModelManager` implements `Model` and owns the filtered and sorted views: the sorted view uses the filtered view as its source, and displayed indices follow that final view. `JsonAdaptedPerson` converts the favourite flag, and each `PersonCard` references the person whose heart it displays. Unrelated person fields and model services are omitted from this feature view.

#### Design overview and relevant classes

Favourite contacts use the existing command, model, storage and UI components. `Person` holds an immutable `boolean favourite`, exposed through `isFavourite()`. It defaults to `false` in the existing constructor; an overloaded constructor accepts an explicit state. Favourite status participates in `equals` and `hashCode`, while `isSamePerson` continues to identify contacts by name.

| Class | Role in this feature |
|-------|----------------------|
| `AddressBookParser`, `FavCommandParser`, `UnfavCommandParser` | Dispatch commands and validate displayed indices using `ParserUtil.parseIndex`. `listfav` argument validation is handled directly in `AddressBookParser`; there is no separate `ListFavCommandParser`. |
| `FavCommand`, `UnfavCommand` | Check the selected person's state and replace that person with a copy containing the new favourite value. |
| `ListFavCommand`, `ListCommand` | Select the favourite predicate or restore the existing show-all predicate. |
| `Model`, `ModelManager` | Expose the displayed list, replace stored persons, and apply predicates to the existing `FilteredList<Person>`. |
| `LogicManager`, `JsonAdaptedPerson` | Save successful command results through the existing storage path and convert the favourite value between JSON and `Person`. |
| `PersonCard`, `PersonListPanel` | Display the state and rebuild visible list-cell graphics when model contents change. |

#### Command execution flow

For `fav 1`, `AddressBookParser` selects `FavCommandParser`, which parses a one-based index using `ParserUtil.parseIndex` and constructs a `FavCommand`. During execution, the command reads `Model#getFilteredPersonList`, checks the list bounds, then checks `Person#isFavourite`. An already favourited person produces a `CommandException` rather than a replacement.

For a valid state change, the command constructs a new `Person` from the original name, phone, email, address, major, graduation year, tags and notes, with `favourite` set to `true`. `Model#setPerson` replaces the stored person, and the result starts with `Added to favourites: ` followed by `Messages.format` output. `unfav` follows the same path through `UnfavCommandParser` and `UnfavCommand`, requires an existing favourite, sets the value to `false`, and returns `Removed from favourites: `.

Invalid index syntax is reported as a `ParseException` containing the corresponding command's usage. An index beyond the current view produces `The person index provided is invalid.` Repeated state changes produce `This person is already a favourite.` or `This person is not a favourite.` Neither command resets the current filter.

`AddressBookParser` accepts `listfav` only when its argument string is blank. `ListFavCommand#execute` calls `Model#updateFilteredPersonList(Person::isFavourite)` and reports the resulting count, or `No favourite contacts found.` This replaces any previous predicate. The existing `ListCommand` restores `PREDICATE_SHOW_ALL_PERSONS`. Both commands reuse the same filtered view; neither edits or deletes a contact. The source list order is retained.

Because `fav` and `unfav` resolve their targets from that view, `unfav 1` after `listfav` acts on the first displayed favourite. Replacing it with a non-favourite causes the `FilteredList` to remove it immediately and reindex the remaining results. Existing `add` and `edit` behaviour still restores the full list; `tag` and `untag` retain the current predicate.

#### Data persistence

`JsonAdaptedPerson` writes the `favourite` boolean alongside the existing contact fields. Its JSON creator accepts a nullable `Boolean` and uses `Boolean.TRUE.equals(favourite)`, so missing or null values in older data become `false`. Conversion back to the model uses the constructor accepting the favourite state. No favourite state is stored in ordinary tags.

`LogicManager#execute` saves the address book after successful execution through the existing `Storage` API. As with other commands, a storage exception is reported after the in-memory model has changed; this path does not roll back the replacement.

`EditCommand`, `TagCommand` and `UntagCommand` explicitly pass the original `person.isFavourite()` when reconstructing a person. This prevents unrelated updates from resetting the state. When adding future contact fields or commands that copy a `Person`, preserve both the favourite state and all other unaffected fields in that copy and in JSON conversion.

Notes and favourite status are preserved together by the combined `Person` constructor and JSON creator. `Person#withNotes` carries the existing favourite value through note changes, so note commands and their undo/redo history retain it.

#### GUI implementation

`PersonListCard.fxml` defines a `favouriteIcon` label next to the name, containing the solid heart character (`♥`). `PersonCard` sets both its `visible` and `managed` properties from `person.isFavourite()`: ordinary contacts show no icon and reserve no icon space. The fixed 24-by-24 label is centred, with a small margin beside the name; it has no click handler.

`DarkTheme.css` styles `#favouriteIcon` with `#e63946` for both text and the one-pixel circular border, a radius of 12, and a transparent background. The ID selector overrides the general list-cell label colour, including for selected cells. Ordinary tag styles are unaffected.

The existing `PersonListPanel` observes the filtered list and creates a new `PersonCard` in `PersonListViewCell#updateItem`, using `getIndex() + 1` for numbering. Model replacement therefore updates the heart immediately; removing a person from the favourite filter updates the visible cards and indices without a separate favourite list or refresh command.

#### Design considerations

* **Separate state from tags:** A boolean gives favourite status one unambiguous value independent of tag names. Tag replacement or removal cannot accidentally remove favourite status, and no reserved custom tag name is needed.
* **Filter instead of copying or deleting data:** Reusing `FilteredList` keeps one source of contact data, preserves ordering, and lets `list` restore hidden contacts. It also gives indexed commands the same selection rule in every view.
* **Immutable replacement:** Commands reuse unchanged fields when constructing the replacement `Person`. This fits the existing observable model update path and avoids changes to unrelated contact attributes.

For user-facing formats and list-switching examples, see [Marking a favourite contact](UserGuide.md#marking-a-favourite-contact-fav), [Removing favourite status](UserGuide.md#removing-favourite-status-unfav) and [Listing favourite contacts](UserGuide.md#listing-favourite-contacts-listfav).

### Tagging and untagging contacts

#### Class structure

<puml src="diagrams/TagManagementClassDiagram.puml" alt="Class structure of tag and untag commands" />

The two parsers implement `Parser<T>` with different command types and create the corresponding `Command` subclasses. Both commands use `Model` to locate and replace the displayed person. Their requested tag sets, the person's stored tag set, and the five ordered default choices all reference the same ordinary `Tag` type. `ParserUtil` resolves default indices and validates custom names; dynamic `SystemTag` reminders are deliberately outside this structure. Navigable associations describe retained references, dashed arrows describe dependencies, and hollow triangles denote inheritance or interface realization. Static members are underlined; only feature-relevant members are shown in these four diagrams.

#### Current implementation

The `tag` command adds tags to a person in the currently displayed list. The `untag` command removes specified tags from that person. Both preserve the other contact fields and reuse the existing `Person` tag collection and storage format.

**Relevant classes:**

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

Given below is the flow for `tag 1 d/2 t/NOC`:

1. `LogicManager#execute` passes the input to `AddressBookParser#parseCommand`, which calls `TagCommandParser#parse`.
1. The parser tokenizes `d/` and `t/`. `ParserUtil#parseIndex` reads the preamble as a one-based person index. The parser requires at least one tag prefix.
1. `ParserUtil#parseDefaultTags` maps `2` to `Classmate` using `DefaultTags.TAGS`. `ParserUtil#parseTags` validates `NOC` through `parseTag`. The parser merges both sets and creates a `TagCommand`.
1. `TagCommand#execute` gets `Model#getFilteredPersonList` and checks that the index is within its size. Thus, index `1` selects the first displayed person, including when a search is active.
1. The command copies `person.getTags()` into a `HashSet` and calls `addAll` with the requested tags. It constructs a new `Person` with the original name, phone, email, address, major and graduation year, together with the updated set, then calls `Model#setPerson`.
1. The command returns a `CommandResult` containing `Tagged person: ` and `Messages.format(updatedPerson)`. `LogicManager` saves UniMate data before returning the result to the UI.

For `untag 1 t/NOC`, `AddressBookParser` selects `UntagCommandParser`. This parser recognizes only `t/`, requires at least one value, and uses `ParserUtil#parseTags` to construct the removal set. `UntagCommand#execute` follows the same person lookup and replacement flow, but calls `removeAll` on the copied set. Its result starts with `Untagged person: `.

Neither command calls `Model#updateFilteredPersonList`, so the current filter predicate is retained. The model's observable list exposes the updated person to the UI.

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

`Person#getTags` returns an unmodifiable view. Each command stores a defensive `Set.copyOf` of its input, copies the person's tags for the update, and replaces the `Person` rather than mutating its exposed set. `addAll` preserves existing tags; `removeAll` preserves tags not requested for removal. Duplicate additions and absent removals are no-ops that still return success. Tag order is not guaranteed. Equality remains case-sensitive, so `NOC` and `noc` are distinct.

**Aspect: Compatibility with existing editing**

The current `EditCommand` still supports replacing the entire tag set through `edit INDEX t/TAG`, and clearing it with `edit INDEX t/`. These operations have not been removed. `tag` and `untag` provide incremental changes; an empty `t/` is rejected by both.

For command formats and user-facing examples, see [Adding tags](UserGuide.md#adding-tags-to-a-person-tag) and [Removing tags](UserGuide.md#removing-tags-from-a-person-untag). Manual checks are listed under [Tagging and untagging a person](#tagging-and-untagging-a-person).

### Recording contact data history

#### Current implementation

The model keeps an in-memory history of contact data states. Successful `add` and `edit` commands record the state from before their change, and the model can restore earlier or later states. The user-facing `undo` and `redo` commands are built on this history (see [Undo and redo commands](#undo-and-redo-commands)). The history lasts only for the current session: it is not saved to storage, and a newly launched app starts with no undo or redo history.

**Relevant classes:**

| Class | Responsibility |
|-------|----------------|
| `AddressBookHistory` | Stores defensive `AddressBook` snapshots in an undo stack and a redo stack, and restores them into the tracked `AddressBook`. |
| `Model` | Declares `commitAddressBook(ReadOnlyAddressBook)`, `canUndoAddressBook()`, `canRedoAddressBook()`, `undoAddressBook()` and `redoAddressBook()`. |
| `ModelManager` | Creates one `AddressBookHistory` for its `AddressBook` and delegates the methods above to it. |
| `AddCommand`, `EditCommand`, `NoteCommand`, `DeleteNoteCommand` | Copy the contact data before changing it, then call `Model#commitAddressBook` with that copy. |
| `UndoCommand`, `RedoCommand` | Check history availability, restore the corresponding state through the model, and reset the displayed list to show all contacts. |

`AddressBookHistory` holds:

* `undoStates`: the contact data from before each recorded change, most recent on top.
* `redoStates`: the contact data that has been undone, most recent on top.
* `currentState`: a copy of the contact data the tracked `AddressBook` is expected to contain.

Every stored state is a copy made with `new AddressBook(...)`. `Person` and its fields are immutable, so later changes to the model cannot alter a stored snapshot.

#### Execution flow

Given below is the flow for a successful `add n/Alex Tan …​`:

1. `AddCommand#execute` checks `Model#hasPerson`. A duplicate throws a `CommandException` here, so nothing is recorded.
1. The command creates `previousState = new AddressBook(model.getAddressBook())`, then calls `Model#addPerson`.
1. The command calls `Model#commitAddressBook(previousState)`. `AddressBookHistory#commit` then:
   1. returns without recording if `previousState` holds the same contacts as the current data;
   1. clears `undoStates` if `previousState` differs from `currentState`, which means an unrecorded change happened earlier (see below);
   1. pushes a copy of `previousState` onto `undoStates`, clears `redoStates`, and sets `currentState` to a copy of the current data.
1. `LogicManager` saves the contact data to storage as before. The history itself is not saved.

The activity diagram below summarises what happens when a command commits:

<puml src="diagrams/CommitActivityDiagram.puml" width="300" alt="CommitActivityDiagram" />

A command that fails validation, such as a duplicate `add`, throws before copying the data and never reaches this flow.

`EditCommand#execute` follows the same pattern. It checks the displayed index and duplicate persons first, copies the data, calls `Model#setPerson`, and then commits. An edit that supplies the person's current values still returns the usual success message. Its commit is ignored because the data did not change, preserving both history stacks. The bare command `edit 1` is invalid because at least one field is required.

`NoteCommand` and `DeleteNoteCommand` use the same snapshot-and-commit flow for actual note changes. An unchanged note produces equal before/after data, so the commit is ignored and redo remains available. `DeleteNoteCommand` returns immediately for an empty note. A new data-changing commit clears redo states; merely viewing a note does not.

`Model#undoAddressBook()` pushes `currentState` onto `redoStates`, pops the top of `undoStates`, and restores it. `Model#redoAddressBook()` does the reverse. Restoring calls `AddressBook#resetData` on the existing `AddressBook`, so:

* the `AddressBook` object and the `FilteredList` built on it are the same objects before and after, and the UI updates through its existing observers;
* restoring a state does not record a new history entry.

Both methods return `true` if a state was restored. They return `false` and leave the data unchanged if there is nothing to restore.

The sequence diagram below shows how `Model#undoAddressBook()` works within the `Model` component:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

`Model#redoAddressBook()` follows the same sequence through `AddressBookHistory#redo()`, with the two stacks swapped: `currentState` is pushed onto `undoStates`, and the top of `redoStates` is popped and restored.

#### Example usage scenario

The diagrams below show how the history changes as a user enters a sequence of commands. Each `abN` is an `AddressBook` snapshot, and each stack is drawn with its top entry first.

Step 1. The user launches UniMate. `currentState` is a copy of the loaded contacts, `ab0`, and both stacks are empty.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user runs `add n/Alex Tan …​`. `AddCommand` commits `ab0`, the state before the add, so `ab0` is pushed onto `undoStates` and `currentState` becomes the new data, `ab1`.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user runs `edit 1 p/91234567`. `EditCommand` commits in the same way, pushing `ab1` onto `undoStates`. `currentState` becomes `ab2`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

Step 4. The user runs `undo`. `currentState`, `ab2`, is pushed onto `redoStates`, and `ab1` is popped from `undoStates` and restored.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />

Step 5. The user runs `redo`. This reverses the previous step: `ab1` is pushed back onto `undoStates`, and `ab2` is popped from `redoStates` and restored.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user runs `undo` again, returning to the state in Step 4, and then runs `add n/Beth Lim …​`. The new commit pushes `ab1` and clears `redoStates`, so the undone edit, `ab2`, can no longer be redone. `currentState` becomes `ab3`.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

Commands that do not change contact data, such as `list` and `find`, and commands that fail before changing anything, record nothing and leave all three parts of the history unchanged.

#### Commands that are not undoable

`delete`, `tag`, `untag` and `clear` change contact data but do not call `Model#commitAddressBook`. If an old snapshot were restored after one of them, it would silently reverse that change as well. For example, after `add` and then `delete 1`, undoing the add would also bring back the deleted contact.

To prevent this, `AddressBookHistory` compares `currentState` with the live contact data. If they differ, a change was made without being recorded, and the history is stale:

* `canUndoAddressBook()` and `canRedoAddressBook()` return `false`.
* `undoAddressBook()` and `redoAddressBook()` discard both stacks and return `false`, leaving the data unchanged.
* The next data-changing commit discards the stale undo states before recording the new one.

Continuing the example above, suppose the user now runs `delete 1`. `DeleteCommand` changes the contacts in the model to `ab4` without committing, so they no longer match `currentState`, `ab3`. The history is now stale: a following `undo` would discard both stacks and report that there is no change to undo, rather than restoring `ab1` and bringing the deleted contact back.

<puml src="diagrams/UndoRedoState6.puml" alt="UndoRedoState6" />

To the user, this means that after a `delete`, `tag`, `untag` or `clear` that changes contact data, `undo` reports that there is nothing to undo until another `add` or `edit` succeeds, and `redo` reports that there is nothing to redo until a later change is undone. A command that fails, or that leaves the data unchanged (such as `tag` with a tag the person already has), does not make the history stale.

These commands therefore need no history code, and any future command that changes data without committing is handled the same way.

#### Design considerations

**Aspect: What is stored for each change**

* **Alternative 1 (current choice):** Store a copy of the whole contact data state.
  * Pros: Easy to implement and to verify, and works the same for any command.
  * Cons: Uses more memory as the number of contacts and history entries grows.
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

**Aspect: How non-undoable commands affect history**

* **Alternative 1 (current choice):** Detect unrecorded changes by comparing the expected state with the live data.
  * Pros: `delete`, `tag`, `untag` and `clear` are not modified, and future commands cannot leave stale history by mistake.
  * Cons: Each history check compares the whole contact list. If an unrecorded change is later reversed exactly, the history becomes valid again, which is safe because the data is identical.
* **Alternative 2:** Have each non-undoable command explicitly clear the history.
  * Pros: Makes the behaviour visible in each command.
  * Cons: Every data-changing command must remember to do it, and `tag`, `untag` and `edit` all use `Model#setPerson`, so the model alone cannot tell them apart.

### Undo and redo commands

#### Current implementation

The `undo` and `redo` commands let users revert and reapply `add` and `edit` changes using the history described in [Recording contact data history](#recording-contact-data-history).

**Relevant classes:**

| Class | Responsibility |
|-------|----------------|
| `AddressBookParser` | Recognises the `undo` and `redo` command words. Throws a `ParseException` with `MESSAGE_INVALID_COMMAND_FORMAT` and the command's usage if any arguments follow the command word. |
| `UndoCommand` | Checks `Model#canUndoAddressBook()`, calls `Model#undoAddressBook()`, and shows all contacts. |
| `RedoCommand` | Checks `Model#canRedoAddressBook()`, calls `Model#redoAddressBook()`, and shows all contacts. |

Neither command has its own parser class, because neither takes any arguments.

#### Execution flow

Given below is the flow for `undo`:

1. `LogicManager#execute` passes the input to `AddressBookParser#parseCommand`. If anything follows the command word, such as in `undo 1`, a `ParseException` is thrown and the model is never reached.
1. The parser returns a new `UndoCommand`, and `LogicManager` calls `UndoCommand#execute(model)`.
1. The command checks `Model#canUndoAddressBook()`. If it returns `false`, the command throws a `CommandException` with `UndoCommand.MESSAGE_FAILURE`. The exception propagates out of `LogicManager#execute` before the save step, so neither the data nor the data file changes.
1. Otherwise, the command calls `Model#undoAddressBook()`, which restores the previous state in place. It then calls `Model#updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS)` so that every restored contact is visible, even if a `find` filter was active, and returns a `CommandResult` with `UndoCommand.MESSAGE_SUCCESS`.
1. `LogicManager` saves the restored contact data through `Storage#saveAddressBook`, as it does for every successful command.

The sequence diagram below shows these steps for `execute("undo")`, including the failure path when there is nothing to undo:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="Interactions inside the Logic component for the `undo` command" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

`redo` follows the same flow using `RedoCommand`, `Model#canRedoAddressBook()` and `Model#redoAddressBook()`.

Neither command calls `Model#commitAddressBook`. Restoring a state moves it between the undo and redo stacks without adding a new entry. Repeated `undo` therefore keeps moving back through earlier states, and `redo` does not clear the remaining redo states.

If saving fails after a successful undo or redo, the in-memory state has already been restored and `LogicManager` reports the file error, as it does for other commands. The history remains consistent with the in-memory data.

**Feedback messages:**

| Situation | Message |
|-----------|---------|
| `undo` succeeds | `Rollback to before the most recent change.` |
| `redo` succeeds | `Restored the most recently undone change.` |
| Nothing to undo, including stale history | `There is no change to undo. Only add and edit can be undone, and not after a delete, tag, untag or clear.` |
| Nothing to redo, including stale history | `There is no change to redo. Only an undone add or edit can be redone, and not after another change has been made.` |
| Arguments after `undo` or `redo` | `Invalid command format!`, followed by the command's usage |

#### Design considerations

**Aspect: Where the availability check happens**

* **Alternative 1 (current choice):** The command checks `canUndoAddressBook()` or `canRedoAddressBook()` first and throws a `CommandException` if it returns `false`.
  * Pros: The failure is reported through the usual command error path. The `boolean` returned by `undoAddressBook()` and `redoAddressBook()` only acts as a safety net.
  * Cons: Each command makes two model calls.
* **Alternative 2:** Call `undoAddressBook()` or `redoAddressBook()` directly and use the returned `boolean` to choose the message.
  * Pros: One model call.
  * Cons: The command's decision depends on the result of a call that may also discard stale history as a side effect, which is harder to follow.

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
| `AddressBookHistoryTest`, `ModelManagerTest` | Committing, undoing and redoing snapshots, ignoring unchanged commits, clearing redo states on a new commit, and stale history after unrecorded changes. |
| `AddCommandTest`, `AddCommandIntegrationTest`, `EditCommandTest` | Successful `add` and `edit` commands record exactly one history entry, while failed or unchanged ones record none. |
| `UndoCommandTest`, `RedoCommandTest` | Each command on its own against a model: success and failure messages, the order of repeated undos and redos, redo invalidation, and resetting the filtered list. |
| `AddressBookParserTest` | `undo` and `redo` are parsed, and extra arguments are rejected with the usage message. |
| `LogicManagerTest`, `UndoRedoIntegrationTest` | Full command strings through `LogicManager#execute`: parser, command, model history and JSON storage together. |

`UndoRedoIntegrationTest` starts each test from the typical contacts saved to a temporary data file. After every command, it checks the full contact list in the model, the contacts read back from the data file, and the feedback message. For failing commands, it also checks that `canUndoAddressBook()` and `canRedoAddressBook()` are unchanged. Its scenarios include chains of adds and edits, redo invalidation by a new change, failed commands between undo and redo, interaction with `list`, `find`, `tags` and `help`, and reopening the saved data with no history.

--------------------------------------------------------------------------------------------------------------------

### Command history navigation

`CommandBox` owns a session-local `CommandHistory`. A `KEY_PRESSED` event filter on its text field
consumes Up/Down events, recalls the corresponding text and moves the caret to its end.
Recalling text clears the error style through the existing text listener and does not execute a command.

`CommandHistory` stores submissions in order, including duplicates and failed commands. `CommandBox`
records non-empty input before calling the executor, so parse and execution errors remain recallable.
Each submission resets the navigation position to the end. On the first Up from the end, history saves
the current draft; Down past the newest entry restores it. Navigation stops at the oldest entry, and
Down at the draft position leaves input unchanged. Editing recalled input does not change stored entries.
History is held in memory and is not written to contact data or preferences.

Automated boundary, draft and submission checks are in `CommandHistoryTest`. For manual UI verification:

1. Submit `list`, then an invalid command. Press Up twice and verify both can be recalled in reverse order.
2. Press Up again at the oldest entry; verify it stays displayed. Press Down to return to the newest entry.
3. Return to the draft position, type a partial command, then press Up and Down; verify the draft is restored.
4. Edit a recalled command and submit it; verify the new submission is the newest entry.
5. Restart UniMate and verify Up/Down leave the input unchanged until a command is submitted.

## **Coming soon**

The features in this section are not implemented in the MVP. The descriptions and diagrams below are proposals for future development.

### \[Proposed\] Data archiving

Data archiving is planned for a future version. Its implementation details have not yet been specified.


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

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

These user stories include requirements for v1.4 and candidates for future iterations. Priority indicates importance, not implementation status. The [User Guide's Features section](UserGuide.md#features) describes the functionality available in v1.4.

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …         | I want to …                                                                     | So that I can …                                                 |
|----------|----------------|---------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `* * *`  | new user       | see usage instructions                                                          | understand UniMate's capabilities and learn its commands        |
| `* * *`  | user           | add a person with contact information and optional major and graduation year     | retain their contact details and academic background                             |
| `* * *`  | user           | view all my stored contacts                                                     | review the people in my collection                              |
| `* * *`  | user           | view a contact's details                                                        | retrieve their information when needed                          |
| `* * *`  | user           | delete a contact                                                                | remove an entry that I no longer need                           |
| `* * *`  | user           | edit a contact's information                                                    | keep their details up to date                                   |
| `* * *`  | returning user | access previously saved contacts                                                | continue using my collection without recreating it              |
| `* * *`  | user           | categorise contacts by module, major, year, nationality, CCA, event, or project | organise people by their university-related context             |
| `* * *`  | user           | assign multiple categories to one contact                                       | represent different contexts without creating duplicate records |
| `* * *`  | user           | find contacts by name                                                           | retrieve someone's details without browsing the entire list     |
| `* * *`  | user           | filter contacts by university-related context                                   | find relevant people when I cannot remember their names         |
| `* *`    | user           | identify contacts from the same tutorial group                                  | contact the appropriate classmates                              |
| `* *`    | user           | add short notes to a contact                                                    | remember useful information, including how we met               |
| `* *`    | user           | distinguish academic contacts from personal contacts                            | focus on the group relevant to my current activity              |
| `* *`    | user           | mark and retrieve favourite contacts                                            | quickly access people I contact frequently                      |
| `* *`    | user           | undo a recent contact change                                                    | recover from an accidental addition, edit or note change        |
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
2.  User provides the person's name, phone number, email address, and address
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

    * 3a1. UniMate informs user that the contact already exists.
      
      Use case ends.

**Use case: View contacts**

**MSS**

1.  User requests to view all existing contacts.
2.  UniMate shows all existing contacts.

    Use case ends.

**Extensions**

* 2a. There are no existing contacts.

    * 2a1. UniMate shows an empty contact list.

      Use case ends.

**Use case: Delete a contact**

**MSS**

1.  <u>User view all existing contacts (Use case: View contacts) </u>
2.  User requests to delete a specific contact from the displayed list. 
3.  UniMate deletes the specified contact, including its stored academic fields and tags.
4.  UniMate informs user that the contact has been deleted and shows the deleted contact's details.

    Use case ends.

**Extensions**

* 2a. The given index is invalid.

    * 3a1. UniMate informs user that the given index is invalid.

      Use case resumes at step 1.

**Use case: Undo a change**

**MSS**

1.  User adds or edits a contact.
2.  User requests to undo the most recent change.
3.  UniMate restores the contacts to their state before that change.
4.  UniMate shows all contacts and informs the user that the change has been undone.

    Use case ends.

**Extensions**

* 2a. There is no add or edit to undo in the current session, or a delete, tag, untag or clear has changed the contacts since the most recent add or edit.

    * 2a1. UniMate informs the user that there is no change to undo.

      Use case ends.

* 2b. User provides extra input after the command.

    * 2b1. UniMate informs the user that the command format is invalid and shows its usage.

      Use case ends.

* 4a. User wants to revert an earlier add or edit.

    * 4a1. User requests to undo again.

      Use case resumes at step 3.

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

      Use case resumes at step 3.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  UniMate should respond to user commands within 1 second for up to 1,000 stored contacts.  
5.  UniMate should work without an Internet connection.  
6.  UniMate should preserve all saved contact information between application sessions.  
7.  Invalid user input should not corrupt or modify existing stored contact data.  
8.  UniMate should display a meaningful error message for invalid user input without crashing.

### Glossary

* **Category**: A grouping used to organise contacts by a shared attribute or context, such as a module, major, CCA, event, or project.
* **CCA (Co-Curricular Activity)**: An organised student activity outside academic classes, such as a sports club, performing arts group, or student society.
* **CLI (Command-Line Interface)**: An interface in which users type text commands to perform operations. UniMate accepts these commands through the command box in its graphical window.
* **Contact**: A record representing a person in the user's collection, containing their name and contact information. Also referred to as a person in this guide.
* **Contact information**: Details recorded about a person, such as their phone number, email address, and postal address.
* **Contact list index**: The one-based position of a contact in the currently displayed list, used by commands to select that contact. It is not a permanent identifier for the person.
* **Graduation year / Class year**: The calendar year in which a person graduates or expects to graduate, such as 2027 (class of 2027). This differs from their current year of study. UniMate accepts years from 2000 to 2099.
* **GUI (Graphical User Interface)**: An interface that presents information through visual elements such as windows, lists, and buttons.
* **Mainstream OS (Operating System)**: Windows, Linux, Unix, or macOS, as used in this guide's non-functional requirements.
* **Major**: A student's principal academic field of study, such as Computer Science.
* **Module**: A university course or unit of study, typically identified by a code such as CS2103T. Here, it refers to an academic course rather than a software component.
* **Private contact detail**: A contact detail that is not meant to be shared with others.
* **Tag**: A text label attached to a contact to help organise the contact collection. A contact can have multiple tags.
* **Tutorial group**: A group of students assigned to attend the same tutorial session for a particular module.
* **University-related context**: Information that connects a contact to university life, such as their module, major, year of study, CCA, or the event or project through which the user knows them.
* **Year of study**: A student's current stage in their university programme, such as Year 1 or Year 2, rather than a calendar year.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test v1.4 manually. Use a separate test folder and test contacts, as these checks modify saved data. Ensure the test contact names do not already exist before each scenario; use different unique names when repeating a scenario.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Ensure Java 25 or later is installed. Copy the v1.4 `unimate.jar` into an empty test folder.

   1. Open a terminal, change to that folder, and run `java -jar unimate.jar`.<br>
      Expected: A window titled `UniMate` opens with sample contacts. Unspecified academic fields display `Not specified`. The window size may not be optimal.

   1. Enter `exit`.<br>
      Expected: The application window closes normally.

1. Saving window preferences

   1. Relaunch the app with `java -jar unimate.jar`. Resize the window to an optimal size, move it to a different location, and close it.

   1. Relaunch the app from the same test folder with `java -jar unimate.jar`.<br>
       Expected: The most recent window size and location are retained.

### Viewing previously added contacts

1. Restoring all saved contacts from other views

   1. Prerequisites: Use a disposable test copy. Add `add n/ListAlice a/NUS m/Computing` and
      `add n/ListBob a/Clementi m/Business`. Run `find ListAlice`, then `fav 1`.
   1. Run `find ListAlice`, then `list`.<br>
      Expected: Every saved contact appears, including both test contacts. Feedback starts with `Listed all persons.`
      and the next line reports `N person(s) listed!`, where `N` equals the full displayed count.
   1. Run `filter m/Computing`, then `list`; repeat with `listfav`, then `list`.<br>
      Expected: Each `list` restores all contacts and their current details. Favourite hearts and update dates remain unchanged.
   1. Run `find NoSuchListContact`, then `list`.<br>
      Expected: The search has no results, but `list` still restores every saved contact and reports the full count.

1. Sorting, persistence and history

   1. Run `sort name desc`, then `find ListAlice`, then `list`.<br>
      Expected: Every contact returns in descending name order, numbered consecutively from `1`.
   1. Close and relaunch the app from the same folder, then run `list`.<br>
      Expected: Both test contacts and all other saved details remain available. The temporary sort is reset to saved order.
   1. Run `add n/ListUndo a/NUS`, then `undo`, then `list`, then `redo`.<br>
      Expected: `ListUndo` is absent after undo and list, and returns after redo; listing does not clear redo history.
   1. Run `list`, then `undo`.<br>
      Expected: The add of `ListUndo` is undone; listing did not create an undo step.

1. Empty address books and invalid input

   1. In the disposable test copy only, run `clear`, then `list`.<br>
      Expected: An empty list with `Listed all persons.` and `0 person(s) listed!`.
   1. Add `add n/ListValidation a/NUS`, then run `find NoSuchListContact`. Try `list all`, `list 1` and
      `list t/Friend`.<br>
      Expected: Each shows `Invalid command format!` with `list` usage instructions. The empty filtered view and
      saved `ListValidation` contact remain unchanged.
   1. Run `  list  ` with surrounding spaces.<br>
      Expected: `ListValidation` is displayed and the count is `1 person(s) listed!`.

### Adding academic fields

1. Valid values and independently optional fields

   1. Prerequisites: Use a test copy of the app in a separate folder. Ensure the names used below do
      not already exist; use different unique names if repeating the tests.
   1. Test case: `add n/AcademicBoth p/91234567 e/both@example.com a/10 Kent Ridge Road m/Computer Science c/2027`<br>
      Expected: A contact is added with major `Computer Science` and class year `2027`. Both values
      appear in the contact card and the success message.
   1. Test case: `add n/AcademicMajor p/91234567 e/major@example.com a/10 Kent Ridge Road m/Mathematics`<br>
      Expected: The contact has major `Mathematics` and an unspecified class year.
   1. Test case: `add n/AcademicYear p/91234567 e/year@example.com a/10 Kent Ridge Road c/2028`<br>
      Expected: The contact has class year `2028` and an unspecified major.
   1. Test case: `add n/AcademicNeither p/91234567 e/neither@example.com a/10 Kent Ridge Road`<br>
      Expected: The contact is added with both academic fields unspecified. Each unspecified field
      is displayed as `Not specified` in the contact card and omitted from the success message.
   1. Repeat the first command with a new unique name and `c/2000`, then another unique name and
      `c/2099`.<br>
      Expected: Both boundary years are accepted.

1. Invalid values and repeated prefixes

   1. Prerequisites: Ensure `AcademicInvalid` does not exist. Use
      `add n/AcademicInvalid p/91234567 e/invalid@example.com a/10 Kent Ridge Road` as the base command
      and append each suffix below separately.
   1. Test cases: `m/`, `m/   `, `c/`, `c/   `.<br>
      Expected: A major or class-year validation error is shown. No contact is added.
   1. Test cases: `c/1999`, `c/2100`, `c/202`, `c/20270`, `c/abcd`.<br>
      Expected: A class-year validation error is shown. No contact is added.
   1. Test cases: `m/Computer Science m/Mathematics`, `c/2027 c/2028`.<br>
      Expected: A repeated-prefix error is shown. No contact is added, and existing contacts are unchanged.

1. Saving, reloading and older data files

   1. Prerequisites: Complete the valid-input tests above in the test copy of the app.
   1. Close and relaunch the app from the same folder, then run `list`.<br>
      Expected: All four test contacts retain their supplied academic values and unspecified fields.
   1. Close the app and back up the test copy's `data/unimate.json`. In that test file, remove the
      `major` and `graduationYear` properties from the `AcademicBoth` contact, leaving valid JSON.
      Relaunch the app and run `find AcademicBoth`.<br>
      Expected: The contact loads with its original name, phone, email, address and tags, and both
      academic fields show `Not specified`. Other contacts retain their academic values. Restore the
      backup with the app closed when finished.

### Editing academic fields

1. Updating and clearing optional fields

   1. Prerequisites: Add a test contact using `add n/AcademicEdit p/91234567 e/edit@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/StudyGroup`.
   1. Before **each** edit below, run `find AcademicEdit` and confirm that it is the only result. Successful edits return the display to the full contact list, so the search must be repeated before editing index `1` again.
   1. Test case: `edit 1 m/Mathematics`.<br>
      Expected: The major changes to `Mathematics`; class year remains `2027`. Other contact details and `StudyGroup` are preserved.
   1. Test case: `edit 1 c/2028`.<br>
      Expected: Class year changes to `2028`; major remains `Mathematics`.
   1. Test case: `edit 1 m/`.<br>
      Expected: The major displays `Not specified`; class year remains `2028`.
   1. Test case: `edit 1 c/`.<br>
      Expected: Both academic fields display `Not specified`. Other contact details and tags remain unchanged.
   1. Close and relaunch the app from the same folder, then run `find AcademicEdit`.<br>
      Expected: The cleared fields remain unspecified.

1. Rejecting invalid edits

   1. Prerequisites: Run `find AcademicEdit`.
   1. Test cases: `edit 1 c/1999`, `edit 1 c/abcd`, `edit 1 m/Computer Science m/Mathematics`.<br>
      Expected: A class-year validation error or repeated-prefix error is shown. The contact is unchanged.
   1. Test case: `edit 1 m/Mathematics c/2100`.<br>
      Expected: A class-year validation error is shown. Neither academic field changes, including the valid major supplied in the same command.

### Searching by academic fields

1. Matching names, majors and class years

   1. Prerequisites: Add these two test contacts:

      * `add n/SearchComputing p/91234567 e/computing@example.com a/10 Kent Ridge Road m/Computer Science c/2027`
      * `add n/SearchBusiness p/91234568 e/business@example.com a/11 Kent Ridge Road m/Business c/2028`

   1. Test case: `find computer`.<br>
      Expected: `SearchComputing` is included and `SearchBusiness` is excluded. Matching is case-insensitive; other contacts with matching names or academic fields may also appear.
   1. Test case: `find 2028`.<br>
      Expected: `SearchBusiness` is included and `SearchComputing` is excluded.
   1. Test case: `find SearchComputing 2028`.<br>
      Expected: Both test contacts appear because matching any keyword is sufficient.
   1. Test case: `find comp`.<br>
      Expected: Neither test contact appears; partial words do not match `Computer` or `SearchComputing`.
   1. Test case: `list`.<br>
      Expected: The full contact list is displayed again, including both test contacts and contacts without academic fields.

### Filtering contacts

1. Combined criteria, independent fields and full-value matching

   1. Prerequisites: Use a disposable test copy of UniMate and restart it before this test so no sort is active.
      Ensure no existing contact uses the names or custom tag `FilterDemo` below. Add these contacts:

      * `add n/FilterMath p/91234567 e/math@example.com a/Kent Ridge m/Mathematics c/2027 t/FilterDemo`
      * `add n/FilterComputing p/92345678 e/computing@example.com a/Clementi m/Computer Science c/2027 t/FilterDemo`
      * `add n/FilterLater p/93456789 e/later@example.com a/Dover m/Computer Science c/2028 t/FilterDemo`
      * `add n/FilterUnknown p/94567890 e/unknown@example.com a/Buona Vista t/FilterDemo`

   1. Run `filter m/computer science c/2027 t/filterdemo`.<br>
      Expected: Only `FilterComputing` appears, numbered `1`, with `1 person(s) listed!`.
      All three criteria must match, and major/tag case differences are ignored.
   1. Run `filter c/2027 t/FilterDemo`.<br>
      Expected: `FilterMath` and `FilterComputing` appear in that order. The previous major criterion
      is replaced, and `FilterUnknown` is excluded because its class year is unspecified.
   1. Run `filter m/Computer t/FilterDemo`.<br>
      Expected: An empty displayed list and `0 person(s) listed!`; partial majors do not match.
   1. Run `filter t/FilterDemo`, then `list`.<br>
      Expected: All four demo contacts appear after the filter, including `FilterUnknown`.
      `list` restores every contact, including those outside the demo group.

1. Invalid input preserves the current view

   1. Run `filter c/2027 t/FilterDemo` and note the two results.
   1. Try each command separately: `filter`, `filter m/`, `filter c/2100`, `filter t/`,
      `filter t/FilterDemo t/Friend`, `filter n/FilterMath`.<br>
      Expected: A usage, value-validation or repeated-prefix error. The same two results remain,
      and no saved contact changes.

1. Displayed indices, changing membership and restarting

   1. Run `filter c/2028 t/FilterDemo`, then `untag 1 t/FilterDemo`.<br>
      Expected: `FilterLater` disappears from the results but remains saved. Run `find FilterLater`
      to see it, then `tag 1 t/FilterDemo` to restore its tag.
   1. Run `filter c/2028 t/FilterDemo`, then `delete 1`.<br>
      Expected: Only `FilterLater` is deleted. Other contacts remain, even though the deleted contact
      was not first in the complete address book.
   1. Run `filter m/Computer Science t/FilterDemo`, then `edit 1 m/Mathematics`.<br>
      Expected: `FilterComputing` changes major and the full list is shown, as for existing edits.
      Reapply the same filter: no demo contact matches now.
   1. Run `filter t/FilterDemo`, close and relaunch the app from the same folder.<br>
      Expected: The full list is shown. Saved contacts and the changes above remain; the filter is temporary.

### Sorting contacts

1. Directions, ties and unspecified fields

   1. Prerequisites: Use a disposable copy of UniMate. Ensure the following names and tag `SortDemo`
      do not already exist. Add these contacts, then run `filter t/SortDemo`:

      * `add n/SortGamma p/91234567 e/gamma@example.com a/Dover m/business c/2027 t/SortDemo`
      * `add n/SortUnknown p/92345678 e/unknown@example.com a/Clementi t/SortDemo`
      * `add n/SortBeta p/93456789 e/beta@example.com a/Kent Ridge m/Business c/2027 t/SortDemo`
      * `add n/sortAlpha p/94567890 e/alpha@example.com a/Utown m/Zoology c/2028 t/SortDemo`

   1. Run `sort name asc`, then `sort name desc`.<br>
      Expected: Ascending shows `sortAlpha`, `SortBeta`, `SortGamma`, `SortUnknown`; descending reverses
      that order. The cards are numbered `1` to `4`. Feedback identifies the field and direction.
   1. Run `sort major asc`, then `sort major desc`.<br>
      Expected: Ascending shows `SortBeta`, `SortGamma`, `sortAlpha`, `SortUnknown`.
      Descending shows `sortAlpha`, `SortBeta`, `SortGamma`, `SortUnknown`.
      Major case differences are ignored, equal majors use ascending names, and unspecified majors stay last.
   1. Run `sort year asc`, then `sort year desc`.<br>
      Expected: The same two orders as the major checks above. `SortUnknown` stays last in both.
   1. Run `filter t/SortDemo m/Business`, then `sort name desc`.<br>
      Expected: Only `SortGamma` and `SortBeta` appear, in that order.
      Run `list`: all contacts return in descending name order. Reapply `filter t/SortDemo` for later checks.

1. Rejected input and displayed indices

   1. Run `sort name asc`. Try `sort`, `sort name`, `sort phone asc`, `sort year ascending`,
      `sort NAME asc` and `sort major asc desc` separately.<br>
      Expected: Usage errors; the same four demo contacts remain in ascending name order.
   1. Run `edit 2 c/2029`.<br>
      Expected: `SortBeta` changes year, even though it was third in the saved demo order.
      The full list appears in ascending name order. Run `filter t/SortDemo`, then `sort year asc`:
      the order is `SortGamma`, `sortAlpha`, `SortBeta`, `SortUnknown`.
   1. Run `undo`, then `filter t/SortDemo`.<br>
      Expected: `SortBeta` returns to `2027`; the order is `SortBeta`, `SortGamma`, `sortAlpha`, `SortUnknown`.
      Run `redo`, then reapply the same filter: the edited order returns. Sorting and filtering have not
      consumed the undo entry or cleared the redo opportunity.
   1. Run `sort name desc`, then `delete 1`.<br>
      Expected: Only `SortUnknown` is removed. The remaining demo contacts are renumbered in descending
      name order; contacts outside `SortDemo` are unaffected.

1. Empty results and restarting

   1. Run `filter t/SortDemo m/NoSuchMajor`, then `sort year desc`.<br>
      Expected: An empty list and successful sort feedback. Run `filter t/SortDemo`:
      the remaining demo contacts appear in descending year order.
   1. Close and relaunch UniMate from the same folder, then run `filter t/SortDemo`.<br>
      Expected: The demo contacts return in saved order: `SortGamma`, `SortBeta`, `sortAlpha`.
      The edit and deletion remain saved, while the previous sort and filter do not survive restarting.

### Deleting a person

1. Deleting a contact with academic fields

   1. Prerequisites: Add a test contact using `add n/DeleteCheck p/91234567 e/delete@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/classmate`.
   1. Run `find DeleteCheck` and confirm that it is the only result. Then run `delete 1`.<br>
      Expected: `DeleteCheck` is removed. The status message includes `Major: Computer Science`, `Class year: 2027` and the contact's tag. The filtered list becomes empty.
   1. Run `list`.<br>
      Expected: Other contacts remain present; only `DeleteCheck` was removed.
   1. Close and relaunch the app from the same folder, then run `find DeleteCheck`.<br>
      Expected: No matching contact is found; the deletion was saved.

1. Rejecting invalid indices

   1. Prerequisites: Run `list` with at least one contact present.
   1. Test cases: `delete 0`, `delete`, `delete x`.<br>
      Expected: An invalid-command-format error is shown and no contact is deleted.
   1. Run `delete` with a numeric index greater than the displayed list size.<br>
      Expected: `The person index provided is invalid.` No contact is deleted.

### Tagging and untagging a person

1. Listing and assigning default tags

   1. Test case: `tags`.<br>
      Expected: The result lists `1. Friend`, `2. Classmate`, `3. Teammate`, `4. Tutor` and `5. Prof`, in that order.
   1. Test case: `add n/DefaultTagCheck p/91234567 e/defaulttags@example.com a/10 Kent Ridge Road d/1 d/2 t/StudyGroup`.<br>
      Expected: The contact has `Friend`, `Classmate` and `StudyGroup`. Tag display order may vary.
   1. Test case: `tags extra`.<br>
      Expected: A usage error is shown; contacts and tags are unchanged.

1. Adding and removing tags

   1. Prerequisites: Run `list` with at least one contact present. Note the first person's original details and tags; use a test contact because the following steps change its tags.
   1. Test case: `tag 1 d/2 t/NOC`<br>
      Expected: `Classmate` and `NOC` are present alongside all original tags. Other contact fields are unchanged. Feedback starts with `Tagged person: ` and includes the updated details.
   1. Repeat `tag 1 d/2 t/NOC`.<br>
      Expected: No duplicate tags appear; the command still succeeds.
   1. Test case: `untag 1 t/NOC t/Classmate`<br>
      Expected: Both named tags are removed, other tags and contact fields remain, and feedback starts with `Untagged person: `.
   1. Repeat `untag 1 t/NOC t/Classmate`.<br>
      Expected: Success with no further tag changes.

1. Invalid input and filtered indices

   1. Test cases: `tag 1 d/6`, `tag 1 t/`, `untag 1`, `untag 1 t/NOC t/`.<br>
      Expected: The corresponding default-index, tag-name or usage error described in the UG; no tags change, including `NOC` in the last example.
   1. Run `find KEYWORD`, replacing `KEYWORD` with a full name word matching an existing contact, then `tag 1 t/NOC` and `untag 1 t/NOC`.<br>
      Expected: Both affect the first search result and retain the search filter.
   1. Run either command with an index greater than the displayed list size and a valid tag.<br>
      Expected: `The person index provided is invalid.` No contact changes.

### Undoing and redoing changes

Perform these scenarios in order; each one relies on the contacts left by the previous one.

1. Undoing and redoing an add

   1. Test case: `add n/UndoCheck p/91234567 e/undo@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/StudyGroup`, then `undo`.<br>
      Expected: `UndoCheck` is removed and the full contact list is shown. The status message is `Rollback to before the most recent change.`
   1. Test case: `redo`.<br>
      Expected: `UndoCheck` reappears with all of its details, including its major, class year and tag. The status message is `Restored the most recently undone change.`
   1. Close and relaunch the app from the same folder, then run `find UndoCheck`.<br>
      Expected: `UndoCheck` is present; the redone state was saved.
   1. Test case: `undo`.<br>
      Expected: `There is no change to undo. Only add and edit can be undone, and not after a delete, tag, untag or clear.` Undo history is not kept between sessions, so `UndoCheck` remains.

1. Undoing and redoing several edits

   1. Prerequisites: Run `find UndoCheck` and confirm that it is the only result.
   1. Run `edit 1 p/98765432 c/2028`, then `edit 1 t/Mentor`.<br>
      Expected: `UndoCheck` has phone `98765432`, class year `2028` and only the tag `Mentor`.
   1. Test case: `undo`.<br>
      Expected: The tag `StudyGroup` replaces `Mentor`; the phone and class year stay `98765432` and `2028`. The full contact list is shown instead of the search result.
   1. Test case: `undo` again.<br>
      Expected: The phone and class year return to `91234567` and `2027`.
   1. Test case: `redo`, then `redo` again.<br>
      Expected: The first `redo` restores phone `98765432` and class year `2028`; the second restores the tag `Mentor`.

1. Redo is unavailable after a new change

   1. Test case: `undo`, then `add n/RedoCheck p/92345678 e/redo@example.com a/20 Kent Ridge Road`, then `redo`.<br>
      Expected: The `undo` restores `UndoCheck`'s `StudyGroup` tag and `RedoCheck` is then added. The `redo` fails with `There is no change to redo. Only an undone add or edit can be redone, and not after another change has been made.` and the contacts are unchanged.

1. Commands that do not affect history

   1. Test case: `undo`.<br>
      Expected: `RedoCheck` is removed.
   1. Run `add n/UndoCheck p/93456789 e/other@example.com a/30 Kent Ridge Road`, `add n/Incomplete`, `list`, `find UndoCheck` and `tags`.<br>
      Expected: The first two fail with a duplicate-person error and an invalid-command-format error. No command changes any contact.
   1. Test case: `redo`.<br>
      Expected: `RedoCheck` is added back. The failed and non-modifying commands did not discard the undone change.
   1. Test cases: `undo 1`, `redo 1`.<br>
      Expected: An invalid-command-format error with the command's usage is shown, and no contact changes.

1. Commands that cannot be undone

   1. Run `find RedoCheck` and confirm that it is the only result. Then run `delete 1`.<br>
      Expected: `RedoCheck` is deleted.
   1. Test cases: `undo`, `redo`.<br>
      Expected: Both report that there is no change to undo or redo. `RedoCheck` stays deleted and `UndoCheck` keeps its current details, even though earlier adds and edits were undoable before the deletion.
   1. To clean up, run `find UndoCheck`, then `delete 1`.

### Managing contact notes

Use a test contact for these checks. Run `list` and identify its displayed index; the examples use `1`.

1. Run `shownote 1` on a contact with no note.<br>
   Expected: `No notes have been added to this contact.` No note icon is visible.
1. Run `note 1 Met during CS2103T`, then `shownote 1`.<br>
   Expected: A note icon appears at the bottom right of the card. The message area shows the saved text beneath the contact's name.
1. Run `note 1 Project teammate`, then `shownote 1`.<br>
   Expected: Only `Project teammate` is shown; the previous note was replaced. Other contact details are preserved.
1. Edit the contact's phone, add a tag with `tag`, and remove it with `untag`, then run `shownote` using the contact's current index.<br>
   Expected: The note is still `Project teammate`.
1. Restart the app, locate the same contact, and run `shownote`.<br>
   Expected: The note and its icon are still present.
1. Use `find` to show a subset of contacts, then use `note 1 Filtered contact`, `shownote 1`, and `delnote 1`.<br>
   Expected: Each command targets the first displayed result and preserves the filter. Deleting the note removes its icon and leaves the contact present.
1. Repeat `delnote 1`, then run `shownote 1`.<br>
   Expected: `This contact has no note to delete.`, followed by `No notes have been added to this contact.` Restarting preserves the empty note.
1. Try `note 1`, `note 0 Text`, `shownote 1 extra`, `delnote 1 extra`, and a `note` containing more than 500 characters. Also try an index beyond the displayed list.<br>
   Expected: An appropriate usage, note-constraint or invalid-index error; existing notes and other contact details are unchanged.

### Undoing and redoing contact changes

Use test contacts and run `list` before each scenario. The examples below use the first displayed contact. Do not run unrelated data-changing commands between the steps.

1. Note creation, replacement and deletion

   1. Run `delnote 1` to start with an empty note, then `note 1 First note`, `note 1 Second note`, and `delnote 1`.
   1. Run `undo` three times, checking `shownote 1` after each.<br>
      Expected: The note changes to `Second note`, then `First note`, then no note. The icon is present only when the note is non-empty.
   1. Run `redo` three times, checking `shownote 1` after each.<br>
      Expected: The note changes to `First note`, then `Second note`, then no note. Other contact details stay unchanged.

1. Unchanged notes preserve redo

   1. Run `note 1 First note`, `note 1 Second note`, then `undo`.
   1. Run `note 1 First note` again, then `shownote 1`.<br>
      Expected: The note remains `First note`; neither command creates a new undo step or clears redo history.
   1. Run `redo`, then `shownote 1`.<br>
      Expected: The note is restored to `Second note`.

1. Deleting an empty note preserves redo

   1. Run `delnote 1`, `note 1 Restore me`, then `undo`.
   1. Run `delnote 1`, then `shownote 1`.<br>
      Expected: `This contact has no note to delete.`, followed by `No notes have been added to this contact.`
   1. Run `redo`, then `shownote 1`.<br>
      Expected: The note is restored to `Restore me`.

1. A new note change clears redo

   1. Run `note 1 First note`, `note 1 Second note`, `undo`, then `note 1 Replacement note`.
   1. Run `redo`, then `shownote 1`.<br>
      Expected: Redo reports that no change is available to redo; the note remains `Replacement note`.

1. Filters, invalid arguments and session history

   1. Run `note 1 Before undo`, then `note 1 After undo`. Use `find KEYWORD`, replacing `KEYWORD` with a full name word matching a test contact, then run `undo`.<br>
      Expected: The full contact list is displayed, and the original first contact's note is `Before undo`.
   1. Apply `find KEYWORD` again, then run `redo`.<br>
      Expected: The full contact list is displayed, and the same contact's note is `After undo`.
   1. Try `undo 1` and `redo 1`.<br>
      Expected: Usage errors; contact data and history remain unchanged.
   1. Restart UniMate, check `shownote 1`, then try `undo` and `redo` before changing any data.<br>
      Expected: `After undo` is still saved, but neither command can restore a state because history does not persist across sessions.

### Saving data

Use only the separate test folder for these checks. The paths below are relative to the folder from which you run `java -jar unimate.jar`. Close the app before changing data files. Keep a backup outside the `data` folder and restore it with the app closed after each scenario.

1. Saving and reloading a contact

   1. Test case: `add n/SaveCheck p/91234567 e/save@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/StudyGroup`.
   1. Close and relaunch the app from the same folder, then run `find SaveCheck`.<br>
      Expected: The contact retains all details, including its major, class year and tag.
   1. Close the app and back up `data/unimate.json` before continuing.

1. Missing data file

   1. With the app closed, move the test copy's `data/unimate.json` to a backup location so that no file remains at that exact path.
   1. Relaunch the app.<br>
      Expected: The sample contacts appear. The backed-up contacts are not loaded, and no data file has been written yet.
   1. Run `list`.<br>
      Expected: `data/unimate.json` is created with the sample contacts. Close the app and restore the backup before the next scenario.

1. Malformed JSON

   1. With the app closed and the valid backup retained, replace the contents of the test copy's `data/unimate.json` with `not valid JSON`.
   1. Relaunch the app without entering a command.<br>
      Expected: The app opens with an empty contact list. The malformed file remains unchanged on disk; the loading error is recorded in the application log.
   1. Run `list`.<br>
      Expected: The command succeeds and replaces the malformed file with valid JSON containing an empty person list. Close the app and restore the valid backup.

1. Valid JSON containing an invalid contact

   1. With the app closed and the valid backup retained, change `SaveCheck`'s `graduationYear` value to `"1999"` in the test file. Keep the JSON syntax valid.
   1. Relaunch the app without entering a command.<br>
      Expected: The whole data file is rejected and the app opens with an empty contact list. It does not partially load the other contacts or silently correct the invalid year. The invalid file remains unchanged at this point.
   1. Close the window using its close button, then restore the valid backup. Do not enter `exit` before restoring, because a successfully executed command would save the empty in-memory list over the invalid file.

1. Loading a file saved under the old filename

   1. With the app closed and the valid backup retained, place a copy of the valid contact file at `data/addressbook.json` and ensure `data/unimate.json` is absent. If the old filename is already in use, perform this scenario in another test folder.
   1. Relaunch the app without entering a command.<br>
      Expected: Sample contacts appear. UniMate v1.4 reads `data/unimate.json` and does not automatically migrate `data/addressbook.json`.
   1. Close the window using its close button. Copy `data/addressbook.json` to `data/unimate.json`, then relaunch and run `find SaveCheck`.<br>
      Expected: The saved contact and its academic fields and tag load correctly. The old file is unchanged. Restore the backup when finished.
