![IUDX](./iudx.png)

# How To Guide

## Deployment
### Maven
### JAR
### Docker

## Logging and Monitoring

## Configuration
### Vertical-1

| Name              | Value    | Description |
| :---------------- | :------: | :----       |
| Python Hat        |   True   | 23.99       |
| SQL Hat           |   True   | 23.99       |
| Codecademy Tee    |  False   | 19.99       |
| Codecademy Hoodie |  False   | 42.99       |


### Vertical-2


| Name              | Value    | Description |
| :---------------- | :------: | :----       |
| Python Hat        |   True   | 23.99       |
| SQL Hat           |   True   | 23.99       |
| Codecademy Tee    |  False   | 19.99       |
| Codecademy Hoodie |  False   | 42.99       |


## Dependencies
### External

RabbitMQ shall have an exchange to allow the publication. 

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
