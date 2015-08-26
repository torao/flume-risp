/*
 * Copyright (c) 2015 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp

import java.net.{InetSocketAddress, SocketAddress}

import org.apache.flume.Context
import org.apache.flume.Sink.Status
import org.apache.flume.conf.Configurable
import org.apache.flume.sink.AbstractSink

import scala.annotation.tailrec

class ServerSink extends AbstractSink with Configurable {

  private[this] case class Config(batchSize:Int, bindAddress:SocketAddress){
    val dispatcher = new Dispatcher(bindAddress, "/api/1.0/logs")
  }

  private[this] var config:Option[Config] = None

  override def configure(context: Context): Unit = {
    val batchSize = context.getInteger("batchSize", 1000)
    val address = new InetSocketAddress(
      context.getString("server.http.address", "0.0.0.0"),
      context.getInteger("server.http.port", 8011))
    config.foreach{ _.dispatcher.shutdown() }
    config = Some(Config(batchSize, address))
  }

  override def start():Unit = {
    super.start()
    assert(config.isDefined)
    config.foreach{ _.dispatcher.start() }
  }

  override def stop():Unit = {
    config.foreach{ _.dispatcher.shutdown() }
    super.stop()
  }

  override def process(): Status = config.map{ c =>
    val channel = getChannel

    @tailrec
    def _deliver(i: Int): Status = channel.take() match {
      case null => Status.BACKOFF
      case e =>
        c.dispatcher.deliver(e)
        if (i <= 1) Status.READY else _deliver(i - 1)
    }

    val transaction = channel.getTransaction
    transaction.begin()
    try {
      val status = _deliver(c.batchSize)
      transaction.commit()
      status
    } catch {
      case ex:Throwable => Status.READY
    } finally {
      transaction.close()
    }
  }.getOrElse(Status.READY)
}
