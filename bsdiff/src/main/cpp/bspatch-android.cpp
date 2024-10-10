#include <jni.h>
#include <string>

extern "C" {
#include "bspatch-android.h"

JNIEXPORT int JNICALL
Java_io_github_junkfood02_bsdiff_LibBSPatch_patch(JNIEnv *env, jobject thiz, jstring old_file_path,
                                                  jstring new_file_path, jstring patch_file_path) {
    const char *oldFilePath = env->GetStringUTFChars(old_file_path, nullptr);
    const char *newFilePath = env->GetStringUTFChars(new_file_path, nullptr);
    const char *patchFilePath = env->GetStringUTFChars(patch_file_path, nullptr);
    const char *programName = "bspatch";
    int argc = 4;
    char *argv[] = {
            const_cast<char *>(programName),
            const_cast<char *>(oldFilePath),
            const_cast<char *>(newFilePath),
            const_cast<char *>(patchFilePath)
    };
    auto res = patch(argc, argv);
    env->ReleaseStringUTFChars(old_file_path, oldFilePath);
    env->ReleaseStringUTFChars(new_file_path, newFilePath);
    env->ReleaseStringUTFChars(patch_file_path, patchFilePath);
    return res;
}
}