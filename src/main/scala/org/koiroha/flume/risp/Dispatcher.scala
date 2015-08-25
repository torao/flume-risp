package org.koiroha.flume.risp

import java.net.SocketAddress
import java.util.concurrent.atomic.AtomicReference

import com.google.protobuf.ByteString
import org.glassfish.grizzly.CompletionHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.websockets._
import org.apache.flume.Event

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.concurrent.{Promise, Future}

class Dispatcher(val address:SocketAddress){
  private[this] val websockets = new AtomicReference(Seq[WebSocket]())

  val app = new WebSocketApplication {
    @tailrec
    override def onConnect(ws:WebSocket):Unit = {
      val current = websockets.get()
      if(websockets.compareAndSet(current, current :+ ws)){
        super.onConnect(ws)
      } else {
        onConnect(ws)
      }
    }
    @tailrec
    override def onClose(ws:WebSocket, frame:DataFrame):Unit = {
      val current = websockets.get()
      if(websockets.compareAndSet(current, current :+ ws)){
        super.onClose(ws, frame)
      } else {
        onClose(ws, frame)
      }
    }
  }

  val server = {
    val s = HttpServer.createSimpleServer()
    val addon = new WebSocketAddOn()
    s.getListeners.foreach{ _.registerAddOn(addon) }
    WebSocketEngine.getEngine.register("", "/api/1.0/logs", app)
    s
  }

  def start():Unit = server.start()

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
    websockets.get().foreach{  _.send(dataframe) }
  }
}
