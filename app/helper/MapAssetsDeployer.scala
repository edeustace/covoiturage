package helper

import com.ee.assets.deployment.{ContentInfo, Deployer}
import java.io.InputStream
import scala.Predef._
import com.ee.assets.deployment.ContentInfo
import scala.util.Right
import play.api.Logger

/**
 * Created by adelegue on 20/12/2013.
 */
object MapAssetsDeployer extends Deployer{

  val cache = collection.mutable.Map[String, InputStream]()

  def generateKey(filename: String,  lastModified: Long, info : ContentInfo) : String = {
    val key = filename
    Logger.logger.debug("key : "+key)
    key
  }

  def generatePath(filename: String,  lastModified: Long, info : ContentInfo) : String = {
    val path = "/compiled-assets"+generateKey(filename, lastModified, info)
    Logger.logger.debug("path : "+path)
    path
  }

  override def deploy(filename: String,  lastModified: Long, contents: => InputStream, info : ContentInfo): Either[String,String] = {

    cache += (generateKey(filename, lastModified, info) -> contents)

    Right(generatePath(filename, lastModified, info))
  }
}
