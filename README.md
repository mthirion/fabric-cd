# fabric-cd
Continuous delivery with Fuse Fabric

#  Definition
Continuous delivery is a process that provides the ability to deliver deployable binaries to an environment continuously.
It means that any binary produced by the process is a candidate for a deployment to the target environment, which can be UAT but usually is production
The process should never produce binaries which are not stable enough ot be a candidate for a deployment

# Example description
The example is an ESB simple application named getCustomer
It exposes a soap-based integration web-service.
This service receives a soap message containing a customer ID, applies an XSLT transformation to it, calls a backend service named CustomerService that returns details about the customer (name, lastname...), applies another XSLT transformation to the response before sending it back to the client application.
The application is a Camel route using CXF endpoints, configured to be deployed on a Fuse Fabric.

# Fuse/Fabric continuous delivery components
Fuse/Fabric rely on maven repositories as artifact repositories to store the applications deployed on them.
They have their own internal maven repositories but can also be reconfigured to use external maven repositories.
With Fuse, the continuous delivery principle is based on:i

- Git as the SCM protocol/server
- Maven as the build framework
- Nexus as a central maven repository
- fabric8 maven plugin as the deployment tool
- Jenkins as the automation engine
- surefire (maven plugin) as the unit/system testing engine
- Soapui (because the example is a web service application) as the integration/performance testing engine

# Fuse/Fabric continuous delivery principle
The principle is the following:
- the new code is developped and tested locally
- at some point in time the code is committed and pushed on the central SCM
- this triggers a Jenkins job that build and unit test the code to ensure the changes don't conflict with any other changes
  this part ensure contiuous integration
- if the build and unit tests succeed, the resulting binary is deployed by Jenkins to Nexus that act as the central repository from where all the Fabric environments feed themselves
- the binary is then deployed by Jenkins to Fabric in the dev environment and some system test are performed
- If the job succeeds the binary is deployed to the next environment (integration) and some integrationt tests are performed
- If the tests succeed, the binary is finally deployed in UAT where performance tests are run

# Example details
I used my (this) github account as the SCM server.
I also used a local install of Nexus and Jenkins.  For the example, the same Fabric URL is used to represent all the environments.

The application code has been simplified for the purpose of the example.  Error handling: message validation... have been remove for clarity purpose.

The application is contained in the getCustomer subdirectory.
The 'cd' directory contains diagrams describing the principle of the CD process (cd-hl), the details of the cd process (cd-details), the simple description of the application (use-case-example) and the high level description of the Fuse/Fabric standard CI/CD architecture reference (fabric-cd).
The repository also contains an io.fabric8.agent.properties, which itself reflects the configuration for the Fabric to point to the external Nexus respository as its source of artifacts and internet proxy.
The example shows the usage of the Nexus "release" repository while the use of the snapshot (to be uncommended if in de) is commented out.
This content of the file can be edited in the Fabric web console under "Wiki>default>io.fabric8.agent.properties".

The Jenkins jobs are stored in the 'jobs' subdirectory.  They are compatible with Jenkins v2.  
The Jenkins "build pipeline plugin" can be used to visualize the relationship between the jobs easierly.  The root job is getCustomer_CI.

### getCustomer_CI
The code is supposed to be committed to the getCustomer subdirectory of the repository in the master branch, which is always the branch with the latest development code.
New code is developed under a maven SNAPSHOT version.

When new code is pushed, the Jenkins getCustomer_CI job is trigger to compile and unit test the code and thus ensure continuous integration of the code.
The compiling is done on a local "build" branch where the code is first merged.  This is to ensure the master branch is always stable.
The unit test is the BlueprintUnitTest.java class triggered by the maven surefire plugin.

### getCustomer_dev
When the code is ready for system tests (which can be a few CI steps away), the getCustomer_dev job is triggered.
The first purpose of this job is to move the code to the "dev" branch, which only contains "deployable" snapshot versions to be tested.
This job then builds and deploys the snapshot version to Nexus (in the Nexus "snapshot" repository), then deploy a profile to Fabric using the fabric8 maven plugin.
The profile is named "redhat-getCustomer".
It doesn't contain any version in its name so the publication of a new maven artifact under that profile triggers a new deployment by the Fabric deployment agent.

The maven "deploy" goal uses the "altSnapshotDeploymentRepository" option so that we don't fully rely on information contained in the pom.xml
The last step of the job is to launch the getCustomer_devtest job, which performs system tests.

### getCustomer_devtest
The system tests is a Java-based JUnit test named testIntegration.java contained in the application.
As the name is not standard, it's not automatically triggered by the surefire plugin but has to be triggered explicitely by the Jenkins job.

To perform system tests, the jobs first launches a SOAPUI mock service to simulate the backend service.
This is done by a shell script step that runs the soapui-runner.sh tool.
The SOAPUI settings and projects files are contained in the application in the src/test/resources/soapui directory
NB: Due to a mismatch between the Jenkins and Maven workspaces, the files have to be moved to the proper location in the Jenkins worspace first, using a shell script.

### getCustomer_promote
The "getCustomer_promote" job, triggered automatically if getCustomer_devtest succeeds, simply move the code to the 'release' branch.
It then triggers the getCustomer_release job

### getCustomer_release
Contrary to the dev branch, the release branch is supposed to contain release versions of the code only. 
Those versions should become candidate to a production deployment at the end of the process, if the process succeeds.

The job uses the  maven release plugin and the "release:prepare" goal to turn the Snapshot version into a Release version.
The version is computed in a pre-task by a shell script (we simply remove the "-SNAPSHOT" suffix.

When the job finished, the getCustomer_int job is called automatically

### getCustomer_int
This job handles the tasks to be performed in the "Integration" environment
It starts from a prepared released version and so read from the release branch.

The job deploys the release version in Nexus (in the Nexus 'release' repository).
The maven "deploy" goal uses the "altReleaseDeploymentRepository" option so that we don't fully rely on information contained in the pom.xml

It then deploys a profile to the Fabric Integration environment.  
The profile doesn't contain any version in its name so the release of a new version under this profile name triggers a new deployment from the Fabric deployment agent.

Once the profile is deployed, the job triggers an integration test.
The test is based on SOAPUI and relies on the soapui-maven plugin.  It's linked to the maven "integration-test" phase.
The SOAPUI test case is preconfigured with some assertions to guarantee its quality (not a saup fault, regex-matching response content...)

The SOAPUI settings and projects files are contained in the application in the src/test/resources/soapui directory
NB: Due to a mismatch between the Jenkins and Maven workspaces, the files have to be moved to the proper location in the Jenkins worspace first, using a shell script.

The test should be a Selenium-based test (possibly triggered from the shell) to become an E2E test


### getCustomer_QA
The last job is getCustomer_QA.  It's triggered automatically by the getCustomer_int job if it succeeded.
This job handles the tasks in the UAT environment.
It also makes use of released applications so reads from the release branch.
It expects the binary to be in Nexus (as done by the previous getCustomer_int job)
The job deploys the code to Fabric in the UAT environment.
It then run performances test based on loadUI and relying on the soapui-maven-plugin

The SOAPUI settings and projects files are contained in the application in the src/test/resources/soapui directory
NB: Due to a mismatch between the Jenkins and Maven workspaces, the files have to be moved to the proper location in the Jenkins worspace first, using a shell script.

The tests are preconfigured with some assertions (average & max response time + number of errors) to guarantee the quality of the result.

## Conclusion
If the entire pipeline succeeds, the new source code has become a candidate to a future deployment.
Indeed the snapshot version has been turned into a release and its robustness (integration and performance test) have been verified.


