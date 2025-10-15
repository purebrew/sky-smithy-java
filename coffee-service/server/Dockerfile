FROM alpine:3.22 AS builder
COPY build/distributions/server.zip .
RUN unzip server.zip && rm server.zip

FROM amazoncorretto:21-alpine3.22
WORKDIR /root
COPY --from=builder server .
ENTRYPOINT ["/root/bin/server"]
EXPOSE 80
