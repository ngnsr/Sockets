## Prime Number Filter
This project is a program that takes integer numbers and determines whether they are prime or not. The client can send numbers to the server one at a time through a dialogue with the user or by generating 50 random values. The server responds to each number with a message indicating whether it is prime or not. The client records the prime numbers in an output text file.

After the client has finished sending numbers, the server sends the client summarized data including:

- the total number of processed numbers,
- the number of prime numbers, and
- the minimum and maximum values.

## Get source code

````bash
git clone https://github.com/ngnsr/Sockets
````

## Compile
./gradlew build

## Start server

````bash
java -jar ./server/build/libs/server.jar
````

## Start client

````bash
java -jar ./client/build/libs/client.jar
````
