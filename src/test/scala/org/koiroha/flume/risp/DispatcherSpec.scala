/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp

import java.net.{URI, InetSocketAddress}
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

import org.apache.flume.Event
import org.apache.flume.event.SimpleEvent
import org.specs2.Specification

import scala.concurrent.{Promise, Await}
import scala.concurrent.duration.Duration
import scala.collection.JavaConversions._

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DispatcherSpec
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
class DispatcherSpec extends Specification { def is = s2"""
Dispatcher should:
not send event without any connections. $st
"""

  def st = {
		val host = "localhost"
		val port = 8011
		val path = "/api/1.0/logs"
	  val addr = new InetSocketAddress(host, port)
	  val dispatcher = new Dispatcher(addr, path)
	  dispatcher.start()

		val headers = Map("a" -> "b", "x" -> "y")
		val body = "hello, world".getBytes(StandardCharsets.UTF_8)
		val expected = new SimpleEvent()
		expected.setHeaders(headers)
		expected.setBody(body)

		val promise = Promise[Event]()
		val receiver = new Receiver(URI.create(s"http://$host:$port$path"), new Consumer[Event]{
			override def accept(t:Event): Unit = promise.success(t)
		}, 30 * 1000)

	  dispatcher.deliver(new SimpleEvent())

		val actual = Await.result(promise.future, Duration.Inf)
	  Await.result(dispatcher.shutdown(), Duration.Inf)
		receiver.close()

		(expected.getHeaders.size() === actual.getHeaders.size()) and
			actual.getHeaders.foldLeft("" === ""){ case (a, b) => a and (b._2 === expected.getHeaders.apply(b._1))} and
		  actual.getBody === expected.getBody
  }
}
