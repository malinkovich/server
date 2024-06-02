FROM tomcat:9.0.87
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
COPY ./target/server.war /usr/local/tomcat/webapps/ROOT.war
#ENV DOCKER_network my_network