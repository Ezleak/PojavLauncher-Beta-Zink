//
// Created by Vera-Firefly on 18.12.2024.
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
#include "osm_bridge_xxx4.h"
#include "osmesa_loader.h"
#include "renderer_config.h"

static struct xxx4_osm_render_window_t *xxx4_osm;
static bool swapBuffers = false;
static bool hasCleaned = false;
static bool hasSetNoRendererBuffer = false;
static char xxx4_no_render_buffer[4];

void setNativeWindowSwapInterval(struct ANativeWindow* nativeWindow, int swapInterval);

void xxx4_osm_set_no_render_buffer(ANativeWindow_Buffer* buf) {
    buf->bits = &xxx4_no_render_buffer;
    buf->width = pojav_environ->savedWidth;
    buf->height = pojav_environ->savedHeight;
    buf->stride = 0;
}

void* xxx4OsmGetCurrentContext() {
    return xxx4_osm->context;
}

bool xxx4OsmloadSymbols() {
    dlsym_OSMesa();
    return true;
}

void* xxx4OsmCreateContext(void* contextSrc) {
    xxx4_osm = malloc(sizeof(struct xxx4_osm_render_window_t));
    if (xxx4_osm == NULL) return NULL;
    memset(xxx4_osm, 0, sizeof(struct xxx4_osm_render_window_t));

    printf("OSMDroid: generating context\n");

    if (contextSrc != NULL)
        void* context = OSMesaCreateContext_p(OSMESA_RGBA, contextSrc);

    if (context == NULL)
    {
        free(xxx4_osm);
        return NULL;
    }

    xxx4_osm->context = (OSMesaContext)context;
    printf("OSMDroid: context=%p\n", context);
    return context;
}

void xxx4_osm_apply_current(ANativeWindow_Buffer* buf) {   
    if (swapBuffers)
        xxx4_osm->context = OSMesaGetCurrentContext_p();
    OSMesaMakeCurrent_p(xxx4_osm->context, buf->bits, GL_UNSIGNED_BYTE, buf->width, buf->height);
    if (buf->stride != xxx4_osm->last_stride)
        OSMesaPixelStore_p(OSMESA_ROW_LENGTH, buf->stride);
    xxx4_osm->last_stride = buf->stride;
}

void xxx4OsmSwapBuffers() {
    if (!swapBuffers) swapBuffers = true;
    ANativeWindow_lock(xxx4_osm->nativeSurface, &xxx4_osm->buffer, NULL);
    xxx4_osm_apply_current(&xxx4_osm->buffer);
    glFinish_p();
    ANativeWindow_unlockAndPost(xxx4_osm->nativeSurface);
}

void xxx4OsmMakeCurrent(void* window) {
    if (window == NULL)
    {
        OSMesaMakeCurrent_p(NULL, NULL, 0, 0, 0);
        xxx4_osm = NULL;
        return;
    }

    if (!hasCleaned)
    {
        printf("OSMDroid: making current\n");
        xxx4_osm->nativeSurface = pojav_environ->pojavWindow;
        ANativeWindow_acquire(xxx4_osm->nativeSurface);
        ANativeWindow_setBuffersGeometry(xxx4_osm->nativeSurface, 0, 0, WINDOW_FORMAT_RGBX_8888);
        ANativeWindow_lock(xxx4_osm->nativeSurface, &xxx4_osm->buffer, NULL);
    }

    if (!hasSetNoRendererBuffer)
    {
        hasSetNoRendererBuffer = true;
        xxx4_osm_set_no_render_buffer(&xxx4_osm->buffer);
    }

    swapBuffers = false;
    xxx4_osm->context = (OSMesaContext)window;
    xxx4_osm_apply_current(&xxx4_osm->buffer);

    if (!hasCleaned)
    {
        hasCleaned = true;
        OSMesaPixelStore_p(OSMESA_Y_UP, 0);
        glClearColor_p(0.4f, 0.4f, 0.4f, 1.0f);
        printf("OSMDroid: vendor: %s\n", glGetString_p(GL_VENDOR));
        printf("OSMDroid: renderer: %s\n", glGetString_p(GL_RENDERER));
        glClear_p(GL_COLOR_BUFFER_BIT);
        ANativeWindow_unlockAndPost(xxx4_osm->nativeSurface);
    }
}


void xxx4OsmSwapInterval(int interval) {
    if (xxx4_osm->nativeSurface != NULL)
        setNativeWindowSwapInterval(xxx4_osm->nativeSurface, interval);
}
