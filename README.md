# Galasa Managers
This repository contains code relating to Galasa Managers. Managers are the power-houses of Galasa and are used to reduce the amount of boilerplate code within a test and provide proven tool interaction code.
The repository has a hierarchical structure with a "galasa-managers-parent" folder at the top containing a "pom.xml" file, listing all currently available Managers. 
Managers are grouped by type within the parent folder, for example, the "galasa-managers-core-parent" folder contains the code and IVT (Installation Verification Test) tests for the Core Manager and Artifact Manager.
Each Manager comes with its own set of associated IVT tests and Javadoc. 
We're adding new Manager every month. Look at the [summary table of Managers](https://galasa.dev/docs/managers/) for a list of available and planned Managers and to find out what they do.

## Contributing

If you are interested in the development of Galasa, take a look at the documentation and feel free to post a question on the <a href="https://join.slack.com/t/galasa/shared_invite/zt-ele2ic8x-VepEO1o13t4Jtb3ZuM4RUA" target="_blank"> Galasa Slack channel</a>, or start sharing usage and development experiences with other Galasa users and the IBM team. You can also raise new ideas / features / bugs etc. as issues on [GitHub](https://github.com/galasa-dev/projectmanagement).  

Take a look at the [contribution guidelines](https://github.com/galasa-dev/projectmanagement/blob/main/contributing.md).
 

## Documentation

More information can be found on the [Galasa Homepage](https://galasa.dev). Questions related to the usage of Galasa can be posted on the [Galasa Slack channel](https://galasa.slack.com).

## Where can I get the latest release?

Find out how to install the Galasa Eclipse plug-in from our [Installing the Galasa plug-in](https://galasa.dev/docs/getting-started/installing) documentation.

Other Galasa repositories are available on [GitHub](https://github.com/galasa-dev). 

## License

This code is under the [Eclipse Public License 2.0](https://github.com/galasa-dev/maven/blob/main/LICENSE).

## Building locally
Use the `./build-locally.sh` script to build locally.

## Updating the versions of things
Use the `./build-release-yaml.sh` script to scan the contents of the managers source code and populate the `release.yaml` file with version information taken from each manager source folder.



