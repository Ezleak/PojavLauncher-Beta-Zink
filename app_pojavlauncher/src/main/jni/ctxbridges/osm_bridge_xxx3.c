//
// Created by Vera-Firefly on 20.11.2024.
//

#include <android/native_window.h>
#include <android/hardware_buffer.h>
#include <stdio.h>
#include <string.h>
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

/*
void xxx3_osm_set_no_render_buffer(AHardwareBuffer* buf) {
    buf->bits = &xxx3_no_render_buffer;
    buf->width = pojav_environ->savedWidth;
    buf->height = pojav_environ->savedHeight;
    buf->stride = 0;
}
*/

void *xxx3OsmGetCurrentContext() {
    return (void *)OSMesaGetCurrentContext_p();
}

void xxx3OsmloadSymbols() {
    dlsym_OSMesa();
}

void xxx3_osm_apply_current_l() {
    OSMesaContext ctx = OSMesaGetCurrentContext_p();
    if (ctx == NULL)
        printf("Zink: attempted to swap buffers without context!");

    OSMesaMakeCurrent_p(ctx, xxx3_osm->framebuffer, GL_UNSIGNED_BYTE, xxx3_osm->width, xxx3_osm->height);
    /*
    if (buf->stride != xxx3_osm->last_stride)
        OSMesaPixelStore_p(OSMESA_ROW_LENGTH, buf->stride);
    xxx3_osm->last_stride = buf->stride;
    */
    OSMesaPixelStore_p(OSMESA_ROW_LENGTH, xxx3_osm->width * 4);
}

void xxx3_osm_apply_current_ll() {
    OSMesaMakeCurrent_p((OSMesaContext)xxx3_osm->window,
                                   setbuffer,
                                   GL_UNSIGNED_BYTE,
                                   xxx3_osm->width,
                                   xxx3_osm->height);

    /*
    if (buf->stride != xxx3_osm->last_stride)
        OSMesaPixelStore_p(OSMESA_ROW_LENGTH, buf->stride);
    xxx3_osm->last_stride = buf->stride;
    */
    OSMesaPixelStore_p(OSMESA_ROW_LENGTH, xxx3_osm->width * 4);
}

void xxx3OsmSwapBuffers() {
    AHardwareBuffer_describe(xxx3_osm->hardwareBuffer, &xxx3_osm->desc);
    AHardwareBuffer_lock(
        xxx3_osm->hardwareBuffer,
        AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN,
        -1,
        NULL,
        &xxx3_osm->destBuffer);
    xxx3_osm_apply_current_l();
    glFinish_p();
    int pixelSize = 4;
    memcpy(xxx3_osm->destBuffer, xxx3_osm->framebuffer, xxx3_osm->width * xxx3_osm->height * pixelSize);
    AHardwareBuffer_unlock(xxx3_osm->hardwareBuffer, NULL);
}

void xxx3OsmMakeCurrent(void *window) {
    if (!hasCleaned)
    {
        printf("OSMDroid: making current\n");
        // xxx3_osm->hardwareBuffer = pojav_environ->pojavWindow;
        AHardwareBuffer_acquire(xxx3_osm->hardwareBuffer);

        AHardwareBuffer_Desc desc =
        {
            xxx3_osm->width,
            xxx3_osm->height,
            1,
            AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM,
            AHARDWAREBUFFER_USAGE_CPU_READ_NEVER
            | AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN
            | AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE
            | AHARDWAREBUFFER_USAGE_GPU_COLOR_OUTPUT,
            0,
            0,
            0
        };
        xxx3_osm->desc = desc;
        AHardwareBuffer_describe(xxx3_osm->hardwareBuffer, &xxx3_osm->desc);
        AHardwareBuffer_allocate(&xxx3_osm->desc, &xxx3_osm->hardwareBuffer);

        AHardwareBuffer_lock(
            xxx3_osm->hardwareBuffer,
            AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN,
            -1,
            NULL,
            &xxx3_osm->destBuffer);
    }

    if (!hasSetNoRendererBuffer)
    {
        hasSetNoRendererBuffer = true;
        // xxx3_osm_set_no_render_buffer(&xxx3_osm->buffer);
    }

    xxx3_osm->window = window;
    xxx3_osm_apply_current_ll();
    OSMesaPixelStore_p(OSMESA_Y_UP, 0);

    if (!hasCleaned)
    {
        hasCleaned = true;
        printf("OSMDroid: vendor: %s\n", glGetString_p(GL_VENDOR));
        printf("OSMDroid: renderer: %s\n", glGetString_p(GL_RENDERER));
        glClear_p(GL_COLOR_BUFFER_BIT);
        glClearColor_p(0.4f, 0.4f, 0.4f, 1.0f);
        AHardwareBuffer_unlock(xxx3_osm->hardwareBuffer, NULL);
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
    xxx3_osm->width = pojav_environ->savedWidth;
    xxx3_osm->height = pojav_environ->savedHeight;
    xxx3_osm->framebuffer = malloc(xxx3_osm->width * xxx3_osm->height * 4);
    if (!xxx3_osm) {
        fprintf(stderr, "Failed to allocate memory for xxx3_osm\n");
        return -1;
    }
    memset(xxx3_osm, 0, sizeof(struct xxx3_osm_render_window_t));

    return 0;
}