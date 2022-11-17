FROM rabbitmq:3.10.1-management

ARG ENV_POSTFIX
ARG RABBITMQ_JWORLDCUP_PASS
ARG RABBITMQ_ADMIN_PASS

RUN rm -f /etc/rabbitmq/conf.d/10-defaults.conf
COPY docker/rabbitmq/rabbitmq${ENV_POSTFIX}.conf /etc/rabbitmq/rabbitmq.conf
COPY docker/rabbitmq/definitions.json /etc/rabbitmq/
ADD certs /etc/rabbitmq/certs
RUN chown -R rabbitmq:rabbitmq /etc/rabbitmq/rabbitmq.conf /etc/rabbitmq/definitions.json /etc/rabbitmq/certs/
RUN set -x; if [ -n "$RABBITMQ_JWORLDCUP_PASS" ]; then \
  sed -i "s/\"password\": \"jworldcup\"/\"password\": \"$RABBITMQ_JWORLDCUP_PASS\"/" /etc/rabbitmq/definitions.json; \
  sed -i "s/stomp.default_pass = jworldcup/stomp.default_pass = ${RABBITMQ_JWORLDCUP_PASS}/" /etc/rabbitmq/rabbitmq.conf; \
fi
RUN set -x; if [ -n "$RABBITMQ_ADMIN_PASS" ]; then \
  sed -i "s/\"password\": \"admin\"/\"password\": \"$RABBITMQ_ADMIN_PASS\"/" /etc/rabbitmq/definitions.json; \
fi

RUN rabbitmq-plugins enable --offline rabbitmq_stomp rabbitmq_web_stomp
