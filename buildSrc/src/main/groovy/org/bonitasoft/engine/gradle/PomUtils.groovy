package org.bonitasoft.engine.gradle


import org.gradle.api.publish.maven.MavenPom

/**
 * Utility class to generate Community information necessary to publish to
 * Maven Central.
 *
 * @author Emmanuel Duchastenier
 */
class PomUtils {

    static void pomCommunityPublication(MavenPom pom) {
        pom.with {
            url = 'https://community.bonitasoft.com/'
            organization {
                name = 'Bonitasoft S.A.'
                url = 'https://community.bonitasoft.com/'
            }
            developers {
                developer {
                    id = "bonita-engine-team"
                    name = "The Bonita Engine Development Team"
                    organization = "Bonitasoft S.A."
                    organizationUrl = "http://community.bonitasoft.com/"
                }
            }
            scm {
                connection = "scm:git:http://github.com/bonitasoft/bonita-engine.git"
                developerConnection = "scm:git:git@github.com:bonitasoft/bonita-engine.git"
                url = "http://github.com/bonitasoft/bonita-engine"
            }
            licenses {
                license {
                    name = 'GNU Lesser General Public License Version 2.1'
                    url = 'http://www.gnu.org/licenses/lgpl-2.1.html'
                    distribution = 'repo'
                }
            }
        }
    }
}
