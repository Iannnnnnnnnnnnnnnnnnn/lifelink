FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

FROM nginx:1.27-alpine

ENV BACKEND_UPSTREAM=http://backend:8081
ENV MINIO_UPSTREAM=http://minio:9000
ENV NGINX_CLIENT_MAX_BODY_SIZE=50m

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80
