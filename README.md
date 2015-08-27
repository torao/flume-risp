# Flume Receiver Intent-Based Sender Push Feature

## 概要

`flume-risp` は **Receiver Intent-based Sender Push メッセージ配信モデル**の Apache Flume Source/Sink 実装です。
従来のクライアント/サーバ型設計と同様に、データ受信側 (ClientSource) が必要に応じて送信側 (ServerSink) と接続し
アドホックなデータ収集を行います。これによりデータの受信側が常に駆動している状態でなくても、必要なときにだけ Flume から
データを収集する運用を行うことができます。

`flume-rips` はデータセンター内からファイアウォールやロードバランサを介して外部にデータを送信できるよう Flume イベントを
WebSockets上で転送します。

Sink は WebSocket サーバとして実装されており、Flume データ転送を要求するクライアントから接続することによってクライアント
(別の Flume Agent) に対してのデータ転送を開始します。

## メモ

* WebSockets 実装の選定について: Finagle は WebSockets 実装がない。Netty はバージョン 4 で WebSockets ハンドシェイクを実装しているが Flume 1.6 は Netty 3.5 に依存している。Grizzly、Jetty は WebSockets クライアント/サーバ共に実装あり。
* TLS (wss) 対応。
