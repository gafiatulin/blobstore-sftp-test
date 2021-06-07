import blobstore.sftp.SftpStore
import blobstore.url.Path
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.effect.std.Console
import com.jcraft.jsch.{JSch, Session}

import java.util.Properties

object Main extends IOApp{

  val session: IO[Session] = IO.delay{
    val jsch    = new JSch()
    val session = jsch.getSession("user", "localhost", 2222)
    session.setTimeout(10000)
    session.setPassword("password")
    val config = new Properties
    config.put("StrictHostKeyChecking", "no")
    session.setConfig(config)
    session
  }

  val store: Resource[IO, SftpStore[IO]] = SftpStore(session, Some(32))

  override def run(args: List[String]): IO[ExitCode] = store.use { client =>
    for {
      l <- client.list(Path("sftp")).compile.toList
      _ <- Console[IO].println(s"Listing ${l.size} file(s):")
      _ <- Console[IO].println(l)
    } yield ()
  }.as(ExitCode.Success)
}
