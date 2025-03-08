# Solar System Project

This project is a simulation of the solar system using Java and the jMonkeyEngine. It visualizes the planets orbiting around the sun and provides an interactive experience to explore the solar system.

## Features

- Realistic orbits of planets
- Scalable distances and sizes
- Interactive camera controls
- Detailed textures for planets

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- jMonkeyEngine 3.6 or higher (handled via Maven dependencies)

## Setup

1. Clone the repository:
    ```bash
    git clone https://github.com/Florian-Audouard/solarsystem.git
    ```
2. Navigate to the project directory:
    ```bash
    cd solarsystem
    ```

## Execution with Maven

Run the following command to compile and execute the project:
```bash
mvn clean compile exec:java
```

## Build with Maven

To package the project into a JAR file, run:
```bash
mvn clean package
```
This will create a JAR file named `SolarSystem.jar` in the `target` directory.

## Running the JAR

Once built, you can run the JAR file using:
```bash
java -jar target/SolarSystem.jar
```