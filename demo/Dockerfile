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

RUN groupadd jenkins -g 1000 && \
    useradd jenkins -u 1000 -g 1000 -G sudo -m -d /home/jenkins

RUN apt-get install -y openssh-server
RUN echo jenkins:jenkins | chpasswd
RUN (echo X11Forwarding yes; echo X11UseLocalhost no) >> /etc/ssh/sshd_config
EXPOSE 22

RUN echo 'Defaults !authenticate' >> /etc/sudoers

# $ docker build -t demo .
# $ docker run --privileged -v /sys/fs/cgroup:/sys/fs/cgroup:ro -p 127.0.0.1:22:22 --rm --name demo … &
# https://unix.stackexchange.com/a/335195/26736
# $ docker exec demo rm /var/run/nologin
# $ ssh -X jenkins@localhost xterm bash
# as needed:
# $ ssh-keygen -f ~/.ssh/known_hosts -R localhost
