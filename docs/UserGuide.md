---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# UniMate User Guide

UniMate is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, UniMate can help you manage contacts faster than traditional GUI applications.

This guide describes the features available in v1.4.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from the [UniMate releases page](https://github.com/AY2627S1-CS2103T-F15-3/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for UniMate.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar unimate.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to UniMate.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `undo` : Reverts the most recent `add` or `edit`.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Prefixed parameters can be in any order. For `sort`, the field must precede the direction.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for `help`, `exit`, and `clear` are ignored.<br>
  For example, `help 123` is interpreted as `help`.<br>
  `list`, `listfav`, `tags`, `undo` and `redo` reject extra parameters, such as `list all` or `undo 1`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to UniMate.

Format: `add n/NAME [p/PHONE_NUMBER] [e/EMAIL] a/ADDRESS [m/MAJOR] [c/CLASS_YEAR] [t/TAG]... [d/DEFAULT_INDEX]...`

<box type="tip" seamless>

**Tip:** A person can have any number of tags, including zero.
Major and class year are optional and can be supplied independently.
</box>

* `m/MAJOR` records the person's academic major. A supplied major must be non-blank text on one line.
* `c/CLASS_YEAR` records the graduation year (class of 20XX). Enter exactly four digits from `2000` to `2099`.
* Omit `m/` or `c/` if the value is unknown or does not apply; an empty supplied value is rejected.
* Each of these prefixes may appear only once. Leading and trailing spaces around their values are ignored.
* The fields are saved with the contact, can be edited using `edit`, are shown in the displayed contact list and appear in command messages when specified.
* A contact with exactly the same name as an existing contact is rejected as a duplicate, even if their major, class year or other details differ. Name comparison is case-sensitive.
* A successful `add` can be reverted with [`undo`](#undoing-a-change-undo).
* Phone and email are independently optional. Omit `p/` or `e/` when unknown; do not supply an empty prefix in `add`. Non-empty values must satisfy the existing phone and email format rules. A contact without either receives the [automatic incomplete reminder](#automatic-incomplete-reminders).

Examples:
* `add n/Alex Tan p/91234567 e/alex@example.com a/10 Kent Ridge Road m/Computer Science c/2027 t/classmate`
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Automatic incomplete reminders

UniMate shows a yellow `incomplete` reminder when **both phone and email are missing**, so you can identify contacts without a recorded way to reach them. This is automatic: there is no `incomplete` command.

| Phone | Email | Reminder |
|-------|-------|----------|
| Present | Present | None |
| Present | Missing | None |
| Missing | Present | None |
| Missing | Missing | `incomplete` |

Address, major, class year, tags and notes do not affect this check. Missing phone and email values appear as `Not specified` on the contact card. A supplied phone still requires at least three digits; non-empty email values retain the existing email format requirements.

**Adding and clearing contact details:**

* `add n/Alice a/NUS` creates a contact without a phone or email and shows the reminder (assuming Alice does not already exist).
* `add n/Bob a/NUS p/91234567` records a phone only and has no incomplete reminder.
* `edit 1 p/91234567` or `edit 1 e/alice@example.com` supplies a contact method and removes the reminder immediately.
* `edit 1 p/` clears the phone; `edit 1 e/` clears the email. `edit 1 p/ e/` clears both, causing the reminder to appear. Omitted fields retain their values. As with the existing optional academic fields, empty prefixes clear fields only in `edit`; `add n/Alice a/NUS p/` is rejected.
* An actual contact-detail change refreshes `lastUpdated` and clears any outdated `update` reminder. If both contact methods are cleared, `incomplete` still appears even though the profile date is now current. Entering unchanged values does not reset the date.

**GUI and custom tags:**

The reminder uses the existing rectangular tag style, with background `#F4C542` and text `#222222`. Ordinary tags remain blue and favourite hearts remain red. `update` and `incomplete` may appear together, once each, when their independent conditions apply.

System reminders are not saved as ordinary tags. Removing a user tag with `untag` cannot remove the date-based or incomplete reminder. An existing custom tag named `incomplete` remains saved under its original name and is displayed in blue as `incomplete (custom)`; manage it using `t/incomplete`. It can coexist with the yellow reminder, just as `update (custom)` can coexist with `update`.

Phone/email values are saved normally. Older JSON files with valid contact details remain compatible; absent, null or empty phone/email fields are treated as unspecified. Other required fields and non-empty contact-value validation remain unchanged.

### Tracking a contact's latest update date

Every contact card shows `Last updated: YYYY-MM-DD` below the academic fields, including contacts
without an `update` reminder. For example, `Last updated: 2026-10-20` means the contact was added
or its basic information last changed on 20 October 2026. Use `list` to view all saved contacts and
their dates, or `find` and `filter` to narrow the displayed contacts. No separate date command is needed.

* A new contact receives the date when `add` succeeds, using your computer's local date.
* A successful `edit` that changes the name, phone, email, address, major or class year records
  the current date. Clearing a previously specified optional field also counts as a change.
* Re-entering unchanged values, commands rejected before changing a contact, tag-only edits, notes,
  favourites and commands that only change the displayed list keep the previous date. The date measures maintenance of
  the contact's basic information, rather than changes to your personal annotations.
* Dates are saved automatically with the contacts and remain visible after restarting UniMate once saving succeeds.
  `undo` and `redo` restore the saved date belonging to the restored contact state.
* The date has day precision: several changes on the same day show the same date.

Example: run `list`, identify the displayed index of the contact, then enter `edit 1 p/91234567`.
If the first contact's phone changes, its card immediately shows today's date. If that phone was
already `91234567`, the previous date remains.

For files that predate date tracking, a missing or null date starts at the day the file is first
loaded. This is a compatibility baseline, not a recovered historical edit date. It is written to
the file after the next successful command; see [Older data files](#automatic-update-reminders).

### Automatic update reminders

UniMate automatically reminds you to check a contact whose basic information has not been updated for **more than one calendar year**. No new command or manual tag is required.

* A yellow rectangular `update` label appears beside the contact's ordinary tags. Its background is `#F4C542` and its text is `#222222`, with the same size and shape as ordinary tags. Existing tags remain blue, and favourite heart icons are unchanged.
* The reminder is calculated from the contact's saved `lastUpdated` date. Exactly one year does not trigger it: if today is `2026-10-18`, a date of `2025-10-18` has no reminder, while `2025-10-17` does.
* New contacts start with today's date. Successfully changing the name, phone, email, address, major or class year through `edit` records today's date and removes the reminder. For example, `edit 1 p/98765432` clears the reminder if it changes the first displayed contact's phone number.
* Entering the same basic values again, editing only tags, changing notes, or using `tag`, `untag`, `fav`, `unfav`, `list`, `listfav` or `find` does not reset the date.
* Reminders are checked when cards are loaded or refreshed. Successful commands refresh the list; no background timer runs while the app is idle. The date is saved with the contact and retained across restarts. Undo and redo restore the recorded contact state, including its date.

**System reminders and custom tags:**

The yellow `update` reminder is generated from the date and is never added to your ordinary tag collection. You cannot permanently remove it with `untag`; update the contact's basic information instead.

A custom tag named `update` is still supported and saved unchanged. It appears in blue as `update (custom)` to distinguish it from the yellow system reminder. Use `t/update` to manage that custom tag as before. Adding or removing it does not create, overwrite or clear the system reminder. Both can be displayed together without duplicate system reminders.

**Older data files:**

When loading a file without `lastUpdated`, UniMate uses the loading date as a compatibility baseline. This is not the contact's true historical modification date. The baseline is retained when the data is saved normally. A supplied invalid date is rejected rather than silently replaced.

### Listing default tags: `tags`

Displays the available default tags and their fixed indices:

1. Friend
2. Classmate
3. Teammate
4. Tutor
5. Prof

Format: `tags`

### Adding tags to a person: `tag`

Adds default or custom tags to an existing person without replacing their existing tags.

Format: `tag INDEX [d/DEFAULT_INDEX]... [t/TAG]...`

**Parameters:**

* `INDEX` is the person's position in the currently displayed list, including results from `find`. It must be a positive integer from `1` to `2147483647` and refer to a displayed person. Signs such as `+1`, zero, negative values and decimals are not accepted.
* `d/DEFAULT_INDEX` selects a default tag from `1` to `5`: `Friend`, `Classmate`, `Teammate`, `Tutor`, `Prof`, respectively. Use [the `tags` command](#listing-default-tags-tags) to view this list. The same integer syntax rules apply.
* `t/TAG` supplies a non-empty custom tag name containing only English letters (`A-Z`, `a-z`) and digits (`0-9`). Spaces within a name and punctuation are not allowed. Leading and trailing whitespace is trimmed.
* At least one `d/` or `t/` value is required. Both prefixes can be repeated and mixed in any order **after `INDEX`**. Separate each prefix from the preceding argument with a space.

Examples (assuming at least one person is displayed):

* `tag 1 d/2` adds `Classmate` to the first displayed person.
* `tag 1 d/1 d/3` adds `Friend` and `Teammate`.
* `tag 1 t/NOC` appends `NOC` without removing existing tags.
* `tag 1 t/NOC t/CCA d/2` appends two custom tags and `Classmate`.

**Expected behaviour:**

* The selected person's tags are updated; their other details and the current list filter are preserved.
* The result message starts with `Tagged person: `, followed by the person's name, phone, email, address, any specified major and class year, and updated tags. Tag display order is not guaranteed.
* Repeated tags and tags already attached to the person are stored only once. Adding an existing tag still returns the success message.
* Names are case-sensitive: `NOC` and `noc` are different tags. Default tags are stored by name in the same collection as custom tags: `d/1` and `t/Friend` select the same tag, whereas `t/friend` is different.
* Custom tags still use `t/` in `add` and `edit`. Unlike `tag`, `edit INDEX t/TAG` replaces the entire tag set.

**Error handling:**

| Input or situation | Result message |
|--------------------|----------------|
| Missing tags (`tag 1`), missing index, or invalid index syntax (`tag 0 t/NOC`, `tag x t/NOC`) | `Invalid command format!`, followed by the `tag` usage instructions. |
| A valid positive index outside the displayed list, with otherwise valid input | `The person index provided is invalid.` |
| Empty, non-integer or out-of-range default index (`tag 1 d/`, `tag 1 d/6`) | `Default tag index must be an integer from 1 to 5. Use tags to view them.` |
| Empty or invalid custom tag (`tag 1 t/`, `tag 1 t/NOC!`, `tag 1 t/two words`) | `Tag names should be alphanumeric` |

These input errors leave all contacts unchanged. If any supplied tag is invalid, none of the tags in that command are added. Use only `d/` and `t/`; unsupported prefixes are not ignored and may cause an index or tag validation error.

### Removing tags from a person: `untag`

Removes specified tags from an existing person while preserving all other tags and contact details.

Format: `untag INDEX t/TAG [t/TAG]...`

**Parameters:**

* `INDEX` identifies a person in the currently displayed list and follows the same positive integer rules as [`tag`](#adding-tags-to-a-person-tag).
* At least one non-empty `t/TAG` is required. Names must contain only English letters and digits; leading and trailing whitespace is trimmed. Names are matched exactly, including case.
* Place `INDEX` first and separate each `t/` prefix from the preceding argument with a space. Repeat `t/` to remove multiple tags.
* `untag` does not accept `d/`. To remove a default tag, use its name, such as `t/Friend`.

Examples (assuming at least one person is displayed):

* `untag 1 t/NOC` removes `NOC` while keeping the person's other tags.
* `untag 1 t/NOC t/CCA` removes both tags.
* `untag 1 t/Friend` removes the default tag `Friend` by name.

**Expected behaviour:**

* The result message starts with `Untagged person: `, followed by the person's name, phone, email, address, any specified major and class year, and remaining tags. Tag display order is not guaranteed.
* The current list filter is preserved. Removing the last tag leaves the person with no tags; it does not delete the person.
* Repeated names and tags the person does not have are ignored. The command still returns the success message, even if no tags were removed. For example, `untag 1 t/noc` does not remove `NOC`.

**Error handling:**

| Input or situation | Result message |
|--------------------|----------------|
| Missing tags (`untag 1`), missing index, or invalid index syntax (`untag 0 t/NOC`) | `Invalid command format!`, followed by the `untag` usage instructions. |
| A valid positive index outside the displayed list, with otherwise valid input | `The person index provided is invalid.` |
| Empty or invalid tag (`untag 1 t/`, `untag 1 t/NOC!`) | `Tag names should be alphanumeric` |
| Unsupported default index syntax (`untag 1 d/1`) | `Invalid command format!`, followed by the `untag` usage instructions. |
| Unsupported prefix after a tag (`untag 1 t/NOC d/1`) | `Tag names should be alphanumeric` |

These input errors leave all contacts unchanged. If any supplied tag is invalid, none of the tags in that command are removed. Unlike `edit 1 t/`, `untag 1 t/` does not clear the tag collection.

For both commands, UniMate saves the updated data automatically. A saving failure reports `Could not save data due to the following error: ...` or `Could not save data to file ... due to insufficient permissions to write to the file or the folder.` The in-memory change has already occurred in this case, so the error does not mean it was rolled back.

Neither command can be undone. After `tag` or `untag` changes a person's tags, earlier `add` and `edit` changes can no longer be undone or redone either. See [`undo`](#undoing-a-change-undo).

### Marking a favourite contact: `fav`

Marks a person in the currently displayed list as a favourite.

Format: `fav INDEX`

* `INDEX` must be a positive integer from `1` to `2147483647` and refer to a person in the currently displayed list. Zero, negative numbers, a leading `+`, decimals and extra arguments are not accepted.
* The person's other details, tags, major and class year are preserved, together with the current list filter.
* A successful command shows `Added to favourites: ` followed by the updated person's details.
* A favourited contact displays a solid heart (`♥`) inside a small circular border to the right of their name. Both the heart and border are red (`#e63946`), with a transparent background. The icon is a visual indicator and cannot be clicked.
* Favourite status is saved automatically and retained after restarting UniMate. New contacts and contacts loaded from older files without favourite status start as not favourited.

Example (assuming at least one person is displayed):

* `fav 1` marks the first displayed person as a favourite and immediately shows the circular heart.

**Error handling:**

| Input or situation | Result message |
|--------------------|----------------|
| Missing or invalid index, or extra arguments (e.g., `fav`, `fav 0`, `fav 1 extra`) | `Invalid command format!`, followed by the `fav` usage instructions. |
| Index exceeds the displayed list size | `The person index provided is invalid.` |
| Person is already favourited | `This person is already a favourite.` |

These errors leave contact data unchanged.

### Removing favourite status: `unfav`

Removes favourite status from a person in the currently displayed list.

Format: `unfav INDEX`

* `INDEX` follows the same rules as [`fav`](#marking-a-favourite-contact-fav).
* A successful command shows `Removed from favourites: ` followed by the person's details, and the circular heart disappears immediately.
* All other contact fields are preserved. The updated favourite status is saved automatically and remains removed after restarting UniMate.
* When viewing favourites using `listfav`, the person immediately disappears from that filtered view. The contact remains stored and can be seen again using `list`.

Example (assuming the first displayed person is favourited):

* `unfav 1` removes favourite status from that person.

**Error handling:**

| Input or situation | Result message |
|--------------------|----------------|
| Missing or invalid index, or extra arguments (e.g., `unfav`, `unfav -1`, `unfav 1 extra`) | `Invalid command format!`, followed by the `unfav` usage instructions. |
| Index exceeds the displayed list size | `The person index provided is invalid.` |
| Person is not favourited | `This person is not a favourite.` |

These errors leave contact data unchanged. After `unfav` removes a person from the favourite list, check the new displayed indices before entering another indexed command.

### Listing favourite contacts: `listfav`

Shows all favourited contacts in their existing order, with displayed indices starting at `1`. This replaces the previous search or list filter. Other contacts are hidden from the view, not deleted or modified.

Format: `listfav`

* No arguments are accepted. For example, `listfav 1` shows `Invalid command format!` followed by the `listfav` usage instructions, and leaves the current view unchanged.
* When favourites exist, the result is `Listed N favourite contact(s).`, where `N` is the number shown. Contact details and circular heart icons remain visible.
* If there are no favourites, the list is empty and the result is `No favourite contacts found.`

**Switching between lists:**

* The existing AB3 `list` command shows all contacts. The new `listfav` command shows only favourites. Both change the view without changing contact data or favourite status.
* `fav INDEX` and `unfav INDEX` always use the indices in the current view, including search results and the favourite list. A displayed index is not a permanent contact identifier.
* For example, run `list`, then `fav 1`, then `listfav`. Run `unfav 1` to remove the first person shown in the favourite list, then `list` to see all contacts again.
* The existing `add` and `edit` commands restore the full list after success. Run `listfav` again to return to favourites. Tag and untag operations preserve the current filter and favourite status.

### Adding or replacing a contact's note: `note`

Saves a short note for an existing contact. Each contact has one note; saving another replaces the previous text.

Format: `note INDEX TEXT`

* `INDEX` is the positive index shown in the current contact list, including search results.
* `TEXT` is required and must be a single line of up to 500 characters. Spaces, punctuation and Unicode text are allowed; control characters such as tabs are not.
* Leading and trailing whitespace is trimmed. Spaces within the note are preserved.
* Everything after the index is note text. No field prefix or quotation marks are needed; text such as `n/` or `t/` is saved literally.
* The contact's other details and the current list filter are preserved.
* Notes are saved automatically with contact data and remain available after restarting UniMate. New contacts have no note; use `note` after `add`.
* Adding or replacing a note can be [undone](#undoing-a-change-undo) and [redone](#redoing-a-change-redo). Saving the same text again does not create an undo step or clear any available redo history, because the note has not changed.

Example: `note 2 Met during CS2103T`

The message area displays `Note saved for NAME.` A small note icon at the bottom right of the contact card indicates that a note is present. Use `shownote` to read it.

### Viewing a contact's note: `shownote`

Displays the contact's note in the message area.

Format: `shownote INDEX`

Example: `shownote 2`

The message starts with `Note for NAME:`, followed by the saved text on the next line. If the contact has no note, it displays `No notes have been added to this contact.` Contact data, the current list filter, and undo/redo history are preserved.

### Deleting a contact's note: `delnote`

Removes the note from a contact while retaining the contact and all its other details.

Format: `delnote INDEX`

Example: `delnote 2`

The message area displays `Note deleted for NAME.`, and the note icon disappears. The current list filter is preserved. If the contact already has no note, the message is `This contact has no note to delete.` and its data stays unchanged.

Deleting an existing note can be undone and redone. Deleting an already empty note does not create an undo step or clear any available redo history.

For all three note commands, an index outside the displayed list reports `The person index provided is invalid.` Missing or invalid index syntax produces a usage error. `note INDEX` without text is also a usage error; use `delnote INDEX` to clear a note. `shownote` and `delnote` reject extra arguments. Invalid note text is rejected without changing the existing note.

### Listing all persons: `list`

Displays every contact currently saved in UniMate, including contacts loaded when you reopen the app.
Each contact card includes the person's name, tags, phone number,
address, email address, major, class year and [latest update date](#tracking-a-contacts-latest-update-date).
Optional academic fields that have no saved value are displayed as
`Major: Not specified` or `Class year: Not specified`.

Format: `list`

* `list` takes no parameters. Leading and trailing whitespace is accepted, but `list all`, `list 1` and
  `list t/Friend` show `Invalid command format!` followed by usage instructions. A rejected command leaves the view unchanged.
* Replaces a `find`, `filter` or `listfav` view with the full saved contact list. Contacts hidden by those views are
  shown again with displayed indices starting at `1`; deleted contacts are not recovered.
* Retains the active `sort` order. Without an active sort, contacts appear in their saved order. Check the displayed
  indices again before using an indexed command such as `edit` or `delete`.
* Lists all saved contacts; it does not provide a recently added view or sort contacts by their creation date.
* The result starts with `Listed all persons.` and reports `N person(s) listed!` on the next line, where `N` is the
  total number of contacts displayed. An empty address book succeeds with `0 person(s) listed!`.
* Viewing contacts does not change their details, recorded update dates or undo/redo history. Saved contacts remain
  available after restarting UniMate from the same folder; active searches, filters and sort choices are temporary.

Examples:

* Run `find Alex`, then `list` to return from search results to every saved contact.
* Run `listfav`, then `list` to include non-favourites again.
* Run `sort name asc`, then `filter m/Computing`, then `list` to show all contacts in ascending name order.

### Editing a person: `edit`

Edits an existing person in UniMate.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [m/MAJOR] [c/CLASS_YEAR] [t/TAG]... `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values. Fields whose prefixes are omitted remain unchanged.
* Use `m/MAJOR` to edit the academic major and `c/CLASS_YEAR` to edit the graduation year.
  A supplied major must be non-blank text on one line; a class year must be four digits from `2000` to `2099`.
* Enter `m/` or `c/` with no value to clear that optional field. Each of these prefixes may appear only once.
* Leading and trailing spaces are ignored. If any supplied value is invalid or an academic prefix is repeated, no fields are updated.
* After a successful edit, the full contact list is shown. If editing search results, run `find` again before using a search-result index for another edit.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.
* Enter `p/` or `e/` without a value to clear that contact method. Clearing both shows the automatic `incomplete` reminder; omitted phone/email prefixes preserve the existing values.
* A successful `edit` that changes the person's details can be reverted with [`undo`](#undoing-a-change-undo).

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 1 m/Computer Science c/2027` Updates the 1st person's major and graduation year.
*  `edit 1 m/ c/` Clears both academic fields while keeping the other details.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name, major or class year: `find`

Finds persons whose names, majors or class years contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names, majors and graduation years.
* Only full words match; for example, `Han` does not match `Hans`, and `Comp` does not match `Computer Science`.
* Persons matching at least one keyword in any of these fields are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.
* Contacts without a major or class year are not matched on that field.

Examples:
* `find John` returns `john` and `John Doe`
* `find Computer Science` returns persons whose major contains `Computer` or `Science`, such as `Computer Science` and `Computer Engineering`
* `find 2027` returns persons whose graduation year is `2027`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Filtering contacts by major, class year and tag: `filter`

Shows contacts whose complete field values match **all** supplied criteria (AND matching).

Format: `filter [m/MAJOR] [c/CLASS_YEAR] [t/TAG]`

* Supply at least one criterion. Each prefix may appear only once, in any order, separated by spaces.
* Major and tag matching ignore case but require the complete value: `m/Computer Science` matches
  `Computer Science`, while `m/Computer` does not. A tag criterion matches any one of the contact's tags.
* Class year matches exactly and must be four digits from `2000` to `2099`.
* Omitted criteria leave those fields unrestricted. An unspecified academic field does not match a
  supplied criterion for that field, but the contact can still match a criterion on another field.
* Leading and trailing spaces around values are ignored; spaces within a major remain significant.
* Blank values, repeated prefixes, unsupported single-letter prefixes and invalid values produce an
  error without changing the current results. Use `t/Friend` to filter a default tag; `d/` is not supported.
* The result message shows the number of matching contacts. Zero matches displays an empty list;
  no contacts are deleted.
* Each `filter` replaces the previous search or filter and retains the active sort order, or the saved
  contact order if no sort has been chosen. `find` replaces the filter; `list` shows all contacts again.
* Indices refer to the currently displayed list. For example, `delete 1` targets the first filtered result.
  Deleting or untagging a contact that no longer matches removes it from the results and renumbers the list.
* Successful `add`, `edit`, `undo` and `redo` commands show the full list.
  Reapply the filter before using a filtered index again. Tag and untag retain the active filter.
* Filters are temporary: restarting UniMate shows the full contact list. Filtering does not create
  an undoable data change or clear redo history.

Examples:
* `filter m/Computer Science c/2027 t/Classmate` shows contacts who match all three criteria.
* `filter c/2027` shows all contacts with class year `2027`, regardless of their major or tags.
* `filter t/friend` shows contacts with a complete tag matching `friend`, ignoring case.
* `list` returns to the full contact list.

Unlike `find Computer Science`, which searches for either word across names and academic fields,
`filter m/Computer Science` checks only the complete major value.

### Sorting contacts by name, major or class year: `sort`

Orders the displayed contacts while keeping the current search or filter.

Format: `sort FIELD DIRECTION`

* Both arguments are required and must appear in this order, in lowercase.
  Extra surrounding spaces and spaces between arguments are allowed.

  | Argument | Accepted values | Meaning |
  | --- | --- | --- |
  | `FIELD` | `name`, `major`, `year` | Full name, full major or graduation year |
  | `DIRECTION` | `asc`, `desc` | A–Z / earliest first, or Z–A / latest first |

* Names and majors are compared case-insensitively. Class years are ordered chronologically.
* Contacts with an unspecified major or class year appear **last** when sorting by that field,
  for both directions. Equal academic values, including unspecified values, use ascending name order.
  Names differing only in case compare equally.
* Sorting an empty list or a single contact succeeds. Sorting does not add or remove contacts.
* `filter`, `find` and `list` retain the chosen sort order. A successful `add`, `edit`, `undo` or `redo`
  shows all contacts in that order; tag, untag and delete retain the current filter and sort.
  Changing a sorted field can move a contact to a new position.
* Numbered commands use the **currently displayed order**. Check the latest card numbers before
  using `edit`, `delete`, `tag` or `untag`; sorting and contact changes can renumber the list.
* Missing arguments, unsupported fields or directions, uppercase arguments and extra arguments produce
  a usage error without changing the current results or saved data.
* Sorting is temporary. It does not reorder saved contacts, create an undoable change or clear redo history.
  Restarting restores the saved contact order and shows all contacts.

Examples:
* `sort name asc` displays names from A to Z.
* `sort major desc` displays majors from Z to A, with unspecified majors last.
* `filter t/Classmate`, then `sort year asc`, shows only classmates, earliest class year first.
  `list` then shows all contacts in the same year order.

### Deleting a person: `delete`

Deletes the specified person from UniMate.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...
* The deleted person message includes the deleted contact's major and class year when those fields are specified.
* A deletion cannot be undone. After a successful `delete`, earlier `add` and `edit` changes can no longer be undone or redone either.

Examples:
* `list` followed by `delete 2` deletes the 2nd person in UniMate.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.
* Deleting a contact with academic fields shows a message such as
  `Deleted person: Alex Tan; Phone: 91234567; Email: alex@example.com; Address: 10 Kent Ridge Road; Major: Computer Science; Class year: 2027; Tags: [classmate]`.

### Clearing all entries: `clear`

Clears all entries from UniMate.

Format: `clear`

<box type="warning" seamless>

**Caution:** `clear` cannot be undone. After it removes your contacts, earlier `add` and `edit` changes can no longer be undone or redone either.
</box>

### Undoing a change: `undo`

Reverts the most recent change made by `add` or `edit`.

Format: `undo`

* Restores your contacts to how they were before the most recent `add` or `edit` that has not already been undone.
* Run `undo` again to revert earlier `add` and `edit` changes one at a time, starting from the most recent.
* After an undo, the full contact list is shown, even if you were viewing the results of `find`.
* Only `add` and `edit` can be undone. An `edit` that leaves every detail unchanged does not count as a change.
* Commands that fail, such as an `add` rejected as a duplicate, and commands that do not change contacts, such as `list`, `find`, `tags` and `help`, do not affect what `undo` reverts.
* `delete`, `tag`, `untag` and `clear` cannot be undone. After one of them changes your contacts, earlier `add` and `edit` changes can no longer be undone or redone either. Changes you make after that can be undone as usual.
* `undo` does not take any parameters. Unlike `help`, extra input such as `undo 1` is rejected and nothing is undone.
* If there is nothing to undo, UniMate shows `There is no change to undo. Only add and edit can be undone, and not after a delete, tag, untag or clear.` and your contacts are unchanged.
* Undo history is kept only while UniMate is open. It is cleared when you close UniMate, so changes from an earlier session cannot be undone.
* The restored contacts are saved automatically.

Examples:
* `add n/Alex Tan p/91234567 e/alex@example.com a/10 Kent Ridge Road` followed by `undo` removes `Alex Tan` again.
* `edit 2 p/98765432` followed by `undo` restores the 2nd person's previous phone number.
* `add n/Alex Tan p/91234567 e/alex@example.com a/10 Kent Ridge Road`, then `edit 1 c/2027`, then `undo` twice first reverts the edit and then removes the added contact.

### Redoing an undone change: `redo`

Reapplies the most recent change that was reverted by `undo`.

Format: `redo`

* The change undone most recently is redone first. Running `redo` repeatedly therefore reapplies undone changes in the order you originally made them.
* After a redo, the full contact list is shown, even if you were viewing the results of `find`.
* Undone changes can be redone only until you change your contacts again. After a successful `add` or `edit`, or a `delete`, `tag`, `untag` or `clear` that changes your contacts, `redo` is no longer available.
* Commands that fail, and commands that do not change contacts, such as `list`, `find`, `tags` and `help`, do not affect what `redo` reapplies.
* `redo` does not take any parameters. Extra input such as `redo 1` is rejected and nothing is redone.
* If there is nothing to redo, UniMate shows `There is no change to redo. Only an undone add or edit can be redone, and not after another change has been made.` and your contacts are unchanged.
* Like undo history, redo history is cleared when you close UniMate. The restored contacts are saved automatically.

Examples:
* `add n/Alex Tan p/91234567 e/alex@example.com a/10 Kent Ridge Road`, then `undo`, then `redo` adds `Alex Tan` back.
* `edit 1 p/91234567`, then `undo`, then `redo` changes the 1st person's phone number to `91234567` again.

<box type="tip" seamless>

**Tip:** If you undo a change by mistake, run `redo` before making any other change to your contacts.
</box>

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Navigating command history

With the command box focused, press **↑** to recall the most recently submitted command.
Press **↑** again to browse older commands, or **↓** to browse newer commands.
After the newest command, **↓** restores the draft you were typing before browsing.
At the oldest command, further **↑** presses keep that command displayed.

Commands are recorded when you press Enter, including unsuccessful commands and repeated submissions.
Empty input is not recorded. You can edit a recalled command and press Enter to submit it again;
browsing alone does not execute commands. Edits made while browsing are not saved in the history.
History is cleared when you close UniMate.

### Saving the data

UniMate automatically saves data after every command. You do not need to save manually.

### Editing the data file

UniMate data is saved automatically as a JSON file `[JAR file location]/data/unimate.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, UniMate starts with an empty contact list at the next run. The invalid file remains on disk until you run a command (UniMate saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause UniMate to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

--------------------------------------------------------------------------------------------------------------------

## Coming soon

The following features are not available in the MVP.

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous UniMate home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME [p/PHONE_NUMBER] [e/EMAIL] a/ADDRESS [m/MAJOR] [c/CLASS_YEAR] [t/TAG]... [d/DEFAULT_INDEX]...` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [m/MAJOR] [c/CLASS_YEAR] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`, `find Computer Science`, `find 2027`
**Filter** | `filter [m/MAJOR] [c/CLASS_YEAR] [t/TAG]` (at least one criterion)<br> e.g., `filter m/Computer Science c/2027 t/Classmate`
**Sort**   | `sort FIELD DIRECTION`<br> e.g., `sort name asc`, `sort major desc`, `sort year asc`
**List all saved contacts** | `list` (no parameters; clears the current filter and reports the total)
**Favourite** | `fav INDEX`<br> e.g., `fav 1`
**Unfavourite** | `unfav INDEX`<br> e.g., `unfav 1`
**List favourites** | `listfav`
**Help**   | `help`
**Tags**   | `tags`
**Tag**    | `tag INDEX [d/DEFAULT_INDEX]... [t/TAG]...` (at least one tag)<br> e.g., `tag 1 d/2 t/NOC`
**Untag**  | `untag INDEX t/TAG [t/TAG]...`<br> e.g., `untag 1 t/NOC`
**Note**   | `note INDEX TEXT`<br> e.g., `note 2 Met during CS2103T`
**Show note** | `shownote INDEX`<br> e.g., `shownote 2`
**Delete note** | `delnote INDEX`<br> e.g., `delnote 2`
**Undo**   | `undo`
**Redo**   | `redo`
