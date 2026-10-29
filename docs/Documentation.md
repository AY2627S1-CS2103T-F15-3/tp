---
  layout: default.md
  title: "Documentation guide"
  pageNav: 3
---

# Documentation Guide

* We use [**MarkBind**](https://markbind.org/) to manage documentation.
* The `docs/` folder contains the source files for the documentation website.
* To learn how set it up and maintain the project website, follow the guide [[se-edu/guides] Working with Forked MarkBind sites](https://se-education.org/guides/tutorials/markbind-forked-sites.html) for project documentation.

**Style guidance:**

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).


**Diagram width**

* The text column is at most about 960px wide, and narrower when the side navigation bars are shown
  (see `#content-wrapper` and the navigation breakpoints in `stylesheets/main.css`):

  | Browser window width | Approximate text column width |
  | --- | --- |
  | Below 992px | Window width minus 40px (no side bars) |
  | 992px to 1299px | Window width minus 340px (left site navigation shown); about 940px at 1280px |
  | 1300px to 1599px | Window width minus 640px (both side bars shown); about 660px at 1300px |
  | 1600px and above | About 960px (column width capped) |

* A diagram wider than the column is scaled down to fit, so its text becomes smaller than body text.
  Keep diagrams at or below about 940px wide where possible. Narrowing them cannot prevent the
  shrinking between 1300px and 1600px, which comes from MarkBind's layout rather than the diagrams.
* Prefer each diagram's natural size. Avoid a `width` attribute that enlarges a small diagram.

**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
