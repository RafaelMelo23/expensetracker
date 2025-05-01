FROM postgres:15-alpine

# Create the directory for the PostgreSQL data
RUN mkdir -p /var/lib/postgresql/data/pgdata

# Set proper ownership
RUN chown -R postgres:postgres /var/lib/postgresql/data

# Switch to the postgres user before the entrypoint runs
USER postgres

# Set the environment variable for the data directory
ENV PGDATA /var/lib/postgresql/data/pgdata