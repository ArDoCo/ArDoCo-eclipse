# textAnalysis.provider
This plugin specifies the interface that needs to be implemented to provide an Analysis for the project.
This is a standalone plugin so it can be imported by the implementing Analysis without other code overhead.

## How to implement a new Analysis as a Service
1. Create a new project
2. Create a Class (your Analysis) that implements the /src/textAnalysis/provider/AnalysisProvider Interface.
3. Add the folders META-INF/services to your folder
4. In services, add a File "textAnalysis.provider.AnalysisProvider" which contains the name (incl. packages) of your implementing Analysis Class.
5. Export this as a jar. In order that the configuration will find the jar, see Infos in the textAnalysis.core/README.md

## Good to know about the provider Interace
It contains only three methods:
- getName -> a *unique* name of your analysis. This will be used for the checkbox in the run-configuration as well as for the XML-Tag in the output file. If there are two analysis with the same name, the plugin will not handle this correctly and will not produce an error.
- isValid -> This checks, if all preconditions are met so that the analysis can be executed correctly. Current example analysis always return true because they are so simple and not dependent from any attribute.
- getXMLEvents -> this method has the parameter of lines of one text file. It should execute the analysis and return the results als well formatted xml-events. These will not be checked but added to the output-xml-file. It does not need to create an opening- and closing analysis-tag. This is done by the method that calls it. 


