from java:8

ADD target/universal/dashboard-*.zip /app/
RUN unzip /app/dashboard*.zip -d /app && rm /app/dashboard*.zip && mv /app/dashboard-* /app/dashboard/

WORKDIR /app/

EXPOSE 9000

ENTRYPOINT ["/app/dashboard/bin/dashboard"]
