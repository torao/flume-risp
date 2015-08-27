/*
 * Copyright (c) 2015 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp

import java.net.{InetSocketAddress, SocketAddress}

import com.google.protobuf.ByteString
import org.apache.flume.Event
import org.glassfish.grizzly.CompletionHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.websockets._
import org.koiroha.flume.risp.Sender.FlumeApp
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.{Future, Promise}

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Sender
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @param docroot directory to send-back as static document. None if no document specified.
 * @author Takami Torao
 */
private class Sender(bindAddress:SocketAddress, urlPath:String, docroot:Option[String] = None) {

  private[this] val app = new FlumeApp()

  private[this] val server = {
    val s = HttpServer.createSimpleServer(docroot.orNull, bindAddress)
    val addon = new WebSocketAddOn()
    s.getListeners.foreach{ _.registerAddOn(addon) }
    WebSocketEngine.getEngine.register("", urlPath, app)
    s
  }

  def start():Unit = {
    server.start()
    val hostPort = bindAddress match {
      case i:InetSocketAddress => s"${i.getHostName}:${i.getPort}"
      case i => i.toString
    }
    Sender.logger.debug(s"flume service on: ws://$hostPort$urlPath")
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

private object Sender {
  private[Sender] val logger = LoggerFactory.getLogger(classOf[Sender])

  private[Sender] class FlumeApp extends WebSocketApplication {
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