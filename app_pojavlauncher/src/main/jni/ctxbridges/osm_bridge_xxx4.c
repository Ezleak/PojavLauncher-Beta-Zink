//
// Created by Vera-Firefly on 21.12.2024.
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
#include "osm_bridge_xxx4.h"
#include "osmesa_loader.h"
#include "renderer_config.h"

static struct xxx4_osm_render_window_t *xxx4_osm;
static bool hasCleaned = false;

void* share_lock() {
    void* destBuffer = NULL;
    AHardwareBuffer_lock(
        xxx4_osm->hardwareBuffer,
        AHARDWAREBUFFER_USAGE_CPU_WRITE_OFTEN,
        -1,
        NULL,
        &destBuffer);
    return destBuffer;
}

void share_unlock() {
    AHardwareBuffer_unlock(xxx4_osm->hardwareBuffer, NULL);
}

void get_desc(AHardwareBuffer_Desc * temp) {
    AHardwareBuffer_describe(xxx4_osm->hardwareBuffer, temp);
}

bool xxx4OsmloadSymbols() {
    dlsym_OSMesa();
    return true;
}

void* xxx4OsmGetCurrentContext() {
    return xxx4_osm->context;
}

void* xxx4OsmCreateContext(void* contextSrc) {
    xxx4_osm = malloc(sizeof(struct xxx4_osm_render_window_t));
    if (!xxx4_osm) {
        fprintf(stderr, "Failed to allocate memory for xxx4_osm\n");
        return NULL;
    }
    memset(xxx4_osm, 0, sizeof(struct xxx4_osm_render_window_t));

    xxx4_osm->width = pojav_environ->savedWidth;
    xxx4_osm->height = pojav_environ->savedHeight;
    xxx4_osm->framebuffer = malloc(xxx4_osm->width * xxx4_osm->height * 4);

    printf("OSMDroid: generating context\n");

    OSMesaContext osmesa_share = NULL;
    if (contextSrc != NULL) osmesa_share = contextSrc;

    OSMesaContext context = OSMesaCreateContext_p(OSMESA_RGBA, osmesa_share);
    if (context == NULL) {
        printf("OSMDroid: OSMesaContext is Null!!!\n");
        return NULL;
    }

    xxx4_osm->context = context;
    printf("OSMDroid: context = %p\n", context);

    return context;
}

void xxx4OsmMakeCurrent(void *window) {
    printf("OSMDroid: making current\n");
        AHardwareBuffer_Desc desc = {
            xxx4_osm->width,
            xxx4_osm->height,
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

        xxx4_osm->desc = desc;
    get_desc(&desc);
    void* ptr = share_lock();
    OSMesaMakeCurrent_p(xxx4_osm->context, ptr, GL_UNSIGNED_BYTE, xxx4_osm->width, xxx4_osm->height);
    OSMesaPixelStore_p(OSMESA_ROW_LENGTH, xxx4_osm->width * 4);
    OSMesaPixelStore_p(OSMESA_Y_UP, 0);
    glClear_p(GL_COLOR_BUFFER_BIT);
    glClearColor_p(0.4f, 0.4f, 0.4f, 1.0f);
    share_unlock();
}

void xxx4OsmSwapBuffers() {
    AHardwareBuffer_Desc desc;
    get_desc(&desc);
    void* ptr = share_lock();
    OSMesaMakeCurrent_p(xxx4_osm->context, ptr, GL_UNSIGNED_BYTE, xxx4_osm->width, xxx4_osm->height);
    OSMesaPixelStore_p(OSMESA_ROW_LENGTH, desc.stride);
    glFinish_p();
    share_unlock();
}

void xxx4OsmSwapInterval(int interval) {
    return;
}
