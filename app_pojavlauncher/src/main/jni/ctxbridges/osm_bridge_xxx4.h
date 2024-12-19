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

extern bool (*xxx4_osm_load_symbols)();
extern xxx4_osm_render_window_t* (*xxx4_osm_get_current)();
extern xxx4_osm_render_window_t* (*xxx4_osm_create_context)(xxx4_osm_render_window_t* share);
extern void (*xxx4_osm_swap_buffers)();
extern void (*xxx4_osm_make_current)(xxx4_osm_render_window_t* bundle);
extern void (*xxx4_osm_swap_interval)(int interval);

void osm_bridge_xxx4() {
    xxx4_osm_load_symbols = xxx4OsmloadSymbols;
    xxx4_osm_create_context = xxx4OsmCreateContext;
    xxx4_osm_make_current = xxx4OsmMakeCurrent;
    xxx4_osm_get_current = xxx4OsmGetCurrentContext;
    xxx4_osm_swap_buffers = xxx4OsmSwapBuffers;
    xxx4_osm_swap_interval = xxx4OsmSwapInterval;
}

bool xxx4OsmloadSymbols();
xxx4_osm_render_window_t* xxx4OsmGetCurrentContext();
xxx4_osm_render_window_t* xxx4OsmCreateContext(xxx4_osm_render_window_t* share);
void xxx4OsmSwapBuffers();
void xxx4OsmMakeCurrent(xxx4_osm_render_window_t* bundle);
void xxx4OsmSwapInterval(int interval);

#endif //OSM_BRIDGE_XXX4_H
