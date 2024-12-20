//
// Created by Vera-Firefly on 20.11.2024.
//

#ifndef OSM_BRIDGE_XXX2_H
#define OSM_BRIDGE_XXX2_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "osmesa_loader.h"

typedef struct {
    struct ANativeWindow *nativeSurface;
    ANativeWindow_Buffer buffer;
    OSMesaContext context;
    int32_t last_stride;
    void* window;
} xxx2_osm_render_window_t;


xxx2_osm_render_window_t* xxx2OsmGetCurrentContext();
bool xxx2OsmloadSymbols();
int xxx2OsmInit();
void xxx2OsmSwapBuffers();
void xxx2OsmMakeCurrent(xxx2_osm_render_window_t* window);
xxx2_osm_render_window_t* xxx2OsmCreateContext(xxx2_osm_render_window_t* contextSrc);
void xxx2OsmSwapInterval(int interval);

#endif //OSM_BRIDGE_XXX2_H
