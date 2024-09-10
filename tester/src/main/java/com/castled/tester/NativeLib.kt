package com.castled.tester

class NativeLib {

    /**
     * A native method that is implemented by the 'tester' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'tester' library on application startup.
        init {
            System.loadLibrary("tester")
        }
    }
}