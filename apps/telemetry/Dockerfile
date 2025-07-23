# Build stage
FROM python:3.10.4-slim-buster

RUN pip install kafka-python

COPY ./telemetry.py ./

CMD ["python", "-u", "./telemetry.py"]