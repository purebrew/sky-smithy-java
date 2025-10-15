FROM krakend:2.11.1 AS builder

COPY krakend.tmpl .
COPY config/partials /etc/krakend/partials
COPY config/templates /etc/krakend/templates

RUN FC_ENABLE=1 \
  FC_OUT=/tmp/krakend.json \
  FC_PARTIALS="/etc/krakend/partials" \
  FC_TEMPLATES="/etc/krakend/templates" \
  krakend check -d -t -c krakend.tmpl

FROM krakend:2.11.1

USER root

COPY --from=builder --chmod=644 /tmp/krakend.json /etc/krakend/krakend.json
COPY jwks.json /etc/krakend/jwks.json