# Downloading with Maven #
  1. DeltaSpike SCXML distributions are deployed at the maven repository, to let maven aware of this repository, add the following repository definition to your pom.xml in repositories section.
```
<repository>  
    <id>deltaspike-scxml-releases</id>  
    <name>Maven Repository for DeltaSpike SCXML (releases</name>  
    <url>https://deltaspike-scxml.googlecode.com/svn/maven-repo/releases</url>  
    <layout>default</layout>  
</repository>  
<repository>  
    <id>deltaspike-scxml-snapshots</id>  
    <name>Maven Repository for DeltaSpike SCXML (snapshots)</name>  
    <url>https://deltaspike-scxml.googlecode.com/svn/maven-repo/snapshots</url>  
    <layout>default</layout>  
    <snapshots>
       <enabled>false</enabled>
    </snapshots>
</repository>  
```
  1. And add the dependency configuration as;
```
<dependency>
    <groupId>org.apache.deltaspike.modules</groupId>
    <artifactId>deltaspike-scxml-module-api</artifactId>
    <version>0.5-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>org.apache.deltaspike.modules</groupId>
    <artifactId>deltaspike-scxml-module-impl</artifactId>
    <version>0.5-SNAPSHOT</version>
</dependency>
```