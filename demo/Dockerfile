FROM jrei/systemd-ubuntu:18.04

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

EXPOSE 8080
