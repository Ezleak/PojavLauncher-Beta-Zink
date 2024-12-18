//
// Created by Vera-Firefly on 18.12.2024.
//

#ifndef OSM_BRIDGE_XXX3_H
#define OSM_BRIDGE_XXX3_H

#include <android/native_window.h>
#include <android/hardware_buffer.h>
#include "osmesa_loader.h"

struct xxx3_osm_render_window_t {
    AHardwareBuffer* hardwareBuffer;
    AHardwareBuffer_Desc desc;
    OSMesaContext context;
    int32_t last_stride;
    uint8_t* framebuffer;
    void* window;
    void* destBuffer;
    int width;
    int height;
};


void* xxx3OsmGetCurrentContext();
void xxx3OsmloadSymbols();
int xxx3OsmInit();
void xxx3OsmSwapBuffers();
void xxx3OsmMakeCurrent(void* window);
void* xxx3OsmCreateContext(void* contextSrc);
void xxx3OsmSwapInterval(int interval);

#endif //OSM_BRIDGE_XXX3_H