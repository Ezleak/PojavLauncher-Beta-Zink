#ifndef XXX4_OSM
#define XXX4_OSM
#include "common.h"
#include "osm_bridge_xxx4.h"

typedef basic_render_window_t* (*xxx4_osm_create_context_t)(basic_render_window_t* share);
typedef void (*xxx4_osm_make_current_t)(basic_render_window_t* bundle);
typedef basic_render_window_t* (*xxx4_osm_get_current_t)();

bool (*xxx4_osm_load_symbols)();
xxx4_osm_get_current_t xxx4_osm_get_current;
xxx4_osm_create_context_t xxx4_osm_create_context;
xxx4_osm_make_current_t xxx4_osm_make_current;
void (*xxx4_osm_swap_buffers)();
void (*xxx4_osm_swap_interval)(int interval);

void osm_bridge_xxx4() {
    xxx4_osm_load_symbols = xxx4OsmloadSymbols;
    xxx4_osm_create_context = (xxx4_osm_create_context_t) xxx4OsmCreateContext;
    xxx4_osm_make_current = (xxx4_osm_make_current_t) xxx4OsmMakeCurrent;
    xxx4_osm_get_current = (xxx4_osm_get_current_t) xxx4OsmGetCurrentContext;
    xxx4_osm_swap_buffers = xxx4OsmSwapBuffers;
    xxx4_osm_swap_interval = xxx4OsmSwapInterval;
}

#endif
