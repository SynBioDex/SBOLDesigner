SBOL Designer
=============

SBOL designer allows you to visualize SBOL designs using SBOL visual icons, edit SBOL designs 
in a GUI, import genetic constructs from an SBOL parts registry, and save your design an SBOL file.

Running SBOL Designer stand-alone
=================================

SBOL designer requires Java 8 or later to be installed on your computer. To run SBOL designer, 
simply double click the jar file. If your OS does not support running Java applications by
double clicking, you can run the designer from command-line by using the `java -jar <jarFileName>`
command at a terminal where `jarFileName` will be the name of the SBOL Designer jar file you
downloaded.

Compilation
=================================
In order to build SBOLDesigner from source, type the following into a Windows shell or create an Eclipse Maven run configuration that has the following goals (excluding the "mvn") after cloning this repository:
mvn clean install exec:java -Dexec.mainClass="com.clarkparsia.sbol.editor.SBOLDesignerStandalone"
mvn package

More information
================

See [http://github.com/SynBioDex/SBOLDesigner/](http://github.com/SynBioDex/SBOLDesigner/) for more information on SBOL designer. Send your questions and comments about SBOL Designer to [michael13162@gmail.com](mailto:michael13162@gmail.com).

For the previous version (unsupported) of SBOL Designer, see [http://github.com/clarkparsia/sbol/](http://github.com/clarkparsia/sbol/).
