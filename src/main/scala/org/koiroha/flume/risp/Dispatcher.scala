/*
 * Copyright (c) 2015 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp

import java.net.SocketAddress

import com.google.protobuf.ByteString
import org.apache.flume.Event
import org.glassfish.grizzly.CompletionHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.websockets._
import org.koiroha.flume.risp.Dispatcher.FlumeApp
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.{Future, Promise}

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Dispatcher
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
private class Dispatcher(val address:SocketAddress, val path:String) {

  import Dispatcher.logger

  private[this] val app = new FlumeApp()

  private[this] val server = {
    val s = HttpServer.createSimpleServer("", address)
    val addon = new WebSocketAddOn()
    s.getListeners.foreach{ _.registerAddOn(addon) }
    WebSocketEngine.getEngine.register("", path, app)
    s
  }

  def start():Unit = {
    server.start()
  }

  def shutdown():Future[Unit] = {
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
    app.send(dataframe)
  }
}

object Dispatcher {
  private[Dispatcher] val logger = LoggerFactory.getLogger(classOf[Dispatcher])

  private[Dispatcher] class FlumeApp extends WebSocketApplication {
    def send(msg:Array[Byte]):Unit = {
      getWebSockets.foreach{ _.send(msg) }
    }
    override def onMessage(ws:WebSocket, text:String) = {
      logger.info(s"onMessage($ws, $text)")
      super.onMessage(ws, text)
    }
    override def onMessage(ws:WebSocket, bytes:Array[Byte]) = {
      logger.info(s"onMessage($ws, ${bytes.length} bytes)")
      super.onMessage(ws, bytes)
    }
    protected override def onError(webSocket: WebSocket, t: Throwable): Boolean = {
      logger.error(s"onError($webSocket)", t)
      super.onError(webSocket, t)
    }
  }
}