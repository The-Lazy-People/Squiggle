package com.thelazypeople.scribbl.model

/**
 * Stores the Information of the point to be populated in [PaintView.kt]. This results in realtime canvas drawing.
 *
 * @param pointX: X value of the point passed.
 * @param pointY: Y value of the point passed.
 * @param type: Type of the Point. 0 -> Path Started. 1 -> Path Continued. 2 -> Path End. 3 -> Clear Screen.
 */

data class Information(
    var pointX: Float = 0.0f,
    var pointY: Float = 0.0f,
    var type: Int? = 0
)
