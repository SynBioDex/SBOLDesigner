Version 0.4
-----------
* ADDED:	User-defined SPARQL endpoints for introducing custom parts registries 
* ADDED:	Improve standalone editor (remember loaded file, warn about unsaved changes, etc.)
* ADDED:	Ability in standalone editor to load designs from a SPARQL endpoint
* MODIFIED:	Heuristics to guess variant type for verification annotations
* FIXED:	Sequence Ontology types used for variant annotations
* FIXED:	URIs of DNA components imported from parts registry
* FIXED:	Preserving URIs during Geneious import/export operations
* FIXED:	Statistics computation when there are annotations without an interval

Version 0.3.5
-------------
* ADDED:	Ability to take a snapshot image of the SBOL design
* FIXED:	Layout problems in Windows

Version 0.3.4
-------------
* ADDED:	Coverage computation for alignments
* ADDED:	A new toolbar item to add a scar between any two non-scar component in the design
* ADDED:	Ability to take a snapshot of the SBOL design as an image file
* FIXED:	Some layout problems in different Operating system (different LookAndFeelManagers)
* FIXED:	Division by zero error in pairwise Pairwise Identity (%) statistics
* FIXED:	Exporting designs with same component appearing multiple times

Version 0.3.3
-------------
* ADDED:	New parts data from Mike with improved type information
* ADDED:	Remember users's "Hide scars" preference when different documents are opened 
* FIXED:	Initialization problems with the plugin
* FIXED:	Backward compatibility with Geneious 5.5.x versions

Version 0.3.2
-------------
* ADDED:	Include Pairwise Identity (%) statistics in the information copied to clipboard
* ADDED:	Recognizing Scar type in Geneious annotations  
* MODIFIED:	Improved heuristics for detecting sequence variant types 
* FIXED:	Handle inserting parts without a recognized type
* FIXED:	Bugs due to confusion between 0-based and 1-based addressing in coordinates of components
* FIXED:	SBOL export bug after modifying a sequence manually
* FIXED:	Handle sequence verification annotations added to unsupported alignment documents
* FIXED:	Tooltips in SBOL visual not being updated after the strand of a component is flipped 	

Version 0.3.1
-------------
* MODIFIED:	Add sequence verification annotations as undirected annotations
* FIXED:	Save strand info when SBOL visual design is saved
* FIXED:	Copying sequence variant annotations to clipboard when files do not have creation dates
* FIXED:	Handling designs where there are two components/annotations at identical intervals and separate strands 

Version 0.3
-----------
* ADDED:	Editing designs with the SBOL visual viewer, part selection through a parts registry
* ADDED:	SBOL import and export capability for partial designs (components without explicit coordinates)
* MODIFIED:	Improve sequence variant annotations copied to the clipboard to contain more info
* FIXED:	Handling components on the negative strand 
* FIXED:	Coordinates of nested components in SBOL export to be relative to the parent component 
* FIXED:	SBOL export problems for annotations when design document has unsaved saves or is deleted
* FIXED:	SBOL import to correctly read DnaComponent types 

Version 0.2
-----------
* ADDED:	SBOL export functionality
* ADDED:	New SBOL serialization format for the sequence variant
* ADDED:	SBOL visual document viewer (experimental)
* ADDED:	Default keyboard binding for adding sequence variant annotations
* MODIFIED:	Add sequence variant annotations to the alignment doc not the design doc

Version 0.1
-----------
* ADDED:	SBOL import functionality
* ADDED:	Create sequence variant annotations
* ADDED:	Copy sequence variant annotations to clipboard