/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.xbean.classloader.NamedClassLoader;
import org.apache.xbean.classloader.ResourceHandle;
import org.apache.xbean.classloader.UnionEnumeration;
import org.apache.xbean.classloader.UrlResourceFinder;

/**
 * This class is higlhy inspired form JarFileClassLoader from xbean. The main diffrence is that
 * it inherits from NamedClassLoader instead of MultiParentClassLoader.
 */
public class MonoParentJarFileClassLoader extends NamedClassLoader {

    private static final URL[] EMPTY_URLS = new URL[0];

    private final UrlResourceFinder resourceFinder = new UrlResourceFinder();

    private final AccessControlContext acc;

    /**
     * Creates a JarFileClassLoader that is a child of the system class loader.
     * 
     * @param name
     *        the name of this class loader
     * @param urls
     *        a list of URLs from which classes and resources should be loaded
     */
    public MonoParentJarFileClassLoader(String name, URL[] urls) {
        super(name, EMPTY_URLS);
        this.acc = AccessController.getContext();
        addURLs(urls);
    }

    /**
     * Creates a JarFileClassLoader that is a child of the specified class loader.
     * 
     * @param name
     *        the name of this class loader
     * @param urls
     *        a list of URLs from which classes and resources should be loaded
     * @param parent
     *        the parent of this class loader
     */
    public MonoParentJarFileClassLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, EMPTY_URLS, parent);
        this.acc = AccessController.getContext();
        addURLs(urls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL[] getURLs() {
        return this.resourceFinder.getUrls();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addURL(final URL url) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                MonoParentJarFileClassLoader.this.resourceFinder.addUrl(url);
                return null;
            }
        }, this.acc);
    }

    /**
     * Adds an array of urls to the end of this class loader.
     * 
     * @param urls
     *        the URLs to add
     */
    protected void addURLs(final URL[] urls) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                if (urls != null && urls.length > 0) {
                    for (final URL url : urls) {
                        MonoParentJarFileClassLoader.this.resourceFinder.addUrl(url);
                    }
                }
                return null;
            }
        }, this.acc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        this.resourceFinder.destroy();
        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL findResource(final String resourceName) {
        return (URL) AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                return MonoParentJarFileClassLoader.this.resourceFinder.findResource(resourceName);
            }
        }, this.acc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<URL> findResources(final String resourceName) throws IOException {
        // TODO this is not right
        // first get the resources from the parent classloaders
        Enumeration<?> parentResources = super.findResources(resourceName);

        // get the classes from my urls
        Enumeration<?> myResources = (Enumeration<?>) AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                return MonoParentJarFileClassLoader.this.resourceFinder.findResources(resourceName);
            }
        }, this.acc);

        // join the two together
        return new UnionEnumeration(parentResources, myResources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String findLibrary(String libraryName) {
        // if the libraryName is actually a directory it is invalid
        int pathEnd = libraryName.lastIndexOf('/');
        if (pathEnd == libraryName.length() - 1) {
            throw new IllegalArgumentException("libraryName ends with a '/' character: " + libraryName);
        }

        // get the name if the library file
        final String resourceName;
        if (pathEnd < 0) {
            resourceName = System.mapLibraryName(libraryName);
        } else {
            String path = libraryName.substring(0, pathEnd + 1);
            String file = libraryName.substring(pathEnd + 1);
            resourceName = path + System.mapLibraryName(file);
        }

        // get a resource handle to the library
        ResourceHandle resourceHandle = (ResourceHandle) AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                return MonoParentJarFileClassLoader.this.resourceFinder.getResource(resourceName);
            }
        }, this.acc);

        if (resourceHandle == null) {
            return null;
        }

        // the library must be accessable on the file system
        URL url = resourceHandle.getUrl();
        if (!"file".equals(url.getProtocol())) {
            return null;
        }

        return new File(URI.create(url.toString())).getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> findClass(final String className) throws ClassNotFoundException {
        try {
            return (Class<?>) AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

                @Override
                public Object run() throws ClassNotFoundException {
                    // First think check if we are allowed to define the package
                    checkPackageDefinition(className);

                    ResourceHandle resourceHandle = findClassFileResource(className);

                    byte[] bytes;
                    Manifest manifest;
                    try {
                        // get the bytes from the class file
                        bytes = resourceHandle.getBytes();

                        // get the manifest for defining the packages
                        manifest = resourceHandle.getManifest();
                    } catch (IOException e) {
                        throw new ClassNotFoundException(className, e);
                    }

                    // get the certificates for the code source
                    Certificate[] certificates = resourceHandle.getCertificates();

                    // the code source url is used to define the package and as the security context for the class
                    URL codeSourceUrl = resourceHandle.getCodeSourceUrl();

                    // define the package (required for security)
                    definePackage(className, codeSourceUrl, manifest);

                    // this is the security context of the class
                    CodeSource codeSource = new CodeSource(codeSourceUrl, certificates);

                    // load the class into the vm
                    return defineClass(className, bytes, 0, bytes.length, codeSource);
                }

                private ResourceHandle findClassFileResource(final String className) throws ClassNotFoundException {
                    // convert the class name to a file name
                    String resourceName = className.replace('.', '/') + ".class";

                    // find the class file resource
                    ResourceHandle resourceHandle = MonoParentJarFileClassLoader.this.resourceFinder.getResource(resourceName);
                    if (resourceHandle == null) {
                        throw new ClassNotFoundException(className);
                    }
                    return resourceHandle;
                }

                private void checkPackageDefinition(final String className) {
                    SecurityManager securityManager = System.getSecurityManager();
                    if (securityManager != null) {
                        String packageName;
                        int packageEnd = className.lastIndexOf('.');
                        if (packageEnd >= 0) {
                            packageName = className.substring(0, packageEnd);
                            securityManager.checkPackageDefinition(packageName);
                        }
                    }
                }
            }, this.acc);
        } catch (final PrivilegedActionException e) {
            throw (ClassNotFoundException) e.getException();
        }
    }

    private void definePackage(String className, URL jarUrl, Manifest manifest) {
        int packageEnd = className.lastIndexOf('.');
        if (packageEnd < 0) {
            return;
        }

        String packageName = className.substring(0, packageEnd);
        String packagePath = packageName.replace('.', '/') + "/";

        Attributes packageAttributes = null;
        Attributes mainAttributes = null;
        if (manifest != null) {
            packageAttributes = manifest.getAttributes(packagePath);
            mainAttributes = manifest.getMainAttributes();
        }
        Package pkg = getPackage(packageName);
        if (pkg != null) {
            if (pkg.isSealed()) {
                if (!pkg.isSealed(jarUrl)) {
                    throw new SecurityException("Package was already sealed with another URL: package=" + packageName + ", url=" + jarUrl);
                }
            } else {
                if (isSealed(packageAttributes, mainAttributes)) {
                    throw new SecurityException("Package was already been loaded and not sealed: package=" + packageName + ", url=" + jarUrl);
                }
            }
        } else {
            String specTitle = getAttribute(Attributes.Name.SPECIFICATION_TITLE, packageAttributes, mainAttributes);
            String specVendor = getAttribute(Attributes.Name.SPECIFICATION_VENDOR, packageAttributes, mainAttributes);
            String specVersion = getAttribute(Attributes.Name.SPECIFICATION_VERSION, packageAttributes, mainAttributes);
            String implTitle = getAttribute(Attributes.Name.IMPLEMENTATION_TITLE, packageAttributes, mainAttributes);
            String implVendor = getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR, packageAttributes, mainAttributes);
            String implVersion = getAttribute(Attributes.Name.IMPLEMENTATION_VERSION, packageAttributes, mainAttributes);

            URL sealBase = null;
            if (isSealed(packageAttributes, mainAttributes)) {
                sealBase = jarUrl;
            }

            definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
        }
    }

    private String getAttribute(Attributes.Name name, Attributes packageAttributes, Attributes mainAttributes) {
        if (packageAttributes != null) {
            String value = packageAttributes.getValue(name);
            if (value != null) {
                return value;
            }
        }
        if (mainAttributes != null) {
            return mainAttributes.getValue(name);
        }
        return null;
    }

    private boolean isSealed(Attributes packageAttributes, Attributes mainAttributes) {
        String sealed = getAttribute(Attributes.Name.SEALED, packageAttributes, mainAttributes);
        if (sealed == null) {
            return false;
        }
        return "true".equalsIgnoreCase(sealed);
    }
}
