FROM node:24-alpine3.21 AS builder
WORKDIR /root/app
COPY package.json .
RUN npm install
COPY . .

FROM node:24-alpine3.21
COPY --from=builder /root/app /root/app
WORKDIR /root/app
ENTRYPOINT ["node", "app.js"]
EXPOSE 80
