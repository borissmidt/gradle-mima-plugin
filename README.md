Adds mima dependency check to your gradle project
it looks for the previous version by looking at the git version tags:
```groovy
mima {
    direction = "backward"  can be "forward" , "backward" or "both"
    //makes the task fail if there was an error detected    
    failOnException = false 
    //the versions to check, defaults to the latest version detected in the git tag.
    compareToVersions = ["0.0.0", "0.1.0"]
}
```

supported versions:

- V0.0.0 suports gradle 4.9 to 6.3

to run the tests pull this repo,
 - install sdkman. `curl -s "https://get.sdkman.io" | bash`
 - go to `cd src/gradleTest/test-projec`
 - `sh test.sh`

to add a new version 
- add your version to`versions` array in test.sh and run it.