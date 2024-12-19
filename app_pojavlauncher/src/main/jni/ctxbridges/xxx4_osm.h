#ifndef XXX4_OSM
#define XXX4_OSM
#include "osm_bridge_xxx4.h"

bool (*xxx4_osm_load_symbols)();
xxx4_osm_render_window_t* (*xxx4_osm_get_current)();
xxx4_osm_render_window_t* (*xxx4_osm_create_context)(xxx4_osm_render_window_t* share);
void (*xxx4_osm_swap_buffers)();
void (*xxx4_osm_make_current)(xxx4_osm_render_window_t* bundle);
void (*xxx4_osm_swap_interval)(int interval);

void osm_bridge_xxx4() {
    xxx4_osm_load_symbols = xxx4OsmloadSymbols;
    xxx4_osm_create_context = xxx4OsmCreateContext;
    xxx4_osm_make_current = xxx4OsmMakeCurrent;
    xxx4_osm_get_current = xxx4OsmGetCurrentContext;
    xxx4_osm_swap_buffers = xxx4OsmSwapBuffers;
    xxx4_osm_swap_interval = xxx4OsmSwapInterval;
}

#endif
