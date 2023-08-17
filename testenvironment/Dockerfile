FROM ubuntu:20.04

RUN apt-get update
RUN apt-get install -y openjdk-17-jdk
RUN apt-get install -y openssh-server
RUN apt-get install -y net-tools curl
RUN echo 'root:secret' | chpasswd
RUN echo 'PermitRootLogin yes' >> /etc/ssh/sshd_config

EXPOSE 22/tcp
EXPOSE 22/udp

CMD ["service", "ssh", "start", "-D"]
