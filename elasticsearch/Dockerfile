FROM docker.elastic.co/elasticsearch/elasticsearch:8.17.1

# Install Nori Korean analysis plugin
RUN bin/elasticsearch-plugin install analysis-nori

# Copy analysis configuration files
COPY analysis /usr/share/elasticsearch/config/analysis/
