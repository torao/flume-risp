/*
 * Copyright (c) 2015 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp

import java.net.SocketAddress
import java.util.concurrent.atomic.AtomicReference

import com.google.protobuf.ByteString
import org.glassfish.grizzly.CompletionHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.websockets._
import org.apache.flume.Event
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.concurrent.{Promise, Future}

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Dispatcher
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
private class Dispatcher(val address:SocketAddress, val path:String) {

  import Dispatcher.logger

  private[this] val websockets = new AtomicReference(Seq[WebSocket]())

  private[this] val app = new WebSocketApplication {
    @tailrec
    override def onConnect(ws: WebSocket): Unit = {
      val current = websockets.get()
      if (websockets.compareAndSet(current, current :+ ws)) {
        logger.debug(s"new connection: $ws")
        super.onConnect(ws)
      } else {
        onConnect(ws)
      }
    }

    @tailrec
    override def onClose(ws: WebSocket, frame: DataFrame): Unit = {
      val current = websockets.get()
      if (websockets.compareAndSet(current, current.filter {
        _ != ws
      })) {
        logger.debug(s"connection closed: $ws")
        super.onClose(ws, frame)
      } else {
        onClose(ws, frame)
      }
    }

    protected override def onError(webSocket: WebSocket, t: Throwable): Boolean = {
      logger.debug(s"onError($webSocket)", t)
      super.onError(webSocket, t)
    }
  }

  private[this] val server = {
    val s = HttpServer.createSimpleServer("", address)
    val addon = new WebSocketAddOn()
    s.getListeners.foreach{ _.registerAddOn(addon) }
    WebSocketEngine.getEngine.register("", path, app)
    logger.debug(s"server http://$address$path")
    s
  }

  def start():Unit = {
    server.start()
    logger.debug(s"start")
  }

  def shutdown():Future[Unit] = {
    logger.debug(s"shutdown")
    val promise = Promise[Unit]()
    val f = server.shutdown()
    f.addCompletionHandler(new CompletionHandler[HttpServer] {
      override def updated(result: HttpServer): Unit = ()
      override def cancelled(): Unit = promise.failure(new Exception("canceled"))
      override def completed(result: HttpServer): Unit = promise.success(())
      override def failed(throwable: Throwable): Unit = promise.failure(throwable)
    })
    promise.future
  }

  def deliver(event:Event):Unit = {
    val dataframe = event.getHeaders.foldLeft(RispEvent.FlumeEvent.newBuilder()){ case (builder, (key, value)) =>
      builder.addHeaders(RispEvent.FlumeEvent.HeaderField.newBuilder().setName(key).setValue(value).build())
      builder
    }.setBody(ByteString.copyFrom(event.getBody)).build().toByteArray
    websockets.get().foreach{  _.send(dataframe) }
  }
}

object Dispatcher {
  private[Dispatcher] val logger = LoggerFactory.getLogger(classOf[Dispatcher])
}