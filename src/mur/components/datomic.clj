(ns mur.components.datomic
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [datomic.api :as dtm]))

;; =================================================================
;; datomic db spec
;; =================================================================

(s/def ::uri
  string?)

(s/def ::datomic-db-config
  (s/keys :req-un [::uri]))

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
  (-> (s/assert ::datomic-db-config option)
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
  (-> (s/assert ::datomic-db-config option)
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
