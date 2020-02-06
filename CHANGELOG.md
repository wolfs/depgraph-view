### Changelog (0.1 - 0.15)

All notable changes up to version 0.15 are documented in this file. All future changes will be automatically
logged by release drafter in [GitHub releases](https://github.com/jenkinsci/depgraph-view-plugin/releases).  


#### Version 0.15.0 (Feb 6, 2020)

-   Switch to semantic versioning
-   Add release-drafter release notes
-   This will be the last revision dependending on 1.501

#### Version 0.14 (Feb 5, 2020)

-   [Fix security vulnerability](https://jenkins.io/security/advisory/2019-07-11/)

#### Version 0.13 (Oct 23, 2017)

-   [Fix security vulnerability](https://jenkins.io/security/advisory/2017-10-23/)

#### Version 0.12 (Oct 16, 2017)

-   Update version of jsPlumb

#### Version 0.11 (Mar 22, 2013)

-   Fix invalid ajax calls for `sidepanel.jelly.`
-   Show right dependency when the project name for copy artifact is relative

#### Version 0.10 (Dec 1, 2012)

-   Switch editing of Dependency Graph off by default
-   Editing of Dependency Graph is configurable globally
-   Disable wrapping of job names

#### Version 0.9 (Nov 22, 2012)

-   Make jQuery-UI a mandatory dependency
    ([JENKINS-15891](https://issues.jenkins-ci.org/browse/JENKINS-15891))
-   Improve Layout algorithm

#### Version 0.8 (Nov 19, 2012)

-   Use jQuery and jQuery-UI Plugin
-   Fix problem with Javascript-Ids from job-names
    [JENKINS-15850](https://issues.jenkins-ci.org/browse/JENKINS-15850)

#### Version 0.7 (Nov 16, 2012)

-   Add ability to switch off graphviz rendering on configuration page

#### Version 0.6 (Nov 14, 2012)

-   Bugfix for jsPlumb display

#### Version 0.5 (Nov 13, 2012)

-   ExtensionPoints for Edges and Subprojects of the Graph
-   Experimental visualisation via jsPlumb
-   Fixed links when jenkins is behind reverse proxy
    ([JENKINS-13446](https://issues.jenkins-ci.org/browse/JENKINS-13446),
    [JENKINS-12112](https://issues.jenkins-ci.org/browse/JENKINS-12112))
-   Fixed invalid image when graphviz outputs warnings
    ([JENKINS-11875](https://issues.jenkins-ci.org/browse/JENKINS-11875))

#### Version 0.4 (Sep 27, 2012)

-   Legend now in separate image

#### Version 0.3 (Sep 25, 2012)

-   Subprojects from the [Parameterized Trigger Plugin](https://plugins.jenkins.io/parameterized-trigger)
    are now shown
-   Do not rely on default encoding when executing dot

#### Version 0.2 (Aug 5, 2011)

-   Use getFullDisplayName instead of getName for project names

#### Version 0.1 (Dec 14, 2010)

-   Initial version
