//
// Created by Vera-Firefly on 20.08.2024.
//

#include <android/native_window.h>
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>
#include <dlfcn.h>
#include <assert.h>
#include <malloc.h>
#include <stdlib.h>
#include "environ/environ.h"
#include "virgl_bridge.h"
#include "egl_loader.h"
#include "osmesa_loader.h"
#include "renderer_config.h"

static struct virgl_render_window_t *virgl_bridge;
static char virgl_no_render_buffer[4];
static bool hasSetNoRendererBuffer = false;

int (*vtest_main_p)(int argc, char **argv);
void (*vtest_swap_buffers_p)(void);
void *gbuffer;

void virgl_set_no_render_buffer(ANativeWindow_Buffer* buf) {
    buf->bits = &virgl_no_render_buffer;
    buf->width = pojav_environ->savedWidth;
    buf->height = pojav_environ->savedHeight;
    buf->stride = 0;
}

void *egl_make_current(void *window) {
    if (pojav_environ->config_renderer == RENDERER_VIRGL)
    {
        EGLBoolean success = eglMakeCurrent_p(
                potatoBridge.eglDisplay,
                window==0 ? (EGLSurface *) 0 : potatoBridge.eglSurface,
                window==0 ? (EGLSurface *) 0 : potatoBridge.eglSurface,
                /* window==0 ? EGL_NO_CONTEXT : */ (EGLContext *) window
        );

        if (success == EGL_FALSE)
            printf("EGLBridge: Error: eglMakeCurrent() failed: %p\n", eglGetError_p());
        else printf("EGLBridge: eglMakeCurrent() succeed!\n");

    
        printf("VirGL: vtest_main = %p\n", vtest_main_p);
        printf("VirGL: Calling VTest server's main function\n");
        vtest_main_p(3, (const char*[]){"vtest", "--no-loop-or-fork", "--use-gles", NULL, NULL});
    } else {
        return NULL;
    }
}

bool loadSymbolsVirGL() {
    dlsym_OSMesa();
    dlsym_EGL();

    char *fileName = calloc(1, 1024);

    sprintf(fileName, "%s/libvirgl_test_server.so", getenv("POJAV_NATIVEDIR"));
    void *handle = dlopen(fileName, RTLD_LAZY);
    printf("VirGL: libvirgl_test_server = %p\n", handle);
    if (!handle) {
        printf("VirGL: %s\n", dlerror());
        return false;
    }
    vtest_main_p = dlsym(handle, "vtest_main");
    vtest_swap_buffers_p = dlsym(handle, "vtest_swap_buffers");

    free(fileName);

    return true;
}

int virglInit() {
    if (pojav_environ->config_renderer != RENDERER_VIRGL)
        return 0;

    virgl_bridge = malloc(sizeof(struct virgl_render_window_t));
    if (!virgl_bridge)
    {
        printf("[ VirGL Bridge ] Failed to allocate memory for virgl_bridge\n");
        return -1;
    }
    memset(virgl_bridge, 0, sizeof(struct virgl_render_window_t));

    virgl_bridge->nativeSurface = pojav_environ->pojavWindow;

    if (potatoBridge.eglDisplay == NULL || potatoBridge.eglDisplay == EGL_NO_DISPLAY)
    {
        potatoBridge.eglDisplay = eglGetDisplay_p(EGL_DEFAULT_DISPLAY);
        if (potatoBridge.eglDisplay == EGL_NO_DISPLAY)
        {
            printf("EGLBridge: Error eglGetDefaultDisplay() failed: %p\n", eglGetError_p());
            return 0;
        }
    }

    printf("EGLBridge: Initializing\n");
    if (!eglInitialize_p(potatoBridge.eglDisplay, NULL, NULL))
    {
        printf("EGLBridge: Error eglInitialize() failed: %s\n", eglGetError_p());
        return 0;
    }

    static const EGLint attribs[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            // Minecraft required on initial 24
            EGL_DEPTH_SIZE, 24,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_NONE
    };

    EGLint num_configs;
    EGLint vid;

    if (!eglChooseConfig_p(potatoBridge.eglDisplay, attribs, &config, 1, &num_configs))
    {
        printf("EGLBridge: Error couldn't get an EGL visual config: %s\n", eglGetError_p());
        return 0;
    }

    assert(config);
    assert(num_configs > 0);

    if (!eglGetConfigAttrib_p(potatoBridge.eglDisplay, config, EGL_NATIVE_VISUAL_ID, &vid))
    {
        printf("EGLBridge: Error eglGetConfigAttrib() failed: %s\n", eglGetError_p());
        return 0;
    }

    ANativeWindow_setBuffersGeometry(virgl_bridge->nativeSurface, 0, 0, vid);

    eglBindAPI_p(EGL_OPENGL_ES_API);

    potatoBridge.eglSurface = eglCreateWindowSurface_p(potatoBridge.eglDisplay, config, virgl_bridge->nativeSurface, NULL);

    if (!potatoBridge.eglSurface)
    {
        printf("EGLBridge: Error eglCreateWindowSurface failed: %p\n", eglGetError_p());
        return 0;
    }

    {
        EGLint val;
        assert(eglGetConfigAttrib_p(potatoBridge.eglDisplay, config, EGL_SURFACE_TYPE, &val));
        assert(val & EGL_WINDOW_BIT);
    }

    printf("EGLBridge: Initialized!\n");
    printf("EGLBridge: ThreadID=%d\n", gettid());
    printf("EGLBridge: EGLDisplay=%p, EGLSurface=%p\n",
           potatoBridge.eglDisplay,
           potatoBridge.eglSurface
    );

    // Init EGL context and vtest server
    const EGLint ctx_attribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
    };

    EGLContext* ctx = eglCreateContext_p(potatoBridge.eglDisplay, config, NULL, ctx_attribs);
    printf("VirGL: created EGL context %p\n", ctx);

    pthread_t t;
    pthread_create(&t, NULL, egl_make_current, (void *)ctx);
    usleep(100*1000); // need enough time for the server to init

    if (OSMesaCreateContext_p == NULL)
    {
        printf("OSMDroid: %s\n",dlerror());
        return 0;
    }

    if (SpareBuffer())
    {
    #ifdef FRAME_BUFFER_SUPPOST

        printf("OSMDroid: width=%i;height=%i, reserving %i bytes for frame buffer\n",
           pojav_environ->savedWidth, pojav_environ->savedHeight,
           pojav_environ->savedWidth * 4 * pojav_environ->savedHeight);
        gbuffer = calloc(pojav_environ->savedWidth *4, pojav_environ->savedHeight +1);

    #else
        printf("[WORNING]: Macro FRAME_BUFFER_SUPPOST is undefined,defult to close\n");
    #endif

    } else printf("OSMDroid: do not set frame buffer\n");
    return 0;
}

void *virglCreateContext(void *contextSrc) {
    printf("OSMDroid: generating context\n");

    OSMesaContext osmesa_share = NULL;
    if (contextSrc != NULL) osmesa_share = contextSrc;

    OSMesaContext context = OSMesaCreateContext_p(OSMESA_RGBA, osmesa_share);
    if (context == NULL) {
        printf("[ VirGL Bridge ] OSMesaContext is Null!!!\n");
        return NULL;
    }

    virgl_bridge->context = context;
    printf("[ VirGL Bridge ] context = %p\n", context);

    return context;
}

void *virglGetCurrentContext() {
    return virgl_bridge->context;
}

void virgl_apply_current(ANativeWindow_Buffer* buf) {
    OSMesaMakeCurrent_p(virgl_bridge->context, buf->bits, GL_UNSIGNED_BYTE, buf->width, buf->height);
}

void virglMakeCurrent(void *window) {
    if (!hasSetNoRendererBuffer) {
        printf("OSMDroid: making current\n");
        virgl_set_no_render_buffer(&virgl_bridge->buffer);
    }

    virgl_apply_current(&virgl_bridge->buffer);

    if (!virgl_set_no_render_buffer)
    {
        virgl_set_no_render_buffer = true;
        printf("OSMDroid: vendor: %s\n",glGetString_p(GL_VENDOR));
        printf("OSMDroid: renderer: %s\n",glGetString_p(GL_RENDERER));
        glClear_p(GL_COLOR_BUFFER_BIT);
        glClearColor_p(0.4f, 0.4f, 0.4f, 1.0f);

        int pixelsArr[4];
        glReadPixels_p(0, 0, 1, 1, GL_RGB, GL_INT, &pixelsArr);

        virglSwapBuffers();
    }
}

void virglSwapBuffers() {
    glFinish_p();
    vtest_swap_buffers_p();
}

void virglSwapInterval(int interval) {
    eglSwapInterval_p(potatoBridge.eglDisplay, interval);
}
