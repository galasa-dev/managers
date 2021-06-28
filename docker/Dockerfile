ARG dockerRepository
ARG branch
FROM ${dockerRepository}/galasadev/galasa-maven-extensions:${branch}

RUN sed -i 's/\/extensions"/\/managers"/' /usr/local/apache2/conf/httpd.conf

COPY repo/ /usr/local/apache2/htdocs/
COPY managers.githash /usr/local/apache2/htdocs/managers.githash
