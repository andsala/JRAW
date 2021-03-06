package net.dean.jraw.test.integration

import com.winterbe.expekt.should
import net.dean.jraw.Endpoint
import net.dean.jraw.oauth.AuthMethod
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.oauth.StatefulAuthHelper
import net.dean.jraw.test.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*

class OAuthHelperTest: Spek({
    describe("automatic") {
        it("should produce a RedditClient authenticated for a script app") {
            val reddit = OAuthHelper.automatic(newOkHttpAdapter(), CredentialsUtil.script)
            ensureAuthenticated(reddit)

            // We should be able to access all Endpoints when using a script app
            for (endpoint in Endpoint.values())
                reddit.canAccess(endpoint).should.be.`true`
        }

        it("should produce a RedditClient authenticated for a userless Credentials") {
            ensureAuthenticated(OAuthHelper.automatic(newOkHttpAdapter(), CredentialsUtil.script))
        }

        it("should produce an authorized RedditClient for a userlessApp Credentials") {
            val credentials = Credentials.userlessApp(CredentialsUtil.app.clientId, UUID.randomUUID())

            // Create a RedditClient with application only and send a request to make sure it works properly
            val reddit = OAuthHelper.automatic(newOkHttpAdapter(), credentials)
            ensureAuthenticated(reddit)
            reddit.authManager.renew()
            ensureAuthenticated(reddit)

        }

        it("should produce an authorized RedditClient for a userless Credentials") {
            val reddit = OAuthHelper.automatic(newOkHttpAdapter(), CredentialsUtil.applicationOnly)
            ensureAuthenticated(reddit)
            reddit.authManager.renew()
            ensureAuthenticated(reddit)

            // Test token revoking while we're here
            reddit.authManager.revokeAccessToken()
            reddit.authManager.revokeRefreshToken()
        }

        it("should throw an Exception when given Credentials for an app") {
            expectException(IllegalArgumentException::class) {
                OAuthHelper.automatic(newOkHttpAdapter(), CredentialsUtil.app)
            }
        }
    }

    describe("interactive") {
        it("should return a fresh StatefulAuthHelper") {
            OAuthHelper.interactive(newOkHttpAdapter(), CredentialsUtil.app).authStatus
                .should.equal(StatefulAuthHelper.Status.INIT)

            OAuthHelper.interactive(newOkHttpAdapter(), createMockCredentials(AuthMethod.WEBAPP)).authStatus
                .should.equal(StatefulAuthHelper.Status.INIT)
        }

        it("should throw an Exception when given a non-installedApp Credentials") {
            expectException(IllegalArgumentException::class) {
                OAuthHelper.interactive(newOkHttpAdapter(), CredentialsUtil.script)
            }
        }
    }
})
