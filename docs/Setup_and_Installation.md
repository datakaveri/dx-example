![IUDX](./iudx.png)

# Setup and Installation Guide

## Configuration
### Modules

#### Api Server Verticle

| Key Name           | Value Datatype    | Value Example | Description |
| :----------------  | :------: | :----  |  :----  |
| dxApiBasePath      |   True   | 23.99  |  :----  |
| dxCatalogueBasePath|   True   | 23.99  |  :----  |
| dxAuthBasePath     |  False   | 19.99  |  :----  |

### Other Configuration

| Key Name           | Value Datatype    | Value Example | Description |
| :----------------  | :------: | :----  |  :----  |
| dxApiBasePath      |   True   | 23.99  |  :----  |
| dxCatalogueBasePath|   True   | 23.99  |  :----  |
| dxAuthBasePath     |  False   | 19.99  |  :----  |


## Dependencies
### External

| Software Name     | Purpose    | 
| :---------------- | :------    |
| RabbitMQ      | For publishing audit logs |
| AAA Server    | For validating  |


### Prerequisites

#### RabbitMQ
RabbitMQ shall have an exchange to allow the publication. 

| Exchange Name     | vHost    | Description |
| :---------------- | :------  | :----       |
| audit-server      |   IUDX-Internal   | Uses this exchange for publishing audit message |

#### PostgresQL

An example template for Database. 

| Exchange Name     | vHost    | Description |
| :---------------- | :------  | :----       |
| audit-server      |   IUDX-Internal   | Uses this exchange for publishing audit message |



## Installation Steps
### Maven
### JAR
### Docker

## Logging and Monitoring

## Testing
### Unit Testing

Explaing how to execute tests. 

### Code Coverage Testing

Explaing how to execute tests. 

### Integration Testing

Explaing how to execute tests. 

### Performance Testing

Explaing how to execute tests. 

### Security Testing

Explaing how to execute tests. 
