# TranskribusErrorRate
A tool for computing error rates for different algorithms:

[![Build Status](http://dbis-halvar.uibk.ac.at/jenkins/buildStatus/icon?job=TranskribusErrorRate)](http://dbis-halvar.uibk.ac.at/jenkins/job/TranskribusErrorRate)

## Running:

### HTR:
It can calculate Character Error Rate (CER), Word Error Rate (WER),
Bag of Tokens (BOG)
and some more metrics. Type
```
java -cp <this-jar> eu.transkribus.errorrate.Transkription --help
```
for more information concerning evaluating an HTR result if the files are
PAGE-XML-files. For raw UTF-8 encoded textfiles use
```
java -cp <this-jar> eu.transkribus.errorrate.TranskriptionTxt
```
or
```
java -cp <this-jar> eu.transkribus.errorrate.TranskriptionTxtLeip
```

### KWS:

To calculate measures for KWS
```
java -cp <this-jar> eu.transkribus.errorrate.KeywordSpotting
```
can be used. Use --help to see the configuration opportunities

### Text2Image

To calculate measures for image alignment
```
java -cp <this-jar> eu.transkribus.errorrate.Text2Image
```
can be used. Use --help to see the configuration opportunities


## Requirements
- Java >= version 7
- Maven
- All further dependencies are gathered via Maven

## Build
```
git clone https://github.com/Transkribus/TranskribusErrorRate
cd TranskribusErrorRate
mvn install
```

### Links
- https://transkribus.eu/TranskribusErrorRate/apidocs/index.html

*** File '(Unnamed)'
README.md
CHANGELOG.md
/home/gundram/devel/src/git/CITlabModule/.git/COMMIT_EDITMSG
src/main/resources/logback.xml
src/main/resources/logback.xml
src/main/resources/logback.xml
/home/gundram/devel/src/git/CITlabModule/.git/COMMIT_EDITMSG
/home/gundram/devel/src/git/TranskribusErrorRate/.git/COMMIT_EDITMSG
README.md
README.md
README.md

*** File '* Startup Log *'
Processing '/etc/joe/joerc'...
Processing '/etc/joe/ftyperc'...
Finished processing /etc/joe/ftyperc
Finished processing /etc/joe/joerc
