//
// Created by Vera-Firefly on 20.11.2024.
//

#include <android/native_window.h>
#include <hardware/hardware.h>
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>
#include <dlfcn.h>
#include <assert.h>
#include <malloc.h>
#include <stdlib.h>
#include "environ/environ.h"
#include "osm_bridge_xxx3.h"
#include "osmesa_loader.h"
#include "renderer_config.h"

static struct xxx3_osm_render_window_t *xxx3_osm;
static bool hasCleaned = false;
static bool hasSetNoRendererBuffer = false;
static char xxx3_no_render_buffer[4];

// void setNativeWindowSwapInterval(AHardwareBuffer* hardwareBuffer, int swapInterval);

void xxx3_osm_set_no_render_buffer(AHardwareBuffer* buf) {
    buf->bits = &xxx3_no_render_buffer;
    buf->width = pojav_environ->savedWidth;
    buf->height = pojav_environ->savedHeight;
    buf->stride = 0;
}

void *xxx3OsmGetCurrentContext() {
    return (void *)OSMesaGetCurrentContext_p();
}

void xxx3OsmloadSymbols() {
    dlsym_OSMesa();
}

void xxx3_osm_apply_current_l(AHardwareBuffer* buf) {
    OSMesaContext ctx = OSMesaGetCurrentContext_p();
    if (ctx == NULL)
        printf("Zink: attempted to swap buffers without context!");

    OSMesaMakeCurrent_p(ctx, buf->bits, GL_UNSIGNED_BYTE, buf->width, buf->height);
    if (buf->stride != xxx3_osm->last_stride)
        OSMesaPixelStore_p(OSMESA_ROW_LENGTH, buf->stride);
    xxx3_osm->last_stride = buf->stride;
}

void xxx3_osm_apply_current_ll(AHardwareBuffer* buf) {
    /* 
    if (SpareBuffer())
    {
    #ifdef FRAME_BUFFER_SUPPOST
        abuffer = malloc(buf->width * buf->height * 4);
        OSMesaMakeCurrent_p((OSMesaContext)xxx3_osm->window,
                                abuffer,
                                GL_UNSIGNED_BYTE,
                                buf->width,
                                buf->height);
    #else
        printf("[ERROR]: Macro FRAME_BUFFER_SUPPOST is undefined\n");
    #endif
    } else 
    */
    OSMesaMakeCurrent_p((OSMesaContext)xxx3_osm->window,
                                   buf->bits,
                                   GL_UNSIGNED_BYTE,
                                   buf->width,
                                   buf->height);

    if (buf->stride != xxx3_osm->last_stride)
        OSMesaPixelStore_p(OSMESA_ROW_LENGTH, buf->stride);
    xxx3_osm->last_stride = buf->stride;
}

void xxx3OsmSwapBuffers() {
    AHardwareBuffer_lock(xxx3_osm->hardwareBuffer, NULL, &xxx3_osm->buffer);
    xxx3_osm_apply_current_l(&xxx3_osm->buffer);
    glFinish_p();
    AHardwareBuffer_unlock(xxx3_osm->hardwareBuffer);
}

void xxx3OsmMakeCurrent(void *window) {
    if (!hasCleaned)
    {
        printf("OSMDroid: making current\n");
        xxx3_osm->hardwareBuffer = pojav_environ->pojavBuffer;
        AHardwareBuffer_acquire(xxx3_osm->hardwareBuffer);
        AHardwareBuffer_setFormat(xxx3_osm->hardwareBuffer, AFORMAT_RGBA_8888);
        AHardwareBuffer_lock(xxx3_osm->hardwareBuffer, NULL, &xxx3_osm->buffer);
    }

    if (!hasSetNoRendererBuffer)
    {
        hasSetNoRendererBuffer = true;
        xxx3_osm_set_no_render_buffer(&xxx3_osm->buffer);
    }

    xxx3_osm->window = window;
    xxx3_osm_apply_current_ll(&xxx3_osm->buffer);
    OSMesaPixelStore_p(OSMESA_Y_UP, 0);

    if (!hasCleaned)
    {
        hasCleaned = true;
        printf("OSMDroid: vendor: %s\n", glGetString_p(GL_VENDOR));
        printf("OSMDroid: renderer: %s\n", glGetString_p(GL_RENDERER));
        glClear_p(GL_COLOR_BUFFER_BIT);
        glClearColor_p(0.4f, 0.4f, 0.4f, 1.0f);
        AHardwareBuffer_unlock(xxx3_osm->hardwareBuffer);
    }
}

void *xxx3OsmCreateContext(void *contextSrc) {
    printf("OSMDroid: generating context\n");
    void *ctx = OSMesaCreateContext_p(OSMESA_RGBA, contextSrc);
    printf("OSMDroid: context=%p\n", ctx);
    return ctx;
}

void xxx3OsmSwapInterval(int interval) {
    // if (xxx3_osm->hardwareBuffer != NULL)
        // setNativeWindowSwapInterval(xxx3_osm->hardwareBuffer, interval);
}

int xxx3OsmInit() {
    if (pojav_environ->config_bridge != BRIDGE_TBL_XXX3)
        return 0;

    xxx3_osm = malloc(sizeof(struct xxx3_osm_render_window_t));
    if (!xxx3_osm) {
        fprintf(stderr, "Failed to allocate memory for xxx3_osm\n");
        return -1;
    }
    memset(xxx3_osm, 0, sizeof(struct xxx3_osm_render_window_t));

    return 0;
}