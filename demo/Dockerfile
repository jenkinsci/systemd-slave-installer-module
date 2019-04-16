FROM jrei/systemd-ubuntu:18.04

#RUN apt-get update \
#    && apt-get install -y … \
#    && apt-get clean \
#    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
RUN apt-get update
RUN apt-get install -y sudo
RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y icedtea-netx
RUN apt-get install -y xterm
RUN apt-get install -y openssh-server
RUN apt-get install -y curl
RUN apt-get install -y iputils-ping
RUN apt-get install -y net-tools

RUN groupadd jenkins -g 1000 && \
    useradd jenkins -u 1000 -g 1000 -G sudo -m -d /home/jenkins

RUN echo jenkins:jenkins | chpasswd
RUN (echo X11Forwarding yes; echo X11UseLocalhost no) >> /etc/ssh/sshd_config
EXPOSE 22

RUN echo 'Defaults !authenticate' >> /etc/sudoers

# $ docker build -t demo .
# $ docker run --privileged -v /sys/fs/cgroup:/sys/fs/cgroup:ro -p 127.0.0.1:22:22 -p 127.0.0.1:8080:8080 --rm --name demo … &
# https://unix.stackexchange.com/a/335195/26736
# $ docker exec demo rm /var/run/nologin
# $ ssh -X jenkins@localhost xterm bash
# as needed:
# $ ssh-keygen -f ~/.ssh/known_hosts -R localhost

EXPOSE 8080
# $ docker cp …/jenkins.war demo:/tmp/jenkins.war
# inside container:
# $ java -jar /tmp/jenkins.war &
# $ docker exec demo cat /home/jenkins/.jenkins/secrets/initialAdminPassword
# run through setup wizard, install no plugins
# configure security, enable JNLP agent port (random is fine)
# manage nodes, create JNLP agent
# download JNLP file, copy into container, run with javaws inside container
# when prompted, install as a service
# watch http://localhost:8080/computer/agent/log on host
# $ sudo systemctl start/stop/restart jenkins-slave-XXXXXXXX
