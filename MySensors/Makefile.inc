SOC=BCM2711
CXXFLAGS= -std=c++11
CPPFLAGS=-march=armv8-a+crc -mtune=cortex-a72 -DMY_RADIO_RFM69 -DMY_RFM69_NEW_DRIVER -DMY_GATEWAY_LINUX -DMY_GATEWAY_MQTT_CLIENT -DMY_DEBUG -DLINUX_SPI_BCM -DLINUX_ARCH_RASPBERRYPI -DMY_MQTT_PASSWORD=\"3zXf_g12h@@PNWp\" -DMY_MQTT_USER=\"mysensors\" -DMY_MQTT_CLIENT_ID=\"mygateway1\" -DMY_MQTT_SUBSCRIBE_TOPIC_PREFIX=\"+/mysensors\" -DMY_MQTT_PUBLISH_TOPIC_PREFIX=\"mysensors/all\" -DMY_CONTROLLER_IP_ADDRESS=127,0,0,1 -DMY_RFM69_FREQUENCY=RFM69_868MHZ
LDFLAGS=-pthread
PREFIX=/usr/local
CC=gcc
CXX=g++
BUILDDIR=build
BINDIR=bin
GATEWAY_DIR=/usr/local/bin
INIT_SYSTEM=systemd
SPI_DRIVER=BCM