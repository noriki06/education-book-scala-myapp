/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

organization := "net.ixias"
name         := "education-book-app-api"
scalaVersion := "3.6.4"

resolvers ++= Seq(
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/ivy-releases/",
  "Sonatype Release"  at "https://oss.sonatype.org/content/repositories/releases/",
  // ixias-v3 (pulled transitively via app-lib) lives in a private S3 Maven repo.
  "IxiaS Releases"    at "https://s3-ap-northeast-1.amazonaws.com/maven.ixias.net/releases",
  "IxiaS Snapshots"   at "https://s3-ap-northeast-1.amazonaws.com/maven.ixias.net/snapshots"
)

libraryDependencies ++= Seq(
  // --[ Local framework ]-----------------------------------
  // Publish app-lib first:  (cd ../app-lib && sbt publishLocal)
  "net.ixias" %% "education-book-app-lib" % "1.0.0-SNAPSHOT",

  // --[ OSS ]-----------------------------------------------
  "mysql"          %  "mysql-connector-java" % "8.0.33",
  "ch.qos.logback" %  "logback-classic"      % "1.3.3",
  "org.typelevel"  %% "cats-core"            % "2.12.0",
  // Password hashing uses ixias.core.security.PBKDF2 (via app-lib) — no extra dependency.

  guice
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(FlywayPlugin)

// Play generates an injected router; controllers are @Inject-constructed.
routesGenerator := InjectedRoutesGenerator

// Custom PathBindable/QueryStringBindable (ixias) available in routes files.
import play.sbt.routes.RoutesKeys
RoutesKeys.routesImport := Seq(
  "mvc.Binders.{ *, given }"
)

scalacOptions ++= Seq(
  "-feature",                  // Warn on features that should be imported explicitly.
  "-Wunused:all",              // Warn on unused code.
  "-Wconf:any:e",              // Treat all warnings as errors.
  "-Werror",                   // Fail the build on any warning.
  "-Wconf:any&src=target/scala-.*/routes/.*:s",  // ...except generated routes.
)

// Point Play at the local config/logger when running from sbt.
javaOptions ++= Seq(
  "-Dconfig.file=conf/application.conf",
  "-Dlogger.file=conf/logback.xml"
)
Compile / run / fork := true

// --[ Flyway migration ]-----------------------------------
// Schema migrations live in etc/database (filesystem locations). There are none
// in the skeleton — add *.sql files there, then apply them with:  sbt flywayMigrate
// Connection settings are read from conf/application.conf (db.app).
import com.typesafe.config.ConfigFactory
lazy val applicationConf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
flywayDriver    := applicationConf.getString("db.app.driver")
flywayUrl       := applicationConf.getString("db.app.url")
flywayUser      := applicationConf.getString("db.app.username")
flywayPassword  := applicationConf.getString("db.app.password")
flywayLocations := Seq("filesystem:" + (baseDirectory.value / ".." / "etc" / "database" / "migration" / "app" / "common").getPath)
flywayTable     := "_flyway_schema"
