## Documentation for registering DOI files via Datacite

## Description

The programme examines the metadata fields of a METS-MODS file from a Goobi process. From these it creates the necessary data for a DOI, and registers it via Datacite.


## Installation and configuration

The programme consists of four files:

```
plugin_intranda_step_datacite_doi.jar
plugin_intranda_step_datacite_doi-GUI.jar
plugin_intranda_step_datacite_doi.xml
plugin_intranda_step_datacite_mapping.xml
```

The file `plugin_intranda_step_datacite_doi.jar` contains the program logic and is an executable file, and should be copied into
`/opt/digiverso/goobi/plugins/import`.

The file `plugin_intranda_step_datacite_doi-GUI.jar` contains data for the presentation of the plugin, and should be copied into
`/opt/digiverso/goobi/plugins/GUI`.

The file `plugin_intranda_step_datacite_mapping.xml` is the mapping file, defining how local metadata should be translated to the form required for the DOI, and should be copied into `/opt/digiverso/goobi/config/`.

The file `plugin_intranda_step_datacite_doi.xml` is the config file, and should be copied into `/opt/digiverso/goobi/config/`.

The config file is used to configure the plug-in and must be structured as follows:

```xml
<config_plugin>
    <config>
        <!-- which projects to use for (can be more then one, otherwise use *) -->
        <project>*</project>
        <step>*</step>

        <!-- authentication and main information -->
        <!-- For testing: for deployment, remove "test" -->
        <SERVICE_ADDRESS>https://mds.test.datacite.org/</SERVICE_ADDRESS>

        <base>10.80831</base>
        <url>https://viewer.goobi.io/idresolver?handle=</url>
        <PASSWORD>password</PASSWORD>
        <USERNAME>YZVP.GOOBI</USERNAME>

        <!-- configuration for Handles -->
        <prefix></prefix>
        <name></name>
        <separator>-</separator>
        <handleMetadata>DOI</handleMetadata>
        
        <!-- configuration for DOIs -->
        <doiMapping>/opt/digiverso/goobi/config/plugin_intranda_step_datacite_mapping.xml</doiMapping>


        <!-- display button to finish the task directly from within the entered plugin -->
        <allowTaskFinishButtons>true</allowTaskFinishButtons>
    </config>

</config_plugin>
```

A copy is in this repro, in the top folder.

The element `"SERVICE_ADDRESS"`
returns the URL for the Datacite service. In the example above, it is the test server.

The element `"base"`
is the DOI base for the institution, which has been registered with Datacite.

The element `"url"`
is the prefix accorded to each DOI link. A DOI "10.80831/goobi-1", for example, will here be given the hyperlink "https://viewer.goobi.io/idresolver?handle=10.80831/goobi-1"

The elements `"PASSWORD"` and `"USERNAME"`
record the identification information registered with Datacite.

The elements `"prefix"`, `"name"` and `"separator"` specify a prefix that may be given to the Handle of the DOi: eg "go-goobi-" before the ID number of the document.

The element `"handleMetadata"`
specifies under which metadata name the handle for the DOI is to be saved in the METS-MODS file. Default is "_urn".

The element `"doiMapping"`
specifies the path to the mapping file.

The element `"allowTaskFinishButtons"`
allows the task to be stopped from within the plugin.


The mapping file looks something like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Mapping>
    <!-- Mandatory fields: -->
    <map>
        <field>title</field>
        <metadata>TitleDocMain</metadata>
        <altMetadata>Title</altMetadata>
        <default>Fragment</default>
    </map>

    <map>
        <field>author</field>
        <metadata>Author</metadata>
        <default>unkn</default>
    </map>

    <map>
        <field>publisher</field>
        <metadata>Publisher</metadata>
        <altMetadata>Source</altMetadata>
        <altMetadata>PublisherName</altMetadata>
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
        <field>editor</field>
        <metadata>Editor</metadata>
    </map>

</Mapping>
```

For each <map>, the <field> specifies the name of the DOI element, and the <metadata> and <altMetadata> entries indicate from which metadata of the mods file the value should be taken, in order. If there is no such entry in the mods file, then the <default> value is taken. The value "unkn" for "unknown" is recommended by Datacite for data which is missing.

For the mandatory fields, a <default> _must_ be specified; for optional fields this is not necessary, but may be done if wished.

The default entry `#CurrentYear` is a special case: it is replaced with the current year in the DOI.


## Mode of operation

The programme examines the metadata fields of a METS-MODS file from a Goobi process. From these it creates the data for a DOI, using the mapping file to translate. It then registers the DOI via Datacite, and writes the generated DOI into the METS-MODS file under the metadata specified in <handleMetadata>.

### Installation 

A process task must be defined, using the step plugin `intranda_step_datacite_doi`. The plugin writes the DOI to the metadata of the process, so the boxes for "Metadata" and "Update metadata index when finishing" should be ticked. The task can then be carried out either automatically or manually.


