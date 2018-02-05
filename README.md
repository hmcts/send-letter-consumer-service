# Letter sender

Consumes messages from a queue, generates PDFs and sends them to a third party supplier's SFTP server for postage.

[![Build Status](https://travis-ci.org/hmcts/send-letter-consumer-service.svg?branch=master)](https://travis-ci.org/hmcts/send-letter-consumer-service)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7c3905b1949948908b9264aa3d85d51c)](https://www.codacy.com/app/HMCTS/send-letter-consumer-service)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/7c3905b1949948908b9264aa3d85d51c)](https://www.codacy.com/app/HMCTS/send-letter-consumer-service)

![Diagram](/doc/arch/diagram.png)

## Getting Started

### Prerequisites
- [JDK 8](https://java.com)

### Building
To build the project execute the following command:
```bash
./gradlew build
```

## Developing

### Unit tests
To run all unit tests execute the following command:
```bash
./gradlew test
```

### Code quality checks
We use [checkstyle](http://checkstyle.sourceforge.net/) and [PMD](https://pmd.github.io/).  
To run all checks execute the following command:
```bash
./gradlew check
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
