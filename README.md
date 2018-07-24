# TranskribusErrorRate
A tool for computing error rates for different algorithms:

[![Build Status](http://dbis-halvar.uibk.ac.at/jenkins/buildStatus/icon?job=TranskribusErrorRate)](http://dbis-halvar.uibk.ac.at/jenkins/job/TranskribusErrorRate)

## Running:

### HTR:
It can calculate Character Error Rate (CER), Word Error Rate (WER),
Bag of Tokens (BOG)
and some more metrics. Type
```
java -cp <this-jar> eu.transkribus.errorrate.HtrError --help
```
for more information concerning evaluating an HTR result if the files are
PAGE-XML-files. For raw UTF-8 encoded textfiles use
```
java -cp <this-jar> eu.transkribus.errorrate.HtrErrorTxt
```
or
```
java -cp <this-jar> eu.transkribus.errorrate.HtrErrorTxtLeip
```

### KWS:

To calculate measures for KWS
```
java -cp <this-jar> eu.transkribus.errorrate.KwsError
```
can be used. Use --help to see the configuration opportunities

### Text2Image

To calculate measures for image alignment
```
java -cp <this-jar> eu.transkribus.errorrate.Text2ImageError
```
can be used. Use --help to see the configuration opportunities


## Requirements
- Java >= version 8
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
