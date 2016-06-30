FROM ubuntu:16.04

Maintainer Matt Turner <matturner@gmail.com>

RUN apt-get update
RUN apt-get install -y --no-install-recommends wget unzip

RUN wget -q --no-check-certificate https://releases.hashicorp.com/consul/0.6.3/consul_0.6.3_linux_amd64.zip -O consul.zip
RUN unzip consul.zip -d /usr/bin && rm consul.zip
ADD consul.json /etc/consul/consul.json
ADD basic_node /opt/consul/bin/
ADD service_process /opt/consul/bin/

RUN wget -q http://get.influxdb.org/telegraf/telegraf_0.10.0-1_amd64.deb -O telegraf.deb
RUN dpkg -i telegraf.deb && rm telegraf.deb
ADD telegraf.conf /etc/telegraf/telegraf.conf

RUN apt-get install -y --no-install-recommends supervisor
ADD supervisord.conf /etc/supervisor/supervisord.conf

CMD supervisord -c /etc/supervisor/supervisord.conf
