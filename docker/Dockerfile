ARG extensionsVersion
ARG dockerRepository
ARG branch
FROM ${dockerRepository}/galasadev/galasa-maven-extensions:${extensionsVersion}-${branch}

ARG gitHash

RUN sed -i 's/\/extensions"/\/managers"/' /usr/local/apache2/conf/httpd.conf

COPY repo/ /usr/local/apache2/htdocs/
RUN echo -n ${gitHash} > /usr/local/apache2/htdocs/managers.githash
