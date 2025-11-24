<h1>Mindgard Burp Extension</h1>
A Payload Generator for Burp Intruder that runs Mindgard tests.

<img src="https://raw.githubusercontent.com/Mindgard/public-resources/refs/heads/main/burp-intruder-payload-generator.png"/>

- [Setup](#setup)
  - [Build](#build)
- [Troubleshooting](#troubleshooting)

## Setup
### Build
``` bash
mvn package
```

## Troubleshooting
 - Some config files are stored in ~/.mindgard which can interfere with changes. They can be safely deleted, then the extension reloaded to generate them again.
