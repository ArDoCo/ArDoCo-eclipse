# textAnalysis.core
This plugin provides the main logic of the project. It sets up the new Run Configuration, dynamically loads the Analysis from jars, executes them and creates the output file. Find instructions about how to implement your own analysis in the textAnalysis.provider bundle. 

## What does it do?
This plugin gives you a new run configuration. This run configuration lets you choose one or multiple input files. It provides checkboxes for all the Analysis that it found (see "where to put the analysis jars"). When you execute the run configuration, it will execute all the checked analysis and print their output in an xml file. This xml file can then be used for further processing.

In order to be able to run this configuration:
- The input file(s) has / have to be (a) .txt file(s)
- at least one analysis has to be checked, and the analysis needs to return that it is valid

## Input and Output files
The input file needs to be a txt file. The output file will be an xml file. It will be created in the same folder as the input file. It's name will be the same than the input file but with a \_analysis. Example: 
- input: input.txt
- output: output_analysis.xml
This output file is created with means of the javax.xml.stream classes and methods. It will have an opening tag _<textAnalysis>_. This tag is of course closed at the end of the file. For every analysis, the Delegate method creates a start and end tag with the name of the analysis. 

## Where to put the analysis jars
The jars are dynamically loaded from a folder. This folder must be specified in the _.textanalysisconfig_ file in the users home directory. If this file does not exist it will automatically be created. Also, a default folder called _.textanalysis_ will be created. This conifg file contains exactly one line which specifies the folder where the analysis-jars are searched. In the default case, this will be the _.textanalysis_ folder
An alternative implementation would have been to give the Run Configuration a field where the directory for the jars can be chosen. This would have included reloading of the tab. Since I expect that this folder is not changing a lot, the way with the config-file is more elegant and saves the UI from some mostly-unused bulk. 

## How dynamic analysis loading works
The dynamic analysis loading works by using the the Java Service Provider. The README.md file in the textAnalysis.provider module specifies how to create an own analysis. I had a lot of issues implementing this, which was mainly an issue with the right classpath and that the Provider Interface was not found. What in the end solved it, are these lines of code from the AnalysisLoader.createProviderClassLoader method:
```java
ClassLoader cLoader = Class.forName(PROVIDER_INTERFACE).getClassLoader();
return new URLClassLoader(urls, cLoader);
```
This means that the Classloader used for loading the jars is the one from the provider-Interface and the urls to the jar are added to it. Therefore, the Provider-Interface can be found on the Classpath without any problems. 
An alternative Implementation would have been to provide Analysis as Eclipse-Plugin. Then my plugin would have to search all installed plugins on the user's computer and find the matching analysis. The implementation with the jars seemed to be easier and more convenient for my first implementation and therefore I chose it. An alternative solution can always be pluged in via the AnalysisLoader class. 

## What errors are possible and how to handle them?
A couple of errors will lead to a read message in the Run Configuration UI. 
-  Problem with the directory for analysis: The directory is either not specified in the configuration file, or it is wrongly specified (doesn't exist or is not a directory, cannot be found).  -> check the entry in the _.textanalysisconfig_ file in your user home directory.
- The Analysis Files can not be converted to URLs: The analysis-jars in the specified folder can not be converted to URLs. -> something is wrong with the filenames or path. 
- IO Exception loading the Source Direction. Classical Java IO exception
- The Provider Interface can not be found. Then something is wrong with the classpath / the Provider implementation is missing. 

## Future Work
Ideas that can be done by future work:
- Make the UI of the Run Configuraion a bit prettier
- Also incorporate complex analysis with own parameters, e.g. by using own Tabs for these Analysis. 
