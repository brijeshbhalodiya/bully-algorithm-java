FROM openjdk:latest

WORKDIR /bully

COPY ./target/classes/ /bully

CMD ["java", "Main"]