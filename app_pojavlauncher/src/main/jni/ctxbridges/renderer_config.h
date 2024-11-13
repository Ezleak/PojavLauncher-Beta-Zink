//
// Created by Vera-Firefly on 2.12.2023.
// Definitions specific to the renderer
//


#define RENDERER_GL4ES 1
#define RENDERER_VK_ZINK 2
#define RENDERER_VIRGL 3
#define RENDERER_VULKAN 4
#define RENDERER_VK_WARLIP 5
#define RENDERER_VK_ZINK_PREF 6

#define BRIDGE_TBL_DEFAULT 0
#define BRIDGE_TBL_XXX1 1
#define BRIDGE_TBL_XXX2 2
#define BRIDGE_TBL_XXX3 3
#define BRIDGE_TBL_XXX4 4



#ifndef POTATOBRIDGE_H
#define POTATOBRIDGE_H
#include <EGL/egl.h>

struct PotatoBridge {
    void* eglContext;    // EGLContext
    void* eglDisplay;    // EGLDisplay
    void* eglSurface;    // EGLSurface
    // void* eglSurfaceRead;
    // void* eglSurfaceDraw;
};

extern struct PotatoBridge potatoBridge;
extern EGLConfig config;

#endif // POTATOBRIDGE_H

#ifndef SPARE_RENDERER_CONFIG_H
#define SPARE_RENDERER_CONFIG_H

int SpareBuffer();

#endif

#ifndef FRAME_BUFFER_SUPPOST
#define FRAME_BUFFER_SUPPOST

extern void *gbuffer;
extern void *mbuffer;

#endif


