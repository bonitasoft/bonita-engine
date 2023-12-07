class Deps {

    public static String bonitaArtifactsModelVersion = "1.0.0"
    public static String springVersion = "5.3.31"
    public static String springBootVersion = "2.7.18"
    public static String springSessionVersion = "2.7.4"
    public static String commonsIOVersion = "2.8.0"
    //fileupload 1.4 does not work on web-side.
    public static String commonscodec = "1.15"
    public static String commonsFileupload = "1.5"
    public static String commonsBeanutilsVersion = "1.9.4"
    public static String commonsCollections4Version = "4.4"
    public static String commonsLangVersion = "3.11"
    public static String commonsDbcp2Version = "2.5.0"
    public static String commonsCLIVersion = "1.4"
    public static String semver4jVersion = '3.1.0'
    public static String slf4jVersion = "1.7.36"
    public static String h2Version = "1.4.199"

    // Attention, see PassingPropertiesJCacheRegionFactory javadoc if this version changes:
    public static String hibernateVersion = "5.4.32.Final"

    public static String jacksonBomVersion = "2.15.3"
    public static String snakeyamlVersion = "1.32"
    public static String jakartaTransactionVersion = "1.3.3"
    public static String jakartaServletVersion = "4.0.4"
    // Keep this until all client projects have migrated to jakarta or it will break their builds !
    public static String javaxServletVersion = "4.0.1"
    // The groovy version must be in synch with the runtime-bom artifact
    public static String groovyVersion = "3.0.19"
    public static String javassistVersion = "3.27.0-GA" //version used by hibernate 5.4.32.Final
    public static String httpComponentsVersion = "4.5.13"
    public static String xstreamVersion = "1.4.20"
    public static String ehcacheVersion = "2.10.10.12.7"
    public static String eclipseCompilerVersion = "3.20.0"
    public static String jbcryptVersion = "0.4"
    public static String activationVersion = "1.2.2"
    public static String quartzVersion = "2.3.2"
    public static String micrometerVersion = "1.6.1"

    public static String mysqlVersion = "8.2.0"
    public static String mssqlVersion = "8.4.1.jre8"
    public static String oracleVersion = "19.3.0.0"
    public static String postgresqlVersion = "42.4.3"
    public static String lombokVersion = "1.18.30"

    public static String narayanaVersion = "5.10.6.Final"
    public static String jaxbVersion = "2.3.1"
    public static String logbackVersion = "1.2.13"
    public static String javaxAnnotationsVersion = "1.3.2"

    // extensions versions
    public static String hazelcastVersion = "5.3.5"
    public static String jcacheVersion = "1.0.0"
    // javax.persistence-api used by hibernate
    public static String javaxPersistenceApiVersion = "2.2"

    public static String guavaVersion = "32.1.2-jre"
    public static String antlr4RuntimeVersion = "4.7.2"

    // bonita-web specific dependencies:
    public static String jsonSimpleVersion = "1.1"
    public static String urlrewriteVersion = "4.0.3"
    public static String jakartaJstlVersion = "1.2.6"
    public static String jakartaJstlApiVersion = "1.2.7"
    public static String restletVersion = "2.3.12"
    public static String xbeanClassloaderVersion = "3.7"
    public static String jgettextVersion = "0.13"
    public static String hamcrestVersion = "2.1"
    public static String woodstoxCoreVersion = "6.4.0"
    public static String woodstoxStax2ApiVersion = "3.1.4"
    public static String keycloakVersion = "21.1.2"
    public static String xmlsecVersion = "2.2.6"
    public static String bcprovVersion = "1.77"
    public static String spnegoVersion = "1.1.1"

    // Test dependency versions:
    public static String junit4Version = "4.13.2"
    public static String junit5Version = "5.6.3"
    public static String awaitilityVersion = "4.0.3"
    public static String assertjVersion = "3.17.2"
    public static String xmlunitVersion = "1.6"
    public static String mockitoVersion = "3.5.10"
    public static String jsonUnitVersion = "2.19.0"
    public static String systemRulesVersion = "1.19.0"
    public static String systemLambdaVersion = "1.2.0"
    public static String concurrentUnitVersion = "0.4.6"
    public static String junitQuickCheck = "1.0"
    public static String jettyVersion = "9.4.9.v20180320"
    public static String jbossLoggingVersion = "3.1.3.GA"
    public static String commonsExecVersion = "1.3"

}
