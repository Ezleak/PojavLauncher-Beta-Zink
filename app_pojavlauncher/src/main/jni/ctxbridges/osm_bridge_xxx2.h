//
// Created by Vera-Firefly on 20.11.2024.
//

#ifndef OSM_BRIDGE_XXX2_H
#define OSM_BRIDGE_XXX2_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "osmesa_loader.h"

struct xxx2_osm_render_window_t {
    struct ANativeWindow *nativeSurface;
    ANativeWindow_Buffer buffer;
    OSMesaContext context;
    int32_t last_stride;
    void* window;
};


void* xxx2OsmGetCurrentContext();
bool xxx2OsmloadSymbols();
int xxx2OsmInit();
void xxx2OsmSwapBuffers();
void xxx2OsmMakeCurrent(void* window);
void* xxx2OsmCreateContext(void* contextSrc);
void xxx2OsmSwapInterval(int interval);

#endif //OSM_BRIDGE_XXX2_H
