ARG dockerRepository
ARG branch
FROM ${dockerRepository}/galasadev/galasa-maven-extensions:${branch}

COPY repo/ /usr/local/apache2/htdocs/
COPY managers.githash /usr/local/apache2/htdocs/managers.githash
