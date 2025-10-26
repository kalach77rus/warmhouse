FROM rust:1-bullseye

WORKDIR /app

COPY Cargo.toml Cargo.lock ./

RUN cargo build --release

COPY . .

RUN cargo build --release
