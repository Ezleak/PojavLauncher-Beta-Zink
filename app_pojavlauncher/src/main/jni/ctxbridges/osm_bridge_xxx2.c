//
// Created by Vera-Firefly on 20.11.2024.
//

#include <android/native_window.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <dlfcn.h>
#include <assert.h>
#include <malloc.h>
#include <stdlib.h>
#include "environ/environ.h"
#include "osm_bridge_xxx2.h"
#include "osmesa_loader.h"
#include "renderer_config.h"

static struct xxx2_osm_render_window_t *xxx2_osm;
static bool hasCleaned = false;
static bool hasSetNoRendererBuffer = false;
static bool swapSurface = false;
static char xxx2_no_render_buffer[4];
static const char* osm_LogTag = "[ XXX2 OSM Bridge ] ";

void* abuffer;

void setNativeWindowSwapInterval(struct ANativeWindow* nativeWindow, int swapInterval);

void xxx2_osm_set_no_render_buffer(ANativeWindow_Buffer* buf) {
    buf->bits = &xxx2_no_render_buffer;
    buf->width = pojav_environ->savedWidth;
    buf->height = pojav_environ->savedHeight;
    buf->stride = 0;
}

void *xxx2OsmGetCurrentContext() {
    return xxx2_osm->context;
}

bool xxx2OsmloadSymbols() {
    dlsym_OSMesa();
    return true;
}

void xxx2_osm_apply_current(ANativeWindow_Buffer* buf) {
    if (swapSurface)
    {
        xxx2_osm->context = OSMesaGetCurrentContext_p();
        abuffer = buf->bits;
    } else {
        abuffer = malloc(buf->width * buf->height * 4);
    }

    OSMesaMakeCurrent_p(xxx2_osm->context, abuffer, GL_UNSIGNED_BYTE, buf->width, buf->height);
    if (buf->stride != xxx2_osm->last_stride)
        OSMesaPixelStore_p(OSMESA_ROW_LENGTH, buf->stride);
    xxx2_osm->last_stride = buf->stride;
}

void xxx2OsmSwapBuffers() {
    if (!swapSurface) swapSurface = true;
    ANativeWindow_lock(xxx2_osm->nativeSurface, &xxx2_osm->buffer, NULL);
    xxx2_osm_apply_current(&xxx2_osm->buffer);
    glFinish_p();
    ANativeWindow_unlockAndPost(xxx2_osm->nativeSurface);
}

void xxx2OsmMakeCurrent(void *window) {
    if (!hasCleaned)
    {
        printf("%s making current\n", osm_LogTag);
        xxx2_osm->nativeSurface = pojav_environ->pojavWindow;
        ANativeWindow_acquire(xxx2_osm->nativeSurface);
        ANativeWindow_setBuffersGeometry(xxx2_osm->nativeSurface, 0, 0, WINDOW_FORMAT_RGBX_8888);
        ANativeWindow_lock(xxx2_osm->nativeSurface, &xxx2_osm->buffer, NULL);
    }

    if (!hasSetNoRendererBuffer)
    {
        hasSetNoRendererBuffer = true;
        xxx2_osm_set_no_render_buffer(&xxx2_osm->buffer);
    }

    
    swapSurface = false;
    xxx2_osm->window = window;
    xxx2_osm->context = xxx2_osm->window;
    xxx2_osm_apply_current(&xxx2_osm->buffer);
    OSMesaPixelStore_p(OSMESA_Y_UP, 0);

    if (!hasCleaned)
    {
        hasCleaned = true;
        printf("%s vendor: %s\n", osm_LogTag, glGetString_p(GL_VENDOR));
        printf("%s renderer: %s\n", osm_LogTag, glGetString_p(GL_RENDERER));
        glClear_p(GL_COLOR_BUFFER_BIT);
        glClearColor_p(0.4f, 0.4f, 0.4f, 1.0f);
        ANativeWindow_unlockAndPost(xxx2_osm->nativeSurface);
    }
}

void *xxx2OsmCreateContext(void *contextSrc) {
    printf("%s generating context\n", osm_LogTag);

    OSMesaContext osmesa_share = NULL;
    if (contextSrc != NULL) osmesa_share = contextSrc;

    OSMesaContext context = OSMesaCreateContext_p(OSMESA_RGBA, osmesa_share);
    if (context == NULL) {
        printf("%s OSMesaContext is Null!!!\n", osm_LogTag);
        return NULL;
    }

    xxx2_osm->context = context;
    printf("%s context = %p\n", osm_LogTag, context);

    return context;
}

void xxx2OsmSwapInterval(int interval) {
    if (xxx2_osm->nativeSurface != NULL)
        setNativeWindowSwapInterval(xxx2_osm->nativeSurface, interval);
}

int xxx2OsmInit() {
    if (pojav_environ->config_bridge != BRIDGE_TBL_XXX2)
        return 0;

    xxx2_osm = malloc(sizeof(struct xxx2_osm_render_window_t));
    if (!xxx2_osm) {
        printf("%s Failed to allocate memory for xxx2_osm\n", osm_LogTag);
        return -1;
    }
    memset(xxx2_osm, 0, sizeof(struct xxx2_osm_render_window_t));

    return 0;
}