# Documentaion for Export and Contact attachment features

This is mainly documentation for the code implementing the contact attachment functionality and note exporter. The documentation for Omni Notes original code is mostly undocumented, so documentation for this code consists of what doxygen auto-generates for undocumented code.

## Omni Notes

As mentioned, the original Omni Notes source isn't very well documented. This gives some information about Omni Notes classes used by source for this project.

- [Note](@ref it.feio.android.omninotes.models.Note) Represents a Note in the application. This is what the exporter code exports.
- [DetailFragment](@ref it.feio.android.omninotes.DetailFragment) has the main responsibility for implementing the GUI that shows a note. This class has most the the GUI related code for the contact attachment and the exporter.

### DetailFragment

When the user selects *Export* in the meny the `showExportPopup` is called initiate the export. The method start by showing a dialog to let the user select a file format. After the user has selected a format the method this then calls `handleExportPopupResult` to show the intent for choosing a filename. When the intent successfully returns the [ExportNoteTask](@ref it.feio.android.omninotes.async.notes.ExportNoteTask) will take over and export the file to storage.

## Exporter Overview

Most of the code related to the exporting code is implemented in `it.feio.android.omninotes.export` package. This gives a brief overview:

- [Exporter](@ref it.feio.android.omninotes.export.Exporter) interface used by the application to access exporter code. Used by [DetailFragment](@ref it.feio.android.omninotes.DetailFragment) class.
- [ExporterBase](@ref it.feio.android.omninotes.export.ExporterBase) abstract class that implements exporter interface. This class in turn is implemnted by each file format implementation.
- [ExporterFactory](@ref it.feio.android.omninotes.export.ExporterFactory) the application code doesn't directly instantiate the exporter implementations, instead this class should be used. Used by [DetailFragment](@ref it.feio.android.omninotes.DetailFragment) class.
- [NoteFacade](@ref it.feio.android.omninotes.export.NoteFacade) class is used by the exporter implementations to convert the information [Note](@ref it.feio.android.omninotes.models.Note) in a note to a human readable format.
- [ExportNoteTask](@ref it.feio.android.omninotes.async.notes.ExportNoteTask) this class implements AsyncTask for exporting a note in the background. Used by the GUI-code when a note needs to be exported.

## Exporter Tests Overview

- [ExportTestBase](@ref it.feio.android.omninotes.ExportTestBase) base class for GUI part of the export. Each file format test implements this this class.
- [ExporterNoteFacadeTest](@ref it.feio.android.omninotes.ExporterNoteFacadeTest) tests [NoteFacade](@ref it.feio.android.omninotes.export.NoteFacade) class to make sure it returns the correct information.
