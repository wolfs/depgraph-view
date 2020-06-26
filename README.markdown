Dependency Graph Viewer
=======================

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/depgraph-view-plugin/master)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Fdepgraph-view-plugin/branches/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/depgraph-view.svg?label=latest%20version)](https://plugins.jenkins.io/depgraph-view)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/depgraph-view.svg?color=red)](https://plugins.jenkins.io/depgraph-view)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.100-green.svg?label=min.%20Jenkins)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg?label=min.%20JDK)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Shows a dependency graph of the projects using graphviz or jsPlumb. Requires a graphviz installation on the server.

### Features

- Show the dependency graph via graphviz or jsPlumb
    - restricted to projects in a view
    - restricted to one project
- Show the graphviz source file or jsPlumb json
- Respects access permissions
- Filter project names using regexes
- Show edge by color depending on edge provider
    - reverse triggers (fan-in, "build when other project builds"): red
    - jenkins dependency graph: black
    - parameterized trigger: blue
    - maven pipeline dependency graph ("withMaven"): green
    - copy artifact (not yet available for pipelines): cyan

### Screenshots

![](https://user-images.githubusercontent.com/849495/85310461-4dc39e00-b4b4-11ea-86aa-493d096f62e6.png)

### Changelog

- [Changelog for version 0.2 - 0.15](https://github.com/jenkinsci/depgraph-view-plugin/blob/master/CHANGELOG.md)
- [GitHub 1.0.0+ Release Changelog](https://github.com/jenkinsci/depgraph-view-plugin/releases)
