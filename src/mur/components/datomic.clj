(ns mur.components.datomic
  (:require
   [com.stuartsierra.component :as c]
   [datomic.api :as dtm]))

;; =================================================================
;; protocols
;; =================================================================

(defprotocol IDatomicDb
  (uri [this]))

(defprotocol IDatomicConn
  (conn [this]))

;; =================================================================
;; temp datomic db
;; =================================================================

(defrecord TempDatomicDB [uri started?]
  c/Lifecycle
  (start [this]
    (if started?
      this
      (do (dtm/create-database uri)
          (assoc this :started? true))))
  (stop [this]
    (if-not started?
      this
      (do (dtm/delete-database uri)
          (assoc this :started? false))))

  IDatomicDb
  (uri [this] uri))

(defn make-temp-datomic-db
  [option]
  (-> option
      (select-keys [:uri])
      (assoc :started? false)
      (map->TempDatomicDB)))

;; =================================================================
;; durable datomic db
;; =================================================================

(defrecord DurableDatomicDB [uri started?]
  c/Lifecycle
  (start [this]
    (if started?
      this
      (do (dtm/create-database uri)
          (assoc this :started? true))))
  (stop [this]
    (if-not started?
      this
      (assoc this :started? false)))

  IDatomicDb
  (uri [this] uri))

(defn make-durable-datomic-db
  [option]
  (-> option
      (select-keys [:uri])
      (assoc :started? false)
      (map->DurableDatomicDB)))

;; =================================================================
;; datomic conn
;; =================================================================

(defrecord DatomicConn [datomic-db conn]
  c/Lifecycle
  (start [this]
    (if (some? conn)
      this
      (assoc this :conn (dtm/connect (uri datomic-db)))))
  (stop [this]
    (if (nil? conn)
      this
      (do (dtm/release conn)
          (assoc this :conn nil))))

  IDatomicConn
  (conn [this] conn))

(defn make-datomic-conn
  []
  (c/using
   (map->DatomicConn {})
   [:datomic-db]))
