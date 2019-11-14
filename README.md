SBOL Designer
=============

SBOL designer allows you to visualize SBOL designs using SBOL visual icons, edit SBOL designs 
in a GUI, import genetic constructs from an SBOL parts registry, and save your design an SBOL file.

Running SBOL Designer stand-alone
=================================

SBOLDesigner requires Java 8 to be installed on your computer. To run SBOL designer, 
simply double click the jar file. If your OS does not support running Java applications by
double clicking, you can run the designer from command-line by using the `java -jar <jarFileName>`
command at a terminal where `jarFileName` will be the name of the SBOL Designer jar file you
downloaded.

Compilation
=================================

In order to build SBOLDesigner from source, type the following into a Windows shell or create an Eclipse Maven run configuration that has the following goals (excluding the "mvn") after cloning this repository:
mvn clean install exec:java -Dexec.mainClass="edu.utah.ece.async.sboldesigner.sbol.editor.SBOLDesignerStandalone" && mvn package

Citation
========

M. Zhang, J. McLaughlin, A. Wipat, and C. Myers, [https://pubs.acs.org/doi/abs/10.1021/acssynbio.6b00275](SBOLDesigner 2: An Intuitive Tool for Structural Genetic Design, in ACS Synthetic Biology), 6(7): 1150-1160, July 21, 2017.

More information
================

See [http://github.com/SynBioDex/SBOLDesigner/](http://github.com/SynBioDex/SBOLDesigner/) for more information on SBOL designer. Send your questions and comments about SBOL Designer to [michael13162@gmail.com](mailto:michael13162@gmail.com).

For the previous version (unsupported) of SBOL Designer, see [http://github.com/clarkparsia/sbol/](http://github.com/clarkparsia/sbol/).
