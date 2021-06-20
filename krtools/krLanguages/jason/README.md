JASON
==========

KR Implementation for GOAL, based on JASON.
We pulled the database functionality out of JASON and use it to implement a GOAL KR implementation.


Dependency information 
=====================

```
<repository>
 <id>goalhub-mvn-repo</id>
 <url>https://raw.github.com/goalhub/mvn-repo/master</url>
</repository>
```
	
```	
<dependency>
 <groupId>com.github.goalhub.krTools.krLanguages</groupId>
 <artifactId>jason</artifactId>
 <version>0.0.1</version>
</dependency>
```

Release Procedure
=============

Ensure your ~/.m2/settings.xml file is as follows:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
		<server>
   			<id>github</id>
   			<username>YOUR_USERNAME</username>
   			<password>YOUR_PASSWORD</password>
		</server>
	</servers>
</settings>
```

Then call:

```
mvn deploy -DcreateChecksum=true
```

Note that you must have a public name and e-mail address set on GitHub for this to work correctly (https://github.com/settings/profile)
