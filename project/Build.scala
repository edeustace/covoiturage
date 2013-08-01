import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "covoiturage-java"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
	"be.objectify"  %%  "deadbolt-java"     % "2.1-RC2",
        // Comment this for local development of the Play Authentication core
        //"com.feth"      %%  "play-authenticate" % "0.2.5-SNAPSHOT",
	"com.feth"      %%  "play-authenticate" % "0.3.0-SNAPSHOT",
        "net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.1.0"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA)
    .settings(
          resolvers += Resolver.url("Objectify Play Repository (release)", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
          resolvers += Resolver.url("Objectify Play Repository (snapshot)", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),

          resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
          resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),

          resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.com/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns),
          resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.com/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns),
          resolvers += Resolver.url("My GitHub Play Repository", url("http://alexanderjarvis.github.com/releases/"))(Resolver.ivyStylePatterns)

        //resolvers ++= Seq("My GitHub Play Repository" at "http://alexanderjarvis.github.com/releases/",
        //Resolvers.easyMailReleaseRepository, Resolvers.authSnapshot)
    )

  object Resolvers {
      val easyMailReleaseRepository = "play-easymail (release)" at "http://joscha.github.com/play-easymail/repo/releases/"
      val easyMailSnapshotRepository = "play-easymail (snapshot)" at "http://joscha.github.com/play-easymail/repo/snapshots/"
      val authRelease = "play-authenticate (release)" at "http://joscha.github.com/play-authenticate/repo/releases/"
      val authSnapshot = "play-authenticate (snapshot)" at "http://joscha.github.com/play-authenticate/repo/snapshots/"
  }

}
