(ns mur.components.timbre
  (:require
   [taoensso.timbre :as log]))

;; =================================================================
;; protocols
;; =================================================================

(defprotocol ILogger
  (log [this level throwable message]))

;; =================================================================
;; logger
;; =================================================================

(defrecord Logger []
  ILogger
  (log [this level throwable message]
    (log/log* this level throwable message)))

(defn make-logger
  [option]
  (-> option
      (select-keys [:level
                    :appenders
                    :ns-whitelist
                    :ns-blacklist
                    :middleware
                    :timestamp-opts
                    :output-fn])
      (map->Logger)))

(defn trace
  [logger message]
  (log logger :trace nil message))

(defn debug [logger message]
  (log logger :debug nil message))

(defn info
  [logger message]
  (log logger :info nil message))

(defn warn [logger message]
  (log logger :warn nil message))

(defn error
  ([logger message]
   (log logger :error nil message))
  ([logger throwable message]
   (log logger :error throwable message)))

(defn fatal
  ([logger message]
   (log logger :fatal nil message))
  ([logger throwable message]
   (log logger :fatal throwable message)))

(defn report [logger message]
  (log logger :report nil message))
