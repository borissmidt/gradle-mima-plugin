**warning still in pre release!!!!**

Adds mima dependency check to your gradle project:
```
mima {
    direction = "backward"  can be "forward" , "backward" or "both"
    failOnException = false //makes the task fail if there was an error detected
    reportSignatureProblems = false //not implemented yet
    sourceSet = this.sourceSets.first() //not implemented yet should use the scala plugin sourceset.
}
```