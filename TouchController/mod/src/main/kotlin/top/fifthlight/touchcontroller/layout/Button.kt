package top.fifthlight.touchcontroller.layout

import top.fifthlight.touchcontroller.state.PointerState

data class ButtonResult(
    val newPointer: Boolean = false,
    val clicked: Boolean = false,
)

fun Context.SwipeButton(
    id: String,
    content: Context.(clicked: Boolean) -> Unit,
): ButtonResult {
    val pointers = getPointersInRect(size)
    var newPointer = false
    var clicked = false
    for (pointer in pointers) {
        when (pointer.state) {
            PointerState.New -> {
                pointer.state = PointerState.SwipeButton(id)
                newPointer = true
                clicked = true
            }

            is PointerState.SwipeButton -> {
                clicked = true
            }

            else -> {}
        }
    }
    content(clicked)
    return ButtonResult(
        newPointer = newPointer,
        clicked = clicked
    )
}

fun Context.Button(
    id: String,
    content: Context.(clicked: Boolean) -> Unit,
): ButtonResult {
    val pointers = getPointersInRect(size)
    var newPointer = false
    var clicked = false
    for (pointer in pointers) {
        when (val state = pointer.state) {
            PointerState.New -> {
                pointer.state = PointerState.Button(id)
                newPointer = true
                clicked = true
            }

            is PointerState.Button -> {
                if (state.id == id) {
                    clicked = true
                }
            }

            else -> {}
        }
    }

    content(clicked)
    return ButtonResult(
        newPointer = newPointer,
        clicked = clicked
    )
}