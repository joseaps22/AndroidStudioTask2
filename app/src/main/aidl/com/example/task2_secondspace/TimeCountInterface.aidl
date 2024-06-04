// TimeCountInterface.aidl
package com.example.task2_secondspace;

// Declare any non-default types here with import statements

interface TimeCountInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void countTime();

    String getBackgroundTime();

    String getForegroundTime();

    void setAppInBackground();

    void setAppInForeground();
}