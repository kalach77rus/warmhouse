# Этап сборки
FROM alpine:latest AS build
RUN apk add --no-cache build-base curl go
WORKDIR /src
COPY . .
RUN go build -o temperature-api .

# Этап рантайма
FROM alpine:latest
WORKDIR /app
COPY --from=build /src/temperature-api .
# ENV TEMP_API_PORT=8081
EXPOSE ${TEMP_API_PORT}
CMD ["sh", "-c", "./temperature-api --port ${TEMP_API_PORT}"]
