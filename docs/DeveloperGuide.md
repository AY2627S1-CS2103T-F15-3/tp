---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# UniMate Developer Guide

This guide describes the v1.3 MVP.

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
  Each `Person` includes `Major` and `GraduationYear` value objects in addition to its existing contact details and tags.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
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
The normal storage path then saves the contact, including its academic fields.

### Editing academic fields

`EditCommandParser` recognises `m/` (major) and `c/` (graduation year) alongside existing edit prefixes.
It rejects repeated academic prefixes and validates supplied values using `ParserUtil.parseMajor` and
`ParserUtil.parseGraduationYear`. An empty prefix value maps to `Major.EMPTY` or `GraduationYear.EMPTY`
to clear the optional field; an omitted prefix leaves the descriptor field null.

`EditCommand.EditPersonDescriptor` stores, copies and compares both academic fields. `EditCommand`
uses the supplied values when constructing the edited `Person`, retaining existing values for omitted fields.
The existing model update and JSON storage path save the edited values without a storage format change.

### Searching by academic fields

`FindCommandParser` splits the input into keywords and passes them to `NameContainsKeywordsPredicate`.
Despite its name, the predicate matches a `Person` if any keyword matches a full word in the person's
`Name`, `Major` or `GraduationYear`, using `StringUtil.containsWordIgnoreCase`. Matching is therefore
case-insensitive, whole-word and `OR`-based across all three fields. An empty `Major` or `GraduationYear`
never matches a keyword. `FindCommand` applies the predicate through `Model#updateFilteredPersonList`;
the model and storage format are unchanged.
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
### Displaying academic fields in the contact list

`ListCommand` resets the model's filtered person list to `PREDICATE_SHOW_ALL_PERSONS`. `PersonListPanel` observes
that list and creates a `PersonCard` for every visible `Person`, so the card layout determines which fields appear
after `list` as well as in filtered results produced by `find`.

`PersonCard` reads the existing `Major` and `GraduationYear` value objects and formats them as labeled rows.
`PersonListCard.fxml` places those rows below the contact details. Because both fields are optional, an empty value
is rendered as `Not specified` instead of leaving an ambiguous blank line. This is a presentation-only change:
`ListCommand`, the model, and the JSON storage format are unchanged.

### Tagging and untagging contacts

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

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed `VersionedAddressBook` class would extend `AddressBook` to represent UniMate contact data with an undo/redo history. It would store that history internally as an `addressBookStateList` and `currentStatePointer`. This class and the undo/redo methods described below do not exist in the current implementation.

The proposed operations are:

* `VersionedAddressBook#commit()` -- Would save the current UniMate data state in its history.
* `VersionedAddressBook#undo()` -- Would restore the previous UniMate data state from its history.
* `VersionedAddressBook#redo()` -- Would restore a previously undone UniMate data state from its history.

These operations would be exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

The following hypothetical scenario illustrates how the proposed mechanism would behave. The `undo` and `redo` commands are not available in the MVP.

Step 1. The user launches the application for the first time. The versioned data model will be initialized with the initial UniMate data state, and the `currentStatePointer` pointing to that single state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in UniMate. The `delete` command calls `Model#commitAddressBook()`, causing the modified UniMate data state after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified UniMate data state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the UniMate data state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous UniMate data state, and restores UniMate to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial UniMate data state, then there are no previous states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores UniMate to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest UniMate data state, then there are no undone states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all UniMate data states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (proposed choice):** Saves the entire UniMate data state.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

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

These user stories include requirements for v1.3 and candidates for future iterations. Priority indicates importance, not implementation status. The [User Guide's Features section](UserGuide.md#features) describes the functionality available in the MVP.

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
| `* *`    | user           | undo a recent contact change                                                    | recover from an accidental addition or edit                     |
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

Given below are instructions to test the v1.3 MVP manually. Use a separate test folder and test contacts, as these checks modify saved data. Ensure the test contact names do not already exist before each scenario; use different unique names when repeating a scenario.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Ensure Java 25 or later is installed. Copy the v1.3 `unimate.jar` into an empty test folder.

   1. Open a terminal, change to that folder, and run `java -jar unimate.jar`.<br>
      Expected: A window titled `UniMate` opens with sample contacts. Unspecified academic fields display `Not specified`. The window size may not be optimal.

   1. Enter `exit`.<br>
      Expected: The application window closes normally.

1. Saving window preferences

   1. Relaunch the app with `java -jar unimate.jar`. Resize the window to an optimal size, move it to a different location, and close it.

   1. Relaunch the app from the same test folder with `java -jar unimate.jar`.<br>
       Expected: The most recent window size and location are retained.

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
      Expected: Sample contacts appear. UniMate v1.3 reads `data/unimate.json` and does not automatically migrate `data/addressbook.json`.
   1. Close the window using its close button. Copy `data/addressbook.json` to `data/unimate.json`, then relaunch and run `find SaveCheck`.<br>
      Expected: The saved contact and its academic fields and tag load correctly. The old file is unchanged. Restore the backup when finished.
