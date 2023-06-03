# Base image
FROM ubuntu:latest

# Set arguement to disable user entry
ARG DEBIAN_FRONTEND=noninteractive

# Install necessary packages
RUN apt-get update && \
    apt-get install -y build-essential cmake git && \
    apt-get install -y verilator yosys && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the Verilog source files and the Makefile to the container
# COPY src /app/src
# COPY testbench.cpp /app/testbench.cpp
# COPY Makefile /app/Makefile

# Run the make command
CMD ["make"]