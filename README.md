Compilation (added January, 2017)
=================================
In order to clean, install, and run SBOLDesigner, type the following into a Windows shell or create an Eclipse Maven run configuration that has the following goals (excluding the "mvn"):
mvn clean install exec:java -Dexec.mainClass="com.clarkparsia.sbol.editor.SBOLDesignerStandalone"

In order to generate sources from Google protocol buffer, right click on the project and run-as Maven generate-sources.  This is required first.  Also, the pom.xml file has protobuffer dependencies as described here:
http://vlkan.com/blog/post/2015/11/27/maven-protobuf/

libRepo is a local Maven repository that is also an included dependency.  This allows for jar files to be included using Maven's pom.xml.  For updates, refer to "Creating a maven repo inside the project" here:
http://blog.valdaris.com/post/custom-jar/


SBOL Designer
=============

SBOL designer allows you to visualize SBOL designs using SBOL visual icons, edit SBOL designs 
in a GUI, import DNA components from an SBOL parts registry, and save your design an SBOL RDF/XML
file.

SBOL Designer can be used as a standalone program or as a [Geneious](http://www.geneious.com/) plugin.

Running SBOL Designer stand-alone
=================================

SBOL designer requires Java 8 or later to be installed on your computer. To run SBOL designer, 
simply double click the jar file. If your OS does not support running Java applications by
double clicking, you can run the designer from command-line by using the `java -jar <jarFileName>`
command at a terminal where `jarFileName` will be the name of the SBOL Designer jar file you
downloaded.

UPDATE: Alternatively, you can build the program yourself; all of the dependencies are included.
 
Using SBOL Designer plugin in Geneious
======================================

SBOL Designer plugin has been tested with Geneious 5.5 and later. To install the plugin, start
Geneious, go to Tools->Plugins menu option, click "Install plugin from a gplugin file" and select 
the SBOL designer gplugin file. The plugin will be ready to use immediately.

If you download a newer version of the plugin at a later time you can follow the same steps and
the new version of the plugin will replace the old version.

UPDATE: Geneious plugin dependencies have been removed.  SBOL Designer will function more as a standalone application in the future.  See below if you wish to use the unsupported version of SBOL Designer (with Geneious plugin capabilities).

More information
================

See [http://github.com/SynBioDex/SBOLDesigner/](http://github.com/SynBioDex/SBOLDesigner/) for more information
on SBOL designer. Send your questions and comments about SBOL Designer to 
[michael13162@gmail.com](mailto:michael13162@gmail.com).

For the previous version (unsupported) of SBOL Designer, see [http://github.com/clarkparsia/sbol/](http://github.com/clarkparsia/sbol/).
