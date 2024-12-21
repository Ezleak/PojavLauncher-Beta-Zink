//
// Created by Vera-Firefly on 21.12.2024.
//

#ifndef OSM_BRIDGE_XXX4_H
#define OSM_BRIDGE_XXX4_H

#include <android/native_window.h>
#include <android/hardware_buffer.h>
#include "osmesa_loader.h"

struct xxx4_osm_render_window_t {
    AHardwareBuffer* hardwareBuffer;
    AHardwareBuffer_Desc desc;
    OSMesaContext context;
    int32_t last_stride;
    uint8_t* framebuffer;
    void* destBuffer;
    int width;
    int height;
};

bool xxx4OsmloadSymbols();
void* xxx4OsmCreateContext(void* contextSrc);
void* xxx4OsmGetCurrentContext();
void xxx4OsmMakeCurrent(void* window);
void xxx4OsmSwapBuffers();
void xxx4OsmSwapInterval(int interval);

#endif //OSM_BRIDGE_XXX4_H