# Stage 1: build Angular
FROM node:18 AS build
WORKDIR /app

# 1. copy only dependency files first (IMPORTANT)
COPY package*.json ./

# 2. install dependencies (cached layer)
RUN npm install

# 3. copy source code
COPY . .

# 4. build Angular
RUN npm run webapp:build:prod


# Stage 2: serve with nginx
FROM nginx:alpine

# nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Angular output
COPY --from=build /app/dist/cabs/ /usr/share/nginx/html/

EXPOSE 80