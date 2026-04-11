# Stage 1: build Angular
FROM node:18 AS build
WORKDIR /app

COPY . .
RUN npm install
RUN npm run webapp:build:prod

# Stage 2: serve with nginx
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist/cabs /usr/share/nginx/html

EXPOSE 80