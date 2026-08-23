FROM container-registry.oracle.com/middleware/weblogic:15.1.1.0-generic-jdk21-ol9

ENV DOMAIN_NAME=todo_domain \
    ADMIN_NAME=AdminServer \
    ADMIN_LISTEN_PORT=7001 \
    ADMINISTRATION_PORT_ENABLED=false \
    PRODUCTION_MODE=dev \
    DB_HOST=db \
    DB_PORT=1521 \
    DB_SERVICE=FREEPDB1 \
    DB_USER=todo \
    DB_PASSWORD=TodoPassword1 \
    DB_URL=jdbc:oracle:thin:@//db:1521/FREEPDB1

USER root
RUN mkdir -p /u01/oracle/properties /u01/oracle/apps /u01/oracle/container-scripts
COPY --chown=oracle:root docker/weblogic/container-scripts/ /u01/oracle/container-scripts/
COPY --chown=oracle:root docker/weblogic/properties/domain.properties /u01/oracle/properties/domain.properties
COPY --chown=oracle:root target/weblogic-todo-jsf.war /u01/oracle/apps/todo.war
RUN chmod +x /u01/oracle/container-scripts/*.sh

USER oracle
WORKDIR /u01/oracle
EXPOSE 7001
CMD ["/u01/oracle/container-scripts/start-todo-domain.sh"]
