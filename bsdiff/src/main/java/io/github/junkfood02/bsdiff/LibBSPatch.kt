package io.github.junkfood02.bsdiff

import java.io.File

object LibBSPatch {
    @Synchronized
    fun init() {
        System.loadLibrary("bspatch-android")
    }

    fun patch(oldFile: File, newFile: File, patchFile: File) {
        patch(oldFile.absolutePath, newFile.absolutePath, patchFile.absolutePath)
    }

    external fun patch(oldFilePath: String, newFilePath: String, patchFilePath: String): Int
}
