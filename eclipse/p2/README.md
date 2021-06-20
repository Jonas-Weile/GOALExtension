GOAL-Eclipse P2 site
==================

A P2 site for use by the Eclipse plug-ins. Maven artifacts are automatically converted to OSGi bundles by using the 'p2-maven-plugin' (https://github.com/reficio/p2-maven-plugin).

Build
=====

Update the version number(s) of the Maven artifact references in the pom file here, for example:
```
com.github.goalhub.runtime:runtime:jar:jar-with-dependencies:1.0.0
```

Then run the following command (and push to git afterwards) to allow the Eclipse plug-ins to use those updated Maven artifacts:
```
mvn clean && mvn package
```