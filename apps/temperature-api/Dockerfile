# Build stage
FROM python:3.10.4-slim-buster

RUN pip install flask

COPY ./temperature-api.py ./

EXPOSE 8081

CMD ["python", "-u", "./temperature-api.py"]