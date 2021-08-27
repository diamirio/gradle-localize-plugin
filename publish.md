## How to publish

* Verify that you have both credentials in your local.properties file.
* Prepare the new version (increase version code) and push everything including the tag.
* Run:
```shell
./gradlew clean build test publishLocalizePluginPublicationToMavenRepository
```
