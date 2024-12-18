//
// Created by Vera-Firefly on 18.12.2024.
//

#ifndef OSM_BRIDGE_XXX4_H
#define OSM_BRIDGE_XXX4_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "osmesa_loader.h"

typedef struct {
    struct ANativeWindow *nativeSurface;
    ANativeWindow_Buffer buffer;
    OSMesaContext context;
    int32_t last_stride;
    void* window;
} xxx4_osm_render_window_t;


xxx4_osm_render_window_t* xxx4OsmGetCurrentContext();
xxx4_osm_render_window_t* xxx4OsmCreateContext(xxx4_osm_render_window_t* share);
void xxx4OsmloadSymbols();
int xxx4OsmInit();
void xxx4OsmSwapBuffers();
void xxx4OsmMakeCurrent(xxx4_osm_render_window_t* bundle);
void xxx4OsmSwapInterval(int interval);

#endif //OSM_BRIDGE_XXX4_H
