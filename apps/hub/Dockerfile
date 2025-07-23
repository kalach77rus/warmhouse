# Build stage
FROM python:3.10.4-slim-buster

RUN pip install requests kafka-python

COPY ./hub.py ./

CMD ["python", "-u", "./hub.py"]