**warning still in pre release!!!!**

Adds mima dependency check to your gradle project
it looks for the previous version by looking at the git version tags:
```groovy
mima {
    direction = "backward"  can be "forward" , "backward" or "both"
    //makes the task fail if there was an error detected    
    failOnException = false 
    //the versions to check, defaults to the latest git tag.
    compareToVersions = ["0.0.0", "0.1.0"]
    //doesn't work yet is on the list if someone is better in gradle be so kind to make a pr:
    exclude {
        problemType {
            packages = ["the.flowering.branches.*"] 
        }
    }
}
```