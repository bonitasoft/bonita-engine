package org.bonitasoft.engine.classloader;

/**
 * @author Baptiste Mesta
 */
class MyClassLoaderListener implements ClassLoaderListener {

    int onDestroyCalled = 0;
    int onUpdateCalled = 0;

    @Override
    public void onUpdate(ClassLoader newClassLoader) {
        onUpdateCalled++;
    }

    @Override
    public void onDestroy(ClassLoader oldClassLoader) {
        onDestroyCalled++;
    }

    public boolean isOnDestroyCalled() {
        return onDestroyCalled > 0;
    }

    public boolean isOnUpdateCalled() {
        return onUpdateCalled > 0;
    }

    public int getOnDestroyCalled() {
        return onDestroyCalled;
    }

    public int getOnUpdateCalled() {
        return onUpdateCalled;
    }
}
