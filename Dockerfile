FROM babashka/babashka:alpine

RUN apk add zip

COPY . .
