# GuaranteedSolaceSubscriber

A simple java application that can be used as a subscriber using the JCSMP Solace native API.

## Prerequisites

    Maven version 3.9.4
    Java 17
    Docker for desktop (Optional)

## How to Run

- Clone this repository to a directory of your choosing.

- To connect this application to the solace broker you will need the following connection details

  - HostName and Port
  - VPN Name
  - Queue Name
  - Username
  - Password

    These will be provided to you separately.

- Once you have received your credentials add them to the application.properties file found here `src-> main-> resources`
- In the directory you cloned the repository into run `mvn clean install`

## Run locally

- Run `java -jar target/GuaranteedSubscriber-1.0.0.jar`

## Run containerized using docker

- Ensure docker desktop is running
- Run `docker-compose up --build `

## Test Units

Sample test units have been created to help subscribers Mock events for their environment. It also validates NEMS payloads, thi logic can be used to extend your connection.
