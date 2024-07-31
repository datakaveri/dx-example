# Setup and Installation

## Prerequisites
- Docker
- Docker Compose
- Java 11
- Maven

## Installation Steps
1. Clone the repository:
    ```bash
    git clone https://github.com/datakaveri/dx-example
    cd dx-example
    ```

2. Build the project:
    ```bash
    mvn clean install
    ```

3. Start the services using Docker Compose:
    ```bash
    docker-compose up --build
    ```

## Configuration
Ensure the following environment variables are set in your `docker-compose.yml`:
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
