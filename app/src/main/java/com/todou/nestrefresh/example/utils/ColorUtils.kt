package com.todou.nestrefresh.example.utils

import kotlin.random.Random


object ColorUtils {

    fun getRandColorString(): String {
        var r: String = Integer.toHexString(Random.nextInt(256)).toUpperCase()
        var g:String = Integer.toHexString(Random.nextInt(256)).toUpperCase()
        var b:String = Integer.toHexString(Random.nextInt(256)).toUpperCase()
        r = if (r.length == 1) "0$r" else r
        g = if(g.length == 1 ) "0$g" else g
        b = if (b.length == 1) "0$b" else b
        return "#$r$g$b"
    }
}
