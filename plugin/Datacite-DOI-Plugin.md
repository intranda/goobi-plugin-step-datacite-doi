---
description: >-
  This is technical documentation for the plugin for registering DOIs with Datacite.
---

# Plugin for registering DOI files via Datacite

## Introduction

This documentation describes the installation, configuration and use of the plugin.

| Details |  |
| :--- | :--- |
| Identifier | plugin_intranda_step_datacite_doi |
| Source code | [https://github.com/intranda/intranda_step_datacite_doi](https://github.com/intranda/intranda_step_datacite_doi) |
| Licence | GPL 2.0 or newer |
| Compatibility | Goobi workflow 2021.03 |
| Documentation date | 23.03.2021 |

### Installation

The program consists of these files:

```
plugin_intranda_step_datacite_doi.jar
plugin_intranda_step_datacite_doi.xml
plugin_intranda_step_datacite_mapping.xml
```

The file `plugin_intranda_step_datacite_doi.jar` contains the program logic, and should be copied to this path:
`/opt/digiverso/goobi/plugins/step`.

The file `plugin_intranda_step_datacite_mapping.xml` is the mapping file, defining how local metadata should be translated to the form required for the DOI, and should be copied to the folder `/opt/digiverso/goobi/config/`.

The file `plugin_intranda_step_datacite_doi.xml` is the config file, and should be copied to the folder `/opt/digiverso/goobi/config/`.


## Configuration

The configuration is done via the configuration file `plugin_intranda_step_datacite_doi.xml` and can be adapted during operation. It is structured as follows:

```xml
<config_plugin>
  <config>

		<!-- which projects to use for (can be more then one, otherwise use *) -->
		<project>*</project>
		<step>*</step>

        <!-- authentication and main information -->
        <!-- For testing: for deployment, remove "test" -->
        <serviceAddress>https://mds.test.datacite.org/</serviceAddress>

		<!-- authentication and main information -->
		<base>10.80831</base>
		<url>https://viewer.goobi.io/idresolver?handle=</url>
		<password></password>
		<username></username>

		<!-- configuration for Handles -->
		<prefix>go</prefix>
		<name>goobi</name>
		<separator>-</separator>
		<handleMetadata>_urn</handleMetadata>

		<!-- configuration for DOIs -->
		<doiMapping>/opt/digiverso/goobi/config/plugin_intranda_step_datacite_mapping.xml</doiMapping>

        <!-- Types of DocStruct which should be given DOIs -->
        <typeForDOI>PeriodicalVolume</typeForDOI>
        <typeForDOI>Article</typeForDOI>

	</config>
</config_plugin>
```

The configuration allows different configurations for different process templates. For this purpose, only the name of the desired template must be entered in the `project` field. The entry with the value `*` is used for all projects for which no separate configuration exists.

| Value  |  Description |
|---|---|
|  `project` | This parameter determines for which project the current block` <config>` is to apply. The name of the project is used here. This parameter can occur several times per `<config>` block.  |
|  `step` | This parameter controls for which workflow steps the block `<config>` should apply. The name of the workflow step is used here. This parameter can occur several times per `<config>` block.  |
|   `serviceAddress` |This parameter defines the URL for the Datacite service. In the example above, it is the test server.   |   
|  `base` |This parameter defines the DOI base for the institution, which has been registered with Datacite. |   
|  `url` | The url parameter defines the prefix accorded to each DOI link. A DOI "10.80831/goobi-1", for example, will here be given the hyperlink "https://viewer.goobi.io/idresolver?handle=10.80831/goobi-1"  |
| `username`  | This is the username that is used for the DataCite registration.|
|  `password`  | This is the password that is used for the DataCite registration.  |
| `prefix` |This is the prefix that may be given to the DOI before the name and ID of the document.  |
|  `name` | This is the name that may be given to the DOI before the ID number of the document.  |
|  `separator` |  Define here a separator that shall be used between the different parts of the DOI.s |
|  `handleMetadata` | specifies under which metadata name the handle for the DOI is to be saved in the METS-MODS file. Default is `_urn`.|
|  `doiMapping` | the path to the mapping file. |
|  `typeForDOI` | With this parameter DocStruct types can be defined which will be given DOIs. If this is empty or missing, the top DocStruct element only will be given a DOI. If the parameter contains the name of a sub-DocStruct, then these will be given DOIs. Several types of DocStruct may be specified by repeating this parameter, in that case all DocStructs specified will be given DOIs.|



## Mapping file
The mapping configuration file looks something like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Mapping>
    <!-- Mandatory fields: -->
    <map>
        <field>title</field>
        <metadata>TitleDocMain</metadata>
        <altMetadata>TitleDocMainShort</altMetadata>
        <altMetadata>Title</altMetadata>
        <default>Fragment</default>
    </map>

    <map>
      <field>author</field>
  		<metadata>Author</metadata>
  		<altMetadata>Composer</altMetadata>
  		<altMetadata>IllustratorArtist</altMetadata>
  		<altMetadata>WriterCorporate</altMetadata>
  		<default>unkn</default>
    </map>

    <map>
      <field>publisher</field>
  		<metadata>Publisher</metadata>
  		<altMetadata>PublisherName</altMetadata>
  		<altMetadata>PublisherPerson</altMetadata>
  		<default>unkn</default>
    </map>

    <map>
        <field>publicationYear</field>
        <metadata>_dateDigitization</metadata>
        <default>#CurrentYear</default>
    </map>

    <map>
        <field>inst</field>
        <default>intranda</default>
    </map>


    <map>
        <field>resourceType</field>
        <default>document</default>
    </map>

    <!-- Optional fields: -->

    <map>
        <field>ISSN</field>
        <metadata>anchor_ISSN</metadata>
    </map>

    <listMap>
        <field>editor</field>
        <list>contributors</list>
        <metadata>Editor</metadata>
    </listMap>

    <listMap dateType="Created">
        <field>date</field>
        <list>dates</list>
        <metadata>Dating</metadata>
        <altMetadata>PublicationYear</altMetadata>
        <altMetadata>anchor_PublicationYear</altMetadata>
    </listMap>

</Mapping>
```

For each `<map>`, the `<field>` specifies the name of the DOI element, and the `<metadata>` and `<altMetadata>` entries indicate from which metadata of the DocStruct the value should be taken, in order. If there is no such entry in the DocStruct, then the `<default>` value is taken. The value `"unkn"` for "unknown" is recommended by Datacite for data which is missing. If the <typeForDOI> is a chapter or other substructure of a containing docstruct, then a values if first looked for in the lower level (eg. chapter), and if none is found there then is is looked for in the parent docstruct.

A `<metadata>` or `<altMetadata>` entry staring with `anchor_` will only be searched for in the documant's anchor file `meta_anchor.-xml`, if such a file exists.

For the mandatory fields, a `<default>` _must_ be specified; for optional fields this is not necessary, but may be done if wished.

The default entry `#CurrentYear` is a special case: it is replaced with the current year in the DOI.

Elements of type `<listMap>` rather than  `<map>` are output in a list, named by the subentry `<list>`. If the `<listMap>` has an attribute, then this will be copied for every entry of the list. 
In the example above this looks like:

```
    <dates>
        <date dateType="Created">1847</date>
    </dates>
```

### Integration of the plugin into the workflow

To put the plugin into operation, it must be activated for one or more desired tasks in the workflow. This is done as shown in the following screenshot by selecting the plugin intranda_step_datacite_doi from the list of installed plugins.


Since this plugin is usually to be executed automatically, the workflow step should be configured as automatic in the workflow. As the plugin writes the DOI to the metadata file of the process, the checkbox for Update metadata index when finishing should be ticked.

### Operation of the plugin

Here we use the term "DOI" to refer to the saved record containing metadata and a hyperlink. The ID number of the DOI we refer to here as a "handle", as it is also registered in the Handle Network.

The program examines the metadata fields of a METS-MODS file from a Goobi process. If one or more `<typeForDOI>` is specified, then it will go through each DocStruct of these types in the file. If not, then it will take the top DocStruct which is not an anchor. From these it creates the data for a DOI, using the mapping file to translate.

If the DocStruct already has a handle registered (under `<handleMetadata>`), then this handle will be updated with the newly generated DOI data.

If the handle is not yet registered, the plugin registers the DOI via the MDS API of DataCite, with handle given by the `<base>` together with any `<prefix>` and `<name>`, and the ID of the document (its CatalogIDDigital) plus an increment, if there are more than one DOIs generated for the given document. The record is given a registered URL defined by the `<url>` followed by the DOI. The generated handle is written into the METS/MODS file under the metadata specified in `<handleMetadata>`. If there is a `<typeForDOI>` of type Article for example, then each Article in the METS/MODS file will be given a DOI, with the handle for the DOI saved in the metadata under `<handleMetadata>` for each Article.
