# delivery-service-java-springboot
DeliveryService using Java + SpringBoot

## How to run tests?
```shell
$ ./mvnw verify
```

## How to run application?
```shell
$ docker-compose up -d
$ ./mvnw spring-boot:run
```

## How to build docker image?
```shell
$ ./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=sivaprasadreddy/delivery-service-java-springboot
```
