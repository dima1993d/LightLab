cmake_minimum_required(VERSION 3.22.1)
project(lightlab C)
add_library(lightlab SHARED drm_probe.c)
