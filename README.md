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
 - Settings errors
   - These can sometimes be caused by changes between extension versions
   - Try deleting ~/.mindgard/burp.json then restarting Burp Suite to generate a clean settings file
